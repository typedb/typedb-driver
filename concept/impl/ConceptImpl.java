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

package grakn.client.concept.impl;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.protocol.ConceptProto;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class ConceptImpl {
    /**
     * Client implementation of Concept
     *
     * @param <SomeConcept> represents the actual class of object to downcast to
     */
    public abstract static class Local<SomeConcept extends Concept<SomeConcept>> implements Concept.Local<SomeConcept> {

        private final ConceptIID iid;

        protected Local(ConceptProto.Concept concept) {
            this.iid = ConceptIID.of(concept.getIid());
        }

        @Override
        public ConceptIID iid() {
            return iid;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{id=" + iid + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConceptImpl.Local<?> that = (ConceptImpl.Local<?>) o;

            return iid.equals(that.iid());
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= iid.hashCode();
            return h;
        }
    }

    /**
     * Client implementation of Concept
     *
     * @param <BaseType> represents the actual class of object to downcast to
     */
    public abstract static class Remote<BaseType extends Concept<BaseType>>
            implements Concept.Remote<BaseType> {

        private final Transaction tx;
        private final ConceptIID iid;

        protected Remote(Transaction tx, ConceptIID iid) {
            this.tx = requireNonNull(tx, "Null tx");
            this.iid = requireNonNull(iid, "Null iid");
        }

        @Override
        public ConceptIID iid() {
            return iid;
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
            return tx().getConcept(iid()) == null;
        }

        @Override
        public String toString() {
            return this.getClass().getCanonicalName() + "{tx=" + tx + ", iid=" + iid + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConceptImpl.Remote<?> that = (ConceptImpl.Remote<?>) o;

            return (tx.equals(that.tx())) &&
                    iid.equals(that.iid());
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= tx.hashCode();
            h *= 1000003;
            h ^= iid.hashCode();
            return h;
        }

        protected Transaction tx() {
            return tx;
        }

        protected abstract Remote<BaseType> asCurrentBaseType(Remote<?> other);

        @SuppressWarnings("unchecked")
        protected  <R extends Remote<BaseType>> Stream<R> conceptStream
                (ConceptProto.Method.Iter.Req request, Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
            return tx.iterateConceptMethod(iid, request, response -> Concept.Remote.of(tx, conceptGetter.apply(response)));
        }

        protected final ConceptProto.Method.Res runMethod(ConceptProto.Method.Req method) {
            return runMethod(iid(), method);
        }

        protected final ConceptProto.Method.Res runMethod(ConceptIID iid, ConceptProto.Method.Req method) {
            return tx().runConceptMethod(iid, method).getConceptMethodRes().getResponse();
        }

    }
}
