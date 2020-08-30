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
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL;
import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL_OR_EMPTY;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;

public abstract class TypeImpl {

    public abstract static class Local implements Type.Local {

        private final String label;
        private final boolean isRoot;

        Local(ConceptProto.Type type) {
            this.label = type.getLabel();
            this.isRoot = type.getRoot();
        }

        public static TypeImpl.Local of(ConceptProto.Type type) {
            switch (type.getSchema()) {
                case ENTITY_TYPE:
                    return new EntityTypeImpl.Local(type);
                case RELATION_TYPE:
                    return new RelationTypeImpl.Local(type);
                case ATTRIBUTE_TYPE:
                    return AttributeTypeImpl.Local.of(type);
                case ROLE_TYPE:
                    return new RoleTypeImpl.Local(type);
                case RULE:
                    return new RuleImpl.Local(type);
                case THING_TYPE:
                    return new ThingTypeImpl.Local(type);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(ConceptProto.Type.SCHEMA.class.getCanonicalName(), type.getSchema()));
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
    }

    public abstract static class Remote implements Type.Remote {

        private final Grakn.Transaction transaction;
        private final String label;
        private final boolean isRoot;

        Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            if (transaction == null) {
                throw new GraknException(ILLEGAL_ARGUMENT_NULL.message("concept"));
            } else if (label == null || label.isEmpty()) {
                throw new GraknException(ILLEGAL_ARGUMENT_NULL_OR_EMPTY.message("label"));
            }
            this.transaction = transaction;
            this.label = label;
            this.isRoot = isRoot;
        }

        public static TypeImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Type type) {
            final String label = type.getLabel();
            switch (type.getSchema()) {
                case ENTITY_TYPE:
                    return new EntityTypeImpl.Remote(transaction, label, type.getRoot());
                case RELATION_TYPE:
                    return new RelationTypeImpl.Remote(transaction, label, type.getRoot());
                case ATTRIBUTE_TYPE:
                    return AttributeTypeImpl.Remote.of(transaction, type, label);
                case ROLE_TYPE:
                    final String scopedLabel = type.getScopedLabel();
                    return new RoleTypeImpl.Remote(transaction, label, scopedLabel, type.getRoot());
                case RULE:
                    return new RuleImpl.Remote(transaction, label, type.getRoot());
                case THING_TYPE:
                    return new ThingTypeImpl.Remote(transaction, label, type.getRoot());
                default:
                case UNRECOGNIZED:
                    throw new GraknException(UNRECOGNISED_FIELD.message(ConceptProto.Type.SCHEMA.class.getCanonicalName(), type.getSchema()));
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
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder()
                                                .setLabel(label)).build();
            runMethod(method);
        }

        @Override
        public final boolean isAbstract() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeIsAbstractReq(ConceptProto.Type.IsAbstract.Req.getDefaultInstance()).build();

            return runMethod(method).getTypeIsAbstractRes().getAbstract();
        }

        @Nullable
        protected <TYPE extends Type.Remote> TYPE getSupertypeInternal(final Function<Type.Remote, TYPE> typeConverter) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeGetSupertypeReq(ConceptProto.Type.GetSupertype.Req.getDefaultInstance()).build();

            final ConceptProto.Type.GetSupertype.Res response = runMethod(method).getTypeGetSupertypeRes();

            switch (response.getResCase()) {
                case TYPE:
                    return typeConverter.apply(of(transaction, response.getType()));
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public Stream<? extends Type.Remote> getSupertypes() {
            if (isRoot()) {
                return Stream.of(this);
            }

            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setTypeGetSupertypesIterReq(ConceptProto.Type.GetSupertypes.Iter.Req.getDefaultInstance()).build();

            return typeStream(method, res -> res.getTypeGetSupertypesIterRes().getType());
        }

        @Override
        public Stream<? extends Type.Remote> getSubtypes() {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setTypeGetSubtypesIterReq(ConceptProto.Type.GetSubtypes.Iter.Req.getDefaultInstance()).build();

            return typeStream(method, res -> res.getTypeGetSubtypesIterRes().getType());
        }

        protected void setSupertypeInternal(Type type) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeSetSupertypeReq(ConceptProto.Type.SetSupertype.Req.newBuilder()
                                                    .setType(type(type))).build();
            runMethod(method);
        }

        @Override
        public final void delete() {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeDeleteReq(ConceptProto.Type.Delete.Req.getDefaultInstance()).build();
            runMethod(method);
        }

        @Override
        public final boolean isDeleted() {
            return transaction.concepts().getType(getLabel()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{concepts=" + transaction + ", label=" + label + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TypeImpl.Remote that = (TypeImpl.Remote) o;

            return this.transaction.equals(that.transaction) &&
                    this.label.equals(that.label);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= transaction.hashCode();
            h *= 1000003;
            h ^= label.hashCode();
            return h;
        }

        protected final Grakn.Transaction tx() {
            return transaction;
        }

        protected Stream<Thing.Remote> thingStream(final ConceptProto.TypeMethod.Iter.Req request, final Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return transaction.concepts().iterateTypeMethod(label, request, response -> ThingImpl.Remote.of(transaction, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(final ConceptProto.TypeMethod.Iter.Req request, final Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return transaction.concepts().iterateTypeMethod(label, request, response -> of(transaction, typeGetter.apply(response)));
        }

        protected ConceptProto.TypeMethod.Res runMethod(final ConceptProto.TypeMethod.Req typeMethod) {
            return transaction.concepts().runTypeMethod(label, typeMethod).getConceptMethodTypeRes().getResponse();
        }
    }
}
