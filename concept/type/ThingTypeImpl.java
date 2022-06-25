/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.AttributeType.ValueType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.rpc.RequestBuilder;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.protocol.ConceptProto;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getInstancesExplicitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getInstancesReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getOwnsExplicitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getOwnsOverriddenReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getOwnsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getPlaysExplicitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getPlaysOverriddenReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getPlaysReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.getSyntaxReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.setAbstractReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.setOwnsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.setPlaysReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.setSupertypeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.unsetAbstractReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.unsetOwnsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.ThingType.unsetPlaysReq;
import static com.vaticle.typedb.client.concept.type.RoleTypeImpl.protoRoleType;

public class ThingTypeImpl extends TypeImpl implements ThingType {

    ThingTypeImpl(Label label, boolean isRoot) {
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
                return new ThingTypeImpl(Label.of(typeProto.getLabel()), typeProto.getRoot());
            case UNRECOGNIZED:
            default:
                throw new TypeDBClientException(BAD_ENCODING, typeProto.getEncoding());
        }
    }

    public static ConceptProto.Type protoThingType(ThingType thingType) {
        return RequestBuilder.Type.ThingType.protoThingType(thingType.getLabel(), TypeImpl.encoding(thingType));
    }

    @Override
    public ThingTypeImpl.Remote asRemote(TypeDBTransaction transaction) {
        return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public final ThingTypeImpl asThingType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        Remote(TypeDBTransaction transaction, Label label, boolean isRoot) {
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
        public Stream<? extends ThingTypeImpl> getSubtypesExplicit() {
            return super.getSubtypesExplicit().map(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances() {
            return stream(getInstancesReq(getLabel()))
                    .flatMap(rp -> rp.getThingTypeGetInstancesResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public Stream<? extends ThingImpl> getInstancesExplicit() {
            return stream(getInstancesExplicitReq(getLabel()))
                    .flatMap(rp -> rp.getThingTypeGetInstancesExplicitResPart().getThingsList().stream())
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
            execute(setPlaysReq(getLabel(), protoRoleType(roleType)));
        }

        @Override
        public final void setPlays(RoleType roleType, RoleType overriddenRoleType) {
            execute(setPlaysReq(getLabel(), protoRoleType(roleType), protoRoleType(overriddenRoleType)));
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
                    .flatMap(rp -> rp.getThingTypeGetPlaysResPart().getRoleTypesList().stream())
                    .map(RoleTypeImpl::of);
        }

        @Override
        public final Stream<RoleTypeImpl> getPlaysExplicit() {
            return stream(getPlaysExplicitReq(getLabel()))
                    .flatMap(rp -> rp.getThingTypeGetPlaysExplicitResPart().getRoleTypesList().stream())
                    .map(RoleTypeImpl::of);
        }

        @Override
        public RoleTypeImpl getPlaysOverridden(RoleType roleType) {
            ConceptProto.ThingType.GetPlaysOverridden.Res res = execute(
                    getPlaysOverriddenReq(getLabel(), protoRoleType(roleType))
            ).getThingTypeGetPlaysOverriddenRes();
            switch (res.getResCase()) {
                case ROLE_TYPE:
                    return RoleTypeImpl.of(res.getRoleType());
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns() {
            return getOwns(false);
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
        public Stream<? extends AttributeType> getOwnsExplicit() {
            return getOwnsExplicit(false);
        }

        @Override
        public Stream<? extends AttributeType> getOwnsExplicit(ValueType valueType) {
            return getOwnsExplicit(valueType, false);
        }

        @Override
        public Stream<? extends AttributeType> getOwnsExplicit(boolean keysOnly) {
            return stream(getOwnsExplicitReq(getLabel(), keysOnly))
                    .flatMap(rp -> rp.getThingTypeGetOwnsExplicitResPart().getAttributeTypesList().stream())
                    .map(AttributeTypeImpl::of);
        }

        @Override
        public Stream<? extends AttributeType> getOwnsExplicit(ValueType valueType, boolean keysOnly) {
            return stream(getOwnsExplicitReq(getLabel(), valueType.proto(), keysOnly))
                    .flatMap(rp -> rp.getThingTypeGetOwnsExplicitResPart().getAttributeTypesList().stream())
                    .map(AttributeTypeImpl::of);
        }

        @Override
        public AttributeTypeImpl getOwnsOverridden(AttributeType attributeType) {
            ConceptProto.ThingType.GetOwnsOverridden.Res res = execute(
                    getOwnsOverriddenReq(getLabel(), protoThingType(attributeType))
            ).getThingTypeGetOwnsOverriddenRes();
            switch (res.getResCase()) {
                case ATTRIBUTE_TYPE:
                    return AttributeTypeImpl.of(res.getAttributeType());
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public final void unsetPlays(RoleType roleType) {
            execute(unsetPlaysReq(getLabel(), protoRoleType(roleType)));
        }

        @Override
        public final void unsetOwns(AttributeType attributeType) {
            execute(unsetOwnsReq(getLabel(), protoThingType(attributeType)));
        }

        @Override
        public ThingTypeImpl.Remote asRemote(TypeDBTransaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final ThingTypeImpl.Remote asThingType() {
            return this;
        }

        @Override
        public final boolean isDeleted() {
            return transactionExt.concepts().getThingType(getLabel().name()) == null;
        }

        @Override
        public final String getSyntax() {
            return execute(getSyntaxReq(getLabel())).getThingTypeGetSyntaxRes().getSyntax();
        }
    }
}
