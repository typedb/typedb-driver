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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static grakn.client.test.behaviour.connection.ConnectionSteps.THREAD_POOL_SIZE;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessions;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsParallelToTransactionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsToTransactions;
import static grakn.client.test.behaviour.connection.ConnectionSteps.sessionsToTransactionsParallel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.threadPool;
import static grakn.client.test.behaviour.util.Util.assertThrows;
import static grakn.common.util.Collections.list;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionSteps {

    // =============================================//
    // sequential sessions, sequential transactions //
    // =============================================//

    @When("session open transaction of type: {transaction_type}")
    public void session_opens_transaction_of_type(GraknClient.Transaction.Type type) {
        for_each_session_open_transactions_of_type(list(type));
    }

    @When("for each session, open transaction(s) of type:")
    public void for_each_session_open_transactions_of_type(List<GraknClient.Transaction.Type> types) {
        for (GraknClient.Session session : sessions) {
            List<GraknClient.Transaction> transactions = new ArrayList<>();
            for (GraknClient.Transaction.Type type : types) {
                GraknClient.Transaction transaction = session.transaction(type);
                transactions.add(transaction);
            }
            sessionsToTransactions.put(session, transactions);
        }
    }

    @Then("for each session, transaction(s) is/are null: {bool}")
    public void for_each_session_transactions_are_null(boolean isNull) {
        for_each_session_transactions_are(transaction -> assertEquals(isNull, isNull(transaction)));
    }

    @Then("for each session, transaction(s) is/are open: {bool}")
    public void for_each_session_transactions_are_open(boolean isOpen) {
        for_each_session_transactions_are(transaction -> assertEquals(isOpen, transaction.isOpen()));
    }

    @Then("transaction commits")
    public void transaction_commits() {
        sessionsToTransactions.get(sessions.get(0)).get(0).commit();
    }

    @Then("transaction commits; throws exception")
    public void transaction_commits_throws_exception() {
        assertThrows(() -> sessionsToTransactions.get(sessions.get(0)).get(0).commit());
    }

    @Then("for each session, transaction(s) commit(s)")
    public void for_each_session_transactions_commit() {
        for (GraknClient.Session session : sessions) {
            for (GraknClient.Transaction transaction : sessionsToTransactions.get(session)) {
                transaction.commit();
            }
        }
    }

    @Then("for each session, transaction(s) commit(s); throws exception")
    public void for_each_session_transactions_commits_throws_exception() {
        for (GraknClient.Session session : sessions) {
            for (GraknClient.Transaction transaction : sessionsToTransactions.get(session)) {
                assertThrows(transaction::commit);
            }
        }
    }

    @Then("for each session, transaction close(s)")
    public void for_each_session_transaction_closes() {
        for (GraknClient.Session session : sessions) {
            for (GraknClient.Transaction transaction : sessionsToTransactions.get(session)) {
                transaction.close();
            }
        }
    }

    private void for_each_session_transactions_are(Consumer<GraknClient.Transaction> assertion) {
        for (GraknClient.Session session : sessions) {
            for (GraknClient.Transaction transaction : sessionsToTransactions.get(session)) {
                assertion.accept(transaction);
            }
        }
    }

    @Then("for each session, transaction(s) has/have type:")
    public void for_each_session_transactions_have_type(List<GraknClient.Transaction.Type> types) {
        for (GraknClient.Session session : sessions) {
            List<GraknClient.Transaction> transactions = sessionsToTransactions.get(session);
            assertEquals(types.size(), transactions.size());

            Iterator<GraknClient.Transaction.Type> typesIterator = types.iterator();
            Iterator<GraknClient.Transaction> transactionIterator = transactions.iterator();
            while (typesIterator.hasNext()) {
                assertEquals(typesIterator.next(), transactionIterator.next().type());
            }
        }
    }

    // ===========================================//
    // sequential sessions, parallel transactions //
    // ===========================================//

    @When("for each session, open transaction(s) in parallel of type:")
    public void for_each_session_open_transactions_in_parallel_of_type(List<GraknClient.Transaction.Type> types) {
        assertTrue(THREAD_POOL_SIZE >= types.size());
        for (GraknClient.Session session : sessions) {
            List<CompletableFuture<GraknClient.Transaction>> transactionsParallel = new ArrayList<>();
            for (GraknClient.Transaction.Type type : types) {
                transactionsParallel.add(CompletableFuture.supplyAsync(() -> session.transaction(type), threadPool));
            }
            sessionsToTransactionsParallel.put(session, transactionsParallel);
        }
    }

    @Then("for each session, transactions in parallel are null: {bool}")
    public void for_each_session_transactions_in_parallel_are_null(boolean isNull) {
        for_each_session_transactions_in_parallel_are(transaction -> assertEquals(isNull, isNull(transaction)));
    }

    @Then("for each session, transactions in parallel are open: {bool}")
    public void for_each_session_transactions_in_parallel_are_open(boolean isOpen) {
        for_each_session_transactions_in_parallel_are(transaction -> assertEquals(isOpen, transaction.isOpen()));
    }

    private void for_each_session_transactions_in_parallel_are(Consumer<GraknClient.Transaction> assertion) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (GraknClient.Session session : sessions) {
            for (CompletableFuture<GraknClient.Transaction> futureTransaction :
                    sessionsToTransactionsParallel.get(session)) {

                assertions.add(futureTransaction.thenApply(transaction -> { assertion.accept(transaction); return null; }));
            }
        }
        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    @Then("for each session, transactions in parallel have type:")
    public void for_each_session_transactions_in_parallel_have_type(List<GraknClient.Transaction.Type> types) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (GraknClient.Session session : sessions) {
            List<CompletableFuture<GraknClient.Transaction>> futureTxs =
                    sessionsToTransactionsParallel.get(session);

            assertEquals(types.size(), futureTxs.size());

            Iterator<GraknClient.Transaction.Type> typesIter = types.iterator();
            Iterator<CompletableFuture<GraknClient.Transaction>> futureTxsIter = futureTxs.iterator();

            while (typesIter.hasNext()) {
                GraknClient.Transaction.Type type = typesIter.next();
                futureTxsIter.next().thenApplyAsync(tx -> {
                    assertEquals(type, tx.type());
                    return null;
                });
            }
        }

        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    // =========================================//
    // parallel sessions, parallel transactions //
    // =========================================//

    @Then("for each session in parallel, transactions in parallel are null: {bool}")
    public void for_each_session_in_parallel_transactions_in_parallel_are_null(boolean isNull) {
        for_each_session_in_parallel_transactions_in_parallel_are(transaction -> assertEquals(isNull, isNull(transaction)));
    }

    @Then("for each session in parallel, transactions in parallel are open: {bool}")
    public void for_each_session_in_parallel_transactions_in_parallel_are_open(boolean isOpen) {
        for_each_session_in_parallel_transactions_in_parallel_are(transaction -> assertEquals(isOpen, transaction.isOpen()));
    }

    private void for_each_session_in_parallel_transactions_in_parallel_are(Consumer<GraknClient.Transaction> assertion) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (CompletableFuture<GraknClient.Session> futureSession : sessionsParallel) {
            for (CompletableFuture<GraknClient.Transaction> futureTransaction : sessionsParallelToTransactionsParallel.get(futureSession)) {
                assertions.add(futureTransaction.thenApply(transaction -> {
                    assertion.accept(transaction);
                    return null;
                }));
            }
        }
        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }
}
