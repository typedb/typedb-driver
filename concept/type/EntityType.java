/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.concept.type;

import grakn.client.Grakn;
import grakn.client.concept.thing.Entity;

import java.util.stream.Stream;

public interface EntityType extends ThingType {

    @Override
    EntityType.Remote asRemote(Grakn.Transaction transaction);

    interface Remote extends ThingType.Remote, EntityType {

        Entity create();

        void setSupertype(EntityType superEntityType);

        @Override
        EntityType getSupertype();

        @Override
        Stream<? extends EntityType> getSupertypes();

        @Override
        Stream<? extends EntityType> getSubtypes();

        @Override
        Stream<? extends Entity> getInstances();
    }
}
