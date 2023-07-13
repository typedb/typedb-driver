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
import com.vaticle.typedb.common.test.TypeDBSingleton;
import com.vaticle.typedb.common.test.cluster.TypeDBClusterRunner;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;

public class ConnectionStepsCluster extends ConnectionStepsBase {

    @Override
    public void beforeAll() {
        super.beforeAll();
    }

    @Before
    public synchronized void before() {
        super.before();
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
        return new TypeDBOptions();
    }

    @Override
    @When("connection opens with default authentication")
    public void connection_opens_with_default_authentication() {
        client = createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address());
    }

    @When("connection opens with authentication: {word}, {word}")
    public void connection_opens_with_authentication(String username, String password) {
        if (client != null) {
            client.close();
            client = null;
        }

        client = createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), username, password, false);
    }

    @When("connection opens with authentication: {word}, {word}; throws exception")
    public void connection_opens_with_authentication_throws_exception(String username, String password) {
        assertThrows(() -> createTypeDBClient(TypeDBSingleton.getTypeDBRunner().address(), username, password, false));
    }

    @Override
    @Given("connection has been opened")
    public void connection_has_been_opened() {
        super.connection_has_been_opened();
    }

    @Override
    @When("connection closes")
    public void connection_closes() {
        super.connection_closes();
    }

    @Given("typedb has configuration")
    public void typedb_has_configuration(Map<String, String> map) {
        TypeDBSingleton.deleteTypeDBRunner();
        Map<String, String> serverOpts = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            serverOpts.put("--" + entry.getKey(), entry.getValue());
        }
        TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1, serverOpts);
        TypeDBSingleton.setTypeDBRunner(clusterRunner);
    }

    @When("typedb starts")
    public void typedb_starts() {
        TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
        if (runner != null && runner.isStopped()) {
            runner.start();
        } else {
            TypeDBClusterRunner clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1);
            TypeDBSingleton.setTypeDBRunner(clusterRunner);
            clusterRunner.start();
        }
    }

    @When("typedb stops")
    public void typedb_stops() {
        TypeDBSingleton.getTypeDBRunner().stop();
    }


    @Override
    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        super.connection_does_not_have_any_database();
    }
}
