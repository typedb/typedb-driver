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
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Optional;
import java.util.Set;

public class ConnectionStepsCluster extends ConnectionStepsBase {
    // TODO: Add 2 more addresses
    public static Set<String> DEFAULT_CLUSTER_ADDRESSES = Set.of(
            "127.0.0.1:11729"
    );

    @Override
    public void beforeAll() {
        super.beforeAll();
    }

    @Before
    public synchronized void before() {
        driverOptions = driverOptions.tlsEnabled(true).tlsRootCAPath(Optional.of(System.getenv("ROOT_CA")));
        super.before();
    }

    @After
    public synchronized void after() {
        super.after();
    }

    Driver createTypeDBDriver(String address, Credentials credentials, DriverOptions driverOptions) {
        return TypeDB.driver(Set.of(address), credentials, driverOptions);
    }

    Driver createTypeDBDriver(Set<String> address, Credentials credentials, DriverOptions driverOptions) {
        return TypeDB.driver(address, credentials, driverOptions);
    }

    @Override
    Driver createDefaultTypeDBDriver() {
        return createTypeDBDriver(DEFAULT_CLUSTER_ADDRESSES, DEFAULT_CREDENTIALS, driverOptions);
    }

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
        mayError.check(() -> driver = createTypeDBDriver(DEFAULT_CLUSTER_ADDRESSES, credentials, driverOptions));
    }

    @When("connection opens with a wrong host{may_error}")
    public void connection_opens_with_a_wrong_host(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(
                DEFAULT_CLUSTER_ADDRESSES.iterator().next().replace("127.0.0.1", "surely-not-localhost"),
                DEFAULT_CREDENTIALS,
                driverOptions
        ));
    }

    @When("connection opens with a wrong port{may_error}")
    public void connection_opens_with_a_wrong_port(Parameters.MayError mayError) {
        mayError.check(() -> driver = createTypeDBDriver(
                DEFAULT_CLUSTER_ADDRESSES.iterator().next().replace("127.0.0.1", "surely-not-localhost"),
                DEFAULT_CREDENTIALS,
                driverOptions
        ));
    }


    @Override
    @When("connection closes")
    public void connection_closes() {
        super.connection_closes();
    }

    @Override
    @Then("connection is open: {bool}")
    public void connection_is_open(boolean isOpen) {
        super.connection_is_open(isOpen);
    }

    @Override
    @Then("connection contains distribution{may_error}")
    public void connection_contains_distribution(Parameters.MayError mayError) {
        super.connection_contains_distribution(mayError);
    }

    @Override
    @Then("connection contains version{may_error}")
    public void connection_contains_version(Parameters.MayError mayError) {
        super.connection_contains_version(mayError);
    }

    @Override
    @Then("connection has {integer} replica(s)")
    public void connection_has_count_replicas(int count) {
        super.connection_has_count_replicas(count);
    }

    @Override
    @Then("connection contains primary replica")
    public void connection_contains_primary_replica() {
        super.connection_contains_primary_replica();
    }

    @Override
    @Then("connection has {integer} database(s)")
    public void connection_has_count_databases(int count) {
        super.connection_has_count_databases(count);
    }

    @Override
    @Then("connection has {integer} user(s)")
    public void connection_has_count_users(int count) {
        super.connection_has_count_users(count);
    }

    @Override
    @When("set driver option use_replication to: {bool}")
    public void set_driver_option_use_replication_to(boolean value) {
        super.set_driver_option_use_replication_to(value);
    }

    @Override
    @When("set driver option primary_failover_retries to: {integer}")
    public void set_driver_option_primary_failover_retries_to(int value) {
        super.set_driver_option_primary_failover_retries_to(value);
    }

    @Override
    @When("set driver option replica_discovery_attempts to: {integer}")
    public void set_driver_option_replica_discovery_attempts_to(int value) {
        super.set_driver_option_replica_discovery_attempts_to(value);
    }
}
