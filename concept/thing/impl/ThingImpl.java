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

import grakn.client.GraknClient;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.impl.ConceptImpl;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.impl.RoleTypeImpl;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.client.concept.type.impl.TypeImpl;
import grakn.client.rpc.TransactionRPC;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_IID;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.client.concept.proto.ConceptProtoBuilder.iid;
import static grakn.client.concept.proto.ConceptProtoBuilder.thing;
import static grakn.client.concept.proto.ConceptProtoBuilder.types;
import static grakn.common.util.Objects.className;

public abstract class ThingImpl extends ConceptImpl implements Thing {

    private final String iid;

    ThingImpl(String iid) {
        if (iid == null || iid.isEmpty()) throw new GraknClientException(MISSING_IID);
        this.iid = iid;
    }

    public static ThingImpl of(ConceptProto.Thing thingProto) {
        switch (thingProto.getType().getEncoding()) {
            case ENTITY_TYPE:
                return EntityImpl.of(thingProto);
            case RELATION_TYPE:
                return RelationImpl.of(thingProto);
            case ATTRIBUTE_TYPE:
                return AttributeImpl.of(thingProto);
            case UNRECOGNIZED:
            default:
                throw new GraknClientException(BAD_ENCODING.message(thingProto.getType().getEncoding()));
        }
    }

    @Override
    public final String getIID() {
        return iid;
    }

    @Override
    public abstract ThingTypeImpl getType();

    @Override
    public ThingImpl asThing() {
        return this;
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[iid:" + iid + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingImpl that = (ThingImpl) o;
        return (this.iid.equals(that.iid));
    }

    @Override
    public int hashCode() {
        return iid.hashCode();
    }

    public abstract static class Remote extends ConceptImpl.Remote implements Thing.Remote {

        final TransactionRPC transactionRPC;
        private final String iid;
        private final int hash;

        Remote(GraknClient.Transaction transaction, String iid) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            this.transactionRPC = (TransactionRPC) transaction;
            if (iid == null || iid.isEmpty()) throw new GraknClientException(MISSING_IID);
            this.iid = iid;
            this.hash = Objects.hash(this.transactionRPC, this.getIID());
        }

        @Override
        public final String getIID() {
            return iid;
        }

        @Override
        public abstract ThingTypeImpl getType();

        @Override
        public final boolean isInferred() {
            ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingIsInferredReq(ConceptProto.Thing.IsInferred.Req.getDefaultInstance());
            return execute(method).getThingIsInferredRes().getInferred();
        }

        @Override
        public final Stream<AttributeImpl<?>> getHas(AttributeType... attributeTypes) {
            ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingGetHasReq(ConceptProto.Thing.GetHas.Req.newBuilder()
                                               .addAllAttributeTypes(types(Arrays.asList(attributeTypes))));
            return thingStream(method, res -> res.getThingGetHasRes().getAttributesList()).map(ThingImpl::asAttribute);
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
            ConceptProto.Thing.Req.Builder method = ConceptProto.Thing.Req.newBuilder()
                    .setThingGetHasReq(ConceptProto.Thing.GetHas.Req.newBuilder().setKeysOnly(onlyKey));
            return thingStream(method, res -> res.getThingGetHasRes().getAttributesList()).map(ThingImpl::asAttribute);
        }

        @Override
        public final Stream<RoleTypeImpl> getPlays() {
            return typeStream(
                    ConceptProto.Thing.Req.newBuilder().setThingGetPlaysReq(
                            ConceptProto.Thing.GetPlays.Req.getDefaultInstance()),
                    res -> res.getThingGetPlaysRes().getRoleTypesList()
            ).map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RelationImpl> getRelations(RoleType... roleTypes) {
            return thingStream(
                    ConceptProto.Thing.Req.newBuilder().setThingGetRelationsReq(
                            ConceptProto.Thing.GetRelations.Req.newBuilder().addAllRoleTypes(types(Arrays.asList(roleTypes)))),
                    res -> res.getThingGetRelationsRes().getRelationsList()
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
                    ConceptProto.Thing.UnsetHas.Req.newBuilder().setAttribute(thing(attribute))
            ));
        }

        @Override
        public final void delete() {
            execute(ConceptProto.Thing.Req.newBuilder().setThingDeleteReq(ConceptProto.Thing.Delete.Req.getDefaultInstance()));
        }

        @Override
        public final boolean isDeleted() {
            return transactionRPC.concepts().getThing(getIID()) == null;
        }

        @Override
        public final ThingImpl.Remote asThing() {
            return this;
        }

        final GraknClient.Transaction tx() {
            return transactionRPC;
        }

        Stream<ThingImpl> thingStream(ConceptProto.Thing.Req.Builder method, Function<ConceptProto.Thing.Res, List<ConceptProto.Thing>> thingListGetter) {
            TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return transactionRPC.stream(request, res -> thingListGetter.apply(res.getThingRes()).stream().map(ThingImpl::of));
        }

        Stream<TypeImpl> typeStream(ConceptProto.Thing.Req.Builder method, Function<ConceptProto.Thing.Res, List<ConceptProto.Type>> typeListGetter) {
            TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return transactionRPC.stream(request, res -> typeListGetter.apply(res.getThingRes()).stream().map(TypeImpl::of));
        }

        ConceptProto.Thing.Res execute(ConceptProto.Thing.Req.Builder method) {
            TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setThingReq(method.setIid(iid(iid)));
            return transactionRPC.execute(request).getThingRes();
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
            return this.transactionRPC.equals(that.transactionRPC) && this.iid.equals(that.iid);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
