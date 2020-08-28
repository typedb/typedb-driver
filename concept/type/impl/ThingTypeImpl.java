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

import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public abstract class ThingTypeImpl {
    /**
     * Client implementation of ThingType
     */
    public static class Local extends TypeImpl.Local implements ThingType.Local {

        public Local(final ConceptProto.Type type) {
            super(type);
        }
    }

    /**
     * Client implementation of ThingType
     */
    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        public Remote(final Concepts concepts, final String label, final boolean isRoot) {
            super(concepts, label, isRoot);
        }

        @Override
        public ThingType.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asThingType);
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

            return thingStream(method, res -> res.getThingTypeGetInstancesIterRes().getThing());
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
        public final Stream<AttributeType.Remote> getOwns(final ValueType valueType, final boolean keysOnly) {
            final ConceptProto.ThingType.GetOwns.Iter.Req.Builder req = ConceptProto.ThingType.GetOwns.Iter.Req.newBuilder()
                    .setKeysOnly(keysOnly);

            if (valueType != null) {
                req.setValueType(valueType(valueType));
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
        public final void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            final ConceptProto.ThingType.SetOwns.Req.Builder req = ConceptProto.ThingType.SetOwns.Req.newBuilder()
                    .setAttributeType(type(attributeType))
                    .setIsKey(isKey);

            if (overriddenType != null) {
                req.setOverriddenType(type(overriddenType));
            }

            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetOwnsReq(req).build();
            runMethod(method);
        }

        @Override
        public final void setPlays(final RoleType role) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetPlaysReq(ConceptProto.ThingType.SetPlays.Req.newBuilder()
                                                     .setRole(type(role))).build();
            runMethod(method);
        }

        @Override
        public final void setPlays(final RoleType role, final RoleType overriddenRole) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeSetPlaysReq(ConceptProto.ThingType.SetPlays.Req.newBuilder()
                                                     .setRole(type(role))
                                                     .setOverriddenRole(type(overriddenRole))).build();
            runMethod(method);
        }

        @Override
        public final void unsetOwns(final AttributeType attributeType) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeUnsetOwnsReq(ConceptProto.ThingType.UnsetOwns.Req.newBuilder()
                                                      .setAttributeType(type(attributeType))).build();
            runMethod(method);
        }

        @Override
        public final void unsetPlays(final RoleType role) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setThingTypeUnsetPlaysReq(ConceptProto.ThingType.UnsetPlays.Req.newBuilder()
                                                       .setRole(type(role))).build();
            runMethod(method);
        }
    }
}
