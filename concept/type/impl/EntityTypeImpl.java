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

package grakn.client.concept.type.impl;

import grakn.client.Grakn;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class EntityTypeImpl {

    public static class Local extends ThingTypeImpl.Local implements EntityType.Local {

        public Local(ConceptProto.Type type) {
            super(type);
        }

        @Override
        public EntityTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new EntityTypeImpl.Remote(transaction, getLabel(), isRoot());
        }
    }

    public static class Remote extends ThingTypeImpl.Remote implements EntityType.Remote {

        public Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        @Override
        public EntityType.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asEntityType);
        }

        @Override
        public final Stream<Entity.Remote> getInstances() {
            return super.getInstances().map(Thing.Remote::asEntity);
        }

        @Override
        public final Stream<EntityType.Remote> getSupertypes() {
            return super.getSupertypes().map(ThingType.Remote::asEntityType);
        }

        @Override
        public final Stream<EntityType.Remote> getSubtypes() {
            return super.getSubtypes().map(ThingType.Remote::asEntityType);
        }

        @Override
        public final Entity.Remote create() {
            ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setEntityTypeCreateReq(ConceptProto.EntityType.Create.Req.getDefaultInstance()).build();

            return ThingImpl.Remote.of(tx(), runMethod(method).getEntityTypeCreateRes().getEntity()).asEntity();
        }

        @Override
        public final void setSupertype(EntityType superEntityType) {
            setSupertypeInternal(superEntityType);
        }
    }
}
