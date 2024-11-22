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

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public abstract class ConnectionStepsBase {
    public static final Map<String, String> serverOptions = Collections.emptyMap();
    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    public static Driver driver;
    public static Driver backgroundDriver;
    public static List<Transaction> transactions = new ArrayList<>();
    public static List<CompletableFuture<Transaction>> transactionsParallel = new ArrayList<>();

    //    public static final Map<String, BiConsumer<Options, String>> optionSetters = map(
//            pair("transaction-timeout-millis", (option, val) -> option.transactionTimeoutMillis(Integer.parseInt(val)))
//    );
    //    public static Options transactionOptions;
    static boolean isBeforeAllRan = false;

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
        cleanupTransactions();

        driver = createTypeDBDriver(TypeDB.DEFAULT_ADDRESS);
        driver.databases().all().forEach(Database::delete);
        driver.close();
        backgroundDriver.close();
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

    abstract Driver createTypeDBDriver(String address);

    abstract Driver createDefaultTypeDBDriver();

//    abstract Options createOptions();

    abstract void connection_opens_with_default_authentication();

    void connection_closes() {
        cleanupTransactions();
        driver.close();
    }

    void connection_is_open(boolean isOpen) {
        assertEquals(isOpen, driver != null && driver.isOpen());
    }

    void connection_has_count_databases(int count) {
        assertEquals(count, driver.databases().all().size());
    }
}
