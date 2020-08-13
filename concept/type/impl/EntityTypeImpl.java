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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RoleType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public class EntityTypeImpl {
    /**
     * Client implementation of a MetaType, a special type of Type
     * TODO: This class is not defined in Concept API, and at server side implementation.
     * TODO: we should remove this class, or implement properly on server side.
     */
    public static class Local extends ThingTypeImpl.Local<EntityType, Entity> implements EntityType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of a MetaType, a special type of Type
     * TODO: This class is not defined in Concept API, and at server side implementation.
     * TODO: we should remove this class, or implement properly on server side.
     */
    public static class Remote extends ThingTypeImpl.Remote<EntityType, Entity> implements EntityType.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final EntityType.Remote setOwns(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.setOwns(attributeType);
        }

        @Override
        public final EntityType.Remote setOwns(AttributeType<?> attributeType, boolean isKey) {
            return (EntityType.Remote) super.setOwns(attributeType, isKey);
        }

        @Override
        public final EntityType.Remote setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType) {
            return (EntityType.Remote) super.setOwns(attributeType, overriddenType);
        }

        @Override
        public final EntityType.Remote setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey) {
            return (EntityType.Remote) super.setOwns(attributeType, overriddenType, isKey);
        }

        @Override
        public Stream<? extends AttributeType.Remote<?>> getOwns(boolean keysOnly) {
            return super.getOwns(keysOnly);
        }

        @Override
        public final EntityType.Remote setPlays(RoleType role) {
            return (EntityType.Remote) super.setPlays(role);
        }

        @Override
        public final EntityType.Remote unsetOwns(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.unsetOwns(attributeType);
        }

        @Override
        public final EntityType.Remote unsetPlays(RoleType role) {
            return (EntityType.Remote) super.unsetPlays(role);
        }

        @Override
        public final EntityType.Remote isAbstract(Boolean isAbstract) {
            return (EntityType.Remote) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Entity.Remote> getInstances() {
            return super.getInstances().map(this::asInstance);
        }

        @Override
        public final Stream<EntityType.Remote> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<EntityType.Remote> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public final EntityType.Remote setLabel(Label label) {
            return (EntityType.Remote) super.setLabel(label);
        }

        @Override
        public final Entity.Remote create() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setEntityTypeCreateReq(ConceptProto.EntityType.Create.Req.getDefaultInstance()).build();

            return Concept.Remote.of(tx(), runMethod(method).getEntityTypeCreateRes().getEntity());
        }

        @Override
        public final EntityType.Remote setSupertype(EntityType superEntityType) {
            return (EntityType.Remote) super.setSupertype(superEntityType);
        }

        @Override
        protected final EntityType.Remote asCurrentBaseType(Concept.Remote<?> other) {
            return other.asEntityType();
        }

        @Override
        protected final boolean equalsCurrentBaseType(Concept.Remote<?> other) {
            return other.isEntityType();
        }

        @Override
        protected final Entity.Remote asInstance(Concept.Remote<?> concept) {
            return concept.asEntity();
        }
    }
}
