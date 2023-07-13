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

import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.user.User;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.client;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserSteps {
    private TypeDBClient getClient() {
        return client;
    }

    public boolean user_is_in_users(String username) {
        Set<String> users = getClient().users().all().stream().map(User::username).collect(Collectors.toSet());
        return users.contains(username);
    }

    @Then("get connected user")
    public void get_connected_user() {
        User ignored = getClient().user();
    }

    @Then("users get user: {word}")
    public void users_get(String username) {
        User ignored = getClient().users().get(username);
    }

    @Then("users get all")
    public void users_get_all() {
        Set<User> ignored = getClient().users().all();
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

    @When("user password update: {word}, {word}")
    public void user_password_update(String passwordOld, String passwordNew) {
        getClient().users().get(getClient().user().username()).passwordUpdate(passwordOld, passwordNew);
    }

    @Then("user expiry-seconds")
    public void user_expiry_seconds() {
        getClient().user().passwordExpirySeconds();
    }

    @When("users password set: {word}, {word}")
    public void user_password_set(String username, String passwordNew) {
        getClient().users().passwordSet(username, passwordNew);
    }

    @Then("users get user: {word}; throws exception")
    public void users_get_throws_exception(String username) {
        assertThrows(() -> {
            User ignored = getClient().users().get(username);
        });
    }

    @Then("users get all; throws exception")
    public void users_get_all_throws_exception() {
        assertThrows(() -> {
            Set<User> ignored = getClient().users().all();
        });
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

    @When("user password update: {word}, {word}; throws exception")
    public void user_password_update_throws_exception(String passwordOld, String passwordNew) {
        assertThrows(() -> getClient().users().get(client.user().username()).passwordUpdate(passwordOld, passwordNew));
    }

    @When("users password set: {word}, {word}; throws exception")
    public void users_password_update_throws_exception(String username, String passwordNew) {
        assertThrows(() -> getClient().users().passwordSet(username, passwordNew));
    }
}
