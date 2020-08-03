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

package grakn.client.test.behaviour.connection.database;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static grakn.client.test.behaviour.connection.ConnectionSteps.THREAD_POOL_SIZE;
import static grakn.client.test.behaviour.connection.ConnectionSteps.client;
import static grakn.client.test.behaviour.connection.ConnectionSteps.threadPool;
import static grakn.common.util.Collections.list;
import static grakn.common.util.Collections.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseSteps {

    @When("connection create database: {word}")
    public void connection_create_database(String name) {
        connection_create_databases(list(name));
    }

    @When("connection create database(s):")
    public void connection_create_databases(List<String> names) {
        for (String name : names) {
            client.databases().create(name);
        }
    }

    @When("connection create databases in parallel:")
    public void connection_create_databases_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        CompletableFuture[] creations = names.stream()
                .map(name -> CompletableFuture.runAsync(() -> client.databases().create(name), threadPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(creations).join();
    }

    @When("connection delete database(s):")
    public void connection_delete_databases(List<String> names) {
        for (String databaseName : names) {
            client.databases().delete(databaseName);
        }
    }

    @When("connection delete databases in parallel:")
    public void connection_delete_databases_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        CompletableFuture[] deletions = names.stream()
                .map(name -> CompletableFuture.runAsync(() -> client.databases().delete(name), threadPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(deletions).join();
    }

    @Then("connection has database(s):")
    public void connection_has_databases(List<String> names) {
        assertEquals(set(names), set(client.databases().all()));
    }

    @Then("connection does not have database(s):")
    public void connection_does_not_have_databases(List<String> names) {
        Set<String> databases = set(client.databases().all());
        for (String databaseName : names) {
            assertFalse(databases.contains(databaseName));
        }
    }
}
