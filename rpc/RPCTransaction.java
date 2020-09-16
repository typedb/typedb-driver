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

package grakn.client.rpc;

import com.google.protobuf.ByteString;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.answer.Answer;
import grakn.client.concept.answer.AnswerGroup;
import grakn.client.concept.answer.ConceptMap;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;
import grakn.protocol.GraknGrpc;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.common.exception.ErrorMessage.Query.BAD_QUERY_OBJECT;
import static grakn.client.concept.proto.ConceptProtoBuilder.concept;

public class RPCTransaction implements Transaction {

    private final Session session;
    private final Transaction.Type type;
    private final Concepts concepts;
    private final RPCTransceiver transceiver;

    RPCTransaction(final RPCSession session, final ByteString sessionId, final Type type, final GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.transceiver = RPCTransceiver.create(GraknGrpc.newStub(session.getChannel()));
            this.session = session;
            this.type = type;
            this.concepts = new Concepts(this);

            final TransactionProto.Transaction.Req openTxReq = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options))).build();

            this.transceiver.execute(openTxReq);
        }
    }

    public RPCTransceiver transceiver() {
        return transceiver;
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
        try (ThreadTrace ignored = traceOnThread("tx.execute.define")) {
            return executeInternal(query, new GraknOptions());
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlUndefine query) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.undefine")) {
            return executeInternal(query, new GraknOptions());
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlInsert query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.insert")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlInsert query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.delete")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<Void>> execute(GraqlDelete query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlMatch query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.get")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<List<ConceptMap>> execute(GraqlMatch query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.define")) {
            return streamInternal(query, new GraknOptions());
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.undefine")) {
            return streamInternal(query, new GraknOptions());
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.insert")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.delete")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Void>> stream(GraqlDelete query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlMatch query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.get")) {
            return streamInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptMap>> stream(GraqlMatch query) {
        return stream(query, new GraknOptions());
    }

    // Aggregate Query

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlMatch.Aggregate query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlMatch.Aggregate query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.get.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlMatch.Aggregate query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlMatch.Aggregate query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.get.aggregate")) {
            return streamInternal(query, options);
        }
    }

    // Group Query

    @Override
    public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlMatch.Group query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlMatch.Group query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.get.group")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlMatch.Group query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlMatch.Group query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.get.group")) {
            return streamInternal(query, options);
        }
    }

    // Group Aggregate Query

    @Override
    public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlMatch.Group.Aggregate query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlMatch.Group.Aggregate query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.get.group.aggregate")) {
            return executeInternal(query, options);
        }
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlMatch.Group.Aggregate query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlMatch.Group.Aggregate query, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.get.group.aggregate")) {
            return streamInternal(query, options);
        }
    }

    // Compute Query

    @Override
    public QueryFuture<List<Numeric>> execute(GraqlCompute.Statistics query) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.compute.statistics")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<Numeric>> stream(GraqlCompute.Statistics query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.compute.statistics")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptList>> execute(GraqlCompute.Path query) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.compute.path")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptList>> stream(GraqlCompute.Path query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.compute.path")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.compute.centrality")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.compute.centrality")) {
            return streamInternal(query);
        }
    }

    @Override
    public QueryFuture<List<ConceptSet>> execute(GraqlCompute.Cluster query) {
        try (ThreadTrace ignored = traceOnThread("tx.execute.compute.cluster")) {
            return executeInternal(query);
        }
    }

    @Override
    public QueryFuture<Stream<ConceptSet>> stream(GraqlCompute.Cluster query) {
        try (ThreadTrace ignored = traceOnThread("tx.stream.compute.cluster")) {
            return streamInternal(query);
        }
    }

    // Generic queries

    @Override
    public QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query) {
        return execute(query, new GraknOptions());
    }

    @Override
    public QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query, GraknOptions options) {
        if (query instanceof GraqlDefine) {
            return execute((GraqlDefine) query);

        } else if (query instanceof GraqlUndefine) {
            return execute((GraqlUndefine) query);

        } else if (query instanceof GraqlInsert) {
            return execute((GraqlInsert) query, options);

        } else if (query instanceof GraqlDelete) {
            return execute((GraqlDelete) query, options);

        } else if (query instanceof GraqlMatch) {
            return execute((GraqlMatch) query, options);

        } else if (query instanceof GraqlMatch.Aggregate) {
            return execute((GraqlMatch.Aggregate) query, options);

        } else if (query instanceof GraqlMatch.Group.Aggregate) {
            return execute((GraqlMatch.Group.Aggregate) query, options);

        } else if (query instanceof GraqlMatch.Group) {
            return execute((GraqlMatch.Group) query, options);

        } else if (query instanceof GraqlCompute.Statistics) {
            return execute((GraqlCompute.Statistics) query);

        } else if (query instanceof GraqlCompute.Path) {
            return execute((GraqlCompute.Path) query);

        } else if (query instanceof GraqlCompute.Centrality) {
            return execute((GraqlCompute.Centrality) query);

        } else if (query instanceof GraqlCompute.Cluster) {
            return execute((GraqlCompute.Cluster) query);

        } else {
            throw new GraknClientException(BAD_QUERY_OBJECT.message(query));
        }
    }

    @Override
    public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query) {
        return stream(query, new GraknOptions());
    }

    @Override
    public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query, GraknOptions options) {
        if (query instanceof GraqlDefine) {
            return stream((GraqlDefine) query);

        } else if (query instanceof GraqlUndefine) {
            return stream((GraqlUndefine) query);

        } else if (query instanceof GraqlInsert) {
            return stream((GraqlInsert) query, options);

        } else if (query instanceof GraqlDelete) {
            return stream((GraqlDelete) query, options);

        } else if (query instanceof GraqlMatch) {
            return stream((GraqlMatch) query, options);

        } else if (query instanceof GraqlMatch.Aggregate) {
            return stream((GraqlMatch.Aggregate) query, options);

        } else if (query instanceof GraqlMatch.Group.Aggregate) {
            return stream((GraqlMatch.Group.Aggregate) query, options);

        } else if (query instanceof GraqlMatch.Group) {
            return stream((GraqlMatch.Group) query, options);

        } else if (query instanceof GraqlCompute.Statistics) {
            return stream((GraqlCompute.Statistics) query);

        } else if (query instanceof GraqlCompute.Path) {
            return stream((GraqlCompute.Path) query);

        } else if (query instanceof GraqlCompute.Centrality) {
            return stream((GraqlCompute.Centrality) query);

        } else if (query instanceof GraqlCompute.Cluster) {
            return stream((GraqlCompute.Cluster) query);

        } else {
            throw new GraknClientException(BAD_QUERY_OBJECT.message(query));
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Answer> RPCIterator<T> getQueryIterator(final GraqlQuery query, final GraknOptions options) {
        final TransactionProto.Transaction.Iter.Req.Builder reqBuilder = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setQueryIterReq(TransactionProto.Transaction.Query.Iter.Req.newBuilder()
                                         .setQuery(query.toString())
                                         .setOptions(options(options)));

        final TransactionProto.Transaction.Iter.Req iterReq = reqBuilder.build();
        return new RPCIterator<>(transceiver, iterReq);
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query) {
        return executeInternal(query, new GraknOptions());
    }

    private <T extends Answer> QueryFuture<List<T>> executeInternal(GraqlQuery query, GraknOptions options) {
        return new QueryExecuteFuture<>(getQueryIterator(query, options));
    }

    private <T extends Answer> QueryFuture<Stream<T>> streamInternal(GraqlQuery query) {
        return streamInternal(query, new GraknOptions());
    }

    private <T extends Answer> QueryFuture<Stream<T>> streamInternal(GraqlQuery query, GraknOptions options) {
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

        transceiver.execute(commitReq);
        close();
    }

    @Override
    public Explanation getExplanation(ConceptMap explainable) {
        AnswerProto.ConceptMap conceptMapProto = conceptMap(explainable);
        AnswerProto.Explanation.Req explanationReq = AnswerProto.Explanation.Req.newBuilder().setExplainable(conceptMapProto).build();
        TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setExplanationReq(explanationReq).build();
        TransactionProto.Transaction.Res response = transceiver.execute(request);
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
}
