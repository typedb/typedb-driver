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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.protocol.session.ConceptProto;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Client implementation of Concept
 *
 * @param <SomeRemoteConcept> represents the actual class of object to downcast to
 */
public abstract class RemoteConceptImpl<SomeRemoteConcept extends Concept.Remote<SomeRemoteConcept>>
        implements Concept.Remote<SomeRemoteConcept> {

    private final GraknClient.Transaction tx;
    private final ConceptId id;

    protected RemoteConceptImpl(GraknClient.Transaction tx, ConceptId id) {
        this.tx = requireNonNull(tx, "Null tx");
        this.id = requireNonNull(id, "Null id");
    }

    @Override
    public ConceptId id() {
        return id;
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
        return tx().getConcept(id()) == null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{tx=" + tx + ", id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteConceptImpl<?> that = (RemoteConceptImpl<?>) o;

        return (tx.equals(that.tx())) &&
                id.equals(that.id());
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= tx.hashCode();
        h *= 1000003;
        h ^= id.hashCode();
        return h;
    }

    GraknClient.Transaction tx() {
        return tx;
    }

    abstract SomeRemoteConcept asCurrentBaseType(Remote<?> other);


    static <R extends Remote<R>>
    Stream<R> conceptStream(GraknClient.Transaction tx, ConceptId id, ConceptProto.Method.Iter.Req request,
                            Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
        return tx.iterateConceptMethod(id, request, response -> Remote.of(conceptGetter.apply(response), tx));
    }

    <R extends Remote<R>> Stream<R> conceptStream
            (ConceptProto.Method.Iter.Req request, Function<ConceptProto.Method.Iter.Res, ConceptProto.Concept> conceptGetter) {
        return conceptStream(tx(), id(), request, conceptGetter);
    }

    protected final ConceptProto.Method.Res runMethod(ConceptProto.Method.Req method) {
        return runMethod(id(), method);
    }

    protected final ConceptProto.Method.Res runMethod(ConceptId id, ConceptProto.Method.Req method) {
        return tx().runConceptMethod(id, method).getConceptMethodRes().getResponse();
    }

}
