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
import grakn.client.test.behaviour.connection.ConnectionSteps;
import io.cucumber.java.en.Then;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;

public class SessionSteps {


    @Then("session(s) is/are null: {boolean}")
    public void sessions_are_null(Boolean isNull) {
        for (GraknClient.Session session : ConnectionSteps.sessionsMap.values()) {
            assertEquals(isNull, isNull(session));
        }
    }

    @Then("session(s) is/are open: {boolean}")
    public void sessions_are_open(Boolean isOpen) {
        for (GraknClient.Session session : ConnectionSteps.sessionsMap.values()) {
            assertEquals(isOpen, session.isOpen());
        }
    }

    @Then("sessions in parallel are null: {boolean}")
    public void sessions_in_parallel_are_null(Boolean isNull) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.sessionsMapParallel
                .values().stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    assertEquals(isNull, isNull(session));
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("sessions in parallel are open: {boolean}")
    public void sessions_in_parallel_are_open(Boolean isOpen) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.sessionsMapParallel
                .values().stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    assertEquals(isOpen, session.isOpen());
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("session(s) has/have keyspace: {word}")
    public void sessions_have_keyspace(String name) {
        for (GraknClient.Session session : ConnectionSteps.sessionsMap.values()) {
            assertEquals(name, session.keyspace().name());
        }
    }

    @Then("sessions have keyspaces:")
    public void sessions_have_keyspaces(Map<String, String> names) {
        for (Map.Entry<String, GraknClient.Session> entry : ConnectionSteps.sessionsMap.entrySet()) {
            assertEquals(names.get(entry.getKey()), entry.getValue().keyspace().name());
        }

        assertEquals(names.size(), ConnectionSteps.sessionsMap.size());
    }

    @Then("sessions in parallel have keyspace: {word}")
    public void sessions_in_parallel_have_keyspace(String name) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.sessionsMapParallel
                .values().stream().map(futureSession -> futureSession
                        .thenApplyAsync(session -> {
                            assertEquals(name, session.keyspace().name());
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("sessions in parallel have keyspaces:")
    public void sessions_in_parallel_have_keyspaces(Map<String, String> names) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.sessionsMapParallel
                .entrySet().stream().map(entry -> entry.getValue()
                        .thenApplyAsync(session -> {
                            assertEquals(names.get(entry.getKey()), session.keyspace().name());
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }
}
