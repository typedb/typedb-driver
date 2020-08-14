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

package grakn.client.concept.thing.impl;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.impl.ConceptImpl;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

public abstract class ThingImpl {
    /**
     * Client implementation of Thing
     */
    public abstract static class Local extends ConceptImpl.Local implements Thing.Local {

        private final ThingType type;
        private final boolean inferred;

        protected Local(ConceptProto.Concept concept) {
            super(concept);
            this.type = Concept.Local.of(concept.getTypeRes().getThingType()).asThingType();
            this.inferred = concept.getInferredRes().getInferred();
        }

        @Override
        public ThingType getType() {
            return type;
        }

        @Override
        public final boolean isInferred() {
            return inferred;
        }
    }

    /**
     * Client implementation of Thing
     */
    public abstract static class Remote extends ConceptImpl.Remote implements Thing.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public ThingType.Remote getType() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance()).build();

            return Concept.Remote.of(tx(), runMethod(method).getThingGetTypeRes().getThingType()).asThingType();
        }

        @Override
        public final boolean isInferred() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();

            return runMethod(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<Attribute.Remote> getHas(AttributeType... attributeTypes) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                                                   .addAllAttributeTypes(RequestBuilder.ConceptMessage.concepts(Arrays.asList(attributeTypes)))).build();

            return conceptStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Concept.Remote::asAttribute);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final <T> Stream<Attribute.Remote> getHas(AttributeType attributeType) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                            .addAllAttributeTypes(RequestBuilder.ConceptMessage.concepts(Collections.singleton(attributeType)))).build();

            return conceptStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Concept.Remote::asAttribute)
                    .map(a -> a);
        }

        @Override
        public final Stream<Attribute.Remote> getHas(boolean keysOnly) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(keysOnly)).build();
            return conceptStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Concept.Remote::asAttribute);
        }

        @Override
        public final Stream<Relation.Remote> getRelations(RoleType... roleTypes) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetRelationsIterReq(ConceptProto.Thing.GetRelations.Iter.Req.newBuilder()
                                                  .addAllRoleTypes(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roleTypes)))).build();

            return conceptStream(method, res -> res.getThingGetRelationsIterRes().getRelation()).map(Concept.Remote::asRelation);
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingGetPlaysIterReq(ConceptProto.Thing.GetPlays.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingGetPlaysIterRes().getRoleType()).map(Concept.Remote::asRoleType);
        }

        @Override
        public Thing.Remote setHas(Attribute attribute) {
            // TODO: replace usage of this method as a getter, with relations(Attribute attribute)
            // TODO: then remove this method altogether and just use has(Attribute attribute)
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingSetHasReq(ConceptProto.Thing.SetHas.Req.newBuilder()
                                               .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();
            runMethod(method);
            return this;
        }

        @Override
        public void unsetHas(Attribute attribute) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingUnsetHasReq(ConceptProto.Thing.UnsetHas.Req.newBuilder()
                                              .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();
            runMethod(method);
        }

    }
}
