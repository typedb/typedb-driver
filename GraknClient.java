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

package grakn.client;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Explanation;
import grakn.client.answer.Numeric;
import grakn.client.answer.Void;
import grakn.client.concept.Concept;
import grakn.client.concept.ValueType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.Label;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.ThingType;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.client.rpc.ResponseReader;
import grakn.client.rpc.Transceiver;
import grakn.protocol.GraknGrpc;
import grakn.protocol.GraknGrpc.GraknBlockingStub;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;
import grakn.protocol.SessionProto;
import grakn.protocol.TransactionProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

/**
 * Entry-point which communicates with a running Grakn server using gRPC.
 */
public class GraknClient implements AutoCloseable {

    public static final String DEFAULT_URI = "localhost:48555";

    private ManagedChannel channel;
    private final String username;
    private final String password;
    private final Databases databases;

    public GraknClient() {
        this(DEFAULT_URI);
    }

    public GraknClient(String address) {
        this(address, null, null);
    }

    public GraknClient(String address, String username, String password) {
        channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext().build();
        this.username = username;
        this.password = password;
        databases = new Databases(channel);
    }

    public GraknClient overrideChannel(ManagedChannel channel) {
        this.channel = channel;
        return this;
    }

    public void close() {
        channel.shutdown();
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isOpen() {
        return !channel.isShutdown() || !channel.isTerminated();
    }

    public Session session(String database) {
        return session(database, Session.SessionType.DATA);
    }

    public Session schemaSession(String database) {
        return session(database, Session.SessionType.SCHEMA);
    }

    public Session session(String database, Session.SessionType type) {
        return new Session(channel, username, password, database, type);
    }

    public Databases databases() {
        return databases;
    }

    /**
     * @see Transaction
     * @see GraknClient
     */
    public static class Session implements AutoCloseable {

        public enum SessionType {
            DATA,
            SCHEMA;
        }

        protected ManagedChannel channel;
        private String username; // TODO: Do we need to save this? It's not used.
        private String password; // TODO: Do we need to save this? It's not used.
        protected String database;
        protected GraknBlockingStub sessionStub;
        protected ByteString sessionId;
        protected boolean isOpen;

        private Session(ManagedChannel channel, String username, String password, String database, SessionType type) {
            this.username = username;
            this.password = password;
            this.database = database;
            this.channel = channel;
            this.sessionStub = GraknGrpc.newBlockingStub(channel);

            SessionProto.Session.Open.Req.Builder open = RequestBuilder.Session.open(database).newBuilderForType();
            open.setDatabase(database);

            switch (type) {
                case DATA:
                    open.setType(SessionProto.Session.Type.DATA);
                    break;
                case SCHEMA:
                    open.setType(SessionProto.Session.Type.SCHEMA);
                    break;
                default:
                    open.setType(SessionProto.Session.Type.UNRECOGNIZED);
            }

            SessionProto.Session.Open.Res response = sessionStub.sessionOpen(open.build());
            sessionId = response.getSessionID();
            isOpen = true;
        }

        public GraknClient.Transaction.Builder transaction() {
            return new Transaction.Builder(channel, this, sessionId);
        }

        public GraknClient.Transaction transaction(Transaction.Type type) {
            return new Transaction(channel, this, sessionId, type);
        }

        public boolean isOpen() {
            return isOpen;
        }

        public void close() {
            if (!isOpen) return;
            sessionStub.sessionClose(RequestBuilder.Session.close(sessionId));
            isOpen = false;
        }

        public Database database() {
            return Database.of(database);
        }
    }

    public static class Transaction implements AutoCloseable {
        private final Session session;
        private final Type type;
        private final Transceiver transceiver;

        public static class Builder {

            private ManagedChannel channel;
            private GraknClient.Session session;
            private ByteString sessionId;

            public Builder(ManagedChannel channel, GraknClient.Session session, ByteString sessionId) {
                this.channel = channel;
                this.session = session;
                this.sessionId = sessionId;
            }

            public GraknClient.Transaction read() {
                return new GraknClient.Transaction(channel, session, sessionId, Transaction.Type.READ);
            }

            public GraknClient.Transaction write() {
                return new GraknClient.Transaction(channel, session, sessionId, Transaction.Type.WRITE);
            }
        }

        public enum Type {
            READ(0),  //Read only transaction where mutations to the graph are prohibited
            WRITE(1); //Write transaction where the graph can be mutated

            private final int type;

            Type(int type) {
                this.type = type;
            }

            public int iid() {
                return type;
            }

            @Override
            public String toString() {
                return this.name();
            }

            public static Type of(int value) {
                for (Type t : Transaction.Type.values()) {
                    if (t.type == value) return t;
                }
                return null;
            }

            public static Type of(String value) {
                for (Type t : Transaction.Type.values()) {
                    if (t.name().equalsIgnoreCase(value)) return t;
                }
                return null;
            }
        }

        private Transaction(ManagedChannel channel, Session session, ByteString sessionId, Type type) {
            try (ThreadTrace trace = traceOnThread(type == Transaction.Type.WRITE ? "tx.write" : "tx.read")) {
                this.transceiver = Transceiver.create(GraknGrpc.newStub(channel));
                this.session = session;
                this.type = type;
                sendAndReceiveOrThrow(RequestBuilder.Transaction.open(sessionId, type));
            }
        }

        public Type type() {
            return type;
        }

        public GraknClient.Session session() {
            return session;
        }

        public Database database() {
            return session.database();
        }

        public QueryFuture<List<ConceptMap>> execute(GraqlDefine query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.define")) {
                return executeInternal(query, Options.DEFAULT);
            }
        }

        public QueryFuture<List<ConceptMap>> execute(GraqlUndefine query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.undefine")) {
                return executeInternal(query, Options.DEFAULT);
            }
        }

