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

package grakn.client.connection;

import com.google.common.collect.AbstractIterator;
import com.google.protobuf.ByteString;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Explanation;
import grakn.client.answer.Numeric;
import grakn.client.answer.Void;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.rpc.ConceptMessage;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.client.rpc.ResponseReader;
import grakn.client.rpc.Transceiver;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;
import grakn.protocol.GraknGrpc;
import grakn.protocol.TransactionProto;
import graql.lang.common.GraqlToken;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import io.grpc.ManagedChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public class GraknTransaction implements Transaction {

    private final Session session;
    private final Type type;
    private final HashMap<String, grakn.client.concept.type.Type.Local> typeCache;
    private final Transceiver transceiver;

    public static class Builder implements Transaction.Builder {

        private ManagedChannel channel;
        private Session session;
        private ByteString sessionId;

        public Builder(ManagedChannel channel, Session session, ByteString sessionId) {
            this.channel = channel;
            this.session = session;
            this.sessionId = sessionId;
        }

        @Override
        public Transaction read() {
            return new GraknTransaction(channel, session, sessionId, Type.READ);
        }

        @Override
        public Transaction write() {
            return new GraknTransaction(channel, session, sessionId, Type.WRITE);
        }
    }

    GraknTransaction(ManagedChannel channel, Session session, ByteString sessionId, Type type) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(type == Type.WRITE ? "tx.write" : "tx.read")) {
            this.transceiver = Transceiver.create(GraknGrpc.newStub(channel));
            this.session = session;
            this.type = type;
            this.typeCache = new HashMap<>();
            sendAndReceiveOrThrow(RequestBuilder.Transaction.open(sessionId, type));
        }
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public Database database() {
        return session.database();
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlDefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.define")) {
            return executeInternal(query, Options.DEFAULT);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlUndefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.undefine")) {
            return executeInternal(query, Options.DEFAULT);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlInsert query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.insert")) {
            return executeInternal(query, options);
        }
    }
    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlInsert query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.delete")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlGet query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlGet query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.define")) {
            return streamInternal(query, Options.DEFAULT);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.undefine")) {
            return streamInternal(query, Options.DEFAULT);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.insert")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.delete")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.get")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query) {
        return stream(query, Options.DEFAULT);
    }

    // Aggregate Query

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.get.aggregate")) {
            return streamInternal(query, options);
        }
    }

    // Group Query

    @Override
    public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.group")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.get.group")) {
            return streamInternal(query, options);
        }
    }

    // Group Aggregate Query

    @Override
    public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.group.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.get.group.aggregate")) {
            return streamInternal(query, options);
        }
    }

    // Compute Query

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlCompute.Statistics query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.compute.statistics")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlCompute.Statistics query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.compute.statistics")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptList>> execute(GraqlCompute.Path query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.compute.path")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptList>> stream(GraqlCompute.Path query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.compute.path")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.compute.centrality")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.compute.centrality")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptSet>> execute(GraqlCompute.Cluster query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.compute.cluster")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptSet>> stream(GraqlCompute.Cluster query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.compute.cluster")) {
            return streamInternal(query);
        }
    }

    // Generic queries

    @Override
    public QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query) {
        return execute(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query, QueryOptions options) {
        if (query instanceof GraqlDefine) {
            return execute((GraqlDefine) query);

        } else if (query instanceof GraqlUndefine) {
            return execute((GraqlUndefine) query);

        } else if (query instanceof GraqlInsert) {
            return execute((GraqlInsert) query, options);

        } else if (query instanceof GraqlDelete) {
            return execute((GraqlDelete) query, options);

        } else if (query instanceof GraqlGet) {
            return execute((GraqlGet) query, options);

        } else if (query instanceof GraqlGet.Aggregate) {
            return execute((GraqlGet.Aggregate) query, options);

        } else if (query instanceof GraqlGet.Group.Aggregate) {
            return execute((GraqlGet.Group.Aggregate) query, options);

        } else if (query instanceof GraqlGet.Group) {
            return execute((GraqlGet.Group) query, options);

        } else if (query instanceof GraqlCompute.Statistics) {
            return execute((GraqlCompute.Statistics) query);

        } else if (query instanceof GraqlCompute.Path) {
            return execute((GraqlCompute.Path) query);

        } else if (query instanceof GraqlCompute.Centrality) {
            return execute((GraqlCompute.Centrality) query);

        } else if (query instanceof GraqlCompute.Cluster) {
            return execute((GraqlCompute.Cluster) query);

        } else {
            throw new IllegalArgumentException("Unrecognised Query object");
        }
    }

    @Override
    public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query) {
        return stream(query, Options.DEFAULT);
    }

    @Override
    public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query, QueryOptions options) {
        if (query instanceof GraqlDefine) {
            return stream((GraqlDefine) query);

        } else if (query instanceof GraqlUndefine) {
            return stream((GraqlUndefine) query);

        } else if (query instanceof GraqlInsert) {
            return stream((GraqlInsert) query, options);

        } else if (query instanceof GraqlDelete) {
            return stream((GraqlDelete) query, options);

        } else if (query instanceof GraqlGet) {
            return stream((GraqlGet) query, options);

        } else if (query instanceof GraqlGet.Aggregate) {
            return stream((GraqlGet.Aggregate) query, options);

        } else if (query instanceof GraqlGet.Group.Aggregate) {
            return stream((GraqlGet.Group.Aggregate) query, options);

        } else if (query instanceof GraqlGet.Group) {
            return stream((GraqlGet.Group) query, options);

        } else if (query instanceof GraqlCompute.Statistics) {
            return stream((GraqlCompute.Statistics) query);

        } else if (query instanceof GraqlCompute.Path) {
            return stream((GraqlCompute.Path) query);

        } else if (query instanceof GraqlCompute.Centrality) {
            return stream((GraqlCompute.Centrality) query);

        } else if (query instanceof GraqlCompute.Cluster) {
            return stream((GraqlCompute.Cluster) query);

        } else {
            throw new IllegalArgumentException("Unrecognised Query object");
        }
    }

    private <T> RPCIterator<T> getQueryIterator(GraqlQuery query, QueryOptions options) {
        return new RPCIterator<>(RequestBuilder.Transaction.query(query.toString(), options),
                response -> ResponseReader.answer(response.getQueryIterRes().getAnswer(), this));
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query) {
        return executeInternal(query, Options.DEFAULT);
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query, QueryOptions options) {
        return new QueryExecuteFuture<>(getQueryIterator(query, options));
    }

    private <T extends Answer> QueryFuture<Stream<T>> streamInternal(GraqlQuery query) {
        return streamInternal(query, Options.DEFAULT);
    }

    private <T extends Answer> QueryFuture<Stream<T>> streamInternal(GraqlQuery query, QueryOptions options) {
        return new QueryStreamFuture<>(getQueryIterator(query, options));
    }

    public void close() {
        transceiver.close();
    }

    @Override
    public boolean isOpen() {
        return transceiver.isOpen();
    }

    private TransactionProto.Transaction.Res sendAndReceiveOrThrow(TransactionProto.Transaction.Req request) {
        try {
            return transceiver.sendAndReceive(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // This is called from classes like Transaction, that impl methods which do not throw InterruptedException
            // Therefore, we have to wrap it in a RuntimeException.
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {
        sendAndReceiveOrThrow(RequestBuilder.Transaction.commit());
        close();
    }

    // TODO: is this a reasonable way of implementing this method?
    @Override
    public ThingType.Remote getRootType() {
        return getType(GraqlToken.Type.THING.toString()).asThingType();
    }

    @Override
    public EntityType.Remote getRootEntityType() {
        return getType(GraqlToken.Type.ENTITY.toString()).asEntityType();
    }

    @Override
    public RelationType.Remote getRootRelationType() {
        return getType(GraqlToken.Type.RELATION.toString()).asRelationType();
    }

    @Override
    public AttributeType.Remote getRootAttributeType() {
        return getType(GraqlToken.Type.ATTRIBUTE.toString()).asAttributeType();
    }

    @Override
    public RoleType.Remote getRootRoleType() {
        return getType(GraqlToken.Type.ROLE.toString()).asRoleType();
    }

    @Override
    public Rule.Remote getRootRule() {
        return getType(GraqlToken.Type.RULE.toString()).asRule();
    }

    @Override
    public EntityType.Remote putEntityType(String label) {
        return grakn.client.concept.type.Type.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putEntityType(label)).getPutEntityTypeRes().getEntityType()).asEntityType();
    }

    @Override
    @Nullable
    public EntityType.Remote getEntityType(String label) {
        grakn.client.concept.type.Type.Remote concept = getType(label);
        if (concept instanceof ThingType.Remote) {
            return (grakn.client.concept.type.EntityType.Remote) concept;
        } else {
            return null;
        }
    }

    @Override
    public RelationType.Remote putRelationType(String label) {
        return grakn.client.concept.type.Type.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putRelationType(label))
                .getPutRelationTypeRes().getRelationType()).asRelationType();
    }

    @Override
    @Nullable
    public RelationType.Remote getRelationType(String label) {
        grakn.client.concept.type.Type.Remote concept = getType(label);
        if (concept instanceof RelationType.Remote) {
            return (RelationType.Remote) concept;
        } else {
            return null;
        }
    }

    @Override
    public AttributeType.Remote putAttributeType(String label, ValueType valueType) {
        return grakn.client.concept.type.Type.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putAttributeType(label, valueType))
                .getPutAttributeTypeRes().getAttributeType()).asAttributeType();
    }

    @Override
    @Nullable
    public AttributeType.Remote getAttributeType(String label) {
        grakn.client.concept.type.Type.Remote concept = getType(label);
        if (concept instanceof AttributeType.Remote) {
            return (AttributeType.Remote) concept;
        } else {
            return null;
        }
    }

    @Override
    public Rule.Remote putRule(String label, Pattern when, Pattern then) {
        return grakn.client.concept.type.Type.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putRule(label, when, then))
                .getPutRuleRes().getRule()).asRule();
    }

    @Override
    @Nullable
    public Rule.Remote getRule(String label) {
        grakn.client.concept.type.Type.Remote concept = getType(label);
        if (concept instanceof Rule.Remote) {
            return (Rule.Remote) concept;
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public grakn.client.concept.type.Type.Remote getType(String label) {
        TransactionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getType(label));
        switch (response.getGetTypeRes().getResCase()) {
            case TYPE:
                final grakn.client.concept.type.Type.Remote type = grakn.client.concept.type.Type.Remote.of(this, response.getGetTypeRes().getType());
                typeCache.put(type.getLabel(), grakn.client.concept.type.Type.Local.of(response.getGetTypeRes().getType()));
                // TODO: maybe we should return the cached Type.Local? It has more information
                return type;
            case RES_NOT_SET:
                return null;
            default:
                throw GraknClientException.resultNotPresent();
        }
    }

    @Nullable
    public grakn.client.concept.type.Type.Local getCachedType(String label) {
        return typeCache.get(label);
    }

    @Override
    @Nullable
    public Thing.Remote getThing(ConceptIID iid) {
        TransactionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getThing(iid));
        switch (response.getGetThingRes().getResCase()) {
            case THING:
                return Thing.Remote.of(this, response.getGetThingRes().getThing());
            case RES_NOT_SET:
                return null;
            default:
                throw GraknClientException.resultNotPresent();
        }
    }

    @Override
    public TransactionProto.Transaction.Res runConceptMethod(ConceptIID iid, ConceptProto.ThingMethod.Req thingMethod) {
        TransactionProto.Transaction.ConceptMethod.Thing.Req conceptMethod = TransactionProto.Transaction.ConceptMethod.Thing.Req.newBuilder()
                .setIid(iid.getValue()).setMethod(thingMethod).build();
        TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setConceptMethodThingReq(conceptMethod).build();

        return sendAndReceiveOrThrow(request);
    }

    @Override
    public TransactionProto.Transaction.Res runConceptMethod(String label, ConceptProto.TypeMethod.Req typeMethod) {
        TransactionProto.Transaction.ConceptMethod.Type.Req conceptMethod = TransactionProto.Transaction.ConceptMethod.Type.Req.newBuilder()
                .setLabel(label).setMethod(typeMethod).build();
        TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setConceptMethodTypeReq(conceptMethod).build();

        return sendAndReceiveOrThrow(request);
    }

    @Override
    public <T> Stream<T> iterateConceptMethod(ConceptIID iid, ConceptProto.ThingMethod.Iter.Req method, Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader) {
        TransactionProto.Transaction.ConceptMethod.Thing.Iter.Req conceptIterMethod = TransactionProto.Transaction.ConceptMethod.Thing.Iter.Req.newBuilder()
                .setIid(iid.getValue()).setMethod(method).build();
        TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder().setConceptMethodThingIterReq(conceptIterMethod).build();

        return iterate(request, res -> responseReader.apply(res.getConceptMethodThingIterRes().getResponse()));
    }

    @Override
    public <T> Stream<T> iterateConceptMethod(String label, ConceptProto.TypeMethod.Iter.Req method, Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader) {
        TransactionProto.Transaction.ConceptMethod.Type.Iter.Req conceptIterMethod = TransactionProto.Transaction.ConceptMethod.Type.Iter.Req.newBuilder()
                .setLabel(label).setMethod(method).build();
        TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder().setConceptMethodTypeIterReq(conceptIterMethod).build();

        return iterate(request, res -> responseReader.apply(res.getConceptMethodTypeIterRes().getResponse()));
    }

    @Override
    public Explanation getExplanation(ConceptMap explainable) {
        AnswerProto.ConceptMap conceptMapProto = conceptMap(explainable);
        AnswerProto.Explanation.Req explanationReq = AnswerProto.Explanation.Req.newBuilder().setExplainable(conceptMapProto).build();
        TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setExplanationReq(explanationReq).build();
        TransactionProto.Transaction.Res response = sendAndReceiveOrThrow(request);
        return ResponseReader.explanation(response.getExplanationRes(), this);
    }

    private AnswerProto.ConceptMap conceptMap(ConceptMap conceptMap) {
        AnswerProto.ConceptMap.Builder conceptMapProto = AnswerProto.ConceptMap.newBuilder();
        conceptMap.map().forEach((var, concept) -> {
            ConceptProto.Concept conceptProto = ConceptMessage.concept(concept);
            conceptMapProto.putMap(var, conceptProto);
        });
        conceptMapProto.setHasExplanation(conceptMap.hasExplanation());
        conceptMapProto.setPattern(conceptMap.queryPattern().toString());
        return conceptMapProto.build();
    }

    @Override
    public <T> Stream<T> iterate(TransactionProto.Transaction.Iter.Req request, Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
        return Objects.requireNonNull(StreamSupport.stream(((Iterable<T>) () -> new RPCIterator<>(request, responseReader)).spliterator(), false));
    }

    private abstract class QueryFutureBase<T> implements QueryFuture<T> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false; // Can't cancel
        }

        @Override
        public boolean isCancelled() {
            return false; // Can't cancel
        }

        @Override
        public boolean isDone() {
            return getIterator().isStarted();
        }

        @Override
        public T get() {
            try {
                getIterator().waitForStart();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
            return getInternal();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws TimeoutException {
            try {
                getIterator().waitForStart(timeout, unit);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
            return getInternal();
        }

        protected abstract RPCIterator<?> getIterator();
        protected abstract T getInternal();
    }

    private class QueryStreamFuture<T> extends QueryFutureBase<Stream<T>> {
        private RPCIterator<T> iterator;

        protected QueryStreamFuture(RPCIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected RPCIterator<?> getIterator() {
            return iterator;
        }

        @Override
        protected Stream<T> getInternal() {
            return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
        }
    }

    private class QueryExecuteFuture<T> extends QueryFutureBase<List<T>> {
        private RPCIterator<T> iterator;

        protected QueryExecuteFuture(RPCIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected RPCIterator<?> getIterator() {
            return iterator;
        }

        @Override
        protected List<T> getInternal() {
            List<T> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);
            return result;
        }
    }

    /**
     * A client-side iterator over gRPC messages. Will send TransactionProto.Transaction.Iter.Req messages until
     * TransactionProto.Transaction.Iter.Res returns done as a message.
     *
     * @param <T> class type of objects being iterated
     */
    public class RPCIterator<T> extends AbstractIterator<T> {
        private Function<TransactionProto.Transaction.Iter.Res, T> responseReader;
        private Batch currentBatch;
        private volatile boolean started;
        private TransactionProto.Transaction.Iter.Res first;
        private TransactionProto.Transaction.Iter.Req.Options options;

        private RPCIterator(TransactionProto.Transaction.Iter.Req req,
                            Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
            this.responseReader = responseReader;
            options = req.getOptions();
            sendRequest(req);
        }

        private void sendRequest(TransactionProto.Transaction.Iter.Req req) {
            currentBatch = new Batch();

            TransactionProto.Transaction.Req transactionReq = TransactionProto.Transaction.Req.newBuilder()
                    .setIterReq(req).build();

            transceiver.sendAndReceiveMultipleAsync(transactionReq, currentBatch);
        }

        private void nextBatch(int iteratorID) {
            TransactionProto.Transaction.Iter.Req iterReq = TransactionProto.Transaction.Iter.Req.newBuilder()
                    .setIteratorID(iteratorID)
                    .setOptions(options)
                    .build();

            sendRequest(iterReq);
        }

        private class Batch extends Transceiver.MultiResponseCollector {
            @Override
            protected boolean isLastResponse(TransactionProto.Transaction.Res response) {
                TransactionProto.Transaction.Iter.Res iterRes = response.getIterRes();
                return iterRes.getIteratorID() != 0 || iterRes.getDone();
            }
        }

        public boolean isStarted() {
            return started;
        }

        public void waitForStart(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (first != null) {
                throw new IllegalStateException("Should not poll RPCIterator multiple times");
            }

            first = currentBatch.poll(timeout, unit).getIterRes();
        }

        public void waitForStart() throws InterruptedException {

        }

        @Override
        protected T computeNext() {
            if (first != null) {
                TransactionProto.Transaction.Iter.Res iterRes = first;
                first = null;
                return responseReader.apply(iterRes);
            }

            TransactionProto.Transaction.Iter.Res res;
            try {
                res = currentBatch.take().getIterRes();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            started = true;
            switch (res.getResCase()) {
                case ITERATORID:
                    nextBatch(res.getIteratorID());
                    return computeNext();
                case DONE:
                    return endOfData();
                case RES_NOT_SET:
                    throw new IllegalStateException("Received an empty response");
                default:
                    return responseReader.apply(res);
            }
        }
    }

    public static class QueryOptionsImpl implements QueryOptions {
        private Map<Option<?>, Object> options;

        public QueryOptionsImpl() {
            options = new HashMap<>();
        }

        public QueryOptionsImpl(Map<Option<?>, Object> options) {
            this.options = options;
        }

        @Override
        public <T> QueryOptions set(Option<T> option, T value) {
            Map<Option<?>, Object> cloned = new HashMap<>(options);
            cloned.put(option, value);
            return new QueryOptionsImpl(cloned);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> QueryOptions whenSet(Option<T> option, Consumer<T> consumer) {
            T value = (T) options.get(option);
            if (value != null) {
                consumer.accept(value);
            }
            return this;
        }

        @Override
        public QueryOptions infer(boolean infer) {
            return set(BooleanOption.INFER, infer);
        }

        @Override
        public QueryOptions explain(boolean explain) {
            return set(BooleanOption.EXPLAIN, explain);
        }

        @Override
        public QueryOptions batchSize(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Batch size cannot be less that 1, was: " + size);
            }
            return set(BatchOption.BATCH_SIZE, TransactionProto.Transaction.Iter.Req.Options.newBuilder().setNumber(size).build());
        }

        @Override
        public QueryOptions batchSize(BatchSize batchSize) {
            if (batchSize == BatchSize.ALL) {
                return set(BatchOption.BATCH_SIZE, TransactionProto.Transaction.Iter.Req.Options.newBuilder().setAll(true).build());
            }
            throw new IllegalArgumentException("Invalid batch size mode: " + batchSize);
        }
    }
}
