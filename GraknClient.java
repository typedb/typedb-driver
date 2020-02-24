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
import grakn.client.concept.Attribute;
import grakn.client.concept.AttributeType;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptId;
import grakn.client.concept.EntityType;
import grakn.client.concept.Label;
import grakn.client.concept.RelationType;
import grakn.client.concept.Role;
import grakn.client.concept.Rule;
import grakn.client.concept.SchemaConcept;
import grakn.client.concept.ConceptImpl;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.currentThreadTrace;
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
                transceiver.send(RequestBuilder.Transaction.open(sessionId, type));
                responseOrThrow();
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

        public List<ConceptMap> execute(GraqlDefine query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.define")) {
                return executeInternal(query, true);
            }
        }

        public List<ConceptMap> execute(GraqlUndefine query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.undefine")) {
                return executeInternal(query, true);
            }
        }

        public List<ConceptMap> execute(GraqlInsert query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.insert")) {
                return executeInternal(query, infer);
            }
        }
        public List<ConceptMap> execute(GraqlInsert query) {
            return execute(query, true);
        }

        public List<Void> execute(GraqlDelete query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.delete")) {
                return executeInternal(query, infer);
            }
        }
        public List<Void> execute(GraqlDelete query) {
            return execute(query, true);
        }

        public List<ConceptMap> execute(GraqlGet query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get")) {
                return executeInternal(query, infer);
            }
        }
        public List<ConceptMap> execute(GraqlGet query) {
            return execute(query, true);
        }

        public Stream<ConceptMap> stream(GraqlDefine query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.define")) {
                return streamInternal(query, true);
            }
        }

        public Stream<ConceptMap> stream(GraqlUndefine query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.undefine")) {
                return streamInternal(query, true);
            }
        }

        public Stream<ConceptMap> stream(GraqlInsert query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.insert")) {
                return streamInternal(query, infer);
            }
        }
        public Stream<ConceptMap> stream(GraqlInsert query) {
            return stream(query, true);
        }

        public Stream<Void> stream(GraqlDelete query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.delete")) {
                return streamInternal(query, infer);
            }
        }
        public Stream<Void> stream(GraqlDelete query) {
            return stream(query, true);
        }

        public Stream<ConceptMap> stream(GraqlGet query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get")) {
                return streamInternal(query, infer);
            }
        }
        public Stream<ConceptMap> stream(GraqlGet query) {
            return stream(query, true);
        }

        // Aggregate Query

        public List<Numeric> execute(GraqlGet.Aggregate query) {
            return execute(query, true);
        }

        public List<Numeric> execute(GraqlGet.Aggregate query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.aggregate")) {
                return executeInternal(query, infer);
            }
        }

        public Stream<Numeric> stream(GraqlGet.Aggregate query) {
            return stream(query, true);
        }

        public Stream<Numeric> stream(GraqlGet.Aggregate query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.aggregate")) {
                return streamInternal(query, infer);
            }
        }

        // Group Query

        public List<AnswerGroup<ConceptMap>> execute(GraqlGet.Group query) {
            return execute(query, true);
        }

        public List<AnswerGroup<ConceptMap>> execute(GraqlGet.Group query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.group")) {
                return executeInternal(query, infer);
            }
        }

        public Stream<AnswerGroup<ConceptMap>> stream(GraqlGet.Group query) {
            return stream(query, true);
        }

        public Stream<AnswerGroup<ConceptMap>> stream(GraqlGet.Group query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.group")) {
                return streamInternal(query, infer);
            }
        }


        // Group Aggregate Query

        public List<AnswerGroup<Numeric>> execute(GraqlGet.Group.Aggregate query) {
            return execute(query, true);
        }

        public List<AnswerGroup<Numeric>> execute(GraqlGet.Group.Aggregate query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.execute.get.group.aggregate")) {
                return executeInternal(query, infer);
            }
        }


        public Stream<AnswerGroup<Numeric>> stream(GraqlGet.Group.Aggregate query) {
            return stream(query, true);
        }

        public Stream<AnswerGroup<Numeric>> stream(GraqlGet.Group.Aggregate query, boolean infer) {
            try (ThreadTrace trace = traceOnThread("tx.stream.get.group.aggregate")) {
                return streamInternal(query, infer);
            }
        }

        // Compute Query

        public List<Numeric> execute(GraqlCompute.Statistics query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.statistics")) {
                return executeInternal(query, false);
            }
        }

        public Stream<Numeric> stream(GraqlCompute.Statistics query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.statistics")) {
                return streamInternal(query, false);
            }
        }

        public List<ConceptList> execute(GraqlCompute.Path query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.path")) {
                return executeInternal(query, false);
            }
        }

        public Stream<ConceptList> stream(GraqlCompute.Path query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.path")) {
                return streamInternal(query, false);
            }
        }

        public List<ConceptSetMeasure> execute(GraqlCompute.Centrality query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.centrality")) {
                return executeInternal(query, false);
            }
        }

        public Stream<ConceptSetMeasure> stream(GraqlCompute.Centrality query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.centrality")) {
                return streamInternal(query, false);
            }
        }

        public List<ConceptSet> execute(GraqlCompute.Cluster query) {
            try (ThreadTrace trace = traceOnThread("tx.execute.compute.cluster")) {
                return executeInternal(query, false);
            }
        }

        public Stream<ConceptSet> stream(GraqlCompute.Cluster query) {
            try (ThreadTrace trace = traceOnThread("tx.stream.compute.cluster")) {
                return streamInternal(query, false);
            }
        }

        // Generic queries

        public List<? extends Answer> execute(GraqlQuery query) {
            return execute(query, true);
        }

        public List<? extends Answer> execute(GraqlQuery query, boolean infer) {
            if (query instanceof GraqlDefine) {
                return execute((GraqlDefine) query);

            } else if (query instanceof GraqlUndefine) {
                return execute((GraqlUndefine) query);

            } else if (query instanceof GraqlInsert) {
                return execute((GraqlInsert) query, infer);

            } else if (query instanceof GraqlDelete) {
                return execute((GraqlDelete) query, infer);

            } else if (query instanceof GraqlGet) {
                return execute((GraqlGet) query, infer);

            } else if (query instanceof GraqlGet.Aggregate) {
                return execute((GraqlGet.Aggregate) query, infer);

            } else if (query instanceof GraqlGet.Group.Aggregate) {
                return execute((GraqlGet.Group.Aggregate) query, infer);

            } else if (query instanceof GraqlGet.Group) {
                return execute((GraqlGet.Group) query, infer);

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

        public Stream<? extends Answer> stream(GraqlQuery query) {
            return stream(query, true);
        }

        public Stream<? extends Answer> stream(GraqlQuery query, boolean infer) {
            if (query instanceof GraqlDefine) {
                return stream((GraqlDefine) query);

            } else if (query instanceof GraqlUndefine) {
                return stream((GraqlUndefine) query);

            } else if (query instanceof GraqlInsert) {
                return stream((GraqlInsert) query, infer);

            } else if (query instanceof GraqlDelete) {
                return stream((GraqlDelete) query, infer);

            } else if (query instanceof GraqlGet) {
                return stream((GraqlGet) query, infer);

            } else if (query instanceof GraqlGet.Aggregate) {
                return stream((GraqlGet.Aggregate) query, infer);

            } else if (query instanceof GraqlGet.Group.Aggregate) {
                return stream((GraqlGet.Group.Aggregate) query, infer);

            } else if (query instanceof GraqlGet.Group) {
                return stream((GraqlGet.Group) query, infer);

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

        private <T extends Answer> List<T> executeInternal(GraqlQuery query, boolean infer) {
            return (List<T>) streamInternal(query, infer).collect(Collectors.toList());
        }

        private <T extends Answer> Stream<T> streamInternal(GraqlQuery query, boolean infer) {
            Iterable<T> iterable = () -> this.rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }

        private <T extends Answer> Iterator<T> rpcIterator(GraqlQuery query, boolean infer) {
            transceiver.send(RequestBuilder.Transaction.query(query.toString(), infer));
            SessionProto.Transaction.Res txResponse = responseOrThrow();
            int iteratorId = txResponse.getQueryIter().getId();
            return new RPCIterator<>(
                    this,
                    iteratorId,
                    response -> (T) ResponseReader.answer(response.getQueryIterRes().getAnswer(), this)
            );
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

        private SessionProto.Transaction.Res responseOrThrow() {
            Transceiver.Response response;

            try {
                response = transceiver.receive();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // This is called from classes like Transaction, that impl methods which do not throw InterruptedException
                // Therefore, we have to wrap it in a RuntimeException.
                throw new RuntimeException(e);
            }

            switch (response.type()) {
                case OK:
                    return response.ok();
                case ERROR:
                    // TODO: parse different GRPC errors into specific GraknClientException
                    throw GraknClientException.create(response.error().getMessage(), response.error());
                case COMPLETED:
                    // This will occur when interrupting a running query/operation on the current transaction
                    throw GraknClientException.create("Transaction interrupted, all running queries have been stopped.");
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }

        public void commit() {
            transceiver.send(RequestBuilder.Transaction.commit());
            responseOrThrow();
            close();
        }

        @Nullable
        public <T extends grakn.client.concept.Type> T getType(Label label) {
            SchemaConcept concept = getSchemaConcept(label);
            if (concept == null || !concept.isType()) return null;
            return (T) concept.asType();
        }

        @Nullable
        public EntityType getEntityType(String label) {
            SchemaConcept concept = getSchemaConcept(Label.of(label));
            if (concept == null || !concept.isEntityType()) return null;
            return concept.asEntityType();
        }

        @Nullable
        public RelationType getRelationType(String label) {
            SchemaConcept concept = getSchemaConcept(Label.of(label));
            if (concept == null || !concept.isRelationType()) return null;
            return concept.asRelationType();
        }

        @Nullable
        public <V> AttributeType<V> getAttributeType(String label) {
            SchemaConcept concept = getSchemaConcept(Label.of(label));
            if (concept == null || !concept.isAttributeType()) return null;
            return concept.asAttributeType();
        }

        @Nullable
        public Role getRole(String label) {
            SchemaConcept concept = getSchemaConcept(Label.of(label));
            if (concept == null || !concept.isRole()) return null;
            return concept.asRole();
        }

        @Nullable
        public Rule getRule(String label) {
            SchemaConcept concept = getSchemaConcept(Label.of(label));
            if (concept == null || !concept.isRule()) return null;
            return concept.asRule();
        }

        @Nullable
        public <T extends SchemaConcept> T getSchemaConcept(Label label) {
            transceiver.send(RequestBuilder.Transaction.getSchemaConcept(label));
            SessionProto.Transaction.Res response = responseOrThrow();
            switch (response.getGetSchemaConceptRes().getResCase()) {
                case NULL:
                    return null;
                default:
                    return (T) ConceptImpl.of(response.getGetSchemaConceptRes().getSchemaConcept(), this).asSchemaConcept();
            }
        }

        public grakn.client.concept.Type getMetaConcept() {
            return getSchemaConcept(Label.of(Graql.Token.Type.THING.toString()));
        }

        public RelationType getMetaRelationType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.RELATION.toString()));
        }

        public Role getMetaRole() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ROLE.toString()));
        }

        public AttributeType getMetaAttributeType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ATTRIBUTE.toString()));
        }

        public EntityType getMetaEntityType() {
            return getSchemaConcept(Label.of(Graql.Token.Type.ENTITY.toString()));
        }

        public Rule getMetaRule() {
            return getSchemaConcept(Label.of(Graql.Token.Type.RULE.toString()));
        }

        @Nullable
        public <T extends Concept> T getConcept(ConceptId id) {
            transceiver.send(RequestBuilder.Transaction.getConcept(id));
            SessionProto.Transaction.Res response = responseOrThrow();
            switch (response.getGetConceptRes().getResCase()) {
                case NULL:
                    return null;
                default:
                    return (T) ConceptImpl.of(response.getGetConceptRes().getConcept(), this);
            }
        }

        public <V> Collection<Attribute<V>> getAttributesByValue(V value) {
            transceiver.send(RequestBuilder.Transaction.getAttributes(value));
            int iteratorId = responseOrThrow().getGetAttributesIter().getId();
            Iterable<Attribute<V>> iterable = () -> new RPCIterator<Attribute<V>>(
                    this, iteratorId, response -> ConceptImpl.of(response.getGetAttributesIterRes().getAttribute(), this).asAttribute()
            );

            return StreamSupport.stream(iterable.spliterator(), false)
                    .collect(Collectors.toSet());
        }


        public EntityType putEntityType(String label) {
            return putEntityType(Label.of(label));
        }

        public EntityType putEntityType(Label label) {
            transceiver.send(RequestBuilder.Transaction.putEntityType(label));
            return ConceptImpl.of(responseOrThrow().getPutEntityTypeRes().getEntityType(), this).asEntityType();
        }

        public <V> AttributeType<V> putAttributeType(String label, AttributeType.DataType<V> dataType) {
            return putAttributeType(Label.of(label), dataType);
        }
        public <V> AttributeType<V> putAttributeType(Label label, AttributeType.DataType<V> dataType) {
            transceiver.send(RequestBuilder.Transaction.putAttributeType(label, dataType));
            return ConceptImpl.of(responseOrThrow().getPutAttributeTypeRes().getAttributeType(), this).asAttributeType();
        }

        public RelationType putRelationType(String label) {
            return putRelationType(Label.of(label));
        }
        public RelationType putRelationType(Label label) {
            transceiver.send(RequestBuilder.Transaction.putRelationType(label));
            return ConceptImpl.of(responseOrThrow().getPutRelationTypeRes().getRelationType(), this).asRelationType();
        }

        public Role putRole(String label) {
            return putRole(Label.of(label));
        }
        public Role putRole(Label label) {
            transceiver.send(RequestBuilder.Transaction.putRole(label));
            return ConceptImpl.of(responseOrThrow().getPutRoleRes().getRole(), this).asRole();
        }

        public Rule putRule(String label, Pattern when, Pattern then) {
            return putRule(Label.of(label), when, then);
        }
        public Rule putRule(Label label, Pattern when, Pattern then) {
            transceiver.send(RequestBuilder.Transaction.putRule(label, when, then));
            return ConceptImpl.of(responseOrThrow().getPutRuleRes().getRule(), this).asRule();
        }

        public Stream<SchemaConcept> sups(SchemaConcept schemaConcept) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptSupsReq(ConceptProto.SchemaConcept.Sups.Req.getDefaultInstance()).build();

            SessionProto.Transaction.Res response = runConceptMethod(schemaConcept.id(), method);
            int iteratorId = response.getConceptMethodRes().getResponse().getSchemaConceptSupsIter().getId();

            Iterable<? extends Concept> iterable = () -> new RPCIterator<>(
                    this, iteratorId, res -> ConceptImpl.of(res.getConceptMethodIterRes().getSchemaConceptSupsIterRes().getSchemaConcept(), this)
            );

            Stream<? extends Concept> sups = StreamSupport.stream(iterable.spliterator(), false);
            return Objects.requireNonNull(sups).map(Concept::asSchemaConcept);
        }

        public SessionProto.Transaction.Res runConceptMethod(ConceptId id, ConceptProto.Method.Req method) {
            SessionProto.Transaction.ConceptMethod.Req conceptMethod = SessionProto.Transaction.ConceptMethod.Req.newBuilder()
                    .setId(id.getValue()).setMethod(method).build();
            SessionProto.Transaction.Req request = SessionProto.Transaction.Req.newBuilder().setConceptMethodReq(conceptMethod).build();

            transceiver.send(request);
            return responseOrThrow();
        }

        public Explanation getExplanation(ConceptMap explainable) {
            AnswerProto.ConceptMap conceptMapProto = conceptMap(explainable);
            AnswerProto.Explanation.Req explanationReq = AnswerProto.Explanation.Req.newBuilder().setExplainable(conceptMapProto).build();
            SessionProto.Transaction.Req request = SessionProto.Transaction.Req.newBuilder().setExplanationReq(explanationReq).build();
            transceiver.send(request);
            SessionProto.Transaction.Res response = responseOrThrow();
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

        private SessionProto.Transaction.Iter.Res iterate(int iteratorId) {
            try (ThreadTrace trace = traceOnThread("iterate")) {
                transceiver.send(RequestBuilder.Transaction.iterate(iteratorId));
                return responseOrThrow().getIterateRes();
            }
        }

        public <T> RPCIterator<T> iterator(int iteratorId, Function<SessionProto.Transaction.Iter.Res, T> responseReader) {
            return new RPCIterator<>(this, iteratorId, responseReader);
        }

        /**
         * A client-side iterator over gRPC messages. Will send SessionProto.Transaction.Iter.Req messages until
         * SessionProto.Transaction.Iter.Res returns done as a message.
         *
         * @param <T> class type of objects being iterated
         */
        public class RPCIterator<T> extends AbstractIterator<T> {
            private final ThreadTrace parentTrace;
            private final int iteratorId;
            private Transaction tx;
            private Function<SessionProto.Transaction.Iter.Res, T> responseReader;

            private RPCIterator(Transaction tx, int iteratorId, Function<SessionProto.Transaction.Iter.Res, T> responseReader) {
                this.tx = tx;
                this.iteratorId = iteratorId;
                this.responseReader = responseReader;

                parentTrace = currentThreadTrace();
            }


            protected final T computeNext() {
                SessionProto.Transaction.Iter.Res response = tx.iterate(iteratorId);

                switch (response.getResCase()) {
                    case DONE:
                        return endOfData();
                    case RES_NOT_SET:
                        throw GraknClientException.unreachableStatement("Unexpected " + response);
                    default:
                        return responseReader.apply(response);
                }
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
