/*
  Copyright 2022 Fraunhofer Institute for Transportation and Infrastructure Systems IVI

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package de.fhg.ivi.drm.it;

import de.fhg.ivi.drm.it.broker.Broker_5_0_0;
import de.fhg.ivi.drm.it.dsc.DSC_7_0_3;
import de.fhg.ivi.ids.dsc_5_1.api.IdsMessagesApiClient;
import de.fhg.ivi.ids.dsc_5_1.api.OfferedResourcesApiClient;
import de.fhg.ivi.ids.dsc_5_1.model.Link;
import de.fhg.ivi.ids.dsc_5_1.model.OfferedResourceView;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DRM Test - DSC 7.0.0 - Broker 5.0.0")
public class DSC_7_0_3_Test implements Broker_5_0_0, DSC_7_0_3, TestPlan {

    @Inject
    IdsMessagesApiClient messagesApiClient;

    @Inject
    OfferedResourcesApiClient offeredResourcesApiClient;

    @Test
    @Order(1)
    @Override
    public void registerProviderAtBroker() {
        var response = getHttpResponseOrFail(
                () -> messagesApiClient.sendConnectorUpdateMessage3(dscCredentials, urlBroker + "/infrastructure")
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    @Order(2)
    @Override
    public void queryConnectorsFromBroker() {
        HttpResponse<String> response = getHttpResponseOrFail(
                () -> messagesApiClient.sendConnectorUpdateMessage2(dscCredentials, URI.create(urlBroker + "/infrastructure"), CONNECTOR_QUERY)
        );

        // then
        String result = assertDoesNotThrow(() -> response.getBody().orElseThrow());
        List<String> registeredConnectors = parseRegisteredConnectors(result);

        assertEquals(1, registeredConnectors.size());

        assertEquals(idsDscAccessUrl, registeredConnectors.get(0));
    }

    @Test
    @Order(3)
    @Override
    public void registerResourceAtBroker() {
        var view = offeredResourcesApiClient.getAll3(dscCredentials, Optional.empty(), Optional.empty());
        var offeredResourceView = assertDoesNotThrow(() -> view.getBody().orElseThrow());

        offeredResourceView.getEmbedded().getResources().stream()
                .map(OfferedResourceView::getLinks)
                .map(links -> links.get("self"))
                .map(Link::getHref)
                .forEach(id -> {
                    var response = getHttpResponseOrFail(
                            () -> messagesApiClient.sendConnectorUpdateMessage(dscCredentials, urlBroker + "/infrastructure", URI.create(id))
                    );
                    assertEquals(HttpStatus.OK, response.getStatus());
                });
    }

    @Test
    @Order(4)
    @Override
    public void unregisterConnectorFromBroker() {
        var response = getHttpResponseOrFail(
                () -> messagesApiClient.sendConnectorUpdateMessage4(dscCredentials, urlBroker + "/infrastructure")
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatus());

        try {
            messagesApiClient.sendConnectorUpdateMessage2(dscCredentials, URI.create(urlBroker + "/infrastructure"), CONNECTOR_QUERY);
            fail();
        } catch (HttpClientResponseException e) {
            assertTrue(e.getResponse().getBody(String.class).orElseThrow().contains("The index is empty"));
        }
    }
}