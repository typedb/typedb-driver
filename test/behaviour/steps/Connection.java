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

package grakn.client.test.behaviour.steps;

import grakn.client.GraknClient;
import grakn.client.test.common.GraknProperties;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Connection {

    private static GraknClient client;

    private int THREAD_POOL_SIZE = 128;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private Map<String, CompletableFuture<GraknClient.Session>> sessionsInParallel = new HashMap<>();
    private Map<String, GraknClient.Session> sessions = new HashMap<>();
    private GraknClient.Session session;
    private GraknClient.Transaction transaction;

    private static GraknClient connectToGraknCore() {
        System.out.println("Establishing Connection to Grakn Core");
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        assertNotNull(address);

        System.out.println("Connection to Grakn Core established");
        return new GraknClient(address);
    }

    private static GraknClient connectToGraknKGMS() {
        System.out.println("Establishing Connection to Grakn");
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        String username = System.getProperty(GraknProperties.GRAKN_USERNAME);
        String password = System.getProperty(GraknProperties.GRAKN_PASSWORD);
        assertNotNull(address);
        assertNotNull(username);
        assertNotNull(password);

        System.out.println("Connection to Grakn KGMS established");
        return new GraknClient(address, username, password);
    }

    private static synchronized void connectToGrakn() {
        if (!isNull(client)) return;

        System.out.println("Connecting to Grakn ...");

        String graknType = System.getProperty(GraknProperties.GRAKN_TYPE);
        assertNotNull(graknType);

        if (graknType.equals(GraknProperties.GRAKN_CORE)) {
            client = connectToGraknCore();
        } else if (graknType.equals(GraknProperties.GRAKN_KGMS)) {
            client = connectToGraknKGMS();
        } else {
            fail("Invalid type of Grakn database: " );
        }

        assertNotNull(client);
    }

    @After
    public void close_session(Scenario scenario) throws ExecutionException, InterruptedException {
        if (!isNull(transaction)) transaction.close();
        if (!isNull(session)) session.close();

        for (GraknClient.Session session : sessions.values()) {
            if (!isNull(session)) session.close();
        }

        for (CompletableFuture<GraknClient.Session> future : sessionsInParallel.values()) {
            future.get().close();
        }
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        if (isNull(client)) {
            connectToGrakn();
        }

        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    @Given("connection has no keyspaces")
    public void connection_has_no_keyspaces() {
        for (String keyspace : client.keyspaces().retrieve()) {
            client.keyspaces().delete(keyspace);
        }
        assertTrue(client.keyspaces().retrieve().isEmpty());
    }

    @When("connection open session for keyspace: {word}")
    public void connection_open_session_for_keyspace(String keyspaceName) {
        session = client.session(keyspaceName);
    }

    @When("connection open sessions for keyspaces:")
    public void connection_open_sessions_for_keyspaces(List<String> keyspaceNames) {
        for (String keyspaceName : keyspaceNames ) {
            sessions.put(keyspaceName, client.session(keyspaceName));
        }
    }

    @When("connection open sessions in parallel for different keyspaces: {number}")
    public void connection_open_parallel_sessions_for_keyspaces(Integer number) {
        assertTrue(THREAD_POOL_SIZE >= number);

        for (int i = 0; i < number; i++) {
            final String keyspaceName = "keyspace_" + i;
            sessionsInParallel.put(
                    keyspaceName,
                    CompletableFuture.supplyAsync(() -> client.session(keyspaceName), threadPool)
            );
        }
    }

    @When("connection delete keyspace: {word}")
    public void connection_delete_keyspace(String keyspaceName) {
        client.keyspaces().delete(keyspaceName);
    }

    @When("connection delete keyspaces:")
    public void connection_delete_keyspaces(List<String> keyspaceNames) {
        for (String keyspaceName : keyspaceNames) {
            client.keyspaces().delete(keyspaceName);
        }
    }

    @Then("connection has keyspace: {word}")
    public void connection_has_keyspace(String keyspaceName) {
        assertTrue(client.keyspaces().retrieve().contains(keyspaceName));
    }

    @Then("connection has keyspaces:")
    public void connection_has_keyspaces(List<String> keyspaceNames) {
        Set<String> actually = new HashSet<>(client.keyspaces().retrieve());
        Set<String> expected = new HashSet<>(keyspaceNames);

        assertEquals(actually, expected);
    }

    @Then("connection does not have keyspace: {word}")
    public void connection_does_not_have_keyspace(String keyspaceName) {
        assertFalse(client.keyspaces().retrieve().contains(keyspaceName));
    }

    @Then("connection does not have keyspaces:")
    public void connection_does_not_have_keyspaces(List<String> keyspaceNames) {
        Set<String> keyspaces = new HashSet<>(client.keyspaces().retrieve());
        for (String keyspaceName : keyspaceNames) {
            assertFalse(keyspaces.contains(keyspaceName));
        }
    }

    @Then("session is null: {boolean}")
    public void session_is_null(Boolean isNull){
        assertEquals(isNull(session), isNull);
    }

    @Then("sessions are null: {boolean}")
    public void sessions_are_null(Boolean isNull) {
        for (GraknClient.Session session : sessions.values()) {
            assertEquals(isNull(session), isNull);
        }
    }

    @Then("sessions in parallel are null: {boolean}")
    public void sessions_in_parallel_are_null(Boolean isNull) {
        Stream<CompletableFuture<Void>> assertions = sessionsInParallel.values().stream()
                .map(futureSession -> futureSession
                        .thenApplyAsync(session -> {
                            assertEquals(isNull(session), isNull);
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("session is open: {boolean}")
    public void session_is_open(Boolean isOpen) {
        assertEquals(session.isOpen(), isOpen);
    }

    @Then("sessions are open: {boolean}")
    public void sessions_are_open(Boolean isOpen) {
        for (GraknClient.Session session : sessions.values()) {
            assertEquals(session.isOpen(), isOpen);
        }
    }

    @Then("sessions in parallel are open: {boolean}")
    public void sessions_in_parallel_are_open(Boolean isOpen) {
        Stream<CompletableFuture<Void>> assertions = sessionsInParallel.values().stream()
                .map(futureSession -> futureSession
                        .thenApplyAsync(session -> {
                            assertEquals(session.isOpen(), isOpen);
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("session has keyspace: {word}")
    public void session_has_name(String keyspaceName) {
        assertEquals(session.keyspace().name(), keyspaceName);
    }

    @Then("sessions have correct keyspaces:")
    public void sessions_have_correct_keyspaces(Map<String, String> keyspaceNames) {
        for (Map.Entry<String, String> entry : keyspaceNames.entrySet()) {
            assertEquals(sessions.get(entry.getKey()).keyspace().name(), entry.getValue());
        }
        assertEquals(sessions.size(), keyspaceNames.size());
    }

    @Then("sessions in parallel have correct keyspaces")
    public void sessions_in_parallel_have_correct_keyspaces() {
        Stream<CompletableFuture<Void>> assertions = sessionsInParallel.entrySet().stream()
                .map(entry -> entry.getValue()
                        .thenApplyAsync(session -> {
                            assertEquals(session.keyspace().name(), entry.getKey());
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @When("session open transaction: {transaction-type}")
    public void session_open_transaction(GraknClient.Transaction.Type type) {
        transaction = type.equals(GraknClient.Transaction.Type.READ) ?
                session.transaction().read() :
                session.transaction().write();
    }

    @Then("transaction is null: {boolean}")
    public void transaction_is_null(Boolean isNull) {
        assertEquals(isNull(transaction), isNull);
    }

    @Then("transaction is open: {boolean}")
    public void transaction_is_open(Boolean isOpen) {
        assertEquals(transaction.isOpen(), isOpen);
    }

    @Then("transaction has type: {transaction-type}")
    public void transaction_has_type(GraknClient.Transaction.Type type) {
        // TODO: Remove the conversion of grakn.core.api.Transaction.Type once we resolve graknlabs/grakn#5289
        assertEquals(GraknClient.Transaction.Type.of(transaction.type().id()), type);
    }

    @Then("transaction has keyspace: {word}")
    public void transaction_has_keyspace(String keyspaceName) {
        assertEquals(transaction.keyspace().name(), keyspaceName);
    }

    @Then("transaction has session has keyspace: {word}")
    public void transaction_has_session_has_keyspace(String keyspaceName) {
        assertEquals(transaction.session().keyspace().name(), keyspaceName);
    }
}
