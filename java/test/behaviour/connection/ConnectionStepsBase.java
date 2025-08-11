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

package com.typedb.driver.test.behaviour.connection;

import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.QueryOptions;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.TransactionOptions;
import com.typedb.driver.api.server.ServerVersion;
import com.typedb.driver.test.behaviour.config.Parameters;
import com.typedb.driver.test.behaviour.util.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.typedb.driver.test.behaviour.util.Util.createTempDir;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class ConnectionStepsBase {
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "password";
    public static final Credentials DEFAULT_CREDENTIALS = new Credentials(ADMIN_USERNAME, ADMIN_PASSWORD);
    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    public static Driver driver;
    public static Driver backgroundDriver;
    private static Optional<Path> tempDir = Optional.empty();
    public static List<Transaction> transactions = new ArrayList<>();
    public static List<Transaction> backgroundTransactions = new ArrayList<>();
    public static List<CompletableFuture<Transaction>> transactionsParallel = new ArrayList<>();

    public static DriverOptions driverOptions = new DriverOptions().tlsEnabled(false);
    public static Optional<TransactionOptions> transactionOptions = Optional.empty();
    public static Optional<QueryOptions> queryOptions = Optional.empty();
    static boolean isBeforeAllRan = false;
    static final int BEFORE_TIMEOUT_MILLIS = 10;

    public static Path tempDir() {
        if (tempDir.isEmpty()) {
            tempDir = Optional.of(createTempDir());
        }
        return tempDir.get();
    }

    public static Path fullPath(String fileName) {
        return tempDir().resolve(fileName);
    }

    public static Transaction tx() {
        return transactions.get(0);
    }

    public static Transaction txPop() {
        return transactions.remove(0);
    }

    public static Optional<Transaction> txOpt() {
        return transactions.isEmpty() ? Optional.empty() : Optional.of(transactions.get(0));
    }

    void beforeAll() {
    } // Can add "before all" setup steps here

    void before() {
        try {
            // Sleep between scenarios to let the driver close completely
            // (`close` is not synced and can cause lock failures in CI)
            // TODO: This might be a bug requiring an investigation. For some reason, it only happens in Java (even not in Python!)
            Thread.sleep(BEFORE_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected exception while sleeping in before:" + e);
        }

        if (!isBeforeAllRan) {
            try {
                beforeAll();
            } finally {
                isBeforeAllRan = true;
            }
        }

        backgroundDriver = createDefaultTypeDBDriver();
    }

    void after() {
        tempDir.ifPresent(Util::deleteDir);
        tempDir = Optional.empty();

        cleanupTransactions();
        cleanupBackgroundTransactions();
        transactionOptions = Optional.empty();
        queryOptions = Optional.empty();

        driver = createDefaultTypeDBDriver();
        driver.users().all().stream().filter(user -> !user.name().equals(ADMIN_USERNAME)).forEach(user -> driver.users().get(user.name()).delete());
        driver.users().get(ADMIN_USERNAME).updatePassword(ADMIN_PASSWORD);
        driver.databases().all().forEach(database -> driver.databases().get(database.name()).delete());
        driver.close();
        backgroundDriver.close();
        driverOptions = new DriverOptions().tlsEnabled(false);
    }

    void cleanupTransactions() {
        transactions.parallelStream().forEach(Transaction::close);
        transactions.clear();

        Stream<CompletableFuture<Void>> closures = transactionsParallel
                .stream().map(futureTransaction -> futureTransaction.thenApplyAsync(transaction -> {
                    transaction.close();
                    return null;
                }));
        CompletableFuture.allOf(closures.toArray(CompletableFuture[]::new)).join();
        transactionsParallel.clear();
    }

    void cleanupBackgroundTransactions() {
        backgroundTransactions.parallelStream().forEach(Transaction::close);
        backgroundTransactions.clear();
    }

    abstract Driver createDefaultTypeDBDriver();

    public static void initTransactionOptionsIfNeeded() {
        if (transactionOptions.isEmpty()) {
            transactionOptions = Optional.of(new TransactionOptions());
        }
    }

    public static void initQueryOptionsIfNeeded() {
        if (queryOptions.isEmpty()) {
            queryOptions = Optional.of(new QueryOptions());
        }
    }

    abstract void connection_opens_with_default_authentication();

    abstract void connection_opens_with_username_password(String username, String password, Parameters.MayError mayError);

    void connection_closes() {
        cleanupTransactions();
        driver.close();
    }

    void connection_is_open(boolean isOpen) {
        assertEquals(isOpen, driver != null && driver.isOpen());
    }

    void connection_contains_distribution(Parameters.MayError mayError) {
        mayError.check(() -> {
            ServerVersion serverVersion = driver.serverVersion();
            assertFalse(serverVersion.getDistribution().isEmpty());
        });
    }

    void connection_contains_version(Parameters.MayError mayError) {
        mayError.check(() -> {
            ServerVersion serverVersion = driver.serverVersion();
            assertFalse(serverVersion.getVersion().isEmpty());
        });
    }

    void connection_has_count_replicas(int count) {
        assertEquals(driver.replicas().size(), count);
    }

    void connection_contains_primary_replica() {
        assertTrue(driver.primaryReplica().isPresent());
    }

    void connection_has_count_databases(int count) {
        assertEquals(count, driver.databases().all().size());
    }

    void connection_has_count_users(int count) {
        assertEquals(count, driver.users().all().size());
    }

    void set_driver_option_use_replication_to(boolean value) {
        driverOptions = driverOptions.useReplication(value);
    }

    void set_driver_option_primary_failover_retries_to(int value) {
        driverOptions = driverOptions.primaryFailoverRetries(value);
    }

    void set_driver_option_replica_discovery_attempts_to(int value) {
        driverOptions = driverOptions.replicaDiscoveryAttempts(value);
    }
}
