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

package com.vaticle.typedb.driver.test.integration;

import com.vaticle.typedb.core.tool.runner.TypeDBCoreRunner;
import com.vaticle.typedb.driver.TypeDB;
import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.vaticle.typedb.driver.api.TypeDBSession.Type.DATA;
import static com.vaticle.typedb.driver.api.TypeDBSession.Type.SCHEMA;
import static com.vaticle.typedb.driver.api.TypeDBTransaction.Type.READ;
import static com.vaticle.typedb.driver.api.TypeDBTransaction.Type.WRITE;
import static org.junit.Assert.assertTrue;

/**
 * Tests for memory leak fixes in native iterator handling.
 *
 * Before the fix, using .limit() on query streams would leak native memory
 * because the SWIG %newobject directives were missing for renamed functions.
 */
public class MemoryLeakTest {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryLeakTest.class);
    private static final String DATABASE = "memory_leak_test";
    private static TypeDBCoreRunner typedb;
    private static TypeDBDriver driver;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        typedb = new TypeDBCoreRunner();
        typedb.start();
        driver = TypeDB.coreDriver(typedb.address());

        if (driver.databases().contains(DATABASE)) {
            driver.databases().get(DATABASE).delete();
        }
        driver.databases().create(DATABASE);

        try (TypeDBSession session = driver.session(DATABASE, SCHEMA);
             TypeDBTransaction tx = session.transaction(WRITE)) {
            tx.query().define("define person sub entity, owns name; name sub attribute, value string;");
            tx.commit();
        }

        try (TypeDBSession session = driver.session(DATABASE, DATA);
             TypeDBTransaction tx = session.transaction(WRITE)) {
            for (int i = 0; i < 100; i++) {
                tx.query().insert("insert $p isa person, has name 'person-" + i + "';");
            }
            tx.commit();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (driver != null) {
            if (driver.databases().contains(DATABASE)) {
                driver.databases().get(DATABASE).delete();
            }
            driver.close();
        }
        if (typedb != null) {
            typedb.stop();
        }
    }

    @Test
    public void streamLimitShouldNotLeakMemory() {
        long initialMemory = getUsedMemory();
        LOG.info("Initial memory: {} MB", initialMemory / 1024 / 1024);

        for (int i = 0; i < 10000; i++) {
            try (TypeDBSession session = driver.session(DATABASE, DATA);
                 TypeDBTransaction tx = session.transaction(READ)) {
                tx.query().get("match $x isa person; get;").limit(10).forEach(cm -> {});
            }

            if (i % 1000 == 0) {
                System.gc();
                LOG.info("Iteration {}, memory: {} MB", i, getUsedMemory() / 1024 / 1024);
            }
        }

        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        long finalMemory = getUsedMemory();
        long memoryGrowth = finalMemory - initialMemory;

        LOG.info("Final memory: {} MB, growth: {} MB", finalMemory / 1024 / 1024, memoryGrowth / 1024 / 1024);
        // Allow 150 MB tolerance - GC may take time to reclaim native memory
        // Before fix: memory grew ~800 MB (continuous leak)
        // After fix: memory stabilizes around 200 MB
        assertTrue("Memory grew by " + memoryGrowth / 1024 / 1024 + " MB, expected < 150 MB",
                   memoryGrowth < 150 * 1024 * 1024);
    }

    private long getUsedMemory() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/status"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VmRSS:")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        return Long.parseLong(parts[1]) * 1024;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOG.warn("Failed to read /proc/self/status: {}", e.getMessage());
        }
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
