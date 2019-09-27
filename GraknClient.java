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
import grakn.client.concept.answer.AnswerGroup;
import grakn.client.concept.answer.ConceptList;
import grakn.client.concept.answer.ConceptMap;
import grakn.client.concept.answer.ConceptSet;
import grakn.client.concept.answer.ConceptSetMeasure;
import grakn.client.concept.answer.Numeric;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.client.rpc.ResponseReader;
import grakn.client.rpc.Transceiver;
import grakn.protocol.keyspace.KeyspaceProto;
import grakn.protocol.keyspace.KeyspaceServiceGrpc;
import grakn.protocol.keyspace.KeyspaceServiceGrpc.KeyspaceServiceBlockingStub;
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
    public static class Session {

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

        public static class Builder implements grakn.core.api.Transaction.Builder {

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
        }

        private Transaction(ManagedChannel channel, Session session, String sessionId, Type type) {
            this.transceiver = Transceiver.create(SessionServiceGrpc.newStub(channel));
            this.session = session;
            this.type = type;
            transceiver.send(RequestBuilder.Transaction.open(sessionId, type));
            responseOrThrow();
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
            return stream(query).collect(Collectors.toList());
        }

        public List<ConceptMap> execute(GraqlUndefine query) {
            return stream(query).collect(Collectors.toList());
        }

        public List<ConceptMap> execute(GraqlInsert query, boolean infer) {
            return stream(query, infer).collect(Collectors.toList());
        }
        public List<ConceptMap> execute(GraqlInsert query) {
            return stream(query).collect(Collectors.toList());
        }

        public List<ConceptSet> execute(GraqlDelete query, boolean infer) {
            return stream(query, infer).collect(Collectors.toList());
        }
        public List<ConceptSet> execute(GraqlDelete query) {
            return stream(query).collect(Collectors.toList());
        }

        public List<ConceptMap> execute(GraqlGet query, boolean infer) {
            return stream(query, infer).collect(Collectors.toList());
        }
        public List<ConceptMap> execute(GraqlGet query) {
            return stream(query).collect(Collectors.toList());
        }

        public Stream<ConceptMap> stream(GraqlDefine query) {
            Iterable<ConceptMap> iterable = () -> this.rpcIterator(query);
            return StreamSupport.stream(iterable.spliterator(), false);
        }

        public Stream<ConceptMap> stream(GraqlUndefine query) {
            Iterable<ConceptMap> iterable = () -> this.rpcIterator(query);
            return StreamSupport.stream(iterable.spliterator(), false);
        }

        public Stream<ConceptMap> stream(GraqlInsert query, boolean infer) {
            Iterable<ConceptMap> iterable = () -> this.rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }
        public Stream<ConceptMap> stream(GraqlInsert query) {
            return stream(query, true);
        }

        public Stream<ConceptSet> stream(GraqlDelete query, boolean infer) {
            Iterable<ConceptSet> iterable = () -> this.rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }
        public Stream<ConceptSet> stream(GraqlDelete query) {
            return stream(query, true);
        }

        public Stream<ConceptMap> stream(GraqlGet query, boolean infer) {
            Iterable<ConceptMap> iterable = () -> this.rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }
        public Stream<ConceptMap> stream(GraqlGet query) {
            return stream(query, true);
        }

        private Iterator rpcIterator(GraqlQuery query) {
            return rpcIterator(query, true);
        }

        private Iterator rpcIterator(GraqlQuery query, boolean infer) {
            transceiver.send(RequestBuilder.Transaction.query(query.toString(), infer));
            SessionProto.Transaction.Res txResponse = responseOrThrow();
            int iteratorId = txResponse.getQueryIter().getId();
            return new RPCIterator<>(
                    this,
                    iteratorId,
                    response -> ResponseReader.answer(response.getQueryIterRes().getAnswer(), this)
            );
        }

        public void abort() {
            close();
        }

        public void close() {
            transceiver.close();
        }

        public boolean isOpen() {
            return transceiver.isOpen();
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
                    return (T) Concept.of(response.getGetSchemaConceptRes().getSchemaConcept(), this).asSchemaConcept();
            }
        }

        public SchemaConcept getMetaConcept() {
            return getSchemaConcept(Label.of(Graql.Token.Type.THING.toString()));
        }

        @Nullable
        public <T extends Concept> T getConcept(ConceptId id) {
            transceiver.send(RequestBuilder.Transaction.getConcept(id));
            SessionProto.Transaction.Res response = responseOrThrow();
            switch (response.getGetConceptRes().getResCase()) {
                case NULL:
                    return null;
                default:
                    return (T) Concept.of(response.getGetConceptRes().getConcept(), this);
            }
        }

        public <V> Collection<Attribute<V>> getAttributesByValue(V value) {
            transceiver.send(RequestBuilder.Transaction.getAttributes(value));
            int iteratorId = responseOrThrow().getGetAttributesIter().getId();
            Iterable<Attribute<V>> iterable = () -> new RPCIterator<Attribute<V>>(
                    this, iteratorId, response -> Concept.of(response.getGetAttributesIterRes().getAttribute(), this).asAttribute()
            );

            return StreamSupport.stream(iterable.spliterator(), false)
                    .collect(Collectors.toSet());
        }

        public EntityType putEntityType(Label label) {
            transceiver.send(RequestBuilder.Transaction.putEntityType(label));
            return Concept.of(responseOrThrow().getPutEntityTypeRes().getEntityType(), this).asEntityType();
        }


        public <V> AttributeType<V> putAttributeType(Label label, AttributeType.DataType<V> dataType) {
            transceiver.send(RequestBuilder.Transaction.putAttributeType(label, dataType));
            return Concept.of(responseOrThrow().getPutAttributeTypeRes().getAttributeType(), this).asAttributeType();
        }


        public RelationType putRelationType(Label label) {
            transceiver.send(RequestBuilder.Transaction.putRelationType(label));
            return Concept.of(responseOrThrow().getPutRelationTypeRes().getRelationType(), this).asRelationType();
        }


        public Role putRole(Label label) {
            transceiver.send(RequestBuilder.Transaction.putRole(label));
            return Concept.of(responseOrThrow().getPutRoleRes().getRole(), this).asRole();
        }


        public Rule putRule(Label label, Pattern when, Pattern then) {
            transceiver.send(RequestBuilder.Transaction.putRule(label, when, then));
            return Concept.of(responseOrThrow().getPutRuleRes().getRule(), this).asRule();
        }


        public Stream<Numeric> stream(GraqlGet.Aggregate query, boolean infer) {
            Iterable<Numeric> iterable = () -> rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<AnswerGroup<ConceptMap>> stream(GraqlGet.Group query, boolean infer) {
            Iterable<AnswerGroup<ConceptMap>> iterable = () -> rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<AnswerGroup<Numeric>> stream(GraqlGet.Group.Aggregate query, boolean infer) {
            Iterable<AnswerGroup<Numeric>> iterable = () -> rpcIterator(query, infer);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<Numeric> stream(GraqlCompute.Statistics query) {
            Iterable<Numeric> iterable = () -> rpcIterator(query, false);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<ConceptList> stream(GraqlCompute.Path query) {
            Iterable<ConceptList> iterable = () -> rpcIterator(query, false);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<ConceptSetMeasure> stream(GraqlCompute.Centrality query) {
            Iterable<ConceptSetMeasure> iterable = () -> rpcIterator(query, false);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<ConceptSet> stream(GraqlCompute.Cluster query) {
            Iterable<ConceptSet> iterable = () -> rpcIterator(query, false);
            return StreamSupport.stream(iterable.spliterator(), false);
        }


        public Stream<SchemaConcept> sups(SchemaConcept schemaConcept) {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setSchemaConceptSupsReq(ConceptProto.SchemaConcept.Sups.Req.getDefaultInstance()).build();

            SessionProto.Transaction.Res response = runConceptMethod(schemaConcept.id(), method);
            int iteratorId = response.getConceptMethodRes().getResponse().getSchemaConceptSupsIter().getId();

            Iterable<? extends Concept> iterable = () -> new RPCIterator<>(
                    this, iteratorId, res -> Concept.of(res.getConceptMethodIterRes().getSchemaConceptSupsIterRes().getSchemaConcept(), this)
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

        private SessionProto.Transaction.Iter.Res iterate(int iteratorId) {
            transceiver.send(RequestBuilder.Transaction.iterate(iteratorId));
            return responseOrThrow().getIterateRes();
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
            private final int iteratorId;
            private Transaction tx;
            private Function<SessionProto.Transaction.Iter.Res, T> responseReader;

            private RPCIterator(Transaction tx, int iteratorId, Function<SessionProto.Transaction.Iter.Res, T> responseReader) {
                this.tx = tx;
                this.iteratorId = iteratorId;
                this.responseReader = responseReader;
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
