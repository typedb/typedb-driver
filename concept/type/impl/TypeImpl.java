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
import grakn.client.common.exception.GraknException;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.Type.SetSupertype;
import grakn.protocol.ConceptProto.TypeMethod;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.MISSING_ARGUMENT;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.common.util.Objects.className;

public abstract class TypeImpl {

    public abstract static class Local implements Type.Local {

        final String label;
        final String scope;
        final boolean isRoot;
        private final int hash;

        Local(final String label, final @Nullable String scope, final boolean isRoot) {
            this.label = label;
            this.scope = scope;
            this.isRoot = isRoot;
            this.hash = Objects.hash(this.scope, this.label);
        }

        public static TypeImpl.Local of(final ConceptProto.Type typeProto) {
            switch (typeProto.getSchema()) {
                case ROLE_TYPE:
                    return RoleTypeImpl.Local.of(typeProto);
                case RULE:
                    return RuleImpl.Local.of(typeProto);
                case UNRECOGNIZED:
                    throw new GraknException(UNRECOGNISED_FIELD.message(className(ConceptProto.Type.SCHEMA.class), typeProto.getSchema()));
                default:
                    return ThingTypeImpl.Local.of(typeProto);

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
        public String toString() {
            return className(this.getClass()) + "[label: " + (scope != null ? scope + ":" : "") + label + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TypeImpl.Local that = (TypeImpl.Local) o;
            return (this.label.equals(that.label) && Objects.equals(this.scope, that.scope));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public abstract static class Remote implements Type.Remote {

        private final Grakn.Transaction transaction;
        final String label;
        final String scope;
        final boolean isRoot;
        private final int hash;

        Remote(final Grakn.Transaction transaction, final String label, @Nullable String scope, final boolean isRoot) {
            if (transaction == null) throw new GraknException(MISSING_ARGUMENT.message("concept"));
            else if (label == null || label.isEmpty()) throw new GraknException(MISSING_ARGUMENT.message("label"));
            this.transaction = transaction;
            this.label = label;
            this.scope = scope;
            this.isRoot = isRoot;
            this.hash = Objects.hash(this.transaction, this.label, this.scope);
        }

        public static TypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type type) {
            switch (type.getSchema()) {
                case ENTITY_TYPE:
                    return EntityTypeImpl.Remote.of(transaction, type);
                case RELATION_TYPE:
                    return RelationTypeImpl.Remote.of(transaction, type);
                case ATTRIBUTE_TYPE:
                    return AttributeTypeImpl.Remote.of(transaction, type);
                case ROLE_TYPE:
                    return RoleTypeImpl.Remote.of(transaction, type);
                case THING_TYPE:
                    return ThingTypeImpl.Remote.of(transaction, type);
                case RULE:
                    return RuleImpl.Remote.of(transaction, type);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(className(ConceptProto.Type.SCHEMA.class), type.getSchema()));
            }
        }

        @Override
        public final boolean isRoot() {
            return isRoot;
        }

        @Override
        public final String getLabel() {
            return label;
        }

        @Override
        public final void setLabel(String label) {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder()
                                                .setLabel(label)).build();
            execute(method);
        }

        @Override
        public final boolean isAbstract() {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder()
                    .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()).build();

            return execute(method).getTypeIsAbstractRes().getAbstract();
        }

        void setSupertypeExecute(Type type) {
            execute(TypeMethod.Req.newBuilder().setTypeSetSupertypeReq(SetSupertype.Req.newBuilder().setType(type(type))).build());
        }

        @Nullable
        <TYPE extends Type.Local> TYPE getSupertypeExecute(final Function<Type.Local, TYPE> typeConstructor) {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder()
                    .setTypeGetSupertypeReq(ConceptProto.Type.GetSupertype.Req.getDefaultInstance()).build();

            final ConceptProto.Type.GetSupertype.Res response = execute(method).getTypeGetSupertypeRes();

            switch (response.getResCase()) {
                case TYPE:
                    return typeConstructor.apply(TypeImpl.Local.of(response.getType()));
                case RES_NOT_SET:
                default:
                    return null;
            }
        }

        <TYPE extends Type.Local> Stream<TYPE> getSupertypes(final Function<Type.Local, TYPE> typeConstructor) {
            final TypeMethod.Iter.Req method = TypeMethod.Iter.Req.newBuilder()
                    .setTypeGetSupertypesIterReq(ConceptProto.Type.GetSupertypes.Iter.Req.getDefaultInstance()).build();
            return stream(method, res -> res.getTypeGetSupertypesIterRes().getType()).map(typeConstructor);
        }

        <TYPE extends Type.Local> Stream<TYPE> getSubtypes(final Function<Type.Local, TYPE> typeConstructor) {
            final TypeMethod.Iter.Req method = TypeMethod.Iter.Req.newBuilder()
                    .setTypeGetSubtypesIterReq(ConceptProto.Type.GetSubtypes.Iter.Req.getDefaultInstance()).build();
            return stream(method, res -> res.getTypeGetSubtypesIterRes().getType()).map(typeConstructor);
        }

        @Override
        public final void delete() {
            final TypeMethod.Req method = TypeMethod.Req.newBuilder()
                    .setTypeDeleteReq(ConceptProto.Type.Delete.Req.getDefaultInstance()).build();
            execute(method);
        }

        @Override
        public final boolean isDeleted() {
            return transaction.concepts().getType(getLabel()) == null;
        }

        protected final Grakn.Transaction tx() {
            return transaction;
        }

        protected Stream<Type.Local> stream(final TypeMethod.Iter.Req request,
                                            final Function<TypeMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return transaction.concepts().iterateTypeMethod(
                    label, scope, request, response -> TypeImpl.Local.of(typeGetter.apply(response))
            );
        }

        protected TypeMethod.Res execute(final TypeMethod.Req typeMethod) {
            return transaction.concepts().runTypeMethod(label, scope, typeMethod)
                    .getConceptMethodTypeRes().getResponse();
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[label: " + (scope != null ? scope + ":" : "") + label + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TypeImpl.Remote that = (TypeImpl.Remote) o;
            return (this.transaction.equals(that.transaction) &&
                    this.label.equals(that.label) &&
                    Objects.equals(this.scope, that.scope));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
