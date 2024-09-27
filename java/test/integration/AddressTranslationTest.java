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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("Duplicates")
public class AddressTranslationTest {
    private static final Logger LOG = LoggerFactory.getLogger(AddressTranslationTest.class);

//    private static final Credential credential = new Credential("admin", "password", false);

//    @Test
//    public void testAddressTranslation() {
//        TypeDBCloudRunner typedb = TypeDBCloudRunner.create(Paths.get("."), 3);
//        typedb.start();
//        Map<String, String> addresses = typedb.externalAddresses().stream().map(address -> pair(address, address))
//                .collect(Collectors.toMap(Pair::first, Pair::second));
//        TypeDBDriver driver = TypeDB.cloudDriver(addresses, credential);
//        driver.databases().create("typedb");
//        TypeDBSession session = driver.session("typedb", TypeDBSession.Type.DATA);
//        Transaction tx = session.transaction(Transaction.Type.WRITE);
//        EntityType root = tx.concepts().getRootEntityType();
//        assertNotNull(root);
//        assertEquals(1, root.getSubtypes(tx).collect(Collectors.toList()).size());
//        tx.close();
//        session.close();
//        driver.close();
//    }
}
