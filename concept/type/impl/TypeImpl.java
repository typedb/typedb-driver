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
import grakn.client.concept.Concept;
import grakn.client.concept.type.Type;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class TypeImpl {

    /**
     * Client implementation of Type
     */
    public abstract static class Local implements Type.Local {

        private final String label;

        protected Local(ConceptProto.Concept concept) {
            this.label = concept.getLabelRes().getLabel();
        }

        @Override
        public final String getLabel() {
            return label;
        }
    }

    /**
     * Client implementation of Type
     */
    public abstract static class Remote implements Type.Remote {

        private final Transaction tx;
        private final String label;

        public Remote(Transaction tx, String label) {
            this.tx = requireNonNull(tx, "Null tx");
            if (label == null || label.isEmpty()) {
                throw new IllegalArgumentException("Null or empty type label");
            }
            this.label = label;
        }

        @Override
        public final boolean isRoot() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeIsRootReq(ConceptProto.Type.IsRoot.Req.getDefaultInstance()).build();

            return runMethod(method).getTypeIsRootRes().getRoot();
        }

        @Override
        public final String getLabel() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeGetLabelReq(ConceptProto.Type.GetLabel.Req.getDefaultInstance()).build();
            return runMethod(method).getTypeGetLabelRes().getLabel();
        }

        @Override
        public void setLabel(String label) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeSetLabelReq(ConceptProto.Type.SetLabel.Req.newBuilder()
                            .setLabel(label)).build();
            runMethod(method);
        }

        @Nullable
        public Type.Remote getSupertype() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeGetSupertypeReq(ConceptProto.Type.GetSupertype.Req.getDefaultInstance()).build();

            ConceptProto.Type.GetSupertype.Res response = runMethod(method).getTypeGetSupertypeRes();

            switch (response.getResCase()) {
                case NULL:
                    return null;
                case TYPE:
                    return Concept.Remote.of(tx(), response.getType()).asType();
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }

        }

        @Override
        public Stream<? extends Type.Remote> getSupertypes() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeGetSupertypesIterReq(ConceptProto.Type.GetSupertypes.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeGetSupertypesIterRes().getType()).map(Concept.Remote::asType);
        }

        @Override
        public Stream<? extends Type.Remote> getSubtypes() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setTypeGetSubtypesIterReq(ConceptProto.Type.GetSubtypes.Iter.Req.getDefaultInstance()).build();

            return conceptStream(method, res -> res.getTypeGetSubtypesIterRes().getType()).map(Concept.Remote::asType);
        }

        protected final void setSupertype(Type type) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setTypeSetSupertypeReq(ConceptProto.Type.SetSupertype.Req.newBuilder()
                            .setType(RequestBuilder.ConceptMessage.from(type))).build();
            runMethod(method);
        }

        @Override
        public final void delete() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setConceptDeleteReq(ConceptProto.Concept.Delete.Req.getDefaultInstance())
                    .build();

            runMethod(method);
        }

        @Override
        public final boolean isDeleted() {
            return tx().getType(getLabel()) == null;
        }

        protected Transaction tx() {
            return tx;
        }

        protected Stream<Concept.Remote> conceptStream
                (ConceptProto.Method.Iter.Req request, Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
            return tx.iterateConceptMethod(label, request, response -> Concept.Remote.of(tx, conceptGetter.apply(response)));
        }

        protected ConceptProto.Method.Res runMethod(ConceptProto.Method.Req method) {
            return runMethod(label, method);
        }

        private ConceptProto.Method.Res runMethod(String label, ConceptProto.Method.Req method) {
            return tx().runConceptMethod(label, method).getConceptMethodRes().getResponse();
        }
    }
}
