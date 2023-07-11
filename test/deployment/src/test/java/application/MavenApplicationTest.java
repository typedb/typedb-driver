/*
 * Copyright (C) 2022 Vaticle
 *
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

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

// TODO: implement more advanced tests using TypeQL queries once TypeDB 2.0 supports them
public class MavenApplicationTest {

    @Test
    public void test() {
        TypeDBClient client = TypeDB.coreClient(TypeDB.DEFAULT_ADDRESS);
        client.databases().create("typedb");
        TypeDBSession session = client.session("typedb", TypeDBSession.Type.DATA);
        TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE);
        ThingType root = tx.concepts().getRootThingType();
        assertNotNull(root);
        assertEquals(4, root.asRemote(tx).getSubtypes().collect(Collectors.toList()).size());
        tx.close();
        session.close();
        client.close();
    }
}
