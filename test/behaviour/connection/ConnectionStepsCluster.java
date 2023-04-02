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

package com.vaticle.typedb.client.test.behaviour.connection;

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.common.test.TypeDBRunner;
import com.vaticle.typedb.common.test.cluster.TypeDBClusterRunner;
import com.vaticle.typedb.common.test.TypeDBSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConnectionStepsCluster extends ConnectionStepsBase {

    @Override
    void beforeAll() {}

    @After
    void after() {
        TypeDBSingleton.getTypeDBRunner().stop();
        TypeDBSingleton.setTypeDBRunner(null);
    }

    @Override
    TypeDBClient createTypeDBClient(String address) {
        return createTypeDBClient(address, "admin", "password", false);
    }

    TypeDBClient createTypeDBClient(String address, String username, String password, boolean tlsEnabled) {
        return TypeDB.clusterClient(address, new TypeDBCredential(username, password, tlsEnabled));
    }

    @Override
    TypeDBOptions createOptions() {
        return TypeDBOptions.cluster();
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        super.connection_has_been_opened();
    }

    @Given("connected as user {word}")
    public void connected_as_user(String username) {
        assertNotNull(client);
        assertEquals(client.asCluster().user().username(), username);
    }

    @Given("cluster has configuration")
    public void cluster_has_configuration(Map<String, String> map) {
        TypeDBSingleton.setTypeDBRunner(null);
        Map<String, String> serverOpts = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            serverOpts.put("--" + entry.getKey(), entry.getValue());
        }
        TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1, serverOpts);
        TypeDBSingleton.setTypeDBRunner(clusterRunner);
    }

    @When("cluster stops")
    public void cluster_stops() {
        TypeDBSingleton.getTypeDBRunner().stop();
    }

    @When("cluster starts")
    public void cluster_starts() throws InterruptedException {
        TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
        if (runner != null && runner.isStopped()) {
            runner.start();
        } else {
            TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1);
            TypeDBSingleton.setTypeDBRunner(clusterRunner);
            clusterRunner.start();
        }
    }

    @When("user connect: {word}, {word}")
    public void user_connect(String username, String password) {
        client = createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), username, password, false);
    }

    @When("disconnect current user")
    public void disconnect_current_user() {
        client.close();
        client = null;
    }

    @When("user connect: {word}, {word}; throws exception")
    public void user_connect_throws_exception(String username, String password) {
        assertThrows(() -> createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), username, password, false));
    }

    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        super.connection_does_not_have_any_database();
    }
}
