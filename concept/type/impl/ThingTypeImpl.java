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
import grakn.client.concept.ValueTypeOld;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public abstract class ThingTypeImpl {
    /**
     * Client implementation of ThingType
     */
    public abstract static class Local extends TypeImpl.Local implements ThingType.Local {

        protected Local(ConceptProto.Concept concept) {
            super(concept);
        }
    }

    /**
     * Client implementation of ThingType
     */
    public abstract static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        protected Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public ThingType.Remote setLabel(Label label) {
            return (ThingType.Remote) super.setLabel(label);
        }

        @Override
        public Stream<? extends ThingType.Remote> getSupertypes() {
            return super.getSupertypes().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends ThingType.Remote> getSubtypes() {
            return super.getSubtypes().map(this::asCurrentBaseType);
        }

        @Override
        public Stream<? extends Thing.Remote> getInstances() {
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
        public ThingType.Remote isAbstract(Boolean isAbstract) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeSetAbstractReq(ConceptProto.ThingType.SetAbstract.Req.newBuilder()
                                                   .setAbstract(isAbstract)).build();

            runMethod(method);
            return this;
        }

        @Override
        public final <D> Stream<AttributeType.Remote> getOwns(ValueTypeOld valueType, boolean keysOnly) {
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
        public ThingType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
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
        public ThingType.Remote setPlays(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeSetPlaysReq(ConceptProto.ThingType.SetPlays.Req.newBuilder()
                                             .setRole(RequestBuilder.ConceptMessage.from(role))).build();

            runMethod(method);
            return this;
        }

        @Override
        public void unsetOwns(AttributeType attributeType) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnsetOwnsReq(ConceptProto.ThingType.UnsetOwns.Req.newBuilder()
                                             .setAttributeType(RequestBuilder.ConceptMessage.from(attributeType))).build();
            runMethod(method);
        }

        @Override
        public void unsetPlays(RoleType role) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setThingTypeUnsetPlaysReq(ConceptProto.ThingType.UnsetPlays.Req.newBuilder()
                                              .setRole(RequestBuilder.ConceptMessage.from(role))).build();
            runMethod(method);
        }

        protected abstract Thing.Remote asInstance(Concept.Remote concept);

        @Override
        protected abstract ThingType.Remote asCurrentBaseType(Concept.Remote other);
    }
}
