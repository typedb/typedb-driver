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

package grakn.client.concept.thing.impl;

import grakn.client.Grakn;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.Thing.Delete;
import grakn.protocol.ConceptProto.Thing.GetPlays;
import grakn.protocol.ConceptProto.Thing.GetRelations;
import grakn.protocol.ConceptProto.Thing.UnsetHas;
import grakn.protocol.ConceptProto.ThingMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_IID;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;
import static grakn.common.util.Objects.className;

public abstract class ThingImpl {

    public abstract static class Local implements Thing.Local {

        private final String iid;
        // TODO: private final ThingType.Local type;
        // We need to store the concept Type, but we need a better way of retrieving it (currently requires a 2nd roundtrip)
        // In 1.8 it was in a "pre-filled response" in ConceptProto.Concept, which was confusing as it was
        // not actually prefilled when using the Concept API - only when using the Query API.

        Local(final String iid) {
            this.iid = iid;
        }

        public static ThingImpl.Local of(final ConceptProto.Thing thingProto) {
            switch (thingProto.getEncoding()) {
                case ENTITY:
                    return EntityImpl.Local.of(thingProto);
                case RELATION:
                    return RelationImpl.Local.of(thingProto);
                case ATTRIBUTE:
                    return AttributeImpl.Local.of(thingProto);
                case UNRECOGNIZED:
                default:
                    throw new GraknClientException(BAD_ENCODING.message(thingProto.getEncoding()));
            }
        }

        @Override
        public String getIID() {
            return iid;
        }

        @Override
        public ThingImpl.Local asThing() {
            return this;
        }

        @Override
        public EntityImpl.Local asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Entity.class.getSimpleName()));
        }

        @Override
        public AttributeImpl.Local<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Attribute.class.getSimpleName()));
        }

        @Override
        public RelationImpl.Local asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Relation.class.getSimpleName()));
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[iid:" + iid + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ThingImpl.Local that = (ThingImpl.Local) o;
            return (this.iid.equals(that.iid));
        }

        @Override
        public final int hashCode() {
            return iid.hashCode();
        }
    }

    public abstract static class Remote implements Thing.Remote {

        private final Grakn.Transaction transaction;
        private final String iid;
        private final int hash;

        protected Remote(final Grakn.Transaction transaction, final String iid) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            else if (iid == null || iid.isEmpty()) throw new GraknClientException(MISSING_IID);
            this.transaction = transaction;
            this.iid = iid;
            this.hash = Objects.hash(this.transaction, this.iid);
        }

        public static ThingImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing protoThing) {

            switch (protoThing.getEncoding()) {
                case ENTITY:
                    return EntityImpl.Remote.of(transaction, protoThing);
                case RELATION:
                    return RelationImpl.Remote.of(transaction, protoThing);
                case ATTRIBUTE:
                    return AttributeImpl.Remote.of(transaction, protoThing);
                default:
                case UNRECOGNIZED:
                    throw new GraknClientException(BAD_ENCODING.message(protoThing.getEncoding()));
            }
        }

        @Override
        public String getIID() {
            return iid;
        }

        public ThingTypeImpl.Local getType() {
            final ThingMethod.Req method = ThingMethod.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance()).build();
            return TypeImpl.Local.of(execute(method).getThingGetTypeRes().getThingType()).asThingType();
        }

        @Override
        public final boolean isInferred() {
            final ThingMethod.Req method = ThingMethod.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();
            return execute(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<AttributeImpl.Local<?>> getHas(AttributeType... attributeTypes) {
            final ThingMethod.Iter.Req method = ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                                                   .addAllAttributeTypes(types(Arrays.asList(attributeTypes)))).build();
            return stream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(ThingImpl.Local::asAttribute);
        }

        @Override
        public final Stream<AttributeImpl.Boolean.Local> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl.Local::asBoolean);
        }

        @Override
        public final Stream<AttributeImpl.Long.Local> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl.Local::asLong);
        }

        @Override
        public final Stream<AttributeImpl.Double.Local> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl.Local::asDouble);
        }

        @Override
        public final Stream<AttributeImpl.String.Local> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl.Local::asString);
        }

        @Override
        public final Stream<AttributeImpl.DateTime.Local> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl.Local::asDateTime);
        }

        @Override
        public final Stream<AttributeImpl.Local<?>> getHas(boolean onlyKey) {
            final ThingMethod.Iter.Req method = ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(onlyKey)).build();
            return stream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(ThingImpl.Local::asAttribute);
        }

        @Override
        public final Stream<RoleTypeImpl.Local> getPlays() {
            return typeStream(
                    ThingMethod.Iter.Req.newBuilder().setThingGetPlaysIterReq(
                            GetPlays.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getThingGetPlaysIterRes().getRoleType()
            ).map(TypeImpl.Local::asRoleType);
        }

        @Override
        public final Stream<RelationImpl.Local> getRelations(RoleType... roleTypes) {
            return stream(
                    ThingMethod.Iter.Req.newBuilder().setThingGetRelationsIterReq(
                            GetRelations.Iter.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))).build(),
                    res -> res.getThingGetRelationsIterRes().getRelation()
            ).map(ThingImpl.Local::asRelation);
        }

        @Override
        public final void setHas(Attribute<?> attribute) {
            execute(ThingMethod.Req.newBuilder().setThingSetHasReq(
                    ConceptProto.Thing.SetHas.Req.newBuilder().setAttribute(thing(attribute))
            ).build());
        }

        @Override
        public final void unsetHas(Attribute<?> attribute) {
            execute(ThingMethod.Req.newBuilder().setThingUnsetHasReq(
                    UnsetHas.Req.newBuilder().setAttribute(thing(attribute))
            ).build());
        }

        @Override
        public final void delete() {
            execute(ThingMethod.Req.newBuilder().setThingDeleteReq(Delete.Req.getDefaultInstance()).build());
        }

        @Override
        public final boolean isDeleted() {
            return transaction.concepts().getThing(getIID()) == null;
        }

        @Override
        public ThingImpl.Remote asThing() {
            return this;
        }

        @Override
        public EntityImpl.Remote asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Entity.class.getSimpleName()));
        }

        @Override
        public RelationImpl.Remote asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Relation.class.getSimpleName()));
        }

        @Override
        public AttributeImpl.Remote<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, Attribute.class.getSimpleName()));
        }

        final Grakn.Transaction tx() {
            return transaction;
        }

        Stream<ThingImpl.Local> stream(final ThingMethod.Iter.Req request, final Function<ThingMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return transaction.concepts().iterateThingMethod(iid, request, response -> ThingImpl.Local.of(thingGetter.apply(response)));
        }

        Stream<TypeImpl.Local> typeStream(final ThingMethod.Iter.Req request, final Function<ThingMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return transaction.concepts().iterateThingMethod(iid, request, response -> TypeImpl.Local.of(typeGetter.apply(response)));
        }

        ThingMethod.Res execute(final ThingMethod.Req method) {
            return transaction.concepts().runThingMethod(iid, method).getConceptMethodThingRes().getResponse();
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[iid:" + iid + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ThingImpl.Remote that = (ThingImpl.Remote) o;
            return (this.transaction.equals(that.transaction) && this.iid.equals(that.iid));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
