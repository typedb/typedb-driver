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
import io.cucumber.java.After;
import io.cucumber.java.en.Given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public static List<GraknClient.Session> sessions = new ArrayList<>();
    public static List<CompletableFuture<GraknClient.Session>> sessionsParallel = new ArrayList<>();
    public static Map<GraknClient.Session, List<GraknClient.Transaction>> sessionsToTransactions = new HashMap<>();
    public static Map<GraknClient.Session, List<CompletableFuture<GraknClient.Transaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<GraknClient.Session>, List<CompletableFuture<GraknClient.Transaction>>> parallelSessionsToParallelTransactions = new HashMap<>();

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
        System.out.println(client.keyspaces().retrieve().toString());
        assertTrue(client.keyspaces().retrieve().isEmpty());
    }

    @After
    public void close_transactions_and_sessions() throws ExecutionException, InterruptedException {
        if (sessions != null) {
            for (GraknClient.Session session : sessions) {
                if (sessionsToTransactions.containsKey(session)) {
                    for (GraknClient.Transaction transaction : sessionsToTransactions.get(session)) {
                        transaction.close();
                    }
                    sessionsToTransactions.remove(session);
                }

                if (sessionsToTransactionsParallel.containsKey(session)) {
                    for (CompletableFuture<GraknClient.Transaction> futureTransaction : sessionsToTransactionsParallel.get(session)) {
                        futureTransaction.get().close();
                    }
                    sessionsToTransactionsParallel.remove(session);
                }

                session.close();
            }
            assertTrue(sessionsToTransactions.isEmpty());
            assertTrue(sessionsToTransactionsParallel.isEmpty());
            sessions = new ArrayList<>();
            sessionsToTransactions = new HashMap<>();
            sessionsToTransactionsParallel = new HashMap<>();
        }

        if (sessionsParallel != null) {
            for (CompletableFuture<GraknClient.Session> futureSession : sessionsParallel) {
                if (parallelSessionsToParallelTransactions.containsKey(futureSession)) {
                    for (CompletableFuture<GraknClient.Transaction> futureTransaction : parallelSessionsToParallelTransactions.get(futureSession)) {
                        futureTransaction.get().close();
                    }
                    parallelSessionsToParallelTransactions.remove(futureSession);
                }
                futureSession.get().close();
            }
            assertTrue(parallelSessionsToParallelTransactions.isEmpty());
            sessionsParallel = new ArrayList<>();
            parallelSessionsToParallelTransactions = new HashMap<>();
        }
    }
}
