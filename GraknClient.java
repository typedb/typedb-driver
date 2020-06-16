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
import grakn.client.concept.type.Role;
import grakn.client.concept.Rule;
import grakn.client.concept.SchemaConcept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.client.rpc.ResponseReader;
import grakn.client.rpc.Transceiver;
import grakn.protocol.keyspace.KeyspaceProto;
import grakn.protocol.keyspace.KeyspaceServiceGrpc;
import grakn.protocol.keyspace.KeyspaceServiceGrpc.KeyspaceServiceBlockingStub;
import grakn.protocol.session.AnswerProto;
import grakn.protocol.session.ConceptProto;
import grakn.protocol.session.SessionProto;
import grakn.protocol.session.SessionServiceGrpc;
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
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

/**
 * Entry-point which communicates with a running Grakn server using gRPC.
 */
public class GraknClient implements AutoCloseable {

    public static final String DEFAULT_URI = "localhost:48555";

    private ManagedChannel channel;
    private String username;
    private String password;
    private Keyspaces keyspaces;

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
        keyspaces = new Keyspaces(channel, this.username, this.password);
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

    public Session session(String keyspace) {
        return new Session(channel, username, password, keyspace);
    }

    public Keyspaces keyspaces() {
        return keyspaces;
    }

    /**
     * @see Transaction
     * @see GraknClient
     */
    public static class Session implements AutoCloseable {

        protected ManagedChannel channel;
        private String username; // TODO: Do we need to save this? It's not used.
        private String password; // TODO: Do we need to save this? It's not used.
        protected String keyspace;
        protected SessionServiceGrpc.SessionServiceBlockingStub sessionStub;
        protected String sessionId;
        protected boolean isOpen;

        private Session(ManagedChannel channel, String username, String password, String keyspace) {
            this.username = username;
            this.password = password;
            this.keyspace = keyspace;
            this.channel = channel;
            this.sessionStub = SessionServiceGrpc.newBlockingStub(channel);

            SessionProto.Session.Open.Req.Builder open = RequestBuilder.Session.open(keyspace).newBuilderForType();
            if (username != null) {
                open = open.setUsername(username);
            }
            if (password != null) {
                open = open.setPassword(password);
            }
            open = open.setKeyspace(keyspace);

            SessionProto.Session.Open.Res response = sessionStub.open(open.build());
            sessionId = response.getSessionId();
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
            sessionStub.close(RequestBuilder.Session.close(sessionId));
            isOpen = false;
        }

        public Keyspace keyspace() {
            return Keyspace.of(keyspace);
        }
    }

    public static class Transaction implements AutoCloseable {
        private final Session session;
        private final Type type;
        private final Transceiver transceiver;

        private int currentIteratorId = 1;

        public static class Builder {

            private ManagedChannel channel;
            private GraknClient.Session session;
            private String sessionId;

            public Builder(ManagedChannel channel, GraknClient.Session session, String sessionId) {
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

            public int id() {
                return type;
            }

            @Override
            public String toString() {
                return this.name();
            }

            public static Type of(int value) {
                for (Type t : Type.values()) {
                    if (t.type == value) return t;
                }
                return null;
            }

            public static Type of(String value) {
                for (Type t : Type.values()) {
                    if (t.name().equalsIgnoreCase(value)) return t;
                }
                return null;
            }
        }

        private Transaction(ManagedChannel channel, Session session, String sessionId, Type type) {
            try (ThreadTrace trace = traceOnThread(type == Type.WRITE ? "tx.write" : "tx.read")) {
                this.transceiver = Transceiver.create(SessionServiceGrpc.newStub(channel));
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

        public Keyspace keyspace() {
            return session.keyspace();
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
                return execute((GraqlDefine) query, options);

            } else if (query instanceof GraqlUndefine) {
                return execute((GraqlUndefine) query, options);

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
                return execute((GraqlCompute.Statistics) query, options);

            } else if (query instanceof GraqlCompute.Path) {
                return execute((GraqlCompute.Path) query, options);

            } else if (query instanceof GraqlCompute.Centrality) {
                return execute((GraqlCompute.Centrality) query, options);

            } else if (query instanceof GraqlCompute.Cluster) {
                return execute((GraqlCompute.Cluster) query, options);

            } else {
                throw new IllegalArgumentException("Unrecognised Query object");
            }
        }

        public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query) {
            return stream(query, Options.DEFAULT);
        }

