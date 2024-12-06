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

// TODO: Uncomment and test when we have replications and encryption

//import com.typedb.core.tool.runner.TypeDBRunner;
//import com.typedb.core.tool.runner.TypeDBSingleton;
//import com.typedb.cloud.tool.runner.TypeDBCloudRunner;
//import com.typedb.driver.TypeDB;
//import com.typedb.driver.api.TypeDBDriver;
//import com.typedb.driver.api.Credentials;
//import com.typedb.driver.api.Options;
//import com.typedb.driver.api.database.Database;
//import io.cucumber.java.After;
//import io.cucumber.java.Before;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.When;
//
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.typedb.driver.test.behaviour.util.Util.assertThrows;
//
//public class ConnectionStepsCloud extends ConnectionStepsBase {
//
//    @Override
//    public void beforeAll() {
//        super.beforeAll();
//        TypeDBCloudRunner cloudRunner = TypeDBCloudRunner.create(Paths.get("."), 1, serverOptions);
//        TypeDBSingleton.setTypeDBRunner(cloudRunner);
//        cloudRunner.start();
//    }
//
//    @Before
//    public synchronized void before() {
//        super.before();
//    }
//
//    @After
//    public synchronized void after() {
//        super.after();
//        try {
//            // sleep for eventual consistency to catch up with database deletion on all servers
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    TypeDBDriver createTypeDBDriver(String address) {
//        return createTypeDBDriver(address, DEFAULT_USERNAME, DEFAULT_PASSWORD, false); // TODO: Probably requires connection settings with tls enabled by default
//    }
//
//    TypeDBDriver createTypeDBDriver(String address, String username, String password, boolean tlsEnabled) {
//        return TypeDB.cloudDriver(address, new Credential(username, password, tlsEnabled));
//    }
//
//    @Override
//    Options createOptions() {
//        return new Options();
//    }
//
//@When("connection opens with default authentication")
//public void connection_opens_with_default_authentication() {
//    driver = createDefaultTypeDBDriver();
//}
//
//@When("connection opens with username '{non_semicolon}', password '{non_semicolon}'{may_error}")
//public void connection_opens_with_username_password(String username, String password, Parameters.MayError mayError) {
//    Credential credentials = new Credential(username, password);
//    mayError.check(() -> driver = createTypeDBDriver(TypeDB.DEFAULT_ADDRESS, credentials, DEFAULT_CONNECTION_SETTINGS)); // TODO: Probably requires connection settings with tls enabled by default
//}
//
//
//    @Override
//    @Given("connection has been opened")
//    public void connection_has_been_opened() {
//        super.connection_has_been_opened();
//    }
//
//    @Override
//    @Given("connection has {integer} database(s)")
//    public void connection_has_count_databases() {
//        super.connection_has_count_databases();
//    }
//
//    @Override
//    @When("connection closes")
//    public void connection_closes() {
//        super.connection_closes();
//    }
//
//    @Given("typedb has configuration")
//    public void typedb_has_configuration(Map<String, String> map) {
//        // no-op: configuration tests are only run on the backend themselves
//    }
//
//    @When("typedb starts")
//    public void typedb_starts() {
//        TypeDBRunner runner = TypeDBSingleton.getTypeDBRunner();
//        if (runner != null && runner.isStopped()) {
//            runner.start();
//        }
//    }
//}
