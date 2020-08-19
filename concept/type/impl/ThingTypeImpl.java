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
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.rpc.ConceptMessage;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

public abstract class ThingTypeImpl {
    /**
     * Client implementation of ThingType
     */
    public static class Local extends TypeImpl.Local implements ThingType.Local {

        public Local(ConceptProto.Type type) {
            super(type);
        }
    }

    /**
     * Client implementation of ThingType
     */
    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        public Remote(Transaction tx, String label) {
            super(tx, label);
        }

        @Override
        public ThingType.Remote getSupertype() {
            return super.getSupertype().asThingType();
        }

        @Override
        public Stream<? extends ThingType.Remote> getSupertypes() {
            return super.getSupertypes().map(Type.Remote::asThingType);
        }

        @Override
        public Stream<? extends ThingType.Remote> getSubtypes() {
            return super.getSubtypes().map(Type.Remote::asThingType);
        }

        @Override
        public Stream<? extends Thing.Remote> getInstances() {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setThingTypeGetInstancesIterReq(ConceptProto.ThingType.GetInstances.Iter.Req.getDefaultInstance()).build();

            return thingStream(method, res -> res.getThingTypeGetInstancesIterRes().getThing()).map(this::asInstance);
        }

        @Override
        public final boolean isAbstract() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeIsAbstractReq(ConceptProto.ThingType.IsAbstract.Req.getDefaultInstance()).build();

            return runMethod(method).getThingTypeIsAbstractRes().getAbstract();
        }

        @Override
        public final void setAbstract() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetAbstractReq(ConceptProto.ThingType.SetAbstract.Req.getDefaultInstance()).build();
            runMethod(method);
        }

        @Override
        public final void unsetAbstract() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeUnsetAbstractReq(ConceptProto.ThingType.UnsetAbstract.Req.getDefaultInstance()).build();
            runMethod(method);
        }

        @Override
        public final Stream<AttributeType.Remote> getOwns(ValueType valueType, boolean keysOnly) {
            final ConceptProto.ThingType.GetOwns.Iter.Req.Builder req = ConceptProto.ThingType.GetOwns.Iter.Req.newBuilder()
                            .setKeysOnly(keysOnly);

            if (valueType != null) {
                req.setValueType(ConceptMessage.valueType(valueType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder().setThingTypeGetOwnsIterReq(req).build();

            return typeStream(method, res -> res.getThingTypeGetOwnsIterRes().getAttributeType()).map(Type.Remote::asAttributeType);
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setThingTypeGetPlaysIterReq(ConceptProto.ThingType.GetPlays.Iter.Req.getDefaultInstance()).build();

            return typeStream(method, res -> res.getThingTypeGetPlaysIterRes().getRole()).map(Type.Remote::asRoleType);
        }

        @Override
        public void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            final ConceptProto.ThingType.SetOwns.Req.Builder req = ConceptProto.ThingType.SetOwns.Req.newBuilder()
                    .setAttributeType(ConceptMessage.type(attributeType))
                    .setIsKey(isKey);

            if (overriddenType != null) {
                req.setOverriddenType(ConceptMessage.type(overriddenType));
            } else {
                req.setNULL(ConceptProto.Null.getDefaultInstance());
            }

            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetOwnsReq(req).build();

            runMethod(method);
        }

        @Override
        public void setPlays(RoleType role) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetPlaysReq(ConceptProto.ThingType.SetPlays.Req.newBuilder()
                            .setRole(ConceptMessage.type(role))).build();
            runMethod(method);
        }

        @Override
        public void unsetOwns(AttributeType attributeType) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeUnsetOwnsReq(ConceptProto.ThingType.UnsetOwns.Req.newBuilder()
                            .setAttributeType(ConceptMessage.type(attributeType))).build();
            runMethod(method);
        }

        @Override
        public void unsetPlays(RoleType role) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeUnsetPlaysReq(ConceptProto.ThingType.UnsetPlays.Req.newBuilder()
                            .setRole(ConceptMessage.type(role))).build();
            runMethod(method);
        }

        protected Thing.Remote asInstance(Thing.Remote concept) {
            // TODO: extract hardcoded error message
            throw GraknConceptException.create("Cannot create instances of ThingType");
        }
    }
}
