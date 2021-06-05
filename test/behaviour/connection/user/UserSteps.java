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

import com.vaticle.typedb.client.api.connection.TypeDBClient;
import com.vaticle.typedb.client.api.connection.user.User;
import io.cucumber.java.en.When;

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

    @When("users contains: {word}")
    public void users_contains(String name) {
        Set<String> users = getClient().users().all().stream().map(User::name).collect(Collectors.toSet());
        assertTrue(users.contains(name));
    }

    @When("not users contains: {word}")
    public void not_users_contains(String name) {
        Set<String> users = getClient().users().all().stream().map(User::name).collect(Collectors.toSet());
        assertFalse(users.contains(name));
    }

    @When("users create: {word}, {word}")
    public void users_create(String name, String password) {
        getClient().users().create(name, password);
    }

    @When("user delete: {word}")
    public void user_delete(String name) {
        getClient().users().get(name).delete();
    }

}
