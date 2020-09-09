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
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
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
import grakn.protocol.ConceptProto.TypeMethod;

import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.client.concept.proto.ConceptProtoBuilder.valueType;

public abstract class ThingTypeImpl {

    public static class Local extends TypeImpl.Local implements ThingType.Local {

        Local(final String label, final boolean isRoot) {
            super(label, null, isRoot);
        }

        public static TypeImpl.Local of(ConceptProto.Type typeProto) {
            switch (typeProto.getEncoding()) {
                case ENTITY_TYPE:
                    return EntityTypeImpl.Local.of(typeProto);
                case RELATION_TYPE:
                    return RelationTypeImpl.Local.of(typeProto);
                case ATTRIBUTE_TYPE:
                    return AttributeTypeImpl.Local.of(typeProto);
                case THING_TYPE:
                    assert typeProto.getRoot();
                    return new ThingTypeImpl.Local(typeProto.getLabel(), typeProto.getRoot());
                case UNRECOGNIZED:
                default:
                    throw new GraknClientException(BAD_ENCODING.message(typeProto.getEncoding()));
            }
        }

        @Override
        public ThingTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, getLabel(), isRoot());
        }
    }

    public static class Remote extends TypeImpl.Remote implements ThingType.Remote {

        public Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, null, isRoot);
        }

        public static ThingTypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type type) {
            return new ThingTypeImpl.Remote(transaction, type.getLabel(), type.getRoot());
        }

        @Override
        public ThingType.Local getSupertype() {
            return super.getSupertypeExecute(Type.Local::asThingType);
        }

        @Override
        public Stream<? extends ThingType.Local> getSupertypes() {
            return super.getSupertypes(Type.Local::asThingType);
        }

        @Override
        public Stream<? extends ThingType.Local> getSubtypes() {
            return super.getSubtypes(Type.Local::asThingType);
        }

        <THING extends Thing.Local> Stream<THING> getInstances(Function<ConceptProto.Thing, THING> thingConstructor) {
            return tx().concepts().iterateTypeMethod(
                    label, scope,
                    TypeMethod.Iter.Req.newBuilder().setThingTypeGetInstancesIterReq(
                            GetInstances.Iter.Req.getDefaultInstance()).build(),
                    response -> thingConstructor.apply(response.getThingTypeGetInstancesIterRes().getThing())
            );
        }

        @Override
        public Stream<? extends Thing.Local> getInstances() {
            return getInstances(ThingImpl.Local::of);
        }

        @Override
        public final void setAbstract() {
            execute(TypeMethod.Req.newBuilder().setThingTypeSetAbstractReq(
                    SetAbstract.Req.getDefaultInstance()
            ).build());
        }

        @Override
        public final void unsetAbstract() {
            execute(TypeMethod.Req.newBuilder().setThingTypeUnsetAbstractReq(
                    UnsetAbstract.Req.getDefaultInstance()
            ).build());
        }

        @Override
        public final Stream<AttributeType.Local> getOwns(final ValueType valueType, final boolean keysOnly) {
            final GetOwns.Iter.Req.Builder req = GetOwns.Iter.Req.newBuilder().setKeysOnly(keysOnly);
            if (valueType != null) req.setValueType(valueType(valueType));
            return stream(
                    TypeMethod.Iter.Req.newBuilder().setThingTypeGetOwnsIterReq(req).build(),
                    res -> res.getThingTypeGetOwnsIterRes().getAttributeType()
            ).map(Type.Local::asAttributeType);
        }

        @Override
        public final Stream<RoleType.Local> getPlays() {
            return stream(
                    TypeMethod.Iter.Req.newBuilder().setThingTypeGetPlaysIterReq(
                            GetPlays.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getThingTypeGetPlaysIterRes().getRole()
            ).map(Type.Local::asRoleType);
        }

        @Override
        public final void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey) {
            final SetOwns.Req.Builder req = SetOwns.Req.newBuilder().setAttributeType(type(attributeType)).setIsKey(isKey);
            if (overriddenType != null) req.setOverriddenType(type(overriddenType));
            execute(TypeMethod.Req.newBuilder().setThingTypeSetOwnsReq(req).build());
        }

        @Override
        public final void setPlays(final RoleType role) {
            execute(TypeMethod.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role))
            ).build());
        }

        @Override
        public final void setPlays(final RoleType role, final RoleType overriddenRole) {
            execute(TypeMethod.Req.newBuilder().setThingTypeSetPlaysReq(
                    SetPlays.Req.newBuilder().setRole(type(role)).setOverriddenRole(type(overriddenRole))
            ).build());
        }

        @Override
        public final void unsetOwns(final AttributeType attributeType) {
            execute(TypeMethod.Req.newBuilder().setThingTypeUnsetOwnsReq(
                    UnsetOwns.Req.newBuilder().setAttributeType(type(attributeType))
            ).build());
        }

        @Override
        public final void unsetPlays(final RoleType role) {
            execute(TypeMethod.Req.newBuilder().setThingTypeUnsetPlaysReq(
                    UnsetPlays.Req.newBuilder().setRole(type(role))
            ).build());
        }

        @Override
        public ThingType.Remote asRemote(Grakn.Transaction transaction) {
            return new ThingTypeImpl.Remote(transaction, label, isRoot);
        }
    }
}
