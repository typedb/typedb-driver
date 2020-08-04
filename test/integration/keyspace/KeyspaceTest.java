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

package grakn.client.test.integration.keyspace;

import grakn.client.GraknClient;
import grakn.client.exception.GraknClientException;
import grakn.common.test.server.GraknProperties;
import grakn.common.test.server.GraknSetup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class KeyspaceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private static GraknClient client;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException, TimeoutException {
        GraknSetup.bootup();
        String address = System.getProperty(GraknProperties.GRAKN_ADDRESS);
        client = new GraknClient(address);
    }

    @AfterClass
    public static void closeSession() throws InterruptedException, TimeoutException, IOException {
        client.close();
        GraknSetup.shutdown();
    }

    @Test
    public void retrievingExistingKeyspaces_createdSessionKeyspaceVisible() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        client.session(randomKeyspace);
        List<String> keyspaces = client.keyspaces().retrieve();
        assertTrue(keyspaces.contains(randomKeyspace));
    }

    @Test
    public void whenDeletingKeyspace_notListedInExistingKeyspaces() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        client.session(randomKeyspace);
        List<String> keyspaces = client.keyspaces().retrieve();

        assertTrue(keyspaces.contains(randomKeyspace));

        client.keyspaces().delete(randomKeyspace);
        List<String> keyspacesUpdated = client.keyspaces().retrieve();

        assertFalse(keyspacesUpdated.contains(randomKeyspace));
    }

    @Test
    public void whenDeletingKeyspace_OpenSessionFails() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        GraknClient.Session session = client.session(randomKeyspace);

        // delete keyspace
        client.keyspaces().delete(randomKeyspace);

        exception.expect(GraknClientException.class);
        exception.expectMessage("session for graph");
        exception.expectMessage("is closed");
        GraknClient.Transaction tx = session.transaction().read();
    }

    @Test
    public void whenDeletingKeyspace_OpenTransactionFails() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        GraknClient.Session session = client.session(randomKeyspace);

        // Hold on to an open tx
        GraknClient.Transaction tx = session.transaction().read();

        // delete keyspace
        client.keyspaces().delete(randomKeyspace);

        exception.expect(GraknClientException.class);
        exception.expectMessage("Operation cannot be executed because the enclosing transaction is closed");

        // try to operate on an open tx
        tx.getEntityType("entity");
    }

    @Test
    public void testDeletingAKeyspace_TheKeyspaceIsRecreatedInNewSession() {
        String randomKeyspace = "a" + UUID.randomUUID().toString().replaceAll("-", "");
        GraknClient.Session remoteSession = client.session(randomKeyspace);

        try (GraknClient.Transaction tx = remoteSession.transaction().write()) {
            tx.putEntityType("easter");
            tx.commit();
        }
        remoteSession.close();
        client.keyspaces().delete(randomKeyspace);

        GraknClient.Session renewedSession = client.session(randomKeyspace);
        try (GraknClient.Transaction tx = renewedSession.transaction().write()) {
            assertNull(tx.getEntityType("easter"));
            assertNotNull(tx.getEntityType("entity"));
        }

        renewedSession.close();
    }

    @Test
    public void whenDeletingNonExistingKeyspace_exceptionThrown() {
        exception.expectMessage("It is not possible to delete keyspace [nonexistingkeyspace] as it does not exist");
        client.keyspaces().delete("nonexistingkeyspace");
    }
}