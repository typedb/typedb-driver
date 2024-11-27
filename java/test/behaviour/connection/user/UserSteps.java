/*
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

package com.typedb.driver.test.behaviour.connection.user;

import com.typedb.driver.api.user.User;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.stream.Collectors;

import static com.typedb.driver.common.collection.Collections.set;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.driver;
import static org.junit.Assert.assertEquals;

public class UserSteps {
    public boolean user_is_in_users(String username) {
        return driver.users().all().stream().map(User::name).collect(Collectors.toSet()).contains(username);
    }

    @Then("get all users:")
    public void get_all_users(List<String> names) {
        assertEquals(set(names), driver.users().all().stream().map(User::name).collect(Collectors.toSet()));
    }

    @Then("get all users{may_error}")
    public void get_all_users_error(Parameters.MayError mayError) {
        mayError.check(() -> driver.users().all());
    }

    @Then("get all users {contains_or_doesnt}: {non_semicolon}")
    public void get_all_users_contains(Parameters.ContainsOrDoesnt containsOrDoesnt, String username) {
        containsOrDoesnt.check(user_is_in_users(username));
    }

    @Then("get user: {non_semicolon}{may_error}")
    public void get_user_error(String username, Parameters.MayError mayError) {
        mayError.check(() -> driver.users().get(username));
    }

    @Then("get user\\({non_semicolon}) get name: {non_semicolon}")
    public void get_user_error(String user, String name) {
        assertEquals(name, driver.users().get(user).name());
    }

    @When("create user with username '{non_semicolon}', password '{non_semicolon}'{may_error}")
    public void create_user(String username, String password, Parameters.MayError mayError) {
        mayError.check(() -> driver.users().create(username, password));
    }

    @When("get user\\({non_semicolon}) set password: {non_semicolon}{may_error}")
    public void get_user_set_password(String username, String passwordNew, Parameters.MayError mayError) {
        mayError.check(() -> driver.users().setPassword(username, passwordNew));
    }

    @When("get user\\({non_semicolon}) update password from '{non_semicolon}' to '{non_semicolon}'{may_error}")
    public void get_user_update_password(String username, String passwordOld, String passwordNew, Parameters.MayError mayError) {
        mayError.check(() -> driver.users().get(username).updatePassword(passwordOld, passwordNew));
    }

    @When("delete user: {non_semicolon}{may_error}")
    public void delete_user(String username, Parameters.MayError mayError) {
        mayError.check(() -> driver.users().delete(username));
    }

    @Then("get current username: {non_semicolon}")
    public void get_current_username(String username) {
        assertEquals(username, driver.users().getCurrentUsername());
    }
}
