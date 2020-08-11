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

import grakn.client.GraknClient;
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
     *
     * @param <SomeThing> The exact type of this class
     * @param <SomeType>  the type of an instance of this class
     */
    public abstract static class Local<
            SomeThing extends Thing<SomeThing, SomeType>,
            SomeType extends ThingType<SomeType, SomeThing>>
            extends ConceptImpl.Local<SomeThing>
            implements Thing.Local<SomeThing, SomeType> {

        private final SomeType type;
        private final boolean inferred;

        protected Local(ConceptProto.Concept concept) {
            super(concept);
            this.type = Concept.Local.of(concept.getTypeRes().getThingType());
            this.inferred = concept.getInferredRes().getInferred();
        }

        @Override
        public final SomeType type() {
            return type;
        }

        @Override
        public final boolean isInferred() {
            return inferred;
        }

        /**
         * Client implementation of Thing
         *
         * @param <SomeRemoteThing> The exact type of this class
         * @param <SomeRemoteType>  the type of an instance of this class
         */
        public abstract static class Remote<
                SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>,
                SomeRemoteType extends ThingType<SomeRemoteType, SomeRemoteThing>>
                extends ConceptImpl.Remote<SomeRemoteThing>
                implements Thing.Remote<SomeRemoteThing, SomeRemoteType> {

            public Remote(GraknClient.Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public ThingType.Remote<SomeRemoteType, SomeRemoteThing> type() {
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setThingTypeReq(ConceptProto.Thing.Type.Req.getDefaultInstance()).build();

                return Concept.Remote.of(tx(), runMethod(method).getThingTypeRes().getThingType());
            }

            @Override
            public final boolean isInferred() {
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();

                return runMethod(method).getThingIsInferredRes().getInferred();
            }

            @Override
            public final Stream<Attribute.Remote<?>> attributes(AttributeType<?>... attributeTypes) {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setThingAttributesIterReq(ConceptProto.Thing.Attributes.Iter.Req.newBuilder()
                                                       .addAllAttributeTypes(RequestBuilder.ConceptMessage.concepts(Arrays.asList(attributeTypes)))).build();

                return conceptStream(method, res -> res.getThingAttributesIterRes().getAttribute()).map(Concept.Remote::asAttribute);
            }

            @SuppressWarnings("unchecked")
            @Override
            public final <T> Stream<Attribute.Remote<T>> attributes(AttributeType<T> attributeType) {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setThingAttributesIterReq(ConceptProto.Thing.Attributes.Iter.Req.newBuilder()
                                .addAllAttributeTypes(RequestBuilder.ConceptMessage.concepts(Collections.singleton(attributeType)))).build();

                return conceptStream(method, res -> res.getThingAttributesIterRes().getAttribute()).map(Concept.Remote::asAttribute)
                        .map(a -> (Attribute.Remote<T>) a);
            }

            @Override
            public final Stream<Attribute.Remote<?>> attributes(boolean keysOnly) {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setThingAttributesIterReq(ConceptProto.Thing.Attributes.Iter.Req.newBuilder().setKeysOnly(keysOnly)).build();
                return conceptStream(method, res -> res.getThingAttributesIterRes().getAttribute()).map(Concept.Remote::asAttribute);
            }

            @Override
            public final Stream<Relation.Remote> relations(RoleType... roleTypes) {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setThingRelationsIterReq(ConceptProto.Thing.Relations.Iter.Req.newBuilder()
                                                      .addAllRoles(RequestBuilder.ConceptMessage.concepts(Arrays.asList(roleTypes)))).build();

                return conceptStream(method, res -> res.getThingRelationsIterRes().getRelation()).map(Concept.Remote::asRelation);
            }

            @Override
            public final Stream<RoleType.Remote> roleTypes() {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setThingRolesIterReq(ConceptProto.Thing.Roles.Iter.Req.getDefaultInstance()).build();

                return conceptStream(method, res -> res.getThingRolesIterRes().getRole()).map(Concept.Remote::asRoleType);
            }

            @Override
            public Thing.Remote<SomeRemoteThing, SomeRemoteType> has(Attribute<?> attribute) {
                // TODO: replace usage of this method as a getter, with relations(Attribute attribute)
                // TODO: then remove this method altogether and just use has(Attribute attribute)
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setThingHasReq(ConceptProto.Thing.Has.Req.newBuilder()
                                                   .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();
                runMethod(method);
                return this;
            }

            @Override
            public Thing.Remote<SomeRemoteThing, SomeRemoteType> unhas(Attribute<?> attribute) {
                ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setThingUnhasReq(ConceptProto.Thing.Unhas.Req.newBuilder()
                                                  .setAttribute(RequestBuilder.ConceptMessage.from(attribute))).build();

                runMethod(method);
                return this;
            }

            @Override
            protected abstract Thing.Remote<SomeRemoteThing, SomeRemoteType> asCurrentBaseType(Concept.Remote<?> other);
        }
    }
}
