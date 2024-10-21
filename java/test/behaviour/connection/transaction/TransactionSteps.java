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

package com.typedb.driver.test.behaviour.connection.transaction;

import com.typedb.driver.api.Transaction;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.driver;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.transactions;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.transactionsParallel;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.txOpt;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.txPop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("CheckReturnValue")
public class TransactionSteps {

    void assertTransactionIsOpen(Optional<Transaction> transaction, boolean isOpen) {
        assertEquals(isOpen, transaction.isPresent() && transaction.get().isOpen());
    }

    void assertTransactionHasType(Transaction transaction, Transaction.Type type) {
        assertEquals(type, transaction.getType());
    }

    @When("connection open {transaction_type} transaction for database: {non_semicolon}{may_error}")
    public void connection_open_transaction_for_database(Transaction.Type type, String databaseName, Parameters.MayError mayError) {
        mayError.check(() -> {
            Transaction transaction = driver.transaction(databaseName, type);
            transactions.add(transaction);
        });
    }

    @When("connection open transaction(s) for database: {non_semicolon}, of type:")
    public void connection_open_transactions_for_database_of_type(String databaseName, List<Transaction.Type> types) {
        for (Transaction.Type type : types) {
            Transaction transaction = driver.transaction(databaseName, type);
            transactions.add(transaction);
        }
    }

    @Then("transaction is open: {bool}")
    public void transaction_is_open(boolean isOpen) {
        assertTransactionIsOpen(txOpt(), isOpen);
    }

    @Then("transactions are open: {bool}")
    public void transactions_are_open(boolean isOpen) {
        for (Transaction transaction : transactions) {
            assertTransactionIsOpen(Optional.of(transaction), isOpen);
        }
    }

    @Then("transaction has type: {transaction_type}")
    public void transaction_has_type(Transaction.Type type) {
        assertTransactionHasType(tx(), type);
    }

    @Then("transactions have type:")
    public void transactions_have_type(List<Transaction.Type> types) {
        Iterator<Transaction.Type> typeIterator = types.iterator();
        for (Transaction transaction : transactions) {
            assertTrue("types list is shorter than saved transactions", typeIterator.hasNext());
            assertTransactionHasType(transaction, typeIterator.next());
        }
        assertFalse("types list is longer than saved transactions", typeIterator.hasNext());
    }

    @Then("transaction commits{may_error}")
    public void transaction_commits(Parameters.MayError mayError) {
        mayError.check(() -> txPop().commit());
    }

    @Then("transaction closes{may_error}")
    public void transaction_closes(Parameters.MayError mayError) {
        mayError.check(() -> txPop().close());
    }

    @Then("transaction rollbacks{may_error}")
    public void transaction_rollbacks(Parameters.MayError mayError) {
        mayError.check(() -> txPop().rollback());
    }

    @When("connection open transactions in parallel for database: {non_semicolon}, of type:")
    public void open_transactions_in_parallel_of_type(String name, List<Transaction.Type> types) {
        assertTrue(THREAD_POOL_SIZE >= types.size());
        for (Transaction.Type type : types) {
            transactionsParallel.add(CompletableFuture.supplyAsync(() -> driver.transaction(name, type), threadPool));
        }
    }

    @Then("transactions in parallel are open: {bool}")
    public void transactions_in_parallel_are_open(boolean isOpen) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();

        for (CompletableFuture<Transaction> futureTransaction : transactionsParallel) {
            assertions.add(futureTransaction.thenApply(transaction -> {
                assertTransactionIsOpen(Optional.of(transaction), isOpen);
                return null;
            }));
        }

        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    @Then("transactions in parallel have type:")
    public void transactions_in_parallel_have_type(List<Transaction.Type> types) {
        Iterator<Transaction.Type> typeIterator = types.iterator();
        List<CompletableFuture<Void>> assertions = new ArrayList<>();

        for (CompletableFuture<Transaction> futureTransaction : transactionsParallel) {
            assertions.add(futureTransaction.thenApply(transaction -> {
                assertTrue("types list is shorter than saved transactions", typeIterator.hasNext());
                assertTransactionHasType(transaction, typeIterator.next());
                return null;
            }));
        }

        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
        assertFalse("types list is longer than saved transactions", typeIterator.hasNext());
    }

//    @Given("set transaction option {word} to: {word}")
//    public void set_transaction_option_to(String option, String value) {
//        if (!optionSetters.containsKey(option)) {
//            throw new RuntimeException("Unrecognised option: " + option);
//        }
//        optionSetters.get(option).accept(transactionOptions, value);
//    }
}
