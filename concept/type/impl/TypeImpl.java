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
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.common.util.Objects.className;

public abstract class TypeImpl implements Type {

    private final String label;
    private final boolean isRoot;
    private final int hash;

    TypeImpl(String label, boolean isRoot) {
        if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
        this.label = label;
        this.isRoot = isRoot;
        this.hash = Objects.hash(this.label);
    }

    public static TypeImpl of(ConceptProto.Type typeProto) {
        switch (typeProto.getEncoding()) {
            case ROLE_TYPE:
                return RoleTypeImpl.of(typeProto);
            case UNRECOGNIZED:
                throw new GraknClientException(BAD_ENCODING.message(typeProto.getEncoding()));
            default:
                return ThingTypeImpl.of(typeProto);
        }
    }

    @Override
    public final String getLabel() {
        return label;
    }

    @Override
    public final boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public TypeImpl asType() {
        return this;
    }

    @Override
    public ThingImpl asThing() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Thing.class)));
    }

    @Override
    public ThingTypeImpl asThingType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(ThingType.class)));
    }

    @Override
    public EntityTypeImpl asEntityType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(EntityType.class)));
    }

    @Override
    public AttributeTypeImpl asAttributeType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(AttributeType.class)));
    }

    @Override
    public RelationTypeImpl asRelationType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(RelationType.class)));
    }

    @Override
    public RoleTypeImpl asRoleType() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(RoleType.class)));
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[label: " + label + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeImpl that = (TypeImpl) o;
        return this.label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public abstract static class Remote implements Type.Remote {

        final RPCTransaction rpcTransaction;
        private String label;
        private final boolean isRoot;
        private final int hash;

        Remote(Grakn.Transaction transaction, String label, boolean isRoot) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
            this.rpcTransaction = (RPCTransaction) transaction;
            this.label = label;
            this.isRoot = isRoot;
            this.hash = Objects.hash(transaction, label);
        }

        @Override
        public final String getLabel() {
            return label;
        }

        @Override
        public boolean isRoot() {
            return isRoot;
        }

        @Override
        public final boolean isRemote() {
            return true;
        }

        @Override
        public final void setLabel(String label) {
            execute(ConceptProto.Type.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder().setLabel(label)));
            this.label = label;
        }

        @Override
        public final boolean isAbstract() {
            return execute(ConceptProto.Type.Req.newBuilder()
                    .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()))
                    .getTypeIsAbstractRes().getAbstract();
        }

        @Override
        public TypeImpl.Remote asType() {
            return this;
        }

        @Override
        public ThingImpl.Remote asThing() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Thing.class)));
        }

        @Override
        public ThingTypeImpl.Remote asThingType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(ThingType.class)));
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(EntityType.class)));
        }

        @Override
        public RelationTypeImpl.Remote asRelationType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(RelationType.class)));
        }

        @Override
        public AttributeTypeImpl.Remote asAttributeType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(AttributeType.class)));
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(RoleType.class)));
        }

        // We can't declare this in Type.Remote because then (for example) EntityTypeImpl.Remote would be expected to
        // implement setSupertype(Type) but in fact, only implements setSupertype(EntityType).
        void setSupertype(Type type) {
            execute(ConceptProto.Type.Req.newBuilder().setTypeSetSupertypeReq(ConceptProto.Type.SetSupertype.Req.newBuilder().setType(type(type))));
        }

        @Nullable
        @Override
        public TypeImpl getSupertype() {
            final ConceptProto.Type.GetSupertype.Res response = execute(ConceptProto.Type.Req.newBuilder()
                    .setTypeGetSupertypeReq(ConceptProto.Type.GetSupertype.Req.getDefaultInstance()))
                    .getTypeGetSupertypeRes();

            return response.getResCase() == ConceptProto.Type.GetSupertype.Res.ResCase.TYPE ? TypeImpl.of(response.getType()) : null;
        }

        @Override
        public Stream<? extends TypeImpl> getSupertypes() {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder()
                    .setTypeGetSupertypesReq(ConceptProto.Type.GetSupertypes.Req.getDefaultInstance());
            return typeStream(method, res -> res.getTypeGetSupertypesRes().getTypeList());
        }

        @Override
        public Stream<? extends TypeImpl> getSubtypes() {
            final ConceptProto.Type.Req.Builder method = ConceptProto.Type.Req.newBuilder()
                    .setTypeGetSubtypesReq(ConceptProto.Type.GetSubtypes.Req.getDefaultInstance());
            return typeStream(method, res -> res.getTypeGetSubtypesRes().getTypeList());
        }

        @Override
        public final void delete() {
            execute(ConceptProto.Type.Req.newBuilder().setTypeDeleteReq(ConceptProto.Type.Delete.Req.getDefaultInstance()));
        }

        @Override
        public final boolean isDeleted() {
            return rpcTransaction.concepts().getType(label) == null;
        }

        final Grakn.Transaction tx() {
            return rpcTransaction;
        }

        Stream<TypeImpl> typeStream(ConceptProto.Type.Req.Builder method, Function<ConceptProto.Type.Res, List<ConceptProto.Type>> typeGetter) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setTypeReq(method.setLabel(label));
            return rpcTransaction.stream(request, res -> typeGetter.apply(res.getTypeRes()).stream().map(TypeImpl::of));
        }

        Stream<ThingImpl> thingStream(ConceptProto.Type.Req.Builder method, Function<ConceptProto.Type.Res, List<ConceptProto.Thing>> thingGetter) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setTypeReq(method.setLabel(label));
            return rpcTransaction.stream(request, res -> thingGetter.apply(res.getTypeRes()).stream().map(ThingImpl::of));
        }

        ConceptProto.Type.Res execute(ConceptProto.Type.Req.Builder method) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setTypeReq(method.setLabel(label));
            return rpcTransaction.execute(request).getTypeRes();
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[label: " + label + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TypeImpl.Remote that = (TypeImpl.Remote) o;
            return this.rpcTransaction.equals(that.rpcTransaction) &&
                    this.getLabel().equals(that.getLabel());
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
