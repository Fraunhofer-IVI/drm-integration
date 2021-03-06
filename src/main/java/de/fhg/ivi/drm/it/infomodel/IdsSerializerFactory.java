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

package de.fhg.ivi.drm.it.infomodel;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class IdsSerializerFactory {

    @Singleton
    public de.fraunhofer.iais.eis.ids.jsonld.Serializer getRDFSerializer() {
        return new Serializer();
    }
}
