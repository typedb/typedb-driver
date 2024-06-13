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

import com.vaticle.typedb.cloud.tool.runner.TypeDBCloudRunner;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.driver.TypeDB;
import com.vaticle.typedb.driver.api.TypeDBCredential;
import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.type.EntityType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.common.collection.Collections.map;
import static com.vaticle.typedb.common.collection.Collections.pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@SuppressWarnings("Duplicates")
public class AddressTranslationTest {
    private static final Logger LOG = LoggerFactory.getLogger(AddressTranslationTest.class);

    private static final TypeDBCredential credential = new TypeDBCredential("admin", "password", false);

    @Test
    public void testAddressTranslation() {
        TypeDBCloudRunner typedb = TypeDBCloudRunner.create(Paths.get("."), 3);
        typedb.start();
        Map<String, String> addresses = typedb.externalAddresses().stream().map(address -> pair(address, address))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        TypeDBDriver driver = TypeDB.cloudDriver(addresses, credential);
        driver.databases().create("typedb");
        TypeDBSession session = driver.session("typedb", TypeDBSession.Type.DATA);
        TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE);
        EntityType root = tx.concepts().getRootEntityType();
        assertNotNull(root);
        assertEquals(1, root.getSubtypes(tx).collect(Collectors.toList()).size());
        tx.close();
        session.close();
        driver.close();
    }
}
