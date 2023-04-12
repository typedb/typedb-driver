/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.api.concept.type.Type;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptImpl;
import com.vaticle.typedb.client.concept.thing.AttributeImpl;
import com.vaticle.typedb.client.concept.thing.EntityImpl;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.protocol.ConceptProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.UNRECOGNISED_ANNOTATION;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.deleteReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.getSubtypesExplicitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.getSubtypesReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.getSupertypeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.getSupertypesReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.setLabelReq;
import static com.vaticle.typedb.client.concept.type.RoleTypeImpl.protoRoleType;
import static com.vaticle.typedb.client.concept.type.ThingTypeImpl.protoThingType;
import static com.vaticle.typedb.common.util.Objects.className;
import static java.util.stream.Collectors.toList;

public abstract class TypeImpl extends ConceptImpl implements Type {

    private final Label label;
    private final boolean isRoot;
    private final boolean isAbstract;
    private final int hash;

    TypeImpl(Label label, boolean isRoot, boolean isAbstract) {
        if (label == null) throw new TypeDBClientException(MISSING_LABEL);
        this.label = label;
        this.isRoot = isRoot;
        this.isAbstract = isAbstract;
        this.hash = Objects.hash(this.label);
    }

    public static TypeImpl of(ConceptProto.Type typeProto) {
        switch (typeProto.getEncoding()) {
            case ROLE_TYPE:
                return RoleTypeImpl.of(typeProto);
            case UNRECOGNIZED:
                throw new TypeDBClientException(BAD_ENCODING, typeProto.getEncoding());
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

    public static Set<ConceptProto.Type.Annotation> protoAnnotations(Collection<TypeQLToken.Annotation> annotations) {
        return annotations.stream().map(annotation -> {
            switch (annotation) {
                case KEY:
                    return ConceptProto.Type.Annotation.newBuilder().setKey(true);
                case UNIQUE:
                    return ConceptProto.Type.Annotation.newBuilder().setUnique(true);
                default:
                    throw new TypeDBClientException(UNRECOGNISED_ANNOTATION, annotation);
            }
        }).map(ConceptProto.Type.Annotation.Builder::build).collect(Collectors.toSet());
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
    public final boolean isAbstract() {
        return isAbstract;
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

        final TypeDBTransaction.Extended transactionExt;
        private Label label;
        private final boolean isRoot;
        private final boolean isAbstract;
        private int hash;

        Remote(TypeDBTransaction transaction, Label label, boolean isRoot, boolean isAbstract) {
            if (transaction == null) throw new TypeDBClientException(MISSING_TRANSACTION);
            if (label == null) throw new TypeDBClientException(MISSING_LABEL);
            this.transactionExt = (TypeDBTransaction.Extended) transaction;
            this.label = label;
            this.isRoot = isRoot;
            this.isAbstract = isAbstract;
            this.hash = Objects.hash(this.transactionExt, label);
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
        public final boolean isAbstract() {
            return isAbstract;
        }

        @Override
        public final void setLabel(String newLabel) {
            execute(setLabelReq(getLabel(), newLabel));
            this.label = Label.of(label.scope().orElse(null), newLabel);
            this.hash = Objects.hash(transactionExt, this.label);
        }

        @Override
        public TypeImpl.Remote asType() {
            return this;
        }

        @Override
        public ThingTypeImpl.Remote asThingType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
        }

        @Override
        public RelationTypeImpl.Remote asRelationType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
        }

        @Override
        public AttributeTypeImpl.Remote asAttributeType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
        }

        @Override
        public ThingImpl.Remote asThing() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
        }

        @Override
        public EntityImpl.Remote asEntity() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
        }

        @Override
        public AttributeImpl.Remote<?> asAttribute() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
        }

        @Override
        public RelationImpl.Remote asRelation() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
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
        public Stream<? extends TypeImpl> getSubtypesExplicit() {
            return stream(getSubtypesExplicitReq(getLabel()))
                    .flatMap(rp -> rp.getTypeGetSubtypesExplicitResPart().getTypesList().stream())
                    .map(TypeImpl::of);
        }

        @Override
        public final void delete() {
            execute(deleteReq(getLabel()));
        }

        final TypeDBTransaction tx() {
            return transactionExt;
        }

        protected ConceptProto.Type.Res execute(TransactionProto.Transaction.Req.Builder request) {
            return transactionExt.execute(request).getTypeRes();
        }

        protected Stream<ConceptProto.Type.ResPart> stream(TransactionProto.Transaction.Req.Builder request) {
            return transactionExt.stream(request).map(TransactionProto.Transaction.ResPart::getTypeResPart);
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
            return this.transactionExt.equals(that.transactionExt) &&
                    this.getLabel().equals(that.getLabel());
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
