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

        protected Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> setLabel(Label label) {
            return (ThingType.Remote<SomeRemoteType, SomeRemoteThing>) super.setLabel(label);
        }

        @Override
        public Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Thing.Remote<SomeRemoteThing, SomeRemoteType>> getInstances() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingTypeGetInstancesIterReq(ConceptProto.ThingType.GetInstances.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingTypeGetInstancesIterRes().getThing()).map(this::asInstance);
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
        public final <D> Stream<AttributeType.Remote<D>> getOwns(ValueType<D> valueType, boolean keysOnly) {
            ConceptProto.ThingType.GetOwns.Iter.Req.Builder req = ConceptProto.ThingType.GetOwns.Iter.Req.newBuilder()
                            .setKeysOnly(keysOnly);

            if (valueType != null) {
                req.setValueType(RequestBuilder.ConceptMessage.setValueType(valueType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder().setThingTypeGetOwnsIterReq(req).build();

            return conceptStream(method, res -> res.getThingTypeGetOwnsIterRes().getAttributeType()).map(Concept.Remote::asAttributeType);
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setThingTypeGetPlaysIterReq(ConceptProto.ThingType.GetPlays.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getThingTypeGetPlaysIterRes().getRole()).map(Concept.Remote::asRoleType);
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey) {
            ConceptProto.ThingType.SetOwns.Req.Builder req = ConceptProto.ThingType.SetOwns.Req.newBuilder()
                    .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))
                    .setIsKey(isKey);

            if (overriddenType != null) {
                req.setOverriddenType(RequestBuilder.ConceptMessage.from(overriddenType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeSetOwnsReq(req).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> setPlays(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeSetPlaysReq(ConceptProto.ThingType.SetPlays.Req.newBuilder()
                                             .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> unsetOwns(AttributeType<?> attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnsetOwnsReq(ConceptProto.ThingType.UnsetOwns.Req.newBuilder()
                                             .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();

            runMethod(method);
            return this;
        }

        @Override
        public ThingType.Remote<SomeRemoteType, SomeRemoteThing> unsetPlays(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnsetPlaysReq(ConceptProto.ThingType.UnsetPlays.Req.newBuilder()
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
