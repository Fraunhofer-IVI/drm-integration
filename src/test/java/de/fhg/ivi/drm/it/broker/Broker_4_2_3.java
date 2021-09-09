package de.fhg.ivi.drm.it.broker;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
public interface Broker_4_2_3 {

    String CONTAINER_IMAGE_BROKER_CORE = "registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker/core:4.2.3-SNAPSHOT";
    String CONTAINER_IMAGE_BROKER_FUSEKI = "app-store.ids.isst.fraunhofer.de:5000/ids/eis-broker-fuseki";
    String CONTAINER_IMAGE_BROKER_ELASTICSEARCH = "elasticsearch:7.9.3";

    String SERVICE_NAME_BROKER = "broker";
    int SERVICE_PORT_BROKER = 8080;

    String CONNECTOR_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/> " +
            " SELECT ?url WHERE { ?x a ids:ConnectorEndpoint . " +
            " ?x ids:accessURL ?url }";

    String urlBroker = "http://" + SERVICE_NAME_BROKER + ":" + SERVICE_PORT_BROKER;

    @Container
    GenericContainer<?> brokerFusekiContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_FUSEKI)
            .withExposedPorts(3030)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("fuseki")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Broker_4_2_3.class)).withPrefix("BROKER_FUSEKI"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    GenericContainer<?> brokerElasticsearchContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_ELASTICSEARCH)
            .withExposedPorts(9200)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("broker-elasticsearch")
            .withEnv(
                    Map.of("http.port", "9200",
                            "http.cors.enabled", "true",
                            "http.cors.allow-origin", "https://localhost",
                            "http.cors.allow-headers", "X-Requested-With,X-Auth-Token,Content-Type,Content-Length,Authorization",
                            "http.cors.allow-credentials", "true",
                            "discovery.type", "single-node"))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Broker_4_2_3.class)).withPrefix("BROKER_ELASTIC"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    GenericContainer<?> brokerContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_CORE)
            .withExposedPorts(SERVICE_PORT_BROKER)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(SERVICE_NAME_BROKER)
            .withEnv(
                    Map.of("SPARQL_ENDPOINT", "http://fuseki:3030/connectorData",
                            "ELASTICSEARCH_HOSTNAME", "broker-elasticsearch",
                            "SHACL_VALIDATION", "true",
                            "DAPS_VALIDATE_INCOMING", "false",
                            "COMPONENT_URI", "https://localhost",
                            "COMPONENT_CATALOGURI", "https://localhost/connectors/"))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Broker_4_2_3.class)).withPrefix(SERVICE_NAME_BROKER))
            .dependsOn(brokerElasticsearchContainer, brokerFusekiContainer)
            .waitingFor(
                    Wait.forListeningPort()
            );

}