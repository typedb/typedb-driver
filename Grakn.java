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

import grakn.client.concept.Concepts;
import grakn.client.concept.answer.Answer;
import grakn.client.concept.answer.AnswerGroup;
import grakn.client.concept.answer.ConceptList;
import grakn.client.concept.answer.ConceptMap;
import grakn.client.concept.answer.ConceptSet;
import grakn.client.concept.answer.ConceptSetMeasure;
import grakn.client.concept.answer.Explanation;
import grakn.client.concept.answer.Numeric;
import grakn.client.concept.answer.Void;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;

import javax.annotation.CheckReturnValue;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static grakn.client.Grakn.Session.Type.DATA;

public interface Grakn {

    interface Client extends AutoCloseable {

        boolean isOpen();

        void close();

        default Session session(String databaseName) {
            return session(databaseName, DATA);
        }

        default Session session(String databaseName, Session.Type type) {
            return session(databaseName, type, new GraknOptions());
        }

        Session session(String databaseName, Session.Type type, GraknOptions options);

        DatabaseManager databases();
    }

    interface DatabaseManager {

        boolean contains(String name);

        void create(String name);

        void delete(String name);

        List<String> all();
    }

    interface Database extends Serializable {

        @CheckReturnValue
        String name();
    }

    interface Session extends AutoCloseable {

        default Transaction transaction() {
            return transaction(Transaction.Type.READ);
        }

        default Transaction transaction(Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        Transaction transaction(Transaction.Type type, GraknOptions options);

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

            public static Type of(int value) {
                for (Type t : values()) {
                    if (t.id == value) return t;
                }
                return null;
            }

            public int id() {
                return id;
            }

            public boolean isData() { return !isSchema; }

            public boolean isSchema() { return isSchema; }
        }
    }

    interface Transaction extends AutoCloseable {

        Transaction.Type type();

        Session session();

        Database database();

        void close();

        QueryFuture<List<ConceptMap>> execute(GraqlDefine query);

        QueryFuture<List<ConceptMap>> execute(GraqlUndefine query);

        QueryFuture<List<ConceptMap>> execute(GraqlInsert query, GraknOptions options);

        QueryFuture<List<ConceptMap>> execute(GraqlInsert query);

        QueryFuture<List<Void>> execute(GraqlDelete query, GraknOptions options);

        QueryFuture<List<Void>> execute(GraqlDelete query);

        QueryFuture<List<ConceptMap>> execute(GraqlMatch query, GraknOptions options);

        QueryFuture<List<ConceptMap>> execute(GraqlMatch query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlDefine query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlUndefine query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query, GraknOptions options);

        QueryFuture<Stream<ConceptMap>> stream(GraqlInsert query);

        QueryFuture<Stream<Void>> stream(GraqlDelete query, GraknOptions options);

        QueryFuture<Stream<Void>> stream(GraqlDelete query);

        QueryFuture<Stream<ConceptMap>> stream(GraqlMatch query, GraknOptions options);

        QueryFuture<Stream<ConceptMap>> stream(GraqlMatch query);

        QueryFuture<List<Numeric>> execute(GraqlMatch.Aggregate query);

        QueryFuture<List<Numeric>> execute(GraqlMatch.Aggregate query, GraknOptions options);

        QueryFuture<Stream<Numeric>> stream(GraqlMatch.Aggregate query);

        QueryFuture<Stream<Numeric>> stream(GraqlMatch.Aggregate query, GraknOptions options);

        QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlMatch.Group query);

        QueryFuture<List<AnswerGroup<ConceptMap>>> execute(GraqlMatch.Group query, GraknOptions options);

        QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlMatch.Group query);

        QueryFuture<Stream<AnswerGroup<ConceptMap>>> stream(GraqlMatch.Group query, GraknOptions options);

        QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlMatch.Group.Aggregate query);

        QueryFuture<List<AnswerGroup<Numeric>>> execute(GraqlMatch.Group.Aggregate query, GraknOptions options);

        QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlMatch.Group.Aggregate query);

        QueryFuture<Stream<AnswerGroup<Numeric>>> stream(GraqlMatch.Group.Aggregate query, GraknOptions options);

        QueryFuture<List<Numeric>> execute(GraqlCompute.Statistics query);

        QueryFuture<Stream<Numeric>> stream(GraqlCompute.Statistics query);

        QueryFuture<List<ConceptList>> execute(GraqlCompute.Path query);

        QueryFuture<Stream<ConceptList>> stream(GraqlCompute.Path query);

        QueryFuture<List<ConceptSetMeasure>> execute(GraqlCompute.Centrality query);

        QueryFuture<Stream<ConceptSetMeasure>> stream(GraqlCompute.Centrality query);

        QueryFuture<List<ConceptSet>> execute(GraqlCompute.Cluster query);

        QueryFuture<Stream<ConceptSet>> stream(GraqlCompute.Cluster query);

        QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query);

        QueryFuture<? extends List<? extends Answer>> execute(GraqlQuery query, GraknOptions options);

        QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query);

        QueryFuture<? extends Stream<? extends Answer>> stream(GraqlQuery query, GraknOptions options);

        boolean isOpen();

        Concepts concepts();

        void commit();

        Explanation getExplanation(ConceptMap explainable);

        enum Type {
            READ(0),
            WRITE(1);

            private final int id;
            private final boolean isWrite;

            Type(int id) {
                this.id = id;
                this.isWrite = id == 1;
            }

            public static Type of(int value) {
                for (Type t : values()) {
                    if (t.id == value) return t;
                }
                return null;
            }

            public int id() {
                return id;
            }

            public boolean isRead() { return !isWrite; }

            public boolean isWrite() { return isWrite; }
        }

        interface QueryFuture<T> extends Future<T> {
            @Override
            T get();

            @Override
            T get(long timeout, TimeUnit unit);
        }
    }

}
