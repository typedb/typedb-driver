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

//import com.typedb.core.tool.runner.TypeDBRunner;
//import com.typedb.core.tool.runner.TypeDBSingleton;
//import com.typedb.core.tool.runner.TypeDBCoreRunner;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ConnectionStepsCore extends ConnectionStepsBase {
    @Override
    public void beforeAll() {
        super.beforeAll();
//        try {
//            TypeDBCoreRunner typeDBCoreRunner = new TypeDBCoreRunner(serverOptions);
//            TypeDBSingleton.setTypeDBRunner(typeDBCoreRunner);
//            typeDBCoreRunner.start();
//        } catch (InterruptedException | java.util.concurrent.TimeoutException | java.io.IOException e) {
//            e.printStackTrace();
//        }
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

//    @Override
//    Options createOptions() {
//        return new Options();
//    }

    @When("typedb starts")
    public void typedb_starts() {
//        TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
//        if (runner != null && runner.isStopped()) {
//            runner.start();
//        }
    }

    @Override
    @When("connection opens with default authentication")
    public void connection_opens_with_default_authentication() {
//        driver = createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address());
        driver = createTypeDBDriver("127.0.0.1:1729");
    }

    @Override
    @When("connection closes")
    public void driver_closes() {
        super.driver_closes();
    }

    @Override
    @Given("connection has been opened")
    public void connection_has_been_opened() {
        super.connection_has_been_opened();
    }

    @Override
    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        super.connection_does_not_have_any_database();
    }

}
