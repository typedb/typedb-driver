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

package typedb.client.concept.thing;

import typedb.client.api.TypeDBTransaction;
import typedb.client.api.concept.thing.Attribute;
import typedb.client.api.concept.thing.Thing;
import typedb.client.api.concept.type.AttributeType;
import typedb.client.api.concept.type.RoleType;
import typedb.client.common.exception.TypeDBClientException;
import typedb.client.concept.ConceptImpl;
import typedb.client.concept.type.RoleTypeImpl;
import typedb.client.concept.type.ThingTypeImpl;
import typedb.protocol.ConceptProto;
import typedb.protocol.TransactionProto;

import java.util.Objects;
import java.util.stream.Stream;

import static typedb.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static typedb.client.common.exception.ErrorMessage.Concept.MISSING_IID;
import static typedb.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static typedb.client.common.rpc.RequestBuilder.Thing.deleteReq;
import static typedb.client.common.rpc.RequestBuilder.Thing.getHasReq;
import static typedb.client.common.rpc.RequestBuilder.Thing.getPlayingReq;
import static typedb.client.common.rpc.RequestBuilder.Thing.getRelationsReq;
import static typedb.client.common.rpc.RequestBuilder.Thing.protoThing;
import static typedb.client.common.rpc.RequestBuilder.Thing.setHasReq;
import static typedb.client.common.rpc.RequestBuilder.Thing.unsetHasReq;
import static typedb.client.concept.type.TypeImpl.protoTypes;
import static grakn.common.util.Objects.className;
import static java.util.Arrays.asList;

public abstract class ThingImpl extends ConceptImpl implements Thing {

    private final String iid;
    private final boolean isInferred;

    ThingImpl(String iid, boolean isInferred) {
        if (iid == null || iid.isEmpty()) throw new TypeDBClientException(MISSING_IID);
        this.iid = iid;
        this.isInferred = isInferred;
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
                throw new TypeDBClientException(BAD_ENCODING, thingProto.getType().getEncoding());
        }
    }

    @Override
    public final String getIID() {
        return iid;
    }

    @Override
    public abstract ThingTypeImpl getType();

    @Override
    public boolean isInferred() {
        return isInferred;
    }

    @Override
    public ThingImpl asThing() {
        return this;
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[" + getType().getLabel() + ":" + iid + "]";
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

        final TypeDBTransaction.Extended transactionRPC;
        private final String iid;
        private final boolean isInferred;
        private final int hash;

        Remote(TypeDBTransaction transaction, String iid, boolean isInferred) {
            if (transaction == null) throw new TypeDBClientException(MISSING_TRANSACTION);
            this.transactionRPC = (TypeDBTransaction.Extended) transaction;
            if (iid == null || iid.isEmpty()) throw new TypeDBClientException(MISSING_IID);
            this.iid = iid;
            this.isInferred = isInferred;
            this.hash = Objects.hash(this.transactionRPC, this.getIID());
        }

        @Override
        public final String getIID() {
            return iid;
        }

        @Override
        public abstract ThingTypeImpl getType();

        @Override
        public boolean isInferred() {
            return isInferred;
        }

        @Override
        public final Stream<AttributeImpl<?>> getHas(AttributeType... attributeTypes) {
            return stream(getHasReq(getIID(), protoTypes(asList(attributeTypes))))
                    .flatMap(rp -> rp.getThingGetHasResPart().getAttributesList().stream())
                    .map(AttributeImpl::of);
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
            return stream(getHasReq(getIID(), onlyKey))
                    .flatMap(rp -> rp.getThingGetHasResPart().getAttributesList().stream())
                    .map(AttributeImpl::of);
        }

        @Override
        public final Stream<RelationImpl> getRelations(RoleType... roleTypes) {
            return stream(getRelationsReq(getIID(), protoTypes(asList(roleTypes))))
                    .flatMap(rp -> rp.getThingGetRelationsResPart().getRelationsList().stream())
                    .map(RelationImpl::of);
        }

        @Override
        public final Stream<RoleTypeImpl> getPlaying() {
            return stream(getPlayingReq(getIID()))
                    .flatMap(rp -> rp.getThingGetPlayingResPart().getRoleTypesList().stream())
                    .map(RoleTypeImpl::of);
        }

        @Override
        public final void setHas(Attribute<?> attribute) {
            execute(setHasReq(getIID(), protoThing(attribute.getIID())));
        }

        @Override
        public final void unsetHas(Attribute<?> attribute) {
            execute(unsetHasReq(getIID(), protoThing(attribute.getIID())));
        }

        @Override
        public final void delete() {
            execute(deleteReq(getIID()));
        }

        @Override
        public final boolean isDeleted() {
            return transactionRPC.concepts().getThing(getIID()) == null;
        }

        @Override
        public final ThingImpl.Remote asThing() {
            return this;
        }

        protected ConceptProto.Thing.Res execute(TransactionProto.Transaction.Req.Builder request) {
            return transactionRPC.execute(request).getThingRes();
        }

        protected Stream<ConceptProto.Thing.ResPart> stream(TransactionProto.Transaction.Req.Builder request) {
            return transactionRPC.stream(request).map(TransactionProto.Transaction.ResPart::getThingResPart);
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
