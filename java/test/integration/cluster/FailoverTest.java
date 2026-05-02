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

import static java.util.Objects.requireNonNull;
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
    private static final int PRIMARY_POLL_RETRIES = 20;
    private static final int PRIMARY_POLL_INTERVAL_SECS = 2;
    private static final int PRIMARY_FAILOVER_RETRIES = 5;
    private static final String CLUSTER_SERVER_SCRIPT = requireNonNull(
            System.getenv("CLUSTER_SERVER_SCRIPT"), "CLUSTER_SERVER_SCRIPT environment variable must be set"
    );

    private static void clusterServer(String command, String nodeId) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(CLUSTER_SERVER_SCRIPT, command, nodeId).inheritIO();
        if (System.getenv("CLUSTER_DIR") == null && System.getenv("BUILD_WORKSPACE_DIRECTORY") != null) {
            pb.environment().put("CLUSTER_DIR", System.getenv("BUILD_WORKSPACE_DIRECTORY"));
        }
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            fail(CLUSTER_SERVER_SCRIPT + " " + command + " " + nodeId + " failed with exit code " + exitCode);
        }
    }

    private static void ensureAllNodesUp() throws IOException, InterruptedException {
        for (int i = 1; i <= ADDRESSES.size(); i++) {
            clusterServer("start", String.valueOf(i));
            clusterServer("await", String.valueOf(i));
        }
    }

    private static String nodeIdFromAddress(String address) {
        String port = address.substring(address.lastIndexOf(':') + 1);
        return port.substring(0, 1);
    }

    private static Driver createDriver() throws InterruptedException {
        String rootCA = System.getenv("ROOT_CA");
        assertNotNull("ROOT_CA environment variable must be set", rootCA);
        for (int attempt = 0; attempt < PRIMARY_POLL_RETRIES; attempt++) {
            try {
                return TypeDB.driver(
                        ADDRESSES,
                        new Credentials(USERNAME, PASSWORD),
                        new DriverOptions(DriverTlsConfig.enabledWithRootCA(rootCA))
                                .primaryFailoverRetries(PRIMARY_FAILOVER_RETRIES)
                );
            } catch (Exception e) {
                if (attempt < PRIMARY_POLL_RETRIES - 1) {
                    System.out.println("  Driver creation failed (attempt " + (attempt + 1) + "/"
                            + PRIMARY_POLL_RETRIES + "): " + e.getMessage()
                            + ". Retrying in " + PRIMARY_POLL_INTERVAL_SECS + "s...");
                    Thread.sleep(PRIMARY_POLL_INTERVAL_SECS * 1000L);
                } else {
                    throw e;
                }
            }
        }
        throw new AssertionError("unreachable");
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

    private static void setupDatabase(Driver driver) throws InterruptedException {
        for (int attempt = 0; attempt < PRIMARY_POLL_RETRIES; attempt++) {
            try {
                if (driver.databases().contains(DATABASE_NAME)) {
                    driver.databases().get(DATABASE_NAME).delete();
                }
                driver.databases().create(DATABASE_NAME);
                try (Transaction tx = driver.transaction(DATABASE_NAME, Transaction.Type.SCHEMA)) {
                    tx.query("define entity person;").resolve();
                    tx.commit();
                }
                return;
            } catch (Exception e) {
                if (attempt < PRIMARY_POLL_RETRIES - 1) {
                    System.out.println("  Database setup failed (attempt " + (attempt + 1) + "/"
                            + PRIMARY_POLL_RETRIES + "): " + e.getMessage()
                            + ". Retrying in " + PRIMARY_POLL_INTERVAL_SECS + "s...");
                    Thread.sleep(PRIMARY_POLL_INTERVAL_SECS * 1000L);
                } else {
                    throw e;
                }
            }
        }
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        ensureAllNodesUp();
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

        System.out.println("Connecting driver...");
        Driver driver = createDriver();

        System.out.println("Setting up database and schema...");
        setupDatabase(driver);
        verifyReadQuery(driver);
        System.out.println("Initial setup verified.");

        for (int iteration = 1; iteration <= FAILOVER_ITERATIONS; iteration++) {
            System.out.println("\n--- Failover iteration " + iteration + "/" + FAILOVER_ITERATIONS + " ---");

            Server primary = getPrimaryServer(driver);
            assertNotNull(primary);
            String primaryAddress = primary.getAddress();
            String nodeId = nodeIdFromAddress(primaryAddress);
            System.out.println("  Primary server: " + primaryAddress + " (node " + nodeId + ")");

            System.out.println("  Read query before kill...");
            verifyReadQuery(driver);

            System.out.println("  Killing node " + nodeId + "...");
            clusterServer("kill", nodeId);

            System.out.println("  Read query immediately after kill (driver auto-failover)...");
            verifyReadQuery(driver);
            System.out.println("  Auto-failover read succeeded.");

            System.out.println("  Confirming new primary...");
            Server newPrimary = getPrimaryServer(driver);
            System.out.println("  New primary: " + newPrimary.getAddress()
                    + " (node " + nodeIdFromAddress(newPrimary.getAddress()) + ")");

            System.out.println("  Read query on confirmed primary...");
            verifyReadQuery(driver);
            System.out.println("  Confirmed primary read succeeded.");

            System.out.println("  Restarting node " + nodeId + "...");
            clusterServer("start", nodeId);
            clusterServer("await", nodeId);
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
