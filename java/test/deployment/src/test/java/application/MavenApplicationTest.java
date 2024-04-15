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

import com.vaticle.typedb.driver.TypeDB;
import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.type.EntityType;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

// TODO: implement more advanced tests using TypeQL queries once TypeDB 2.0 supports them
public class MavenApplicationTest {

    @Test
    public void test() {
        TypeDBDriver driver = TypeDB.coreDriver(TypeDB.DEFAULT_ADDRESS);
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
