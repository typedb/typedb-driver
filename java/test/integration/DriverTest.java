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

package com.typedb.driver.test.integration;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.DriverTlsConfig;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.database.Database;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class DriverTest {
    private static final String DB_NAME = "typedb";
    private static final String ADDRESS = "127.0.0.1:1729";
    private static Driver typedbDriver;

    @BeforeClass
    public static void setUpClass() {
        typedbDriver = TypeDB.driver(ADDRESS, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.disabled()));
        if (typedbDriver.databases().contains(DB_NAME)) typedbDriver.databases().get(DB_NAME).delete();
        typedbDriver.databases().create(DB_NAME);
    }

    @AfterClass
    public static void close() {
        typedbDriver.close();
    }

    @Test
    public void transaction_on_close_callback() {
        Database db = typedbDriver.databases().get(DB_NAME);
        db.delete();
        typedbDriver.databases().create(DB_NAME);

        AtomicBoolean calledOnClose = new AtomicBoolean(false);

        localhostTypeDBTX(transaction -> {

            transaction.onClose(error -> {
                calledOnClose.set(true);
            });

            transaction.close();
            assertTrue(calledOnClose.get());
        }, Transaction.Type.READ);
    }

    private void localhostTypeDBTX(Consumer<Transaction> fn, Transaction.Type type/*, Options options*/) {
        try (Transaction transaction = typedbDriver.transaction(DB_NAME, type/*, options*/)) {
            fn.accept(transaction);
        }
    }
}
