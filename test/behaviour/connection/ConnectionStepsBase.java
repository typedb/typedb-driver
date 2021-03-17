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

import grakn.client.api.GraknClient;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import grakn.client.api.database.Database;
import grakn.common.test.server.GraknSingleton;

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

public abstract class ConnectionStepsBase {
    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static List<GraknSession> sessions = new ArrayList<>();
    public static List<CompletableFuture<GraknSession>> sessionsParallel = new ArrayList<>();
    public static Map<GraknSession, List<GraknTransaction>> sessionsToTransactions = new HashMap<>();
    public static Map<GraknSession, List<CompletableFuture<GraknTransaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<GraknSession>, List<CompletableFuture<GraknTransaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();
    private static boolean isBeforeAllRan = false;

    public static GraknTransaction tx() {
        return sessionsToTransactions.get(sessions.get(0)).get(0);
    }

    abstract void beforeAll();

    void before() {
        if (!isBeforeAllRan) {
            try {
                beforeAll();
            } finally {
                isBeforeAllRan = true;
            }
        }
        assertNull(client);
        String address = GraknSingleton.getGraknRunner().address();
        assertNotNull(address);
        client = createGraknClient(address);
        client.databases().all().forEach(Database::delete);
        System.out.println("ConnectionSteps.before");
    }

    void after() {
        // TODO: Remove this once the server segfault issue is fixed (grakn#6135)
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sessions.parallelStream().forEach(GraknSession::close);
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
        client.databases().all().forEach(Database::delete);
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
