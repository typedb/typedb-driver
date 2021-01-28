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
import grakn.common.test.server.GraknCoreRunner;
import grakn.common.test.server.GraknSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionStepsCore extends ConnectionStepsBase {
    private static GraknCoreRunner server;

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TimeoutException {
        setupServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, IOException, TimeoutException {
        teardownServer();
    }

    static void setupServer() throws InterruptedException, TimeoutException, IOException {
        server = new GraknCoreRunner(true);
        server.start();
        GraknSingleton.setGraknRunner(server);
    }

    static void teardownServer() throws InterruptedException, IOException, TimeoutException {
        server.stop();
    }

    @Before
    public synchronized void before() {
        setupClient();
    }

    @After
    public synchronized void after() {
        teardownClient();
    }

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    @Given("connection does not have any database")
    public void connection_does_not_have_any_database() {
        assertTrue(client.databases().all().isEmpty());
    }

    @Override
    GraknClient createGraknClient(String address) {
        return GraknClient.core(address);
    }
}
