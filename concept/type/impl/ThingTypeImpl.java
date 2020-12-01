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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.ThingType.GetInstances;
import grakn.protocol.ConceptProto.ThingType.GetOwns;
import grakn.protocol.ConceptProto.ThingType.GetPlays;
import grakn.protocol.ConceptProto.ThingType.SetAbstract;
import grakn.protocol.ConceptProto.ThingType.SetOwns;
import grakn.protocol.ConceptProto.ThingType.SetPlays;
import grakn.protocol.ConceptProto.ThingType.UnsetAbstract;
import grakn.protocol.ConceptProto.ThingType.UnsetOwns;
import grakn.protocol.ConceptProto.ThingType.UnsetPlays;

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
    public ThingTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
        return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public final ThingTypeImpl asThingType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        Remote(Grakn.Transaction transaction, String label, boolean isRoot) {
            super(transaction, label, isRoot);
        }

        void setSupertype(ThingType thingType) {
            super.setSupertype(thingType);
        }

        @Override
        public ThingTypeImpl getSupertype() {
            final TypeImpl supertype = super.getSupertype();
            return supertype != null ? supertype.asThingType() : null;
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSupertypes() {
            return super.getSupertypes().map(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes() {
            return super.getSubtypes().map(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances() {
            final ConceptProto.Type.Req.Builder request = ConceptProto.Type.Req.newBuilder()
                    .setThingTypeGetInstancesReq(GetInstances.Req.getDefaultInstance());
            return thingStream(request, res -> res.getThingTypeGetInstancesRes().getThingList());
        }

        @Override
        public final void setAbstract() {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetAbstractReq(SetAbstract.Req.getDefaultInstance()));
        }

        @Override
        public final void unsetAbstract() {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetAbstractReq(UnsetAbstract.Req.getDefaultInstance()));
        }

        @Override
        public final Stream<RoleTypeImpl> getPlays() {
            return typeStream(
                    ConceptProto.Type.Req.newBuilder().setThingTypeGetPlaysReq(
                            GetPlays.Req.getDefaultInstance()),
                    res -> res.getThingTypeGetPlaysRes().getRoleList()
            ).map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<AttributeTypeImpl> getOwns(ValueType valueType, boolean keysOnly) {
            final GetOwns.Req.Builder req = GetOwns.Req.newBuilder().setKeysOnly(keysOnly);
            if (valueType != null) req.setValueType(valueType(valueType));
            return typeStream(
                    ConceptProto.Type.Req.newBuilder().setThingTypeGetOwnsReq(req),
                    res -> res.getThingTypeGetOwnsRes().getAttributeTypeList()
            ).map(TypeImpl::asAttributeType);
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
        public final void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            final SetOwns.Req.Builder req = SetOwns.Req.newBuilder().setAttributeType(type(attributeType)).setIsKey(isKey);
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
        public final void setPlays(RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public final void setPlays(RoleType role, RoleType overriddenRole) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role)).setOverriddenRole(type(overriddenRole))));
        }

        @Override
        public final void unsetOwns(AttributeType attributeType) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetOwnsReq(
                    UnsetOwns.Req.newBuilder().setAttributeType(type(attributeType))));
        }

        @Override
        public final void unsetPlays(RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetPlaysReq(
                    UnsetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public ThingTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final ThingTypeImpl.Remote asThingType() {
            return this;
        }
    }
}
