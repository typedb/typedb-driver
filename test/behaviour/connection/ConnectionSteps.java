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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConnectionSteps {

    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static Map<Integer, GraknClient.Session> sessionsMap = new HashMap<>();
    public static Map<String, CompletableFuture<GraknClient.Session>> sessionsMapParallel = new HashMap<>();
    public static Map<Integer, GraknClient.Transaction> transactionsMap = new HashMap<>();
    public static Map<String, CompletableFuture<GraknClient.Transaction>> transactionsMapParallel = new HashMap<>();

    private static GraknClient connect_to_grakn_core() {
        System.out.println("Establishing Connection to Grakn Core");
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        assertNotNull(address);

        System.out.println("Connection to Grakn Core established");
        return new GraknClient(address);
    }

    private static GraknClient connect_to_grakn_kgms() {
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

    private static synchronized void connect_to_grakn() {
        if (!isNull(client)) return;

        System.out.println("Connecting to Grakn ...");

        String graknType = System.getProperty(GraknProperties.GRAKN_TYPE);
        assertNotNull(graknType);

        if (graknType.equals(GraknProperties.GRAKN_CORE)) {
            client = connect_to_grakn_core();
        } else if (graknType.equals(GraknProperties.GRAKN_KGMS)) {
            client = connect_to_grakn_kgms();
        } else {
            fail("Invalid type of Grakn database: ");
        }

        assertNotNull(client);
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        if (isNull(client)) {
            connect_to_grakn();
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

        if (transactionsMap != null) {
            for (GraknClient.Transaction transaction : transactionsMap.values()) {
                if (!isNull(transaction)) transaction.close();
            }
            transactionsMap = new HashMap<>();
        }

        if (transactionsMapParallel != null) {
            for (CompletableFuture<GraknClient.Transaction> futureTransaction : transactionsMapParallel.values()) {
                futureTransaction.get().close();
            }
            transactionsMapParallel = new HashMap<>();
        }

        if (sessionsMap != null) {
            for (GraknClient.Session session : sessionsMap.values()) {
                if (!isNull(session)) session.close();
            }
            sessionsMap = new HashMap<>();
        }

        if (sessionsMapParallel != null) {
            for (CompletableFuture<GraknClient.Session> futureSession : sessionsMapParallel.values()) {
                futureSession.get().close();
            }
            sessionsMapParallel = new HashMap<>();
        }
    }
}
