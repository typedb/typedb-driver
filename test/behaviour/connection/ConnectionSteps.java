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
import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.common.test.server.GraknSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionSteps {

    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static List<Session> sessions = new ArrayList<>();
    public static List<CompletableFuture<Session>> sessionsParallel = new ArrayList<>();
    public static Map<Session, List<Transaction>> sessionsToTransactions = new HashMap<>();
    public static Map<Session, List<CompletableFuture<Transaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<Session>, List<CompletableFuture<Transaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();

    public static Transaction tx() {
        return sessionsToTransactions.get(sessions.get(0)).get(0);
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        assertTrue(client.databases().all().isEmpty());
    }

    @Before
    public synchronized void before() {
        assertNull(client);
        String address = GraknSingleton.getGraknRunner().address();
        assertNotNull(address);
        client = GraknClient.core(address);
        client.databases().all().forEach(database -> client.databases().delete(database));
        System.out.println("ConnectionSteps.before");
    }

    @After
    public synchronized void after() {
        sessions.parallelStream().forEach(Session::close);
        sessions.clear();

        Stream<CompletableFuture<Void>> closures = sessionsParallel
                .stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    session.close();
                    return null;
                }));
        CompletableFuture.allOf(closures.toArray(CompletableFuture[]::new)).join();
        sessionsParallel.clear();

        sessionsToTransactions.clear();
        sessionsToTransactionsParallel.clear();
        sessionsParallelToTransactionsParallel.clear();
        client.databases().all().forEach(database -> client.databases().delete(database));
        client.close();
        assertFalse(client.isOpen());
        client = null;
        System.out.println("ConnectionSteps.after");
    }
}
