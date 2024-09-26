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

package com.vaticle.typedb.driver.test.behaviour.connection.transaction;

import com.vaticle.typedb.driver.api.Transaction;
import com.vaticle.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.vaticle.typedb.driver.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.vaticle.typedb.driver.test.behaviour.connection.ConnectionStepsBase.driver;
import static com.vaticle.typedb.driver.test.behaviour.connection.ConnectionStepsBase.transactions;
import static com.vaticle.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("CheckReturnValue")
public class TransactionSteps {

    // ======================= //
    // sequential transactions //
    // ======================= //

    @When("connection open {transaction_type} transaction for database: {string}{may_error}")
    public void connection_open_transaction_for_database(Transaction.Type type, String databaseName, Parameters.MayError mayError) {
        mayError.check(() -> {
            Transaction transaction = driver.transaction(databaseName, type/*, transactionOptions*/);
            transactions.add(transaction);
        });
    }

    @Then("transaction is open: {bool}")
    public void transaction_is_open(boolean isOpen) {
        assertEquals(isOpen, tx().isOpen());
    }

    @Then("transaction has type: {transaction_type}")
    public void transaction_has_type(Transaction.Type type) {
        assertEquals(type, tx().getType());
    }

    @Then("transaction commits{may_error}")
    public void transaction_commits(Parameters.MayError mayError) {
        mayError.check(() -> tx().commit());
    }

    @Then("transaction closes{may_error}")
    public void transaction_closes(Parameters.MayError mayError) {
        mayError.check(() -> tx().close());
    }

    // ===================== //
    // parallel transactions //
    // ===================== //

    @When("open transactions in parallel of type:")
    public void open_transactions_in_parallel_of_type(List<Transaction.Type> types) {
        assertTrue(THREAD_POOL_SIZE >= types.size());
        List<CompletableFuture<Transaction>> transactionsParallel = new ArrayList<>();
//        for (Transaction.Type type : types) {
//            transactionsParallel.add(CompletableFuture.supplyAsync(() -> session.transaction(type), threadPool));
//        }
    }

    @Then("for each session, transactions in parallel are null: {bool}")
    public void for_each_session_transactions_in_parallel_are_null(boolean isNull) {
        for_each_session_transactions_in_parallel_are(transaction -> assertEquals(isNull, isNull(transaction)));
    }

    @Then("for each session, transactions in parallel are open: {bool}")
    public void for_each_session_transactions_in_parallel_are_open(boolean isOpen) {
        for_each_session_transactions_in_parallel_are(transaction -> assertEquals(isOpen, transaction.isOpen()));
    }

    private void for_each_session_transactions_in_parallel_are(Consumer<Transaction> assertion) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
//        for (TypeDBSession session : sessions) {
//            for (CompletableFuture<Transaction> futureTransaction :
//                    sessionsToTransactionsParallel.get(session)) {
//
//                assertions.add(futureTransaction.thenApply(transaction -> {
//                    assertion.accept(transaction);
//                    return null;
//                }));
//            }
//        }
        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    @Then("for each session, transactions in parallel have type:")
    public void for_each_session_transactions_in_parallel_have_type(List<Transaction.Type> types) {
        List<CompletableFuture<Void>> assertions = new ArrayList<>();
//        for (TypeDBSession session : sessions) {
//            List<CompletableFuture<Transaction>> futureTxs =
//                    sessionsToTransactionsParallel.get(session);
//
//            assertEquals(types.size(), futureTxs.size());
//
//            Iterator<Transaction.Type> typesIter = types.iterator();
//            Iterator<CompletableFuture<Transaction>> futureTxsIter = futureTxs.iterator();
//
//            while (typesIter.hasNext()) {
//                Transaction.Type type = typesIter.next();
//                futureTxsIter.next().thenApplyAsync(tx -> {
//                    assertEquals(type, tx.type());
//                    return null;
//                });
//            }
//        }

        CompletableFuture.allOf(assertions.toArray(new CompletableFuture[0])).join();
    }

    // ========================= //
    // transaction configuration //
    // ========================= //

//    @Given("set transaction option {word} to: {word}")
//    public void set_transaction_option_to(String option, String value) {
//        if (!optionSetters.containsKey(option)) {
//            throw new RuntimeException("Unrecognised option: " + option);
//        }
//        optionSetters.get(option).accept(transactionOptions, value);
//    }
}
