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
import grakn.client.common.exception.GraknException;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
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

import static grakn.client.common.exception.ErrorMessage.ClientInternal.MISSING_ARGUMENT;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
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
            switch (thingProto.getSchema()) {
                case ENTITY:
                    return EntityImpl.Local.of(thingProto);
                case RELATION:
                    return RelationImpl.Local.of(thingProto);
                case ATTRIBUTE:
                    return AttributeImpl.Local.of(thingProto);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(
                            className(ConceptProto.Thing.SCHEMA.class), thingProto.getSchema())
                    );
            }
        }

        @Override
        public String getIID() {
            return iid;
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
            if (transaction == null) throw new GraknException(MISSING_ARGUMENT.message("concepts"));
            else if (iid == null || iid.isEmpty()) throw new GraknException(MISSING_ARGUMENT.message("iid"));
            this.transaction = transaction;
            this.iid = iid;
            this.hash = Objects.hash(this.transaction, this.iid);
        }

        public static ThingImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing protoThing) {

            switch (protoThing.getSchema()) {
                case ENTITY:
                    return EntityImpl.Remote.of(transaction, protoThing);
                case RELATION:
                    return RelationImpl.Remote.of(transaction, protoThing);
                case ATTRIBUTE:
                    return AttributeImpl.Remote.of(transaction, protoThing);
                default:
                case UNRECOGNIZED:
                    throw new GraknException(UNRECOGNISED_FIELD.message(className(ConceptProto.Thing.SCHEMA.class), protoThing.getSchema()));
            }
        }

        @Override
        public String getIID() {
            return iid;
        }

        public ThingType.Local getType() {
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
        public final Stream<? extends Attribute.Local<?>> getHas(AttributeType... attributeTypes) {
            final ThingMethod.Iter.Req method = ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                                                   .addAllAttributeTypes(types(Arrays.asList(attributeTypes)))).build();
            return stream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Local::asAttribute);
        }

        @Override
        public final Stream<? extends Attribute.Boolean.Local> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Local::asBoolean);
        }

        @Override
        public final Stream<? extends Attribute.Long.Local> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Local::asLong);
        }

        @Override
        public final Stream<? extends Attribute.Double.Local> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Local::asDouble);
        }

        @Override
        public final Stream<? extends Attribute.String.Local> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Local::asString);
        }

        @Override
        public final Stream<? extends Attribute.DateTime.Local> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Local::asDateTime);
        }

        @Override
        public final Stream<? extends Attribute.Local<?>> getHas(boolean onlyKey) {
            final ThingMethod.Iter.Req method = ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(onlyKey)).build();
            return stream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Local::asAttribute);
        }

        @Override
        public final Stream<RoleType.Local> getPlays() {
            return typeStream(
                    ThingMethod.Iter.Req.newBuilder().setThingGetPlaysIterReq(
                            GetPlays.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getThingGetPlaysIterRes().getRoleType()
            ).map(Type.Local::asRoleType);
        }

        @Override
        public final Stream<? extends Relation.Local> getRelations(RoleType... roleTypes) {
            return stream(
                    ThingMethod.Iter.Req.newBuilder().setThingGetRelationsIterReq(
                            GetRelations.Iter.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))).build(),
                    res -> res.getThingGetRelationsIterRes().getRelation()
            ).map(Thing.Local::asRelation);
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

        final Grakn.Transaction tx() {
            return transaction;
        }

        Stream<Thing.Local> stream(final ThingMethod.Iter.Req request,
                                   final Function<ThingMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return transaction.concepts().iterateThingMethod(iid, request, response -> ThingImpl.Local.of(thingGetter.apply(response)));
        }

        Stream<Type.Local> typeStream(final ThingMethod.Iter.Req request,
                                      final Function<ThingMethod.Iter.Res, ConceptProto.Type> typeGetter) {
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

            ThingImpl.Remote that = (ThingImpl.Remote) o;
            return (this.transaction.equals(that.transaction) && this.iid.equals(that.iid));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
