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

package com.vaticle.typedb.client.test.behaviour.connection;

import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.common.test.server.TypeDBSingleton;

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

    public static TypeDBClient client;
    public static List<TypeDBSession> sessions = new ArrayList<>();
    public static List<CompletableFuture<TypeDBSession>> sessionsParallel = new ArrayList<>();
    public static Map<TypeDBSession, List<TypeDBTransaction>> sessionsToTransactions = new HashMap<>();
    public static Map<TypeDBSession, List<CompletableFuture<TypeDBTransaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<TypeDBSession>, List<CompletableFuture<TypeDBTransaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();
    private static boolean isBeforeAllRan = false;

    public static TypeDBTransaction tx() {
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
        String address = TypeDBSingleton.getTypeDBRunner().address();
        assertNotNull(address);
        client = createTypeDBClient(address);
        client.databases().all().forEach(Database::delete);
        System.out.println("ConnectionSteps.before");
    }

    void after() {
        // TODO: Remove this once the server segfault issue is fixed (typedb#6135)
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sessions.parallelStream().forEach(TypeDBSession::close);
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

    abstract TypeDBClient createTypeDBClient(String address);

    void connection_has_been_opened() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    void connection_does_not_have_any_database() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }
}
