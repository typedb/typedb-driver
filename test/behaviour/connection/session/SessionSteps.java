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

import grakn.client.GraknClient;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static grakn.client.test.behaviour.connection.ConnectionSteps.THREAD_POOL_SIZE;
import static grakn.client.test.behaviour.connection.ConnectionSteps.client;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessions;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.threadPool;
import static grakn.common.util.Collections.list;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SessionSteps {

    @When("connection open session for keyspace: {word}")
    public void connection_open_session_for_keyspace(String name) {
        connection_open_sessions_for_keyspaces(list(name));
    }

    @When("connection open session(s) for keyspace(s):")
    public void connection_open_sessions_for_keyspaces(List<String> names) {
        for (String name : names) {
            sessions.add(client.session(name));
        }
    }

    @When("connection open sessions in parallel for keyspaces:")
    public void connection_open_sessions_in_parallel_for_keyspaces(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        for (String name : names) {
            sessionsParallel.add(CompletableFuture.supplyAsync(() -> client.session(name), threadPool));
        }
    }

    @Then("session(s) is/are null: {bool}")
    public void sessions_are_null(Boolean isNull) {
        for (GraknClient.Session session : sessions) {
            assertEquals(isNull, isNull(session));
        }
    }

    @Then("session(s) is/are open: {bool}")
    public void sessions_are_open(Boolean isOpen) {
        for (GraknClient.Session session : sessions) {
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
                    assertEquals(isOpen, session.isOpen()); return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new)).join();
    }

    @Then("session(s) has/have keyspace(s):")
    public void sessions_have_keyspaces(List<String> names) {
        assertEquals(names.size(), sessions.size());
        Iterator<GraknClient.Session> sessionIter = sessions.iterator();

        for (String name : names) {
            assertEquals(name, sessionIter.next().database().name());
        }
    }

    @Then("sessions in parallel have keyspaces:")
    public void sessions_in_parallel_have_keyspaces(List<String> names) {
        assertEquals(names.size(), sessionsParallel.size());
        Iterator<CompletableFuture<GraknClient.Session>> futureSessionIter = sessionsParallel.iterator();
        CompletableFuture[] assertions = new CompletableFuture[names.size()];

        int i = 0;
        for (String name : names) {
            assertions[i++] = futureSessionIter.next().thenApplyAsync(session -> {
                assertEquals(name, session.database().name()); return null;
            });
        }

        CompletableFuture.allOf(assertions).join();
    }
}