        public QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query, QueryOptions options) {
            if (query instanceof GraqlDefine) {
                return stream((GraqlDefine) query, options);

            } else if (query instanceof GraqlUndefine) {
                return stream((GraqlUndefine) query, options);

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
                return stream((GraqlCompute.Statistics) query, options);

            } else if (query instanceof GraqlCompute.Path) {
                return stream((GraqlCompute.Path) query, options);

            } else if (query instanceof GraqlCompute.Centrality) {
                return stream((GraqlCompute.Centrality) query, options);

            } else if (query instanceof GraqlCompute.Cluster) {
                return stream((GraqlCompute.Cluster) query, options);

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

        private SessionProto.Transaction.Res sendAndReceiveOrThrow(SessionProto.Transaction.Req request) {
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

        @Nullable
        public grakn.client.concept.type.Type.Remote<?, ?> getType(Label label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(label);
            if (concept instanceof grakn.client.concept.type.Type.Remote) {
                return (grakn.client.concept.type.Type.Remote<?, ?>) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public EntityType.Remote getEntityType(String label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(Label.of(label));
            if (concept instanceof EntityType.Remote) {
                return (EntityType.Remote) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public RelationType.Remote getRelationType(String label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(Label.of(label));
            if (concept instanceof RelationType.Remote) {
                return (RelationType.Remote) concept;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public <V> AttributeType.Remote<V> getAttributeType(String label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(Label.of(label));
            if (concept instanceof AttributeType.Remote) {
                return (AttributeType.Remote<V>) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public Role.Remote getRole(String label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(Label.of(label));
            if (concept instanceof Role.Remote) {
                return (Role.Remote) concept;
            } else {
                return null;
            }
        }

        @Nullable
        public Rule.Remote getRule(String label) {
            SchemaConcept.Remote<?> concept = getSchemaConcept(Label.of(label));
            if (concept instanceof Rule.Remote) {
                return (Rule.Remote) concept;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public SchemaConcept.Remote<?> getSchemaConcept(Label label) {
            SessionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getSchemaConcept(label));
            switch (response.getGetSchemaConceptRes().getResCase()) {
                case NULL:
                    return null;
                case SCHEMACONCEPT:
                    return Concept.Remote.of(response.getGetSchemaConceptRes().getSchemaConcept(), this).asSchemaConcept();
                default:
                    throw GraknClientException.resultNotPresent();
            }
        }

        public SchemaConcept.Remote<?> getMetaConcept() {
            return getSchemaConcept(Label.of(Graql.Token.Type.THING.toString()));
        }

        public RelationType.Remote getMetaRelationType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.RELATION.toString())).asRelationType();
        }

        public Role.Remote getMetaRole() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ROLE.toString())).asRole();
        }

        public AttributeType.Remote<?> getMetaAttributeType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ATTRIBUTE.toString())).asAttributeType();
        }

        public EntityType.Remote getMetaEntityType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ENTITY.toString())).asEntityType();
        }

        public Rule.Remote getMetaRule() {
            return getSchemaConcept(Label.of(Graql.Token.Type.RULE.toString())).asRule();
        }

        @Nullable
        public Concept.Remote<?> getConcept(ConceptId id) {
            SessionProto.Transaction.Res response = sendAndReceiveOrThrow(RequestBuilder.Transaction.getConcept(id));
            switch (response.getGetConceptRes().getResCase()) {
                case NULL:
                    return null;
                case CONCEPT:
                    return Concept.Remote.of(response.getGetConceptRes().getConcept(), this);
                default:
                    throw GraknClientException.resultNotPresent();
            }
        }

        @SuppressWarnings("unchecked")
        public <V> Collection<Attribute.Remote<V>> getAttributesByValue(V value) {
            return iterate(RequestBuilder.Transaction.getAttributes(value),
                    response -> (Attribute.Remote<V>) Concept.Remote.of(response.getGetAttributesIterRes().getAttribute(), this).asAttribute()).collect(Collectors.toSet());
        }

        public EntityType.Remote putEntityType(String label) {
            return putEntityType(Label.of(label));
        }

