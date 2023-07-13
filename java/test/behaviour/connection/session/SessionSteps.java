/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.test.behaviour.connection.session;

import com.vaticle.typedb.client.api.TypeDBSession;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.api.TypeDBSession.Type.DATA;
import static com.vaticle.typedb.client.api.TypeDBSession.Type.SCHEMA;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.client;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.optionSetters;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionOptions;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessions;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionsParallel;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.vaticle.typedb.common.collection.Collections.list;
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
            sessions.add(client.session(name, SCHEMA, sessionOptions));
        }
    }

    @When("connection open (data )session(s) for database(s):")
    public void connection_open_data_sessions_for_databases(List<String> names) {
        for (String name : names) {
            sessions.add(client.session(name, DATA, sessionOptions));
        }
    }

    @When("connection open (data )sessions in parallel for databases:")
    public void connection_open_data_sessions_in_parallel_for_databases(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        for (String name : names) {
            sessionsParallel.add(CompletableFuture.supplyAsync(() -> client.session(name, DATA, sessionOptions), threadPool));
        }
    }

    @When("connection close all sessions")
    public void connection_close_all_sessions() {
        for (TypeDBSession session : sessions) {
            session.close();
        }
        sessions.clear();
    }

    @Then("session(s) is/are null: {bool}")
    public void sessions_are_null(Boolean isNull) {
        for (TypeDBSession session : sessions) {
            assertEquals(isNull, isNull(session));
        }
    }

    @Then("session(s) is/are open: {bool}")
    public void sessions_are_open(Boolean isOpen) {
        for (TypeDBSession session : sessions) {
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

    @Then("session(s) has/have database: {word}")
    public void sessions_have_database(String name) {
        sessions_have_databases(list(name));
    }

    @Then("session(s) has/have database(s):")
    public void sessions_have_databases(List<String> names) {
        assertEquals(names.size(), sessions.size());
        Iterator<TypeDBSession> sessionIter = sessions.iterator();

        for (String name : names) {
            assertEquals(name, sessionIter.next().database_name());
        }
    }

    @Then("sessions in parallel have databases:")
    public void sessions_in_parallel_have_databases(List<String> names) {
        assertEquals(names.size(), sessionsParallel.size());
        Iterator<CompletableFuture<TypeDBSession>> futureSessionIter = sessionsParallel.iterator();
        CompletableFuture[] assertions = new CompletableFuture[names.size()];

        int i = 0;
        for (String name : names) {
            assertions[i++] = futureSessionIter.next().thenApplyAsync(session -> {
                assertEquals(name, session.database_name());
                return null;
            });
        }

        CompletableFuture.allOf(assertions).join();
    }

    @Given("set session option {word} to: {word}")
    public void set_session_option_to(String option, String value) {
        if (!optionSetters.containsKey(option)) {
            throw new RuntimeException("Unrecognised option: " + option);
        }
        optionSetters.get(option).accept(sessionOptions, value);
    }
}
