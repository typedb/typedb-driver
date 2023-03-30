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
import java.util.Map;

import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConnectionStepsCluster extends ConnectionStepsBase {

    @Override
    void beforeAll() {
        TypeDBClusterRunner cluster = TypeDBClusterRunner.create(Paths.get("."), 1);
        TypeDBSingleton.setTypeDBRunner(cluster);
    }

    @Before
    public synchronized void before() {
        super.before();
        TypeDBRunner cluster = TypeDBSingleton.getTypeDBRunner();
        cluster.start();
        TypeDBClient.Cluster clusterClient = createTypeDBClient(cluster.address()).asCluster();
        clusterClient.users().all().stream().filter(user -> !user.username().equals("admin"))
                .forEach(user -> clusterClient.users().delete(user.username()));
        cluster.stop();
    }

    @After
    public synchronized void after() {
        super.after();
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
    public void connected_as_user(String user) {
        assertNotNull(client);
        assertEquals(client.asCluster().username(), user);
    }

    @Given("cluster has configuration")
    public void cluster_has_configuration(Map<String, String> serverOpts) {
        TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1, serverOpts);
        TypeDBSingleton.setTypeDBRunner(clusterRunner);
    }

    @When("cluster stops")
    public void cluster_stops() {
        TypeDBSingleton.getTypeDBRunner().stop();
    }

    @When("cluster starts")
    public void cluster_starts() {
        TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
        if (runner != null) {
            runner.start();
        } else {
            TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1);
            TypeDBSingleton.setTypeDBRunner(clusterRunner);
            clusterRunner.start();
        }
    }

    @When("connect as user {word} with password {word}")
    public void connect_as_user_with_password(String user, String password) {
        client = createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), user, password, false);
    }

    @When("connect as user {word} with password {word}; throws exception")
    public void connect_as_user_with_password_throws_exception(String user, String password) {
        assertThrows(() -> createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), user, password, false));
    }

    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        super.connection_does_not_have_any_database();
    }
}
