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

package grakn.client.test.behaviour.connection;

import grakn.client.GraknClient;
import grakn.client.api.Client;
import grakn.common.test.server.GraknCoreRunner;
import grakn.common.test.server.GraknSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConnectionStepsCore extends ConnectionStepsBase {
    private GraknCoreRunner server;

    @Override
    void beforeAll() {
        try {
            server = new GraknCoreRunner();
        } catch (InterruptedException | TimeoutException | IOException e) {
            throw new RuntimeException(e);
        }
        server.start();
        GraknSingleton.setGraknRunner(server);
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
    Client createGraknClient(String address) {
        return GraknClient.core(address);
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        super.connection_has_been_opened();
    }

    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        super.connection_does_not_have_any_database();
    }

}
