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

package application;

import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.Transaction;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MavenApplicationTest {
    private static final String DB_NAME = "typedb";

    @Test
    public void test() {
        Driver driver = TypeDB.driver(
                TypeDB.DEFAULT_ADDRESS,
                new Credentials("admin", "password"),
                new DriverOptions(false, null)
        );
        if (driver.databases().contains(DB_NAME)) {
            driver.databases().get(DB_NAME).delete();
        }
        driver.databases().create(DB_NAME);
        Transaction tx = driver.transaction(DB_NAME, Transaction.Type.SCHEMA);
        com.typedb.driver.api.answer.QueryAnswer answer = tx.query("define entity person;").resolve();
        assertNotNull(answer);
        assertTrue(answer.isOk());
        tx.close();
        driver.close();
        System.out.println("Driver is tested successfully!");
    }
}
