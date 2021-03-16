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

package grakn.client.concept.type;

import grakn.client.api.Transaction;
import grakn.client.api.concept.type.AttributeType;
import grakn.client.api.concept.type.AttributeType.ValueType;
import grakn.client.api.concept.type.RoleType;
import grakn.client.api.concept.type.ThingType;
import grakn.client.common.GraknClientException;
import grakn.client.common.RequestBuilder;
import grakn.client.concept.thing.ThingImpl;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.common.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.common.RequestBuilder.Type.ThingType.getInstancesReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.getOwnsReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.getPlaysReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.setAbstractReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.setOwnsReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.setPlaysReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.setSupertypeReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.unsetAbstractReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.unsetOwnsReq;
import static grakn.client.common.RequestBuilder.Type.ThingType.unsetPlaysReq;
import static grakn.client.concept.type.RoleTypeImpl.protoRoleTypes;

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

    public static ConceptProto.Type protoThingType(ThingType thingType) {
        return RequestBuilder.Type.ThingType.protoThingType(thingType.getLabel(), TypeImpl.encoding(thingType));
    }

    @Override
    public ThingTypeImpl.Remote asRemote(Transaction transaction) {
        return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public final ThingTypeImpl asThingType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        Remote(Transaction transaction, String label, boolean isRoot) {
            super(transaction, label, isRoot);
        }

        void setSupertype(ThingType thingType) {
            execute(setSupertypeReq(getLabel(), protoThingType(thingType)));
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
            return stream(getInstancesReq(getLabel()))
                    .flatMap(rp -> rp.getThingTypeGetInstancesResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public final void setAbstract() {
            execute(setAbstractReq(getLabel()));
        }

        @Override
        public final void unsetAbstract() {
            execute(unsetAbstractReq(getLabel()));
        }

        @Override
        public final void setPlays(RoleType roleType) {
            execute(setPlaysReq(getLabel(), protoRoleTypes(roleType)));
        }

        @Override
        public final void setPlays(RoleType roleType, RoleType overriddenRoleType) {
            execute(setPlaysReq(getLabel(), protoRoleTypes(roleType), protoRoleTypes(overriddenRoleType)));
        }

        @Override
        public void setOwns(AttributeType attributeType) {
            setOwns(attributeType, false);
        }

        @Override
        public void setOwns(AttributeType attributeType, boolean isKey) {
            execute(setOwnsReq(getLabel(), protoThingType(attributeType), isKey));
        }

        @Override
        public void setOwns(AttributeType attributeType, AttributeType overriddenType) {
            setOwns(attributeType, overriddenType, false);
        }

        @Override
        public final void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            execute(setOwnsReq(getLabel(), protoThingType(attributeType), protoThingType(overriddenType), isKey));
        }

        @Override
        public final Stream<RoleTypeImpl> getPlays() {
            return stream(getPlaysReq(getLabel()))
                    .flatMap(rp -> rp.getThingTypeGetPlaysResPart().getRolesList().stream())
                    .map(RoleTypeImpl::of);
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
            return stream(getOwnsReq(getLabel(), keysOnly))
                    .flatMap(rp -> rp.getThingTypeGetOwnsResPart().getAttributeTypesList().stream())
                    .map(AttributeTypeImpl::of);
        }

        @Override
        public final Stream<AttributeTypeImpl> getOwns(ValueType valueType, boolean keysOnly) {
            return stream(getOwnsReq(getLabel(), valueType.proto(), keysOnly))
                    .flatMap(rp -> rp.getThingTypeGetOwnsResPart().getAttributeTypesList().stream())
                    .map(AttributeTypeImpl::of);
        }

        @Override
        public final void unsetPlays(RoleType roleType) {
            execute(unsetPlaysReq(getLabel(), protoRoleTypes(roleType)));
        }

        @Override
        public final void unsetOwns(AttributeType attributeType) {
            execute(unsetOwnsReq(getLabel(), protoThingType(attributeType)));
        }

        @Override
        public ThingTypeImpl.Remote asRemote(Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final ThingTypeImpl.Remote asThingType() {
            return this;
        }
    }
}