        public QueryFuture<List<ConceptMap>> execute(GraqlInsert query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.insert")) {
                return executeInternal(query, options);
            }
        }
        public QueryFuture<List<ConceptMap>> execute(GraqlInsert query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<List<Void>> execute(GraqlDelete query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.delete")) {
                return executeInternal(query, options);
            }
        }
        public QueryFuture<List<Void>> execute(GraqlDelete query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<List<ConceptMap>> execute(GraqlGet query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get")) {
                return executeInternal(query, options);
            }
        }
        public QueryFuture<List<ConceptMap>> execute(GraqlGet query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.define")) {
                return streamInternal(query, Options.DEFAULT);
            }
        }

        public QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.undefine")) {
                return streamInternal(query, Options.DEFAULT);
            }
        }

        public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.insert")) {
                return streamInternal(query, options);
            }
        }
        public QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<Void>> stream(GraqlDelete query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.delete")) {
                return streamInternal(query, options);
            }
        }
        public QueryFuture<Stream<Void>> stream(GraqlDelete query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get")) {
                return streamInternal(query, options);
            }
        }
        public QueryFuture<Stream<ConceptMap>> stream(GraqlGet query) {
            return stream(query, Options.DEFAULT);
        }

        // Aggregate Query

        public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.aggregate")) {
                return executeInternal(query, options);
            }
        }

        public QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.aggregate")) {
                return streamInternal(query, options);
            }
        }

        // Group Query

        public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.group")) {
                return executeInternal(query, options);
            }
        }

        public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.group")) {
                return streamInternal(query, options);
            }
        }


        // Group Aggregate Query

        public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query) {
            return execute(query, Options.DEFAULT);
        }

        public QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.group.aggregate")) {
                return executeInternal(query, options);
            }
        }


        public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query, QueryOptions options) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.group.aggregate")) {
                return streamInternal(query, options);
            }
        }

        // Compute Query

        public QueryFuture<List<Numeric>> execute(GraqlCompute.Statistics query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.statistics")) {
                return executeInternal(query);
            }
        }

        public QueryFuture<Stream<Numeric>> stream(GraqlCompute.Statistics query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.statistics")) {
                return streamInternal(query);
            }
        }

        public QueryFuture<List<ConceptList>> execute(GraqlCompute.Path query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.path")) {
                return executeInternal(query);
            }
        }

        public QueryFuture<Stream<ConceptList>> stream(GraqlCompute.Path query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.path")) {
                return streamInternal(query);
            }
        }

        public QueryFuture<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.centrality")) {
                return executeInternal(query);
            }
        }

        public QueryFuture<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.centrality")) {
                return streamInternal(query);
            }
        }

        public QueryFuture<List<ConceptSet>> execute(GraqlCompute.Cluster query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.cluster")) {
                return executeInternal(query);
            }
        }

        public QueryFuture<Stream<ConceptSet>> stream(GraqlCompute.Cluster query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.cluster")) {
                return streamInternal(query);
            }
        }

        // Generic queries

        public QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query) {
            return execute(query, Options.DEFAULT);
        }

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

        public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query) {
            return stream(query, Options.DEFAULT);
        }

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

        public boolean isOpen() {
            return transceiver.isOpen();
        }

        // TODO remove - backwards compatibility
        public boolean isClosed() {
            return !isOpen();
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

        public void commit() {
            sendAndReceiveOrThrow(RequestBuilder.Transaction.commit());
            close();
        }

        public ThingType.Remote<?, ?> getRootType() {
            grakn.client.concept.type.Type.Remote<?> concept = getType(Label.of("thing")); // TODO do this properly
            if (concept instanceof ThingType.Remote) {
                return (ThingType.Remote<?, ?>) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public ThingType.Remote<?, ?> getThingType(Label label) {
            grakn.client.concept.type.Type.Remote<?> concept = getType(label);
            if (concept instanceof ThingType.Remote) {
                return (ThingType.Remote<?, ?>) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public EntityType.Remote getEntityType(String label) {
            grakn.client.concept.type.Type.Remote<?> concept = getType(Label.of(label));
            if (concept instanceof ThingType.Remote) {
                return (grakn.client.concept.type.EntityType.Remote) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public RelationType.Remote getRelationType(String label) {
            grakn.client.concept.type.Type.Remote<?> concept = getType(Label.of(label));
            if (concept instanceof RelationType.Remote) {
                return (RelationType.Remote) concept;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public AttributeType.Remote<?> getAttributeType(String label) {
            grakn.client.concept.type.Type.Remote<?> concept = getType(Label.of(label));
            if (concept instanceof AttributeType.Remote) {
                return (AttributeType.Remote<?>) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public Rule.Remote getRule(String label) {
            grakn.client.concept.type.Type.Remote<?> concept = getType(Label.of(label));
            if (concept instanceof Rule.Remote) {
                return (Rule.Remote) concept;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public grakn.client.concept.type.Type.Remote<?> getType(Label label) {
            TransactionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getType(label));
            switch (response.getGetTypeRes().getResCase()) {
                case NULL:
                    return null;
                case TYPE:
                    return Concept.Remote.of(this, response.getGetTypeRes().getType()).asSchemaConcept();
                default:
                    throw GraknClientException.resultNotPresent();
            }
        }

        public grakn.client.concept.type.Type.Remote<?> getMetaConcept() {
            return getType(Label.of(Graql.Token.Type.THING.toString()));
        }

        public RelationType.Remote getMetaRelationType() {
            return getType(Label.of(Graql.Token.Type.RELATION.toString())).asRelationType();
        }

        public RoleType.Remote getMetaRole() {
            return getType(Label.of(Graql.Token.Type.ROLE.toString())).asRole();
        }

        public AttributeType.Remote<?> getMetaAttributeType() {
            return getType(Label.of(Graql.Token.Type.ATTRIBUTE.toString())).asAttributeType();
        }

        public EntityType.Remote getMetaEntityType() {
            return getType(Label.of(Graql.Token.Type.ENTITY.toString())).asEntityType();
        }

        public Rule.Remote getMetaRule() {
            return getType(Label.of(Graql.Token.Type.RULE.toString())).asRule();
        }

        @Nullable
        public Concept.Remote<?> getConcept(ConceptIID iid) {
            TransactionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getConcept(iid));
            switch (response.getGetConceptRes().getResCase()) {
                case NULL:
                    return null;
                case CONCEPT:
                    return Concept.Remote.of(this, response.getGetConceptRes().getConcept());
                default:
                    throw GraknClientException.resultNotPresent();
            }
        }

        public EntityType.Remote putEntityType(String label) {
            return putEntityType(Label.of(label));
        }

        public EntityType.Remote putEntityType(Label label) {
            return Concept.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putEntityType(label)).getPutEntityTypeRes().getEntityType()).asEntityType();
        }

        public <V> AttributeType.Remote<V> putAttributeType(String label, ValueType<V> valueType) {
            return putAttributeType(Label.of(label), valueType);
        }
        @SuppressWarnings("unchecked")
        public <V> AttributeType.Remote<V> putAttributeType(Label label, ValueType<V> valueType) {
            return (AttributeType.Remote<V>) Concept.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putAttributeType(label, valueType))
                    .getPutAttributeTypeRes().getAttributeType()).asAttributeType();
        }

        public RelationType.Remote putRelationType(String label) {
            return putRelationType(Label.of(label));
        }
        public RelationType.Remote putRelationType(Label label) {
            return Concept.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putRelationType(label))
                    .getPutRelationTypeRes().getRelationType()).asRelationType();
        }

        public Rule.Remote putRule(String label, Pattern when, Pattern then) {
            return putRule(Label.of(label), when, then);
        }
        public Rule.Remote putRule(Label label, Pattern when, Pattern then) {
            return Concept.Remote.of(this, sendAndReceiveOrThrow(RequestBuilder.Transaction.putRule(label, when, then))
                    .getPutRuleRes().getRule()).asRule();
        }

        public TransactionProto.Transaction.Res runConceptMethod(ConceptIID iid, ConceptProto.Method.Req method) {
            TransactionProto.Transaction.ConceptMethod.Req conceptMethod = TransactionProto.Transaction.ConceptMethod.Req.newBuilder()
                    .setIid(iid.getValue()).setMethod(method).build();
            TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder().setConceptMethodReq(conceptMethod).build();

            return sendAndReceiveOrThrow(request);
        }

        public <T> Stream<T> iterateConceptMethod(ConceptIID iid, ConceptProto.Method.Iter.Req method, Function<ConceptProto.Method.Iter.Res, T> responseReader) {
            TransactionProto.Transaction.ConceptMethod.Iter.Req conceptIterMethod = TransactionProto.Transaction.ConceptMethod.Iter.Req.newBuilder()
                    .setIid(iid.getValue()).setMethod(method).build();
            TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder().setConceptMethodIterReq(conceptIterMethod).build();

            return iterate(request, res -> responseReader.apply(res.getConceptMethodIterRes().getResponse()));
        }

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
                ConceptProto.Concept conceptProto = RequestBuilder.ConceptMessage.from(concept);
                conceptMapProto.putMap(var.name(), conceptProto);
            });
            conceptMapProto.setHasExplanation(conceptMap.hasExplanation());
            conceptMapProto.setPattern(conceptMap.queryPattern().toString());
            return conceptMapProto.build();
        }

        public <T> Stream<T> iterate(TransactionProto.Transaction.Iter.Req request, Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
            return Objects.requireNonNull(StreamSupport.stream(((Iterable<T>) () -> new RPCIterator<>(request, responseReader)).spliterator(), false));
        }

        public abstract class QueryFuture<T> implements Future<T> {
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

        private class QueryStreamFuture<T> extends QueryFuture<Stream<T>> {
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

        private class QueryExecuteFuture<T> extends QueryFuture<List<T>> {
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

        interface Option<T> {
        }

        public enum BooleanOption implements Option<Boolean> {
            INFER,
            EXPLAIN;
        }

        public enum BatchOption implements Option<TransactionProto.Transaction.Iter.Req.Options> {
            BATCH_SIZE;
        }

        public enum BatchSize {
            ALL;
        }

        private static class QueryOptionsImpl implements QueryOptions {
            private Map<Option<?>, Object> options;

            private QueryOptionsImpl() {
                options = new HashMap<>();
            }

            private QueryOptionsImpl(Map<Option<?>, Object> options) {
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

        public interface QueryOptions {
            QueryOptions infer(boolean infer);
            QueryOptions explain(boolean explain);
            QueryOptions batchSize(int size);
            QueryOptions batchSize(BatchSize batchSize);

            <T> QueryOptions set(Option<T> flag, T value);
            <T> QueryOptions whenSet(Option<T> option, Consumer<T> consumer);
        }

        public interface Options {
            QueryOptions DEFAULT = new QueryOptionsImpl();

            static QueryOptions infer(boolean infer) {
                return DEFAULT.infer(infer);
            }

            static QueryOptions explain(boolean explain) {
                return DEFAULT.explain(explain);
            }

            static QueryOptions batchSize(int size) {
                return DEFAULT.batchSize(size);
            }

            static QueryOptions batchSize(BatchSize batchSize) {
                return DEFAULT.batchSize(batchSize);
            }
        }
    }

    /**
     * Internal class used to handle database related operations
     */

    public static final class Databases {
        private final GraknGrpc.GraknBlockingStub blockingStub;

        Databases(ManagedChannel channel) {
            blockingStub = GraknGrpc.newBlockingStub(channel);
        }

        public boolean contains(String name) {
            try {
                return blockingStub.databaseContains(RequestBuilder.DatabaseMessage.contains(name)).getContains();
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }

        public void create(String name) {
            try {
                blockingStub.databaseCreate(RequestBuilder.DatabaseMessage.create(name));
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }

        public void delete(String name) {
            try {
                blockingStub.databaseDelete(RequestBuilder.DatabaseMessage.delete(name));
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }

        public List<String> all() {
            try {
                return ImmutableList.copyOf(blockingStub.databaseAll(RequestBuilder.DatabaseMessage.all()).getNamesList().iterator());
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }
    }

    /**
     * An identifier for an isolated scope of a data in the database.
     */
    public static class Database implements Serializable {

        private static final long serialVersionUID = 2726154016735929123L;
        public static final String DEFAULT = "grakn";

        private final String name;

        Database(String name) {
            if (name == null) {
                throw new NullPointerException("Null name");
            }
            this.name = name;
        }

        @CheckReturnValue
        public static Database of(String name) {
            return new Database(name);
        }

        @CheckReturnValue
        public String name() {
            return name;
        }

        public final String toString() {
            return name();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Database that = (Database) o;
            return this.name.equals(that.name);
        }

        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= this.name.hashCode();
            return h;
        }
    }
}
