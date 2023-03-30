/*
 *  Copyright (C) 2022 Vaticle
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.vaticle.typedb.client.test.behaviour.connection.user;

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.common.test.TypeDBSingleton;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.client;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class UserSteps {

    private TypeDBClient.Cluster getClient() {
        assert client.isCluster();
        return (TypeDBClient.Cluster) client;
    }

    public boolean user_is_in_users(String username) {
        Set<String> users = getClient().users().all().stream().map(User::username).collect(Collectors.toSet());
        return users.contains(username);
    }

    @Then("users contains: {word}")
    public void users_contains(String username) {
        assertTrue(user_is_in_users(username));
    }

    @Then("users not contains: {word}")
    public void not_users_contains(String username) {
        assertFalse(user_is_in_users(username));
    }

    @When("users create: {word}, {word}")
    public void users_create(String username, String password) {
        getClient().users().create(username, password);
    }

    @When("users delete: {word}")
    public void users_delete(String username) {
        getClient().users().delete(username);
    }

    @When("users password set: {word}, {word}")
    public void user_password_set(String username, String password) {
        getClient().users().passwordSet(username, password);
    }

    @When("disconnect current user")
    public void disconnect_current_user() {
        client.close();
        client = null;
    }

    @When("user password update: {word}, {word}, {word}")
    public void user_password_update(String username, String passwordOld, String passwordNew) {
        getClient().users().get(username).passwordUpdate(passwordOld, passwordNew);
    }

    @When("user password set: {word}, {word}")
    public void user_password_update(String username, String passwordNew) {
        getClient().users().passwordSet(username, passwordNew);
    }

    @Then("users contains: {word}; throws exception")
    public void users_contains_throws_exception(String username) {
        assertThrows(() -> user_is_in_users(username));
    }

    @Then("users not contains: {word}; throws exception")
    public void not_users_contains_throws_exception(String username) {
        assertThrows(() -> user_is_in_users(username));
    }

    @When("users create: {word}, {word}; throws exception")
    public void users_create_throws_exception(String username, String password) {
        assertThrows(() -> getClient().users().create(username, password));
    }

    @When("users delete: {word}; throws exception")
    public void users_delete_throws_exception(String username) {
        assertThrows(() -> getClient().users().delete(username));
    }

    @When("users password set: {word}, {word}; throws exception")
    public void user_password_set_throws_exception(String username, String password) {
        assertThrows(() -> getClient().users().passwordSet(username, password));
    }

    @When("user password set: {word}, {word}; throws exception")
    public void user_password_update_throws_exception(String username, String passwordNew) {
        assertThrows(() -> getClient().users().passwordSet(username, passwordNew));
    }

    @When("user connect: {word}, {word}")
    public void user_connect(String username, String password) {
        String address = TypeDBSingleton.getTypeDBRunner().address();
        TypeDBCredential credential = new TypeDBCredential(username, password, false);
        try (TypeDBClient.Cluster client = TypeDB.clusterClient(address, credential)) {
            List<Database.Cluster> ignored = client.databases().all();
        }
    }
}
