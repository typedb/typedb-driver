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

import grakn.client.answer.Answer;
import grakn.client.answer.AnswerGroup;
import grakn.client.answer.ConceptList;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.ConceptSet;
import grakn.client.answer.ConceptSetMeasure;
import grakn.client.answer.Explanation;
import grakn.client.answer.Numeric;
import grakn.client.answer.Void;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.client.connection.GraknClient;
import grakn.client.connection.GraknDatabase;
import grakn.client.connection.GraknTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import io.grpc.ManagedChannel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Grakn {

    static Client client() {
        return new GraknClient();
    }

    static Client client(String address) {
        return new GraknClient(address);
    }

    static Client client(String address, String username, String password) {
        return new GraknClient(address);
    }

    interface Client extends AutoCloseable {

        GraknClient overrideChannel(ManagedChannel channel);

        boolean isOpen();

        void close();

        Session session(String databaseName);

        Session schemaSession(String databaseName);

        Session session(String databaseName, Session.Type type);

        DatabaseManager databases();
    }

    /**
     * Manages a collection of Grakn databases.
     */
    interface DatabaseManager {

        boolean contains(String name);

        void create(String name);

        void delete(String name);

        List<String> all();
    }

    interface Database extends Serializable {

        @CheckReturnValue
        static Database of(String name) {
            return new GraknDatabase(name);
        }

        @CheckReturnValue
        String name();
    }

    /**
     * @see Transaction
     * @see Client
     */
    interface Session extends AutoCloseable {

        Transaction.Builder transaction();

        Transaction transaction(Transaction.Type type);

        boolean isOpen();

        void close();

        Database database();

        enum Type {
            DATA(0),
            SCHEMA(1);

            private final int id;
            private final boolean isSchema;

            Type(int id) {
                this.id = id;
                this.isSchema = id == 1;
            }

            public static Session.Type of(int value) {
                for (Session.Type t : Session.Type.values()) {
                    if (t.id == value) return t;
                }
                return null;
            }

            public boolean isData() { return !isSchema; }

            public boolean isSchema() { return isSchema; }
        }
    }

    interface Transaction extends AutoCloseable {

        Type type();

        Session session();

        Database database();

        void close();

        QueryFuture<List<ConceptMap>> execute(GraqlDefine query);

        QueryFuture<List<ConceptMap>> execute(GraqlUndefine query);

        QueryFuture<List<ConceptMap>> execute(GraqlInsert query, QueryOptions options);

        QueryFuture<List<ConceptMap>> execute(GraqlInsert query);

        QueryFuture<List<Void>> execute(GraqlDelete query, QueryOptions options);

        QueryFuture<List<Void>> execute(GraqlDelete query);

        QueryFuture<List<ConceptMap>> execute(GraqlGet query, QueryOptions options);

        QueryFuture<List<ConceptMap>> execute(GraqlGet query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query, QueryOptions options);

        QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query);

        QueryFuture<Stream<Void>> stream(GraqlDelete query, QueryOptions options);

        QueryFuture<Stream<Void>> stream(GraqlDelete query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlGet query, QueryOptions options);

        QueryFuture<Stream<ConceptMap>> stream(GraqlGet query);

        QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query);

        QueryFuture<List<Numeric>> execute(GraqlGet.Aggregate query, QueryOptions options);

        QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query);

        QueryFuture<Stream<Numeric>> stream(GraqlGet.Aggregate query, QueryOptions options);

        QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query);

        QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlGet.Group query, QueryOptions options);

        QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query);

        QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlGet.Group query, QueryOptions options);

        QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query);

        QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlGet.Group.Aggregate query, QueryOptions options);

        QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query);

        QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlGet.Group.Aggregate query, QueryOptions options);

        QueryFuture<List<Numeric>> execute(GraqlCompute.Statistics query);

        QueryFuture<Stream<Numeric>> stream(GraqlCompute.Statistics query);

        QueryFuture<List<ConceptList>> execute(GraqlCompute.Path query);

        QueryFuture<Stream<ConceptList>> stream(GraqlCompute.Path query);

        QueryFuture<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query);

        QueryFuture<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query);

        QueryFuture<List<ConceptSet>> execute(GraqlCompute.Cluster query);

        QueryFuture<Stream<ConceptSet>> stream(GraqlCompute.Cluster query);

        QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query);

        QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query, QueryOptions options);

        QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query);

        QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query, QueryOptions options);

        boolean isOpen();

        void commit();

        ThingType.Remote getRootType();

        EntityType.Remote getRootEntityType();

        RelationType.Remote getRootRelationType();

        AttributeType.Remote getRootAttributeType();

        RoleType.Remote getRootRoleType();

        Rule.Remote getRootRule();

        EntityType.Remote putEntityType(String label);

        @Nullable
        EntityType.Remote getEntityType(String label);

        RelationType.Remote putRelationType(String label);

        @Nullable
        RelationType.Remote getRelationType(String label);

        AttributeType.Remote putAttributeType(String label, ValueType valueType);

        @Nullable
        AttributeType.Remote getAttributeType(String label);

        Rule.Remote putRule(String label, Pattern when, Pattern then);

        @Nullable
        Rule.Remote getRule(String label);

        @Nullable
        grakn.client.concept.type.Type.Remote getType(String label);

        @Nullable
        grakn.client.concept.type.Type.Local getCachedType(String label);

        @Nullable
        Thing.Remote getThing(String iid);

        TransactionProto.Transaction.Res runConceptMethod(String iid, ConceptProto.ThingMethod.Req thingMethod);

        TransactionProto.Transaction.Res runConceptMethod(String label, ConceptProto.TypeMethod.Req typeMethod);

        <T> Stream<T> iterateConceptMethod(String iid, ConceptProto.ThingMethod.Iter.Req thingMethod, Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader);

        <T> Stream<T> iterateConceptMethod(String label, ConceptProto.TypeMethod.Iter.Req typeMethod, Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader);

        Explanation getExplanation(ConceptMap explainable);

        <T> Stream<T> iterate(TransactionProto.Transaction.Iter.Req request, Function<TransactionProto.Transaction.Iter.Res, T> responseReader);

        interface Builder {

            /**
             * Read-only transaction, where database mutation is prohibited
             */
            Transaction read();

            /**
             * Write transaction, where database mutation is allowed
             */
            Transaction write();
        }

        /**
         * An extension of @code Future that catches @code InterruptedException and @code ExecutionException.
         */
        interface QueryFuture<T> extends Future<T> {
            @Override
            T get();

            @Override
            T get(long timeout, TimeUnit unit) throws TimeoutException;
        }

        interface Option<T> {
        }

        interface QueryOptions {

            QueryOptions infer(boolean infer);

            QueryOptions explain(boolean explain);

            QueryOptions batchSize(int size);

            QueryOptions batchSize(BatchSize batchSize);

            <T> QueryOptions set(Option<T> flag, T value);

            <T> QueryOptions whenSet(Option<T> option, Consumer<T> consumer);
        }

        // TODO: align this tremendously awkward interface with core
        interface Options {
            QueryOptions DEFAULT = new GraknTransaction.QueryOptionsImpl();

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

        enum Type {

            /**
             * Read-only transaction, where database mutation is prohibited
             */
            READ(0),

            /**
             * Write transaction, where database mutation is allowed
             */
            WRITE(1);

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

        enum BooleanOption implements Option<Boolean> {
            INFER,
            EXPLAIN
        }

        enum BatchOption implements Option<TransactionProto.Transaction.Iter.Req.Options> {
            BATCH_SIZE
        }

        enum BatchSize {
            ALL
        }
    }
}
