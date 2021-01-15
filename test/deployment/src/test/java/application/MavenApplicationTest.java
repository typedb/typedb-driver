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

import grakn.client.Grakn;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.concept.type.ThingType;
import grakn.client.GraknClient;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// TODO: implement more advanced tests using Graql queries once Grakn 2.0 supports them
public class MavenApplicationTest {

    @Test
    public void test() {
        GraknClient.Cluster client = new GraknClient.Cluster("localhost:40001", "localhost:40101", "localhost:40201");
        client.databases().create("grakn");
        Session session = client.session("grakn", Session.Type.DATA);
        Transaction tx = session.transaction(Transaction.Type.WRITE);
        tx.close();

        Transaction tx2 = session.transaction(Transaction.Type.WRITE);
        tx2.close();

        Transaction tx3 = session.transaction(Transaction.Type.WRITE);
        tx3.close();

        session.close();
        client.close();
    }
}
