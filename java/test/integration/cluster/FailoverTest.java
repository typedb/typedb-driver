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

package com.typedb.driver.test.integration.cluster;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.DriverTlsConfig;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FailoverTest {

    private static final Set<String> ADDRESSES = Set.of(
            "127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"
    );
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String DATABASE_NAME = "test-failover";
    private static final int FAILOVER_ITERATIONS = 10;
    private static final int POST_KILL_WAIT_SECS = 5;
    private static final int PRIMARY_POLL_RETRIES = 20;
    private static final int PRIMARY_POLL_INTERVAL_SECS = 2;
    private static final String CLUSTER_NODE_SCRIPT = "tool/test/cluster-server.sh";

    private static void clusterNode(String command, String nodeId) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(CLUSTER_NODE_SCRIPT, command, nodeId)
                .inheritIO()
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            fail(CLUSTER_NODE_SCRIPT + " " + command + " " + nodeId + " failed with exit code " + exitCode);
        }
    }

    private static String nodeIdFromAddress(String address) {
        String port = address.substring(address.lastIndexOf(':') + 1);
        return port.substring(0, 1);
    }

    private static Driver createDriver() {
        return TypeDB.driver(
                ADDRESSES,
                new Credentials(USERNAME, PASSWORD),
                new DriverOptions(DriverTlsConfig.disabled())
        );
    }

    private static Server getPrimaryServer(Driver driver) throws InterruptedException {
        for (int attempt = 0; attempt < PRIMARY_POLL_RETRIES; attempt++) {
            Optional<? extends Server> primary = driver.primaryServer();
            if (primary.isPresent()) {
                return primary.get();
            }
            if (attempt < PRIMARY_POLL_RETRIES - 1) {
                System.out.println("  No primary server found (attempt " + (attempt + 1) + "/" + PRIMARY_POLL_RETRIES
                        + "). Retrying in " + PRIMARY_POLL_INTERVAL_SECS + "s...");
                Thread.sleep(PRIMARY_POLL_INTERVAL_SECS * 1000L);
            }
        }
        fail("Retry limit exceeded while seeking a primary server.");
        return null;
    }

    @Before
    public void setUp() {
        try {
            Driver driver = createDriver();
            if (driver.databases().contains(DATABASE_NAME)) {
                driver.databases().get(DATABASE_NAME).delete();
            }
            driver.close();
        } catch (Exception e) {
            // Best effort cleanup
        }
    }

    @After
    public void tearDown() {
        try {
            Driver driver = createDriver();
            if (driver.databases().contains(DATABASE_NAME)) {
                driver.databases().get(DATABASE_NAME).delete();
            }
            driver.close();
        } catch (Exception e) {
            // Best effort cleanup
        }
    }

    @Test
    public void primaryFailover() throws Exception {
        System.out.println("=== Cluster Failover Test ===");

        // Connect driver (cluster must already be running via start-cluster-servers.sh)
        System.out.println("Connecting driver...");
        Driver driver = createDriver();

        // Setup database with schema
        System.out.println("Setting up database and schema...");
        driver.databases().create(DATABASE_NAME);

        try (Transaction tx = driver.transaction(DATABASE_NAME, Transaction.Type.SCHEMA)) {
            tx.query("define entity person;").resolve();
            tx.commit();
        }

        verifyReadQuery(driver);
        System.out.println("Initial setup verified.");

        // Failover loop
        for (int iteration = 1; iteration <= FAILOVER_ITERATIONS; iteration++) {
            System.out.println("\n--- Failover iteration " + iteration + "/" + FAILOVER_ITERATIONS + " ---");

            Server primary = getPrimaryServer(driver);
            assertNotNull(primary);
            String primaryAddress = primary.getAddress();
            String nodeId = nodeIdFromAddress(primaryAddress);
            System.out.println("  Primary server: " + primaryAddress + " (node " + nodeId + ")");

            System.out.println("  Killing node " + nodeId + "...");
            clusterNode("kill", nodeId);

            System.out.println("  Waiting " + POST_KILL_WAIT_SECS + "s for re-election...");
            Thread.sleep(POST_KILL_WAIT_SECS * 1000L);

            System.out.println("  Verifying read query on new primary...");
            verifyReadQuery(driver);
            System.out.println("  Read query succeeded.");

            System.out.println("  Restarting node " + nodeId + "...");
            clusterNode("start", nodeId);
            clusterNode("await", nodeId);
            System.out.println("  Node " + nodeId + " restarted.");
        }

        System.out.println("\n=== All " + FAILOVER_ITERATIONS + " failover iterations passed! ===");
        driver.close();
    }

    private void verifyReadQuery(Driver driver) {
        try (Transaction tx = driver.transaction(DATABASE_NAME, Transaction.Type.READ)) {
            QueryAnswer answer = tx.query("match entity $t;").resolve();
            assertTrue("Expected at least one entity type in read query results",
                    answer.asConceptRows().stream().findAny().isPresent());
        }
    }
}
