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

package com.vaticle.typedb.client.test.behaviour.connection.database;

import com.vaticle.typedb.client.api.database.Database;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.client;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.vaticle.typedb.common.collection.Collections.list;
import static com.vaticle.typedb.common.collection.Collections.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @When("connection delete database: {word}")
    public void connection_delete_database(String name) {
        connection_delete_databases(list(name));
    }

    @When("connection delete database(s):")
    public void connection_delete_databases(List<String> names) {
        for (String databaseName : names) {
            client.databases().get(databaseName).delete();
        }
    }

    @Then("connection delete database; throws exception: {word}")
    public void connection_delete_database_throws_exception(String name) {
        connection_delete_databases_throws_exception(list(name));
    }

    @Then("connection delete database(s); throws exception")
    public void connection_delete_databases_throws_exception(List<String> names) {
        for (String databaseName : names) {
            try {
                client.databases().get(databaseName).delete();
                fail();
            } catch (Exception e) {
                // successfully failed
            }
        }
    }

    @When("connection delete databases in parallel:")
    public void connection_delete_databases_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        CompletableFuture[] deletions = names.stream()
                .map(name -> CompletableFuture.runAsync(() -> client.databases().get(name).delete(), threadPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(deletions).join();
    }

    @When("connection has database: {word}")
    public void connection_has_database(String name) {
        connection_has_databases(list(name));
    }

    @Then("connection has database(s):")
    public void connection_has_databases(List<String> names) {
        assertEquals(set(names), client.databases().all().stream().map(Database::name).collect(Collectors.toSet()));
    }

    @Then("connection does not have database: {word}")
    public void connection_does_not_have_database(String name) {
        connection_does_not_have_databases(list(name));
    }


    @Then("connection does not have database(s):")
    public void connection_does_not_have_databases(List<String> names) {
        Set<String> databases = client.databases().all().stream().map(Database::name).collect(Collectors.toSet());
        for (String databaseName : names) {
            assertFalse(databases.contains(databaseName));
        }
    }
}
