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

import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public class ThingTypeImpl extends TypeImpl implements ThingType {

    ThingTypeImpl(final String label, final boolean isRoot) {
        super(label, isRoot);
    }

    public static ThingTypeImpl of(final ConceptProto.Type typeProto) {
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
    public ThingTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
        return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public final ThingTypeImpl asThingType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        public static ThingTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type type) {
            return new ThingTypeImpl.Remote(transaction, type.getLabel(), type.getRoot());
        }

        @Override
        public ThingTypeImpl getSupertype() {
            return super.getSupertypeExecute(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSupertypes() {
            return super.getSupertypes(TypeImpl::asThingType);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes() {
            return super.getSubtypes(TypeImpl::asThingType);
        }

        <THING extends ThingImpl> Stream<THING> getInstances(Function<ThingImpl, THING> thingConstructor) {
            final ConceptProto.Type.Req.Builder request = ConceptProto.Type.Req.newBuilder()
                    .setThingTypeGetInstancesReq(GetInstances.Req.getDefaultInstance());

            return thingStream(request, res -> res.getThingTypeGetInstancesRes().getThing()).map(thingConstructor);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances() {
            return getInstances(x -> x);
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
            return stream(
                    ConceptProto.Type.Req.newBuilder().setThingTypeGetPlaysReq(
                            GetPlays.Req.getDefaultInstance()),
                    res -> res.getThingTypeGetPlaysRes().getRole()
            ).map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<AttributeTypeImpl> getOwns(final ValueType valueType, final boolean keysOnly) {
            final GetOwns.Req.Builder req = GetOwns.Req.newBuilder().setKeysOnly(keysOnly);
            if (valueType != null) req.setValueType(valueType(valueType));
            return stream(
                    ConceptProto.Type.Req.newBuilder().setThingTypeGetOwnsReq(req),
                    res -> res.getThingTypeGetOwnsRes().getAttributeType()
            ).map(TypeImpl::asAttributeType);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns() {
            return getOwns(null, false);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns(final ValueType valueType) {
            return getOwns(valueType, false);
        }

        @Override
        public Stream<AttributeTypeImpl> getOwns(final boolean keysOnly) {
            return getOwns(null, keysOnly);
        }

        @Override
        public final void setOwns(final AttributeType attributeType, final AttributeType overriddenType, final boolean isKey) {
            final SetOwns.Req.Builder req = SetOwns.Req.newBuilder().setAttributeType(type(attributeType)).setIsKey(isKey);
            if (overriddenType != null) req.setOverriddenType(type(overriddenType));
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetOwnsReq(req));
        }

        @Override
        public void setOwns(final AttributeType attributeType, final AttributeType overriddenType) {
            setOwns(attributeType, overriddenType, false);
        }

        @Override
        public void setOwns(final AttributeType attributeType, final boolean isKey) {
            setOwns(attributeType, null, isKey);
        }

        @Override
        public void setOwns(final AttributeType attributeType) {
            setOwns(attributeType, null, false);
        }

        @Override
        public final void setPlays(final RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public final void setPlays(final RoleType role, final RoleType overriddenRole) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role)).setOverriddenRole(type(overriddenRole))));
        }

        @Override
        public final void unsetOwns(final AttributeType attributeType) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetOwnsReq(
                    UnsetOwns.Req.newBuilder().setAttributeType(type(attributeType))));
        }

        @Override
        public final void unsetPlays(final RoleType role) {
            execute(ConceptProto.Type.Req.newBuilder().setThingTypeUnsetPlaysReq(
                    UnsetPlays.Req.newBuilder().setRole(type(role))));
        }

        @Override
        public ThingTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final ThingTypeImpl.Remote asThingType() {
            return this;
        }
    }
}
