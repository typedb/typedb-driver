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
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public class ThingTypeImpl extends TypeImpl implements ThingType {

    ThingTypeImpl(String label, boolean isRoot) {
        super(label, isRoot);
    }

    public static ThingTypeImpl of(ConceptProto.Type typeProto) {
        switch (typeProto.getEncoding()) {
            case ENTITY_TYPE:
                return EntityTypeImpl.of(typeProto);
            case RELATION_TYPE:
                return RelationTypeImpl.of(typeProto);
            case ATTRIBUTE_TYPE:
                return AttributeTypeImpl.of(typeProto);
            case THING_TYPE:
                assert typeProto.getRoot();
                return new ThingTypeImpl(typeProto.getLabel(), typeProto.getRoot());
            case UNRECOGNIZED:
            default:
                throw new GraknClientException(BAD_ENCODING.message(typeProto.getEncoding()));
        }
    }

    @Override
    public ThingTypeImpl.Remote asRemote(GraknClient.Transaction transaction) {
        return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public final ThingTypeImpl asThingType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        Remote(GraknClient.Transaction transaction, String label, boolean isRoot) {
            super(transaction, label, isRoot);
        }

        void setSupertype(ThingType thingType) {
            super.setSupertype(thingType);
        }

        @Override
        public ThingTypeImpl getSupertype() {
            TypeImpl supertype = super.getSupertype();
            return supertype != null ? supertype.asThingType() : null;
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSupertypes() {
            Stream<? extends TypeImpl> supertypes = super.getSupertypes();
            return supertypes.map(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes() {
            return super.getSubtypes().map(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances() {
            ConceptProto.Type.Req.Builder request = ConceptProto.Type.Req.newBuilder()
                    .setThingTypeGetInstancesReq(ConceptProto.ThingType.GetInstances.Req.getDefaultInstance());
            return thingStream(request, res -> res.getThingTypeGetInstancesRes().getThingsList());
        }

        @Override
        public final void setAbstract() {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetAbstractReq(ConceptProto.ThingType.SetAbstract.Req.getDefaultInstance()));
        }

        @Override
        public final void unsetAbstract() {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetAbstractReq(ConceptProto.ThingType.UnsetAbstract.Req.getDefaultInstance()));
        }

        @Override
        public final void setPlays(RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public final void setPlays(RoleType role, RoleType overriddenRole) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(type(role)).setOverriddenRole(type(overriddenRole))));
        }

        @Override
        public final void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            ConceptProto.ThingType.SetOwns.Req.Builder req = ConceptProto.ThingType.SetOwns.Req.newBuilder()
                    .setAttributeType(type(attributeType)).setIsKey(isKey);
            if (overriddenType != null) req.setOverriddenType(type(overriddenType));
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetOwnsReq(req));
        }

        @Override
        public void setOwns(AttributeType attributeType, AttributeType overriddenType) {
            setOwns(attributeType, overriddenType, false);
        }

        @Override
        public void setOwns(AttributeType attributeType, boolean isKey) {
            setOwns(attributeType, null, isKey);
        }

        @Override
        public void setOwns(AttributeType attributeType) {
            setOwns(attributeType, null, false);
        }

        @Override
        public final Stream<RoleTypeImpl> getPlays() {
            ConceptProto.Type.Req.Builder request = ConceptProto.Type.Req.newBuilder()
                    .setThingTypeGetPlaysReq(ConceptProto.ThingType.GetPlays.Req.getDefaultInstance());
            return typeStream(request, res -> res.getThingTypeGetPlaysRes().getRolesList()).map(TypeImpl::asRoleType);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns() {
            return getOwns(null, false);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns(ValueType valueType) {
            return getOwns(valueType, false);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns(boolean keysOnly) {
            return getOwns(null, keysOnly);
        }

        @Override
        public final Stream<AttributeTypeImpl> getOwns(ValueType valueType, boolean keysOnly) {
            ConceptProto.ThingType.GetOwns.Req.Builder getOwnsReq = ConceptProto.ThingType.GetOwns.Req.newBuilder().setKeysOnly(keysOnly);
            if (valueType != null) getOwnsReq.setValueType(valueType(valueType));
            ConceptProto.Type.Req.Builder request = ConceptProto.Type.Req.newBuilder().setThingTypeGetOwnsReq(getOwnsReq);
            return typeStream(request, res -> res.getThingTypeGetOwnsRes().getAttributeTypesList()).map(TypeImpl::asAttributeType);
        }

        @Override
        public final void unsetPlays(RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetPlaysReq(
                    ConceptProto.ThingType.UnsetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public final void unsetOwns(AttributeType attributeType) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetOwnsReq(
                    ConceptProto.ThingType.UnsetOwns.Req.newBuilder().setAttributeType(type(attributeType))));
        }

        @Override
        public ThingTypeImpl.Remote asRemote(GraknClient.Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final ThingTypeImpl.Remote asThingType() {
            return this;
        }
    }
}
