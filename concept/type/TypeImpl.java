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

package grakn.client.concept.type;

import grakn.client.api.GraknTransaction;
import grakn.client.api.concept.thing.Attribute;
import grakn.client.api.concept.thing.Entity;
import grakn.client.api.concept.thing.Relation;
import grakn.client.api.concept.thing.Thing;
import grakn.client.api.concept.type.AttributeType;
import grakn.client.api.concept.type.EntityType;
import grakn.client.api.concept.type.RelationType;
import grakn.client.api.concept.type.RoleType;
import grakn.client.api.concept.type.ThingType;
import grakn.client.api.concept.type.Type;
import grakn.client.common.exception.GraknClientException;
import grakn.client.common.Label;
import grakn.client.concept.ConceptImpl;
import grakn.client.concept.thing.AttributeImpl;
import grakn.client.concept.thing.EntityImpl;
import grakn.client.concept.thing.RelationImpl;
import grakn.client.concept.thing.ThingImpl;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.client.common.rpc.RequestBuilder.Type.deleteReq;
import static grakn.client.common.rpc.RequestBuilder.Type.getSubtypesReq;
import static grakn.client.common.rpc.RequestBuilder.Type.getSupertypeReq;
import static grakn.client.common.rpc.RequestBuilder.Type.getSupertypesReq;
import static grakn.client.common.rpc.RequestBuilder.Type.isAbstractReq;
import static grakn.client.common.rpc.RequestBuilder.Type.setLabelReq;
import static grakn.client.concept.type.RoleTypeImpl.protoRoleType;
import static grakn.client.concept.type.ThingTypeImpl.protoThingType;
import static grakn.common.util.Objects.className;
import static java.util.stream.Collectors.toList;

public abstract class TypeImpl extends ConceptImpl implements Type {

    private final Label label;
    private final boolean isRoot;
    private final int hash;

    TypeImpl(Label label, boolean isRoot) {
        if (label == null) throw new GraknClientException(MISSING_LABEL);
        this.label = label;
        this.isRoot = isRoot;
        this.hash = Objects.hash(this.label);
    }

    public static TypeImpl of(ConceptProto.Type typeProto) {
        switch (typeProto.getEncoding()) {
            case ROLE_TYPE:
                return RoleTypeImpl.of(typeProto);
            case UNRECOGNIZED:
                throw new GraknClientException(BAD_ENCODING, typeProto.getEncoding());
            default:
                return ThingTypeImpl.of(typeProto);
        }
    }

    public static ConceptProto.Type.Encoding encoding(Type type) {
        if (type.isEntityType()) {
            return ConceptProto.Type.Encoding.ENTITY_TYPE;
        } else if (type.isRelationType()) {
            return ConceptProto.Type.Encoding.RELATION_TYPE;
        } else if (type.isAttributeType()) {
            return ConceptProto.Type.Encoding.ATTRIBUTE_TYPE;
        } else if (type.isRoleType()) {
            return ConceptProto.Type.Encoding.ROLE_TYPE;
        } else if (type.isThingType()) {
            return ConceptProto.Type.Encoding.THING_TYPE;
        } else {
            return ConceptProto.Type.Encoding.UNRECOGNIZED;
        }
    }

    public static List<ConceptProto.Type> protoTypes(Collection<? extends Type> types) {
        return types.stream().map(type -> {
            if (type.isThingType()) return protoThingType(type.asThingType());
            else return protoRoleType(type.asRoleType());
        }).collect(toList());
    }

    @Override
    public final Label getLabel() {
        return label;
    }

    @Override
    public final boolean isRoot() {
        return isRoot;
    }

    @Override
    public TypeImpl asType() {
        return this;
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[label: " + label + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TypeImpl that = (TypeImpl) o;
        return this.label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public abstract static class Remote extends ConceptImpl.Remote implements Type.Remote {

        final GraknTransaction.Extended transactionRPC;
        private Label label;
        private final boolean isRoot;
        private int hash;

        Remote(GraknTransaction transaction, Label label, boolean isRoot) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            if (label == null) throw new GraknClientException(MISSING_LABEL);
            this.transactionRPC = (GraknTransaction.Extended) transaction;
            this.label = label;
            this.isRoot = isRoot;
            this.hash = Objects.hash(this.transactionRPC, label);
        }

        @Override
        public final Label getLabel() {
            return label;
        }

        @Override
        public boolean isRoot() {
            return isRoot;
        }

        @Override
        public final void setLabel(String newLabel) {
            execute(setLabelReq(getLabel(), newLabel));
            this.label = Label.of(label.scope().orElse(null), newLabel);
            this.hash = Objects.hash(transactionRPC, this.label);
        }

        @Override
        public final boolean isAbstract() {
            return execute(isAbstractReq(getLabel())).getTypeIsAbstractRes().getAbstract();
        }

        @Override
        public TypeImpl.Remote asType() {
            return this;
        }

        @Override
        public ThingTypeImpl.Remote asThingType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
        }

        @Override
        public RelationTypeImpl.Remote asRelationType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
        }

        @Override
        public AttributeTypeImpl.Remote asAttributeType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
        }

        @Override
        public ThingImpl.Remote asThing() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
        }

        @Override
        public EntityImpl.Remote asEntity() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
        }

        @Override
        public AttributeImpl.Remote<?> asAttribute() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
        }

        @Override
        public RelationImpl.Remote asRelation() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
        }

        @Nullable
        @Override
        public TypeImpl getSupertype() {
            ConceptProto.Type.GetSupertype.Res res = execute(getSupertypeReq(getLabel())).getTypeGetSupertypeRes();
            switch (res.getResCase()) {
                case TYPE:
                    return TypeImpl.of(res.getType());
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public Stream<? extends TypeImpl> getSupertypes() {
            return stream(getSupertypesReq(getLabel()))
                    .flatMap(rp -> rp.getTypeGetSupertypesResPart().getTypesList().stream())
                    .map(TypeImpl::of);
        }

        @Override
        public Stream<? extends TypeImpl> getSubtypes() {
            return stream(getSubtypesReq(getLabel()))
                    .flatMap(rp -> rp.getTypeGetSubtypesResPart().getTypesList().stream())
                    .map(TypeImpl::of);
        }

        @Override
        public final void delete() {
            execute(deleteReq(getLabel()));
        }

        final GraknTransaction tx() {
            return transactionRPC;
        }

        protected ConceptProto.Type.Res execute(TransactionProto.Transaction.Req.Builder request) {
            return transactionRPC.execute(request).getTypeRes();
        }

        protected Stream<ConceptProto.Type.ResPart> stream(TransactionProto.Transaction.Req.Builder request) {
            return transactionRPC.stream(request).map(TransactionProto.Transaction.ResPart::getTypeResPart);
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[label: " + label.scopedName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeImpl.Remote that = (TypeImpl.Remote) o;
            return this.transactionRPC.equals(that.transactionRPC) &&
                    this.getLabel().equals(that.getLabel());
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
