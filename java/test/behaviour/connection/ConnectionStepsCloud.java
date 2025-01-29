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
package com.typedb.driver.test.behaviour.connection;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ConnectionStepsCloud extends ConnectionStepsBase {
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
    Driver createTypeDBDriver(String address, Credentials credentials, DriverOptions driverOptions) {
        return TypeDB.clusterDriver(address, credentials, driverOptions);
    }

    @Override
    Driver createDefaultTypeDBDriver() {
        // TODO: Add encryption to cluster tests
        return createTypeDBDriver(TypeDB.DEFAULT_ADDRESS, DEFAULT_CREDENTIALS, DEFAULT_CONNECTION_SETTINGS);
    }

//    @Override
//    Options createOptions() {
//        return new Options();
//    }

    @When("typedb starts")
    public void typedb_starts() {
    }

    @When("connection opens with default authentication")
    public void connection_opens_with_default_authentication() {
        driver = createDefaultTypeDBDriver();
    }

    @When("connection opens with username '{non_semicolon}', password '{non_semicolon}'{may_error}")
    public void connection_opens_with_username_password(String username, String password, Parameters.MayError mayError) {
        Credentials credentials = new Credentials(username, password);
        mayError.check(() -> driver = createTypeDBDriver(TypeDB.DEFAULT_ADDRESS, credentials, DEFAULT_CONNECTION_SETTINGS));
    }

    @When("connection opens with a wrong host{may_error}")
    public void connection_opens_with_a_wrong_host(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(
                TypeDB.DEFAULT_ADDRESS.replace("localhost", "surely-not-localhost"),
                DEFAULT_CREDENTIALS,
                DEFAULT_CONNECTION_SETTINGS
        ));
    }

    @When("connection opens with a wrong port{may_error}")
    public void connection_opens_with_a_wrong_port(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(
                TypeDB.DEFAULT_ADDRESS.replace("localhost", "surely-not-localhost"),
                DEFAULT_CREDENTIALS,
                DEFAULT_CONNECTION_SETTINGS
        ));
    }

    @Override
    @When("connection closes")
    public void connection_closes() {
        super.connection_closes();
    }

    @Override
    @Given("connection is open: {bool}")
    public void connection_is_open(boolean isOpen) {
        super.connection_is_open(isOpen);
    }

    @Override
    @Given("connection has {integer} database(s)")
    public void connection_has_count_databases(int count) {
        super.connection_has_count_databases(count);
    }

    @Override
    @Given("connection has {integer} user(s)")
    public void connection_has_count_users(int count) {
        super.connection_has_count_users(count);
    }
}
