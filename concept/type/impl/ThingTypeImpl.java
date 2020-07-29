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
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.ValueType;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public abstract class ThingTypeImpl {
    /**
     * Client implementation of Type
     *
     * @param <SomeType>  The exact type of this class
     */
    public abstract static class Local<
            SomeType extends ThingType<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends TypeImpl.Local<SomeType>
            implements ThingType.Local<SomeType, SomeThing> {

        protected Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of Type
     *
     * @param <SomeRemoteType>  The exact type of this class
     * @param <SomeRemoteThing> the exact type of instances of this class
     */
    public abstract static class Remote<
            SomeRemoteType extends ThingType<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
            extends TypeImpl.Remote<SomeRemoteType>
            implements ThingType.Remote<SomeRemoteType, SomeRemoteThing> {

        protected Remote(GraknClient.Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> label(Label label) {
            return (ThingType.Remote<SomeRemoteType, SomeRemoteThing>) super.label(label);
        }

        @Override
        public Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> sups() {
            return super.sups().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> subs() {
            return super.subs().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Thing.Remote<SomeRemoteThing, SomeRemoteType>> instances() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingTypeInstancesIterReq(ConceptProto.ThingType.Instances.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingTypeInstancesIterRes().getThing()).map(this::asInstance);
        }

        @Override
        public final Boolean isAbstract() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeIsAbstractReq(ConceptProto.ThingType.IsAbstract.Req.getDefaultInstance()).build();

            return runMethod(method).getThingTypeIsAbstractRes().getAbstract();
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> isAbstract(Boolean isAbstract) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeSetAbstractReq(ConceptProto.ThingType.SetAbstract.Req.newBuilder()
                                                   .setAbstract(isAbstract)).build();

            runMethod(method);
            return this;
        }

        @Override
        public final <D> Stream<AttributeType.Remote<D>> attributes(ValueType<D> valueType, boolean keysOnly) {
            ConceptProto.ThingType.Attributes.Iter.Req.Builder req = ConceptProto.ThingType.Attributes.Iter.Req.newBuilder()
                            .setKeysOnly(keysOnly);

            if (valueType != null) {
                req.setValueType(RequestBuilder.ConceptMessage.setValueType(valueType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder().setThingTypeAttributesIterReq(req).build();

            return conceptStream(method, res -> res.getThingTypeAttributesIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
        }

        @Override
        public final Stream<RoleType.Remote> playing() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingTypePlayingIterReq(ConceptProto.ThingType.Playing.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingTypePlayingIterRes().getRole()).map(Concept.Remote::asRole);
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> has(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey) {
            ConceptProto.ThingType.Has.Req.Builder req = ConceptProto.ThingType.Has.Req.newBuilder()
                    .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))
                    .setIsKey(isKey);

            if (overriddenType != null) {
                req.setOverriddenType(RequestBuilder.ConceptMessage.from(overriddenType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeHasReq(req).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> plays(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypePlaysReq(ConceptProto.ThingType.Plays.Req.newBuilder()
                                             .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> unhas(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnhasReq(ConceptProto.ThingType.Unhas.Req.newBuilder()
                                             .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> unplay(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnplayReq(ConceptProto.ThingType.Unplay.Req.newBuilder()
                                              .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        protected abstract Thing.Remote<SomeRemoteThing, SomeRemoteType> asInstance(Concept.Remote<?> concept);

        @Override
        protected abstract ThingType.Remote<SomeRemoteType, SomeRemoteThing> asCurrentBaseType(Concept.Remote<?> other);

        protected abstract boolean equalsCurrentBaseType(Concept.Remote<?> other);
    }
}
