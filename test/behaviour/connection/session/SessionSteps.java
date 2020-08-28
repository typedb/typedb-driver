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

package grakn.client.test.behaviour.connection.session;

import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static grakn.client.Grakn.Session.Type.SCHEMA;
import static grakn.client.test.behaviour.connection.ConnectionSteps.THREAD_POOL_SIZE;
import static grakn.client.test.behaviour.connection.ConnectionSteps.client;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessions;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsToTransactions;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsToTransactionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.threadPool;
import static grakn.common.collection.Collections.list;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionSteps {

    @When("connection open schema session for database: {word}")
    public void connection_open_schema_session_for_database(String name) {
        connection_open_schema_sessions_for_databases(list(name));
    }

    @When("connection open (data )session for database: {word}")
    public void connection_open_data_session_for_database(String name) {
        connection_open_data_sessions_for_databases(list(name));
    }

    @When("connection open schema session(s) for database(s):")
    public void connection_open_schema_sessions_for_databases(List<String> names) {
        for (String name : names) {
            sessions.add(client.session(name, SCHEMA));
        }
    }

    @When("connection open (data )session(s) for database(s):")
    public void connection_open_data_sessions_for_databases(List<String> names) {
        for (String name : names) {
            sessions.add(client.session(name));
        }
    }

    @When("connection open (data )sessions in parallel for databases:")
    public void connection_open_data_sessions_in_parallel_for_databases(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        for (String name : names) {
            sessionsParallel.add(CompletableFuture.supplyAsync(() -> client.session(name), threadPool));
        }
    }

    @When("connection close all sessions")
    public void connection_close_all_sessions() throws ExecutionException, InterruptedException {
        for (Session session : sessions) {
            if (sessionsToTransactions.containsKey(session)) {
                for (Transaction transaction : sessionsToTransactions.get(session)) {
                    transaction.close();
                }
                sessionsToTransactions.remove(session);
            }

            if (sessionsToTransactionsParallel.containsKey(session)) {
                for (CompletableFuture<Transaction> futureTransaction : sessionsToTransactionsParallel.get(session)) {
                    futureTransaction.get().close();
                }
                sessionsToTransactionsParallel.remove(session);
            }

            session.close();
        }
        sessions.clear();
    }

    @Then("session(s) is/are null: {bool}")
    public void sessions_are_null(Boolean isNull) {
        for (Session session : sessions) {
            assertEquals(isNull, isNull(session));
        }
    }

    @Then("session(s) is/are open: {bool}")
    public void sessions_are_open(Boolean isOpen) {
        for (Session session : sessions) {
            assertEquals(isOpen, session.isOpen());
        }
    }

    @Then("sessions in parallel are null: {bool}")
    public void sessions_in_parallel_are_null(Boolean isNull) {
        Stream<CompletableFuture<Void>> assertions = sessionsParallel
                .stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    assertEquals(isNull, isNull(session));
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new)).join();
    }

    @Then("sessions in parallel are open: {bool}")
    public void sessions_in_parallel_are_open(Boolean isOpen) {
        Stream<CompletableFuture<Void>> assertions = sessionsParallel
                .stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    assertEquals(isOpen, session.isOpen());
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new)).join();
    }

    @Then("session(s) has/have database(s):")
    public void sessions_have_databases(List<String> names) {
        assertEquals(names.size(), sessions.size());
        Iterator<Session> sessionIter = sessions.iterator();

        for (String name : names) {
            assertEquals(name, sessionIter.next().database().name());
        }
    }

    @Then("sessions in parallel have databases:")
    public void sessions_in_parallel_have_databases(List<String> names) {
        assertEquals(names.size(), sessionsParallel.size());
        Iterator<CompletableFuture<Session>> futureSessionIter = sessionsParallel.iterator();
        CompletableFuture[] assertions = new CompletableFuture[names.size()];

        int i = 0;
        for (String name : names) {
            assertions[i++] = futureSessionIter.next().thenApplyAsync(session -> {
                assertEquals(name, session.database().name());
                return null;
            });
        }

        CompletableFuture.allOf(assertions).join();
    }
}
