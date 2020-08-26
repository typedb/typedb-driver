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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Thing;
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

        private final Concepts concepts;
        private final String label;
        private final boolean isRoot;

        public Remote(final Concepts concepts, final String label, final boolean isRoot) {
            if (concepts == null) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL.message("concept"));
            }
            this.concepts = concepts;
            if (label == null || label.isEmpty()) {
                throw new GraknClientException(ILLEGAL_ARGUMENT_NULL_OR_EMPTY.message("label"));
            }
            this.label = label;
            this.isRoot = isRoot;
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
                    return typeConverter.apply(Type.Remote.of(concepts, response.getType()));
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public Stream<? extends Type.Remote> getSupertypes() {
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
            return concepts.getType(getLabel()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{concepts=" + concepts + ", label=" + label + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final TypeImpl.Remote that = (TypeImpl.Remote) o;

            return this.concepts.equals(that.concepts) &&
                    this.label.equals(that.label);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= concepts.hashCode();
            h *= 1000003;
            h ^= label.hashCode();
            return h;
        }

        protected final Concepts concepts() {
            return concepts;
        }

        protected Stream<Thing.Remote> thingStream(final ConceptProto.TypeMethod.Iter.Req request, final Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Thing> thingGetter) {
            return concepts.iterateTypeMethod(label, request, response -> Thing.Remote.of(concepts, thingGetter.apply(response)));
        }

        protected Stream<Type.Remote> typeStream(final ConceptProto.TypeMethod.Iter.Req request, final Function<ConceptProto.TypeMethod.Iter.Res, ConceptProto.Type> typeGetter) {
            return concepts.iterateTypeMethod(label, request, response -> Type.Remote.of(concepts, typeGetter.apply(response)));
        }

        protected ConceptProto.TypeMethod.Res runMethod(final ConceptProto.TypeMethod.Req typeMethod) {
            return concepts.runTypeMethod(label, typeMethod).getConceptMethodTypeRes().getResponse();
        }
    }
}
