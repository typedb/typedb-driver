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

package grakn.client.test.behaviour.connection;

import grakn.client.GraknClient;
import grakn.client.test.setup.GraknProperties;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConnectionSteps {

    private int THREAD_POOL_SIZE = 32;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static Map<String, GraknClient.Session> sessionsMap = new HashMap<>();
    public static Map<String, CompletableFuture<GraknClient.Session>> sessionsMapParallel = new HashMap<>();





    private GraknClient.Session session;
    private List<GraknClient.Session> sessions = new ArrayList<>();
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
            fail("Invalid type of Grakn database: ");
        }

        assertNotNull(client);
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        if (isNull(client)) {
            connectToGrakn();
        }

        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    @Given("connection delete all keyspaces")
    public void connection_delete_all_keyspaces() {
        for (String keyspace : client.keyspaces().retrieve()) {
            client.keyspaces().delete(keyspace);
        }
    }

    @Given("connection does not have any keyspace")
    public void connection_does_not_have_any_keyspace() {
        assertTrue(client.keyspaces().retrieve().isEmpty());
    }

    @After
    public void close_transactions_and_sessions(Scenario scenario) throws ExecutionException, InterruptedException {
        if (transaction != null) transaction.close();
        if (session != null) session.close();

        if (sessionsMap != null) {
            for (GraknClient.Session session : sessionsMap.values()) {
                if (!isNull(session)) session.close();
            }
            sessionsMap = new HashMap<>();
        }

        if (sessionsMapParallel != null) {
            for (CompletableFuture<GraknClient.Session> future : sessionsMapParallel.values()) {
                future.get().close();
            }
            sessionsMapParallel = new HashMap<>();
        }
    }

    @When("connection open {number} session(s) for one keyspace: {word}")
    public void connection_open_many_sessions_for_one_keyspace(int number, String name) {
        for (int i = 0; i < number; i++) {
            sessionsMap.put(Integer.toString(i), client.session(name));
        }
    }

    @When("connection open many sessions for many keyspaces:")
    public void connection_open_many_sessions_for_many_keyspaces(Map<String, String> names) {
        for (Map.Entry<String, String> name : names.entrySet()) {
            sessionsMap.put(name.getKey(), client.session(name.getValue()));
        }
    }

    @When("connection open {number} sessions in parallel for one keyspace: {word}")
    public void connection_open_many_sessions_in_parallel_for_one_keyspace(int number, String name) {
        assertTrue(THREAD_POOL_SIZE >= number);
        sessionsMapParallel = new HashMap<>();

        for (int i = 0; i < number; i++) {
            sessionsMapParallel.put(
                    Integer.toString(i),
                    CompletableFuture.supplyAsync(() -> client.session(name), threadPool)
            );
        }
    }

    @When("connection open many sessions in parallel for many keyspaces:")
    public void connection_open_many_sessions_in_parallel_for_many_keyspaces(Map<String, String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        for (Map.Entry<String, String> name : names.entrySet()) {
            sessionsMapParallel.put(
                    name.getKey(),
                    CompletableFuture.supplyAsync(() -> client.session(name.getValue()), threadPool)
            );
        }
    }

    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================

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
