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

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;

public class TransactionSteps {

    @When("session open {number} transaction(s) of type: {transaction-type}")
    public void session_open_n_transactions(int number, GraknClient.Transaction.Type type) {
        GraknClient.Session session = ConnectionSteps.sessionsMap.get(0);

        for (int i = 0; i < number; i++) {
            GraknClient.Transaction transaction = type.equals(GraknClient.Transaction.Type.READ) ?
                    session.transaction().read() :
                    session.transaction().write();
            ConnectionSteps.transactionsMap.put(i, transaction);
        }
    }

    @Then("transaction(s) is/are null: {boolean}")
    public void transactions_are_null(Boolean isNull) {
        for (GraknClient.Transaction transaction: ConnectionSteps.transactionsMap.values()) {
            assertEquals(isNull(transaction), isNull);
        }
    }

    @Then("transaction(s) is/are open: {boolean}")
    public void transactions_are_open(Boolean isOpen) {
        for (GraknClient.Transaction transaction: ConnectionSteps.transactionsMap.values()) {
            assertEquals(transaction.isOpen(), isOpen);
        }
    }

    @Then("transaction(s) has/have type: {transaction-type}")
    public void transactions_have_type(GraknClient.Transaction.Type type) {
        for (GraknClient.Transaction transaction: ConnectionSteps.transactionsMap.values()) {
            assertEquals(transaction.type(), type);
        }
    }

    @Then("transaction(s) has/have keyspace: {word}")
    public void transactions_have_keyspace(String name) {
        for (GraknClient.Transaction transaction: ConnectionSteps.transactionsMap.values()) {
            assertEquals(transaction.keyspace().name(), name);
        }
    }
}
