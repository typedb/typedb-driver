/*
 *  Copyright (C) 2021 Vaticle
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
import com.vaticle.typedb.common.test.server.TypeDBSingleton;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.client;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class UserSteps {

    private TypeDBClient.Cluster getClient() {
        assert client.isCluster();
        return (TypeDBClient.Cluster) client;
    }

    @Given("users contains: {word}")
    public void users_contains(String username) {
        Set<String> users = getClient().users().all().stream().map(User::username).collect(Collectors.toSet());
        assertTrue(users.contains(username));
    }

    @Then("users not contains: {word}")
    public void not_users_contains(String username) {
        Set<String> users = getClient().users().all().stream().map(User::username).collect(Collectors.toSet());
        assertFalse(users.contains(username));
    }

    @Then("users create: {word}, {word}")
    public void users_create(String username, String password) {
        getClient().users().create(username, password);
    }

    @Then("user password: {word}, {word}")
    public void user_password(String username, String password) {
        getClient().users().get(username).password(password);
    }

    @Then("user connect: {word}, {word}")
    public void user_connect(String username, String password) {
        String address = TypeDBSingleton.getTypeDBRunner().address();
        TypeDBCredential credential = new TypeDBCredential(username, password, false);
        try (TypeDBClient.Cluster client = TypeDB.clusterClient(address, credential)) {
            List<Database.Cluster> ignored = client.databases().all();
        }
    }

    @Then("user delete: {word}")
    public void user_delete(String username) {
        getClient().users().get(username).delete();
    }
}
