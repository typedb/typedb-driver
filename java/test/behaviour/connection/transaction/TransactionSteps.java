/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.test.behaviour.connection.transaction;

import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typeql.lang.TypeQL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.optionSetters;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessions;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionsParallel;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionsParallelToTransactionsParallel;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionsToTransactions;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.sessionsToTransactionsParallel;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.transactionOptions;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrowsWithMessage;
import static com.vaticle.typedb.common.collection.Collections.list;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("CheckReturnValue")
public class TransactionSteps {

    // =============================================//
    // sequential sessions, sequential transactions //
    // =============================================//

    @When("(for each )session(,) open(s) transaction(s) of type: {transaction_type}")
    public void session_opens_transaction_of_type(TypeDBTransaction.Type type) {
        for_each_session_open_transactions_of_type(list(type));
    }

    @When("(for each )session(,) open transaction(s) of type:")
    public void for_each_session_open_transactions_of_type(List<TypeDBTransaction.Type> types) {
        for (TypeDBSession session : sessions) {
            List<TypeDBTransaction> transactions = new ArrayList<>();
            for (TypeDBTransaction.Type type : types) {
                TypeDBTransaction transaction = session.transaction(type, transactionOptions);
                transactions.add(transaction);
            }
            sessionsToTransactions.put(session, transactions);
        }
    }

    @When("(for each )session(,) open transaction(s) of type; throws exception: {transaction_type}")
    public void for_each_session_open_transactions_of_type_throws_exception(TypeDBTransaction.Type type) {
        for_each_session_open_transactions_of_type_throws_exception(list(type));
    }

    @Then("(for each )session(,) open transaction(s) of type; throws exception")
    public void for_each_session_open_transactions_of_type_throws_exception(List<TypeDBTransaction.Type> types) {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction.Type type : types) {
                assertThrows(() -> session.transaction(type));
            }
        }
    }

    @Then("(for each )session(,) transaction(s) is/are null: {bool}")
    public void for_each_session_transactions_are_null(boolean isNull) {
        for_each_session_transactions_are(transaction -> assertEquals(isNull, isNull(transaction)));
    }

    @Then("(for each )session(,) transaction(s) is/are open: {bool}")
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

    @Then("transaction commits; throws exception containing {string}")
    public void transaction_commits_throws_exception(String exception) {
        assertThrowsWithMessage(() -> sessionsToTransactions.get(sessions.get(0)).get(0).commit(), exception);
    }

    @Then("(for each )session(,) transaction(s) commit(s)")
    public void for_each_session_transactions_commit() {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction transaction : sessionsToTransactions.get(session)) {
                transaction.commit();
            }
        }
    }

    @Then("(for each )session(,) transaction(s) commit(s); throws exception")
    public void for_each_session_transactions_commits_throws_exception() {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction transaction : sessionsToTransactions.get(session)) {
                assertThrows(transaction::commit);
            }
        }
    }

    @Then("(for each )session(,) transaction close(s)")
    public void for_each_session_transaction_closes() {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction transaction : sessionsToTransactions.get(session)) {
                transaction.close();
            }
        }
    }

    private void for_each_session_transactions_are(Consumer<TypeDBTransaction> assertion) {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction transaction : sessionsToTransactions.get(session)) {
                assertion.accept(transaction);
            }
        }
    }

    @Then("(for each )session(,) transaction(s) has/have type: {transaction_type}")
    public void for_each_session_transactions_have_type(TypeDBTransaction.Type type) {
        for_each_session_transactions_have_type(list(type));
    }

    @Then("(for each )session(,) transaction(s) has/have type:")
    public void for_each_session_transactions_have_type(List<TypeDBTransaction.Type> types) {
        for (TypeDBSession session : sessions) {
            List<TypeDBTransaction> transactions = sessionsToTransactions.get(session);
            assertEquals(types.size(), transactions.size());

            Iterator<TypeDBTransaction.Type> typesIterator = types.iterator();
            Iterator<TypeDBTransaction> transactionIterator = transactions.iterator();
            while (typesIterator.hasNext()) {
                assertEquals(typesIterator.next(), transactionIterator.next().type());
            }
        }
    }

    // ===========================================//
    // sequential sessions, parallel transactions //
    // ===========================================//

    @When("for each session, open transaction(s) in parallel of type:")
    public void for_each_session_open_transactions_in_parallel_of_type(List<TypeDBTransaction.Type> types) {
        assertTrue(THREAD_POOL_SIZE >= types.size());
        for (TypeDBSession session : sessions) {
            List<CompletableFuture<TypeDBTransaction>> transactionsParallel = new ArrayList<>();
            for (TypeDBTransaction.Type type : types) {
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

    private void for_each_session_transactions_in_parallel_are(Consumer<TypeDBTransaction> assertion) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (TypeDBSession session : sessions) {
            for (CompletableFuture<TypeDBTransaction> futureTransaction :
                    sessionsToTransactionsParallel.get(session)) {

                assertions.add(futureTransaction.thenApply(transaction -> {
                    assertion.accept(transaction);
                    return null;
                }));
            }
        }
        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    @Then("for each session, transactions in parallel have type:")
    public void for_each_session_transactions_in_parallel_have_type(List<TypeDBTransaction.Type> types) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (TypeDBSession session : sessions) {
            List<CompletableFuture<TypeDBTransaction>> futureTxs =
                    sessionsToTransactionsParallel.get(session);

            assertEquals(types.size(), futureTxs.size());

            Iterator<TypeDBTransaction.Type> typesIter = types.iterator();
            Iterator<CompletableFuture<TypeDBTransaction>> futureTxsIter = futureTxs.iterator();

            while (typesIter.hasNext()) {
                TypeDBTransaction.Type type = typesIter.next();
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

    private void for_each_session_in_parallel_transactions_in_parallel_are(Consumer<TypeDBTransaction> assertion) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
        for (CompletableFuture<TypeDBSession> futureSession : sessionsParallel) {
            for (CompletableFuture<TypeDBTransaction> futureTransaction : sessionsParallelToTransactionsParallel.get(futureSession)) {
                assertions.add(futureTransaction.thenApply(transaction -> {
                    assertion.accept(transaction);
                    return null;
                }));
            }
        }
        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    // ===================================//
    // transaction configuration          //
    // ===================================//

    @Given("set transaction option {word} to: {word}")
    public void set_transaction_option_to(String option, String value) {
        if (!optionSetters.containsKey(option)) {
            throw new RuntimeException("Unrecognised option: " + option);
        }
        optionSetters.get(option).accept(transactionOptions, value);
    }

    // ===================================//
    // transaction behaviour with queries //
    // ===================================//

    @Then("for each transaction, define query; throws exception containing {string}")
    public void for_each_transaction_execute_define_throws_exception(String expectedException, String defineQueryStatements) {
        for (TypeDBSession session : sessions) {
            for (TypeDBTransaction transaction : sessionsToTransactions.get(session)) {
                try {
                    transaction.query().define(TypeQL.parseQuery(defineQueryStatements).asDefine());
                    fail();
                } catch (Exception e) {
                    assertThat(e.getMessage(), Matchers.containsString(expectedException));
                }
            }
        }
    }
}
