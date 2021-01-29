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
import grakn.common.test.server.GraknSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class ConnectionStepsBase {
    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static List<GraknClient.Session> sessions = new ArrayList<>();
    public static List<CompletableFuture<GraknClient.Session>> sessionsParallel = new ArrayList<>();
    public static Map<GraknClient.Session, List<GraknClient.Transaction>> sessionsToTransactions = new HashMap<>();
    public static Map<GraknClient.Session, List<CompletableFuture<GraknClient.Transaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<GraknClient.Session>, List<CompletableFuture<GraknClient.Transaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();
    private static boolean isBeforeAllRan = false;

    public static GraknClient.Transaction tx() {
        return sessionsToTransactions.get(sessions.get(0)).get(0);
    }

    abstract void beforeAll();

    void before() {
        if (!isBeforeAllRan) {
            beforeAll();
            isBeforeAllRan = true;
        }
        assertNull(client);
        String address = GraknSingleton.getGraknRunner().address();
        assertNotNull(address);
        client = createGraknClient(address);
        client.databases().all().forEach(database -> client.databases().delete(database));
        System.out.println("ConnectionSteps.before");
    }

    void after() {
        sessions.parallelStream().forEach(GraknClient.Session::close);
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

    abstract GraknClient createGraknClient(String address);

    void connection_has_been_opened() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    void connection_does_not_have_any_database() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }
}
