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
import grakn.client.concept.type.Type;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.Thing.Delete;
import grakn.protocol.ConceptProto.Thing.GetPlays;
import grakn.protocol.ConceptProto.Thing.GetRelations;
import grakn.protocol.ConceptProto.Thing.UnsetHas;
import grakn.protocol.TransactionProto;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_IID;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;
import static grakn.common.util.Objects.className;

public abstract class ThingImpl implements Thing {

    private final String iid;
    // TODO: private final ThingType type;
    // We need to store the concept Type, but we need a better way of retrieving it (currently requires a 2nd roundtrip)
    // In 1.8 it was in a "pre-filled response" in ConceptProto.Concept, which was confusing as it was
    // not actually prefilled when using the Concept API - only when using the Query API.

    ThingImpl(final String iid) {
        if (iid == null || iid.isEmpty()) throw new GraknClientException(MISSING_IID);
        this.iid = iid;
    }

    public static ThingImpl of(final ConceptProto.Thing thingProto) {
        switch (thingProto.getEncoding()) {
            case ENTITY:
                return EntityImpl.of(thingProto);
            case RELATION:
                return RelationImpl.of(thingProto);
            case ATTRIBUTE:
                return AttributeImpl.of(thingProto);
            case UNRECOGNIZED:
            default:
                throw new GraknClientException(BAD_ENCODING.message(thingProto.getEncoding()));
        }
    }

    @Override
    public final String getIID() {
        return iid;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public ThingImpl asThing() {
        return this;
    }

    @Override
    public final TypeImpl asType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Type.class)));
    }

    @Override
    public EntityImpl asEntity() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Entity.class)));
    }

    @Override
    public AttributeImpl<?> asAttribute() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.class)));
    }

    @Override
    public RelationImpl asRelation() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Relation.class)));
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[iid:" + iid + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ThingImpl that = (ThingImpl) o;
        return (this.iid.equals(that.iid));
    }

    @Override
    public int hashCode() {
        return iid.hashCode();
    }

    public abstract static class Remote implements Thing.Remote {

        final RPCTransaction rpcTransaction;
        private final String iid;
        private final int hash;

        Remote(final Grakn.Transaction transaction, final String iid) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            this.rpcTransaction = (RPCTransaction) transaction;
            if (iid == null || iid.isEmpty()) throw new GraknClientException(MISSING_IID);
            this.iid = iid;
            this.hash = Objects.hash(this.rpcTransaction, this.getIID());
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
        public final String getIID() {
            return iid;
        }

        public ThingTypeImpl getType() {
            final ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingGetTypeReq(ConceptProto.Thing.GetType.Req.getDefaultInstance());
            return TypeImpl.of(execute(method).getThingGetTypeRes().getThingType()).asThingType();
        }

        @Override
        public final boolean isInferred() {
            final ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance());
            return execute(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<AttributeImpl<?>> getHas(AttributeType... attributeTypes) {
            final ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingGetHasReq(ConceptProto.Thing.GetHas.Req.newBuilder()
                            .addAllAttributeTypes(types(Arrays.asList(attributeTypes))));
            return stream(method, res -> res.getThingGetHasRes().getAttribute()).map(ThingImpl::asAttribute);
        }

        @Override
        public final Stream<AttributeImpl.Boolean> getHas(AttributeType.Boolean attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl::asBoolean);
        }

        @Override
        public final Stream<AttributeImpl.Long> getHas(AttributeType.Long attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl::asLong);
        }

        @Override
        public final Stream<AttributeImpl.Double> getHas(AttributeType.Double attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl::asDouble);
        }

        @Override
        public final Stream<AttributeImpl.String> getHas(AttributeType.String attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl::asString);
        }

        @Override
        public final Stream<AttributeImpl.DateTime> getHas(AttributeType.DateTime attributeType) {
            return getHas((AttributeType) attributeType).map(AttributeImpl::asDateTime);
        }

        @Override
        public final Stream<AttributeImpl<?>> getHas(boolean onlyKey) {
            final ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingGetHasReq(ConceptProto.Thing.GetHas.Req.newBuilder().setKeysOnly(onlyKey));
            return stream(method, res -> res.getThingGetHasRes().getAttribute()).map(ThingImpl::asAttribute);
        }

        @Override
        public final Stream<RoleTypeImpl> getPlays() {
            return typeStream(
                    ConceptProto.Thing.Req.newBuilder().setThingGetPlaysReq(
                            GetPlays.Req.getDefaultInstance()),
                    res -> res.getThingGetPlaysRes().getRoleType()
            ).map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RelationImpl> getRelations(final RoleType... roleTypes) {
            return stream(
                    ConceptProto.Thing.Req.newBuilder().setThingGetRelationsReq(
                            GetRelations.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))),
                    res -> res.getThingGetRelationsRes().getRelation()
            ).map(ThingImpl::asRelation);
        }

        @Override
        public final void setHas(Attribute<?> attribute) {
            execute(ConceptProto.Thing.Req.newBuilder().setThingSetHasReq(
                    ConceptProto.Thing.SetHas.Req.newBuilder().setAttribute(thing(attribute))
            ));
        }

        @Override
        public final void unsetHas(Attribute<?> attribute) {
            execute(ConceptProto.Thing.Req.newBuilder().setThingUnsetHasReq(
                    UnsetHas.Req.newBuilder().setAttribute(thing(attribute))
            ));
        }

        @Override
        public final void delete() {
            execute(ConceptProto.Thing.Req.newBuilder().setThingDeleteReq(Delete.Req.getDefaultInstance()));
        }

        @Override
        public final boolean isDeleted() {
            return rpcTransaction.concepts().getThing(getIID()) == null;
        }

        @Override
        public final boolean isRemote() {
            return true;
        }

        @Override
        public final ThingImpl.Remote asThing() {
            return this;
        }

        @Override
        public final TypeImpl.Remote asType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Type.class)));
        }

        @Override
        public EntityImpl.Remote asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Entity.class)));
        }

        @Override
        public RelationImpl.Remote asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Relation.class)));
        }

        @Override
        public AttributeImpl.Remote<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.class)));
        }

        final Grakn.Transaction tx() {
            return rpcTransaction;
        }

        Stream<ThingImpl> stream(final ConceptProto.Thing.Req.Builder method, final Function<ConceptProto.Thing.Res, ConceptProto.Thing> thingGetter) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return rpcTransaction.stream(request, res -> ThingImpl.of(thingGetter.apply(res.getThingRes())));
        }

        Stream<TypeImpl> typeStream(final ConceptProto.Thing.Req.Builder method, final Function<ConceptProto.Thing.Res, ConceptProto.Type> typeGetter) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return rpcTransaction.stream(request, res -> TypeImpl.of(typeGetter.apply(res.getThingRes())));
        }

        ConceptProto.Thing.Res execute(final ConceptProto.Thing.Req.Builder method) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return rpcTransaction.execute(request).getThingRes();
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
            return this.rpcTransaction.equals(that.rpcTransaction) && this.iid.equals(that.iid);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
