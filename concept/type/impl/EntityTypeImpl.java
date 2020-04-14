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

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.Role;
import grakn.protocol.session.ConceptProto;

import java.util.stream.Stream;

public class EntityTypeImpl {
    /**
     * Client implementation of a MetaType, a special type of Type
     * TODO: This class is not defined in Concept API, and at server side implementation.
     * TODO: we should remove this class, or implement properly on server side.
     */
    public static class Local extends TypeImpl.Local<EntityType, Entity> implements EntityType.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of a MetaType, a special type of Type
     * TODO: This class is not defined in Concept API, and at server side implementation.
     * TODO: we should remove this class, or implement properly on server side.
     */
    public static class Remote extends TypeImpl.Remote<EntityType, Entity> implements EntityType.Remote {

        public Remote(GraknClient.Transaction tx, ConceptId id) {
            super(tx, id);
        }

        @Override
        public final EntityType.Remote key(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.key(attributeType);
        }

        @Override
        public final EntityType.Remote has(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.key(attributeType);

        }

        @Override
        public final EntityType.Remote plays(Role role) {
            return (EntityType.Remote) super.plays(role);

        }

        @Override
        public final EntityType.Remote unkey(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.unkey(attributeType);

        }

        @Override
        public final EntityType.Remote unhas(AttributeType<?> attributeType) {
            return (EntityType.Remote) super.unhas(attributeType);

        }

        @Override
        public final EntityType.Remote unplay(Role role) {
            return (EntityType.Remote) super.unplay(role);
        }

        @Override
        public final EntityType.Remote isAbstract(Boolean isAbstract) {
            return (EntityType.Remote) super.isAbstract(isAbstract);
        }

        @Override
        public final Stream<Entity.Remote> instances() {
            return super.instances().map(this::asInstance);
        }

        @Override
        public final Stream<EntityType.Remote> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public final Stream<EntityType.Remote> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public final EntityType.Remote label(Label label) {
            return (EntityType.Remote) super.label(label);
        }

        @Override
        public final Entity.Remote create() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setEntityTypeCreateReq(ConceptProto.EntityType.Create.Req.getDefaultInstance()).build();

            return Concept.Remote.of(runMethod(method).getEntityTypeCreateRes().getEntity(), tx());
        }

        @Override
        public final EntityType.Remote sup(EntityType superEntityType) {
            return (EntityType.Remote) super.sup(superEntityType);
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
