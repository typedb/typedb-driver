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

import grakn.client.Grakn.Transaction;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL;
import static grakn.client.common.exception.ErrorMessage.ClientInternal.ILLEGAL_ARGUMENT_NULL_OR_EMPTY;
import static grakn.client.concept.ConceptMessageWriter.type;

public abstract class TypeImpl {

    /**
     * Client implementation of Type
     */
    public abstract static class Local implements Type.Local {

        private final String label;
        private final boolean isRoot;

        protected Local(ConceptProto.Type type) {
            this.label = type.getLabel();
            this.isRoot = type.getRoot();
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

    /**
     * Client implementation of Type
     */
    public abstract static class Remote implements Type.Remote {

        private final Transaction tx;
        private final String label;

        public Remote(final Transaction tx, final String label) {
            if (tx == null) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL.message("tx"));
            }
            this.tx = tx;
            if (label == null || label.isEmpty()) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL_OR_EMPTY.message("label"));
            }
            this.label = label;
        }

        @Override
        public boolean isRoot() {
            final Type.Local cached = tx().getCachedType(label);
            if (cached != null) {
                return cached.isRoot();
            }
            return tx().getType(label).isRoot();
        }

        @Override
        public final String getLabel() {
            return label;
        }

        @Override
        public void setLabel(String label) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder()
                            .setLabel(label)).build();
            // TODO: update the transaction's type cache
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
                    return typeConverter.apply(Type.Remote.of(tx(), response.getType()));
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public Stream<? extends Type.Remote> getSupertypes() {
            ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
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
            return tx().getType(getLabel()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{tx=" + tx + ", label=" + label + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypeImpl.Remote that = (TypeImpl.Remote) o;

            return this.tx.equals(that.tx) &&
                    this.label.equals(that.label);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= tx.hashCode();
            h *= 1000003;
            h ^= label.hashCode();
            return h;
        }

        protected Transaction tx() {
            return tx;
        }

        protected Stream<Thing.Remote> thingStream(ConceptProto.TypeMethod.Iter.Req request, Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return tx.iterateConceptMethod(label, request, response -> Thing.Remote.of(tx, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(ConceptProto.TypeMethod.Iter.Req request, Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return tx.iterateConceptMethod(label, request, response -> Type.Remote.of(tx, typeGetter.apply(response)));
        }

        protected ConceptProto.TypeMethod.Res runMethod(ConceptProto.TypeMethod.Req typeMethod) {
            return tx().runConceptMethod(label, typeMethod).getConceptMethodTypeRes().getResponse();
        }
    }
}
