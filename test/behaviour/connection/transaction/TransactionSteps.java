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

package grakn.client.test.behaviour.connection.transaction;

import grakn.client.GraknClient;
import grakn.client.test.behaviour.connection.ConnectionSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionSteps {

    @When("session open {number} transaction(s) of type: {transaction-type}")
    public void session_open_n_transactions_of_type(int number, GraknClient.Transaction.Type type) {
        GraknClient.Session session = ConnectionSteps.sessions.get(0);

        for (int i = 0; i < number; i++) {
            GraknClient.Transaction transaction = session.transaction(type);
            ConnectionSteps.transactionsMap.put(i, transaction);
        }
    }

    @When("session open {number} transaction(s) in parallel of type: {transaction-type}")
    public void session_open_n_transactions_in_parallel_of_type(int number, GraknClient.Transaction.Type type) {
        assertTrue(ConnectionSteps.THREAD_POOL_SIZE >= number);
        GraknClient.Session session = ConnectionSteps.sessions.get(0);

        for (int i = 0; i < number; i++) {
            ConnectionSteps.transactionsMapParallel.put(i, CompletableFuture.supplyAsync(
                    () -> session.transaction(type),
                    ConnectionSteps.threadPool
            ));
        }
    }

    @When("session open many transactions of type:")
    public void session_open_many_transactions_of_type(Map<Integer,GraknClient.Transaction.Type> types) {
        GraknClient.Session session = ConnectionSteps.sessions.get(0);

        for (Map.Entry<Integer, GraknClient.Transaction.Type> type : types.entrySet()) {
            GraknClient.Transaction transaction = session.transaction(type.getValue());
            ConnectionSteps.transactionsMap.put(type.getKey(), transaction);
        }
    }

    @When("session open many transactions in parallel of type:")
    public void session_open_many_transactions_in_parallel_of_type(Map<Integer,GraknClient.Transaction.Type> types) {
        assertTrue(ConnectionSteps.THREAD_POOL_SIZE >= types.size());
        GraknClient.Session session = ConnectionSteps.sessions.get(0);

        for (Map.Entry<Integer, GraknClient.Transaction.Type> type : types.entrySet()) {
            ConnectionSteps.transactionsMapParallel.put(type.getKey(), CompletableFuture.supplyAsync(
                    () -> session.transaction(type.getValue()),
                    ConnectionSteps.threadPool
            ));
        }
    }

    @When("sessions each open {number} transaction(s) of type: {transaction-type}")
    public void sessions_each_open_n_transactions_of_type(int number, GraknClient.Transaction.Type type) {
        int index = 0;
        for (GraknClient.Session session : ConnectionSteps.sessions) {
            for (int i = 0; i < number; i++) {
                GraknClient.Transaction transaction = session.transaction(type);
                ConnectionSteps.transactionsMap.put(index++, transaction);
            }
        }
    }

    @When("sessions each open {number} transaction(s) in parallel of type: {transaction-type}")
    public void sessions_each_open_n_transactions_in_parallel_of_type(int number, GraknClient.Transaction.Type type) {
        assertTrue(ConnectionSteps.THREAD_POOL_SIZE >= number);
        int index = 0;
        for (GraknClient.Session session : ConnectionSteps.sessions) {
            for (int i = 0; i < number; i++) {
                ConnectionSteps.transactionsMapParallel.put(index++, CompletableFuture.supplyAsync(
                        () -> session.transaction(type),
                        ConnectionSteps.threadPool
                ));
            }
        }
    }

    @Then("transaction(s) is/are null: {boolean}")
    public void transactions_are_null(Boolean isNull) {
        for (GraknClient.Transaction transaction : ConnectionSteps.transactionsMap.values()) {
            assertEquals(isNull, isNull(transaction));
        }
    }

    @Then("transaction(s) is/are open: {boolean}")
    public void transactions_are_open(Boolean isOpen) {
        for (GraknClient.Transaction transaction : ConnectionSteps.transactionsMap.values()) {
            assertEquals(isOpen, transaction.isOpen());
        }
    }

    @Then("transaction(s) has/have type: {transaction-type}")
    public void transactions_have_type(GraknClient.Transaction.Type type) {
        for (GraknClient.Transaction transaction : ConnectionSteps.transactionsMap.values()) {
            assertEquals(type, transaction.type());
        }
    }

    @Then("transaction(s) has/have keyspace: {word}")
    public void transactions_have_keyspace(String name) {
        for (GraknClient.Transaction transaction : ConnectionSteps.transactionsMap.values()) {
            assertEquals(name, transaction.keyspace().name());
        }
    }

    @Then("transactions in parallel are null: {boolean}")
    public void transactions_in_parallel_are_null(Boolean isNull) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.transactionsMapParallel
                .values().stream().map(futureTransaction -> futureTransaction.thenApplyAsync(transaction -> {
                    assertEquals(isNull, isNull(transaction));
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("transactions in parallel are open: {boolean}")
    public void transactions_in_parallel_are_open(Boolean isOpen) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.transactionsMapParallel
                .values().stream().map(futureTransaction -> futureTransaction.thenApplyAsync(transaction -> {
                    assertEquals(isOpen, transaction.isOpen());
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("transactions in parallel have type: {transaction-type}")
    public void transactions_in_parallel_have_type(GraknClient.Transaction.Type type) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.transactionsMapParallel
                .values().stream().map(futureTransaction -> futureTransaction.thenApplyAsync(transaction -> {
                    assertEquals(type, transaction.type());
                    return null;
                }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }

    @Then("transactions in parallel have keyspace: {word}")
    public void transactions_in_parallel_have_keyspace(String name) {
        Stream<CompletableFuture<Void>> assertions = ConnectionSteps.transactionsMapParallel
                .values().stream().map(futureTransaction -> futureTransaction
                        .thenApplyAsync(transaction -> {
                            assertEquals(name, transaction.keyspace().name());
                            return null;
                        }));

        CompletableFuture.allOf(assertions.toArray(CompletableFuture[]::new));
    }
}