        public EntityType.Remote putEntityType(Label label) {
            return Concept.Remote.of(sendAndReceiveOrThrow(RequestBuilder.Transaction.putEntityType(label)).getPutEntityTypeRes().getEntityType(), this).asEntityType();
        }

        public <V> AttributeType.Remote<V> putAttributeType(String label, ValueType<V> valueType) {
            return putAttributeType(Label.of(label), valueType);
        }
        @SuppressWarnings("unchecked")
        public <V> AttributeType.Remote<V> putAttributeType(Label label, ValueType<V> valueType) {
            return (AttributeType.Remote<V>) Concept.Remote.of(sendAndReceiveOrThrow(RequestBuilder.Transaction.putAttributeType(label, valueType))
                    .getPutAttributeTypeRes().getAttributeType(), this).asAttributeType();
        }

        public RelationType.Remote putRelationType(String label) {
            return putRelationType(Label.of(label));
        }
        public RelationType.Remote putRelationType(Label label) {
            return Concept.Remote.of(sendAndReceiveOrThrow(RequestBuilder.Transaction.putRelationType(label))
                    .getPutRelationTypeRes().getRelationType(), this).asRelationType();
        }

        public Role.Remote putRole(String label) {
            return putRole(Label.of(label));
        }
        public Role.Remote putRole(Label label) {
            return Concept.Remote.of(sendAndReceiveOrThrow(RequestBuilder.Transaction.putRole(label))
                    .getPutRoleRes().getRole(), this).asRole();
        }

        public Rule.Remote putRule(String label, Pattern when, Pattern then) {
            return putRule(Label.of(label), when, then);
        }
        public Rule.Remote putRule(Label label, Pattern when, Pattern then) {
            return Concept.Remote.of(sendAndReceiveOrThrow(RequestBuilder.Transaction.putRule(label, when, then))
                    .getPutRuleRes().getRule(), this).asRule();
        }

        public Stream<SchemaConcept.Remote<?>> sups(SchemaConcept.Remote<?> schemaConcept) {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setSchemaConceptSupsIterReq(ConceptProto.SchemaConcept.Sups.Iter.Req.getDefaultInstance()).build();

            return iterateConceptMethod(schemaConcept.id(), method,
                    res -> Concept.Remote.of(res.getSchemaConceptSupsIterRes().getSchemaConcept(), this).asSchemaConcept());
        }

        public SessionProto.Transaction.Res runConceptMethod(ConceptId id, ConceptProto.Method.Req method) {
            SessionProto.Transaction.ConceptMethod.Req conceptMethod = SessionProto.Transaction.ConceptMethod.Req.newBuilder()
                    .setId(id.getValue()).setMethod(method).build();
            SessionProto.Transaction.Req request = SessionProto.Transaction.Req.newBuilder().setConceptMethodReq(conceptMethod).build();

            return sendAndReceiveOrThrow(request);
        }

        public <T> Stream<T> iterateConceptMethod(ConceptId id, ConceptProto.Method.Iter.Req method, Function<ConceptProto.Method.Iter.Res, T> responseReader) {
            SessionProto.Transaction.ConceptMethod.Iter.Req conceptIterMethod = SessionProto.Transaction.ConceptMethod.Iter.Req.newBuilder()
                    .setId(id.getValue()).setMethod(method).build();
            SessionProto.Transaction.Iter.Req request = SessionProto.Transaction.Iter.Req.newBuilder().setConceptMethodIterReq(conceptIterMethod).build();

            return iterate(request, res -> responseReader.apply(res.getConceptMethodIterRes().getResponse()));
        }

