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

import com.google.protobuf.ByteString;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.Grakn.Database;
import grakn.client.Grakn.QueryOptions;
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
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.connection.GraknConcepts;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;
import grakn.protocol.GraknGrpc;
import grakn.protocol.OptionsProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.READ;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.RpcMessageWriter.tracingData;
import static grakn.client.common.exception.ErrorMessage.Query.UNRECOGNISED_QUERY_OBJECT;
import static grakn.client.concept.ConceptMessageWriter.concept;
import static grakn.client.connection.ConnectionMessageWriter.batchSize;
import static grakn.client.connection.ConnectionMessageWriter.options;

public class GraknTransaction implements Transaction {

    private final Session session;
    private final Transaction.Type type;
    private final Concepts concepts;
    private final GraknTransceiver transceiver;

    public static class Builder implements Transaction.Builder {

        private final ManagedChannel channel;
        private final Session session;
        private final ByteString sessionId;
        private final QueryOptions options;

        Builder(final ManagedChannel channel, final Session session, final ByteString sessionId) {
            this(channel, session, sessionId, new QueryOptions());
        }

        Builder(final ManagedChannel channel, final Session session, final ByteString sessionId, final QueryOptions options) {
            this.channel = channel;
            this.session = session;
            this.sessionId = sessionId;
            this.options = options;
        }

        @Override
        public Transaction read() {
            return new GraknTransaction(channel, session, sessionId, READ, options);
        }

        @Override
        public Transaction write() {
            return new GraknTransaction(channel, session, sessionId, WRITE, options);
        }
    }

    GraknTransaction(final ManagedChannel channel, final Session session, final ByteString sessionId, final Type type) {
        this(channel, session, sessionId, type, new QueryOptions());
    }

    GraknTransaction(final ManagedChannel channel, final Session session, final ByteString sessionId, final Type type, final QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.transceiver = GraknTransceiver.create(GraknGrpc.newStub(channel));
            this.session = session;
            this.type = type;
            this.concepts = new GraknConcepts(this.transceiver);

            final TransactionProto.Transaction.Req openTxReq = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options))).build();

            this.transceiver.sendAndReceiveOrThrow(openTxReq);
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
            return executeInternal(query, new QueryOptions());
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlUndefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.undefine")) {
            return executeInternal(query, new QueryOptions());
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
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.delete")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query) {
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlGet query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlGet query) {
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.define")) {
            return streamInternal(query, new QueryOptions());
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.undefine")) {
            return streamInternal(query, new QueryOptions());
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
        return stream(query, new QueryOptions());
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.delete")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query) {
        return stream(query, new QueryOptions());
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.stream.get")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query) {
        return stream(query, new QueryOptions());
    }

    // Aggregate Query

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query) {
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query) {
        return stream(query, new QueryOptions());
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
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.group")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query) {
        return stream(query, new QueryOptions());
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
        return execute(query, new QueryOptions());
    }

    @Override
    public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query, QueryOptions options) {
        try (GrablTracingThreadStatic.ThreadTrace trace = traceOnThread("tx.execute.get.group.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query) {
        return stream(query, new QueryOptions());
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
        return execute(query, new QueryOptions());
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
            throw new GraknClientException(UNRECOGNISED_QUERY_OBJECT.message(query));
        }
    }

    @Override
    public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query) {
        return stream(query, new QueryOptions());
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
            throw new GraknClientException(UNRECOGNISED_QUERY_OBJECT.message(query));
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Answer> RpcIterator<T> getQueryIterator(final GraqlQuery query, final QueryOptions options) {
        final OptionsProto.Options.Builder optionsBuilder = OptionsProto.Options.newBuilder();
        if (options.infer() != null) {
            optionsBuilder.setInfer(options.infer());
        }
        if (options.explain() != null) {
            optionsBuilder.setExplain(options.explain());
        }

        final TransactionProto.Transaction.Iter.Req.Builder reqBuilder = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setQueryIterReq(TransactionProto.Transaction.Query.Iter.Req.newBuilder()
                        .setQuery(query.toString())
                        .setOptions(optionsBuilder));

        if (options.batchSize() != null) {
            reqBuilder.setOptions(batchSize(options.batchSize()));
        }

        final TransactionProto.Transaction.Iter.Req iterReq = reqBuilder.build();
        return new RpcIterator<>(transceiver, iterReq, response -> (T) Answer.of(this, response.getQueryIterRes().getAnswer()));
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query) {
        return executeInternal(query, new QueryOptions());
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query, QueryOptions options) {
        return new QueryExecuteFuture<>(getQueryIterator(query, options));
    }

    private <T extends Answer> QueryFuture<Stream<T>> streamInternal(GraqlQuery query) {
        return streamInternal(query, new QueryOptions());
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

    @Override
    public Concepts concepts() {
        return concepts;
    }

    @Override
    public void commit() {
        final TransactionProto.Transaction.Req commitReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance()).build();

        transceiver.sendAndReceiveOrThrow(commitReq);
        close();
    }

    @Override
    public Explanation getExplanation(ConceptMap explainable) {
        AnswerProto.ConceptMap conceptMapProto = conceptMap(explainable);
        AnswerProto.Explanation.Req explanationReq = AnswerProto.Explanation.Req.newBuilder().setExplainable(conceptMapProto).build();
        TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setExplanationReq(explanationReq).build();
        TransactionProto.Transaction.Res response = transceiver.sendAndReceiveOrThrow(request);
        return Explanation.of(this, response.getExplanationRes());
    }

    private AnswerProto.ConceptMap conceptMap(ConceptMap conceptMap) {
        AnswerProto.ConceptMap.Builder conceptMapProto = AnswerProto.ConceptMap.newBuilder();
        conceptMap.map().forEach((var, concept) -> {
            ConceptProto.Concept conceptProto = concept(concept);
            conceptMapProto.putMap(var, conceptProto);
        });
        conceptMapProto.setHasExplanation(conceptMap.hasExplanation());
        conceptMapProto.setPattern(conceptMap.queryPattern().toString());
        return conceptMapProto.build();
    }

    private abstract static class QueryFutureBase<T> implements QueryFuture<T> {
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
            getIterator().waitForStart();
            return getInternal();
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            try {
                getIterator().waitForStart(timeout, unit);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(ex);
            } catch (TimeoutException ex) {
                throw new GraknClientException(ex);
            }
            return getInternal();
        }

        protected abstract RpcIterator<?> getIterator();
        protected abstract T getInternal();
    }

    private static class QueryStreamFuture<T> extends QueryFutureBase<Stream<T>> {
        private RpcIterator<T> iterator;

        protected QueryStreamFuture(RpcIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected RpcIterator<?> getIterator() {
            return iterator;
        }

        @Override
        protected Stream<T> getInternal() {
            return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
        }
    }

    private static class QueryExecuteFuture<T> extends QueryFutureBase<List<T>> {
        private RpcIterator<T> iterator;

        protected QueryExecuteFuture(RpcIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected RpcIterator<?> getIterator() {
            return iterator;
        }

        @Override
        protected List<T> getInternal() {
            List<T> result = new ArrayList<>();
            iterator.forEachRemaining(result::add);
            return result;
        }
    }
}
