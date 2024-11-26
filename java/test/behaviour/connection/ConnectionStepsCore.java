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
import com.typedb.driver.api.Driver;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ConnectionStepsCore extends ConnectionStepsBase {
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
    Driver createTypeDBDriver(String address) {
        return TypeDB.coreDriver(address);
    }

    @Override
    Driver createDefaultTypeDBDriver() {
        return createTypeDBDriver(TypeDB.DEFAULT_ADDRESS);
    }

//    @Override
//    Options createOptions() {
//        return new Options();
//    }

    @When("typedb starts")
    public void typedb_starts() {
    }

    @Override
    @When("connection opens with default authentication")
    public void connection_opens_with_default_authentication() {
        driver = createDefaultTypeDBDriver();
    }

    @When("connection opens with a wrong host{may_error}")
    public void connection_opens_with_a_wrong_host(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(TypeDB.DEFAULT_ADDRESS.replace("localhost", "surely-not-localhost")));
    }

    @When("connection opens with a wrong port{may_error}")
    public void connection_opens_with_a_wrong_port(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(TypeDB.DEFAULT_ADDRESS.replace("localhost", "surely-not-localhost")));
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
}
