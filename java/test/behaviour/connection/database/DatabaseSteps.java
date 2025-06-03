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

package com.typedb.driver.test.behaviour.connection.database;

import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.database.Database;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.typedb.driver.common.collection.Collections.list;
import static com.typedb.driver.common.collection.Collections.set;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.THREAD_POOL_SIZE;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.backgroundDriver;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.driver;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.fullPath;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.typedb.driver.test.behaviour.util.Util.readFileToString;
import static com.typedb.driver.test.behaviour.util.Util.removeTwoSpacesInTabulation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseSteps {
    public void createDatabases(Driver driver, List<String> names) {
        for (String name : names) {
            driver.databases().create(name);
        }
    }

    public void deleteDatabases(Driver driver, List<String> names) {
        for (String databaseName : names) {
            driver.databases().get(databaseName).delete();
        }
    }

    public String executeAndRetrieveSchemaForComparison(Driver driver, String schemaQuery) {
        String tempDatabaseName = createTemporaryDatabaseWithSchema(driver, schemaQuery);
        return driver.databases().get(tempDatabaseName).schema();
    }

    public String createTemporaryDatabaseWithSchema(Driver driver, String schemaQuery) {
        String name = "temp-" + (int) (Math.random() * 10000);
        createDatabases(driver, list(name));
        Transaction transaction = driver.transaction(name, Transaction.Type.SCHEMA);
        transaction.query(schemaQuery).resolve();
        transaction.commit();
        return name;
    }

    public void importDatabase(String name, String schema, String dataFile, Parameters.MayError mayError) {
        Path dataPath = fullPath(dataFile);
        mayError.check(() -> driver.databases().importFile(name, schema, dataPath.toString()));
    }

    @When("connection create database: {non_semicolon}{may_error}")
    public void connection_create_database(String name, Parameters.MayError mayError) {
        mayError.check(() -> createDatabases(driver, list(name)));
    }

    @When("connection create database with empty name{may_error}")
    public void connection_create_database_with_empty_name(Parameters.MayError mayError) {
        mayError.check(() -> createDatabases(driver, list("")));
    }

    @When("connection create database(s):")
    public void connection_create_databases(List<String> names) {
        createDatabases(driver, names);
    }

    @When("connection create databases in parallel:")
    public void connection_create_databases_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        CompletableFuture[] creations = names.stream()
                .map(name -> CompletableFuture.runAsync(() -> driver.databases().create(name), threadPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(creations).join();
    }

    @When("in background, connection create database: {non_semicolon}{may_error}")
    public void in_background_connection_create_database(String name, Parameters.MayError mayError) {
        mayError.check(() -> createDatabases(backgroundDriver, list(name)));
    }

    @When("connection delete database: {word}{may_error}")
    public void connection_delete_database(String name, Parameters.MayError mayError) {
        mayError.check(() -> deleteDatabases(driver, list(name)));
    }

    @When("connection delete database(s):")
    public void connection_delete_databases(List<String> names) {
        deleteDatabases(driver, names);
    }

    @When("connection delete databases in parallel:")
    public void connection_delete_databases_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        CompletableFuture[] deletions = names.stream()
                .map(name -> CompletableFuture.runAsync(() -> driver.databases().get(name).delete(), threadPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(deletions).join();
    }

    @When("in background, connection delete database: {non_semicolon}{may_error}")
    public void in_background_connection_delete_database(String name, Parameters.MayError mayError) {
        mayError.check(() -> deleteDatabases(backgroundDriver, list(name)));
    }

    @When("connection has database: {word}")
    public void connection_has_database(String name) {
        connection_has_databases(list(name));
    }

    @Then("connection has database(s):")
    public void connection_has_databases(List<String> names) {
        assertTrue(names.stream().allMatch(name -> driver.databases().contains(name)));
    }

    @Then("connection does not have database: {word}")
    public void connection_does_not_have_database(String name) {
        connection_does_not_have_databases(list(name));
    }

    @Then("connection does not have database(s):")
    public void connection_does_not_have_databases(List<String> names) {
        Set<String> databases = driver.databases().all().stream().map(Database::name).collect(Collectors.toSet());
        for (String databaseName : names) {
            assertFalse(databases.contains(databaseName));
        }
    }

    @Then("connection get database\\({word}) has schema:")
    public void connection_get_database_has_schema(String name, String expectedSchema) {
        String expectedSchemaRetrieved;
        if (expectedSchema.isBlank()) {
            expectedSchemaRetrieved = "";
        } else {
            expectedSchemaRetrieved = executeAndRetrieveSchemaForComparison(driver, expectedSchema);
        }

        String realSchema = driver.databases().get(name).schema();
        assertEquals(expectedSchemaRetrieved, realSchema);
    }

    @Then("connection get database\\({word}) has type schema:")
    public void connection_get_database_has_type_schema(String name, String expectedSchema) {
        String expectedSchemaRetrieved;
        if (expectedSchema.isBlank()) {
            expectedSchemaRetrieved = "";
        } else {
            String tempDatabaseName = createTemporaryDatabaseWithSchema(driver, expectedSchema);
            expectedSchemaRetrieved = driver.databases().get(tempDatabaseName).typeSchema();
        }

        String realSchema = driver.databases().get(name).typeSchema();
        assertEquals(expectedSchemaRetrieved, realSchema);
    }

    @Then("file\\({word}) has schema:")
    public void file_has_schema(String fileName, String expectedSchema) {
        String fileSchema = readFileToString(fullPath(fileName));
        String expectedSchemaRetrieved;
        if (expectedSchema.isBlank()) {
            expectedSchemaRetrieved = "";
        } else {
            expectedSchemaRetrieved = executeAndRetrieveSchemaForComparison(driver, removeTwoSpacesInTabulation(expectedSchema));
        }
        String fileSchemaRetrieved = executeAndRetrieveSchemaForComparison(driver, fileSchema);
        assertEquals(expectedSchemaRetrieved, fileSchemaRetrieved);
    }

    @When("connection import database\\({word}) from schema file\\({word}), data file\\({word}){may_error}")
    public void connection_import_database_from_schema_file_data_file(String name, String schemaFile, String dataFile, Parameters.MayError mayError) {
        Path schemaPath = fullPath(schemaFile);
        String schema = readFileToString(schemaPath);
        importDatabase(name, schema, dataFile, mayError);
    }

    @When("connection import database\\({word}) from data file\\({word}) and schema{may_error}")
    public void connection_import_database_from_schema_file_data_file(String name, String dataFile, Parameters.MayError mayError, String schema) {
        importDatabase(name, removeTwoSpacesInTabulation(schema), dataFile, mayError);
    }

    @When("connection get database\\({word}) export to schema file\\({word}), data file\\({word}){may_error}")
    public void connection_get_database_export_to_schema_file_data_file(String name, String schemaFile, String dataFile, Parameters.MayError mayError) {
        Path schemaPath = fullPath(schemaFile);
        Path dataPath = fullPath(dataFile);
        mayError.check(() -> driver.databases().get(name).exportFile(schemaPath.toString(), dataPath.toString()));
    }
}