        public Explanation getExplanation(ConceptMap explainable) {
            AnswerProto.ConceptMap conceptMapProto = conceptMap(explainable);
            AnswerProto.Explanation.Req explanationReq = AnswerProto.Explanation.Req.newBuilder().setExplainable(conceptMapProto).build();
            SessionProto.Transaction.Req request = SessionProto.Transaction.Req.newBuilder().setExplanationReq(explanationReq).build();
            SessionProto.Transaction.Res response = sendAndReceiveOrThrow(request);
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

        public <T> Stream<T> iterate(SessionProto.Transaction.Iter.Req request, Function<SessionProto.Transaction.Iter.Res, T> responseReader) {
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
         * A client-side iterator over gRPC messages. Will send SessionProto.Transaction.Iter.Req messages until
         * SessionProto.Transaction.Iter.Res returns done as a message.
         *
         * @param <T> class type of objects being iterated
         */
        public class RPCIterator<T> extends AbstractIterator<T> {
            private Function<SessionProto.Transaction.Iter.Res, T> responseReader;
            private Batch currentBatch;
            private volatile boolean started;
            private SessionProto.Transaction.Iter.Res first;
            private SessionProto.Transaction.Iter.Req.Options options;

            private RPCIterator(SessionProto.Transaction.Iter.Req req,
                                Function<SessionProto.Transaction.Iter.Res, T> responseReader) {
                this.responseReader = responseReader;
                options = req.getOptions();
                sendRequest(req);
            }

            private void sendRequest(SessionProto.Transaction.Iter.Req req) {
                currentBatch = new Batch();

                SessionProto.Transaction.Req transactionReq = SessionProto.Transaction.Req.newBuilder()
                        .setIterReq(req).build();

                transceiver.sendAndReceiveMultipleAsync(transactionReq, currentBatch);
            }

            private void nextBatch(int iteratorId) {
                SessionProto.Transaction.Iter.Req iterReq = SessionProto.Transaction.Iter.Req.newBuilder()
                        .setIteratorId(iteratorId)
                        .setOptions(options)
                        .build();

                sendRequest(iterReq);
            }

            private class Batch extends Transceiver.MultiResponseCollector {
                @Override
                protected boolean isLastResponse(SessionProto.Transaction.Res response) {
                    SessionProto.Transaction.Iter.Res iterRes = response.getIterRes();
                    return iterRes.getIteratorId() != 0 || iterRes.getDone();
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
                    SessionProto.Transaction.Iter.Res iterRes = first;
                    first = null;
                    return responseReader.apply(iterRes);
                }

                SessionProto.Transaction.Iter.Res res;
                try {
                    res = currentBatch.take().getIterRes();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                started = true;
                switch (res.getResCase()) {
                    case ITERATORID:
                        nextBatch(res.getIteratorId());
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

        public enum IterOption implements Option<SessionProto.Transaction.Iter.Req.Options> {
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
                return set(IterOption.BATCH_SIZE, SessionProto.Transaction.Iter.Req.Options.newBuilder().setNumber(size).build());
            }

            @Override
            public QueryOptions batchSize(BatchSize batchSize) {
                if (batchSize == BatchSize.ALL) {
                    return set(IterOption.BATCH_SIZE, SessionProto.Transaction.Iter.Req.Options.newBuilder().setAll(true).build());
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
     * Internal class used to handle keyspace related operations
     */

    public static final class Keyspaces {
        private String username;
        private String password;

        private KeyspaceServiceBlockingStub keyspaceBlockingStub;

        Keyspaces(ManagedChannel channel, String username, String password) {
            keyspaceBlockingStub = KeyspaceServiceGrpc.newBlockingStub(channel);
            this.username = username;
            this.password = password;
        }

        public void delete(String name) {
            try {
                KeyspaceProto.Keyspace.Delete.Req request = RequestBuilder.KeyspaceMessage.delete(name, this.username, this.password);
                keyspaceBlockingStub.delete(request);
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }

        public List<String> retrieve() {
            try {
                KeyspaceProto.Keyspace.Retrieve.Req request = RequestBuilder.KeyspaceMessage.retrieve(this.username, this.password);
                return ImmutableList.copyOf(keyspaceBlockingStub.retrieve(request).getNamesList().iterator());
            } catch (StatusRuntimeException e) {
                throw GraknClientException.create(e.getMessage(), e);
            }
        }
    }

    /**
     * An identifier for an isolated scope of a data in the database.
     */
    public static class Keyspace implements Serializable {

        private static final long serialVersionUID = 2726154016735929123L;
        public static final String DEFAULT = "grakn";

        private final String name;

        Keyspace(String name) {
            if (name == null) {
                throw new NullPointerException("Null name");
            }
            this.name = name;
        }

        @CheckReturnValue
        public static Keyspace of(String name) {
            return new Keyspace(name);
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

            Keyspace that = (Keyspace) o;
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
