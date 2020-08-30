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
import grakn.common.collection.Bytes;
import grakn.protocol.ConceptProto;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.MISSING_ARGUMENT;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;
import static grakn.common.collection.Bytes.bytesToHexString;

public abstract class ThingImpl {

    public abstract static class Local implements Thing.Local {

        private final String iid;
        // TODO: private final ThingType.Local type;
        // We (probably) need to storae the concept Type, but we should have a better way of retrieving it.
        // In 1.8 it was in a "pre-filled response" in ConceptProto.Concept, which was highly confusing as it was
        // not actually prefilled when using the Concept API - only when using the Query API.
        // We should probably create a dedicated Proto class for Graql (AnswerProto or QueryProto) and keep the code clean.

        protected Local(final ConceptProto.Thing thing) {
            this.iid = Bytes.bytesToHexString(thing.getIid().toByteArray());
        }

        public static ThingImpl.Local of(final ConceptProto.Thing thing) {
            switch (thing.getSchema()) {
                case ENTITY:
                    return new EntityImpl.Local(thing);
                case RELATION:
                    return new RelationImpl.Local(thing);
                case ATTRIBUTE:
                    return AttributeImpl.Local.of(thing);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(
                            ConceptProto.Thing.SCHEMA.class.getSimpleName(), thing.getSchema())
                    );
            }
        }

        @Override
        public String getIID() {
            return iid;
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

        public static ThingImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thing) {
            final String iid = bytesToHexString(thing.getIid().toByteArray());
            switch (thing.getSchema()) {
                case ENTITY:
                    return new EntityImpl.Remote(transaction, iid);
                case RELATION:
                    return new RelationImpl.Remote(transaction, iid);
                case ATTRIBUTE:
                    return AttributeImpl.Remote.of(transaction, thing, iid);
                default:
                case UNRECOGNIZED:
                    throw new GraknException(UNRECOGNISED_FIELD.message(ConceptProto.Thing.SCHEMA.class.getCanonicalName(), thing.getSchema()));
            }
        }

        @Override
        public String getIID() {
            return iid;
        }

        public ThingType.Local getType() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance()).build();

            return TypeImpl.Local.of(runMethod(method).getThingGetTypeRes().getThingType()).asThingType();
        }

        @Override
        public final boolean isInferred() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance()).build();

            return runMethod(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<? extends Attribute.Remote<?>> getHas(AttributeType... attributeTypes) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder()
                                                   .addAllAttributeTypes(types(Arrays.asList(attributeTypes)))).build();
            return thingStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Remote::asAttribute);
        }

        @Override
        public final Stream<? extends Attribute.Boolean.Remote> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asBoolean);
        }

        @Override
        public final Stream<? extends Attribute.Long.Remote> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asLong);
        }

        @Override
        public final Stream<? extends Attribute.Double.Remote> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDouble);
        }

        @Override
        public final Stream<? extends Attribute.String.Remote> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asString);
        }

        @Override
        public final Stream<? extends Attribute.DateTime.Remote> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(Attribute.Remote::asDateTime);
        }

        @Override
        public final Stream<? extends Attribute.Remote<?>> getHas(boolean onlyKey) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetHasIterReq(ConceptProto.Thing.GetHas.Iter.Req.newBuilder().setKeysOnly(onlyKey)).build();
            return thingStream(method, res -> res.getThingGetHasIterRes().getAttribute()).map(Thing.Remote::asAttribute);
        }

        @Override
        public final Stream<RoleType.Remote> getPlays() {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetPlaysIterReq(ConceptProto.Thing.GetPlays.Iter.Req.getDefaultInstance()).build();
            return typeStream(method, res -> res.getThingGetPlaysIterRes().getRoleType()).map(Type.Remote::asRoleType);
        }

        @Override
        public final Stream<? extends Relation> getRelations(RoleType... roleTypes) {
            final ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setThingGetRelationsIterReq(ConceptProto.Thing.GetRelations.Iter.Req.newBuilder()
                                                         .addAllRoleTypes(types(Arrays.asList(roleTypes)))).build();
            return thingStream(method, res -> res.getThingGetRelationsIterRes().getRelation()).map(Thing.Remote::asRelation);
        }

        @Override
        public final void setHas(Attribute<?> attribute) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingSetHasReq(ConceptProto.Thing.SetHas.Req.newBuilder()
                                               .setAttribute(thing(attribute))).build();
            runMethod(method);
        }

        @Override
        public final void unsetHas(Attribute<?> attribute) {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingUnsetHasReq(ConceptProto.Thing.UnsetHas.Req.newBuilder()
                                                 .setAttribute(thing(attribute))).build();
            runMethod(method);
        }

        @Override
        public final void delete() {
            final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                    .setThingDeleteReq(ConceptProto.Thing.Delete.Req.getDefaultInstance())
                    .build();
            runMethod(method);
        }

        @Override
        public final boolean isDeleted() {
            return transaction.concepts().getThing(getIID()) == null;
        }

        protected final Grakn.Transaction tx() {
            return transaction;
        }

        protected Stream<Thing.Remote> thingStream(final ConceptProto.ThingMethod.Iter.Req request, final Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return transaction.concepts().iterateThingMethod(iid, request, response -> of(transaction, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(final ConceptProto.ThingMethod.Iter.Req request, final Function<ConceptProto.ThingMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return transaction.concepts().iterateThingMethod(iid, request, response -> TypeImpl.Remote.of(transaction, typeGetter.apply(response)));
        }

        protected ConceptProto.ThingMethod.Res runMethod(final ConceptProto.ThingMethod.Req method) {
            return transaction.concepts().runThingMethod(iid, method).getConceptMethodThingRes().getResponse();
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{concepts=" + transaction + ", iid=" + iid + "}";
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
