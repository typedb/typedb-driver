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

package grakn.client.test.behaviour.connection.keyspace;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static grakn.client.test.behaviour.connection.ConnectionSteps.THREAD_POOL_SIZE;
import static grakn.client.test.behaviour.connection.ConnectionSteps.client;
import static grakn.client.test.behaviour.connection.ConnectionSteps.threadPool;
import static grakn.common.util.Collections.list;
import static grakn.common.util.Collections.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyspaceSteps {

    @When("connection create keyspace: {word}")
    public void connection_create_keyspace(String name) {
        connection_create_keyspaces(list(name));
    }

    @When("connection create keyspace(s):")
    public void connection_create_keyspaces(List<String> names) {
        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        for (String name : names) {
            client.session(name);
        }
    }

    @When("connection create keyspaces in parallel:")
    public void connection_create_keyspaces_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        CompletableFuture[] creations = new CompletableFuture[names.size()];
        int i = 0;
        for (String name : names) {
            creations[i++] = CompletableFuture.supplyAsync(() -> client.session(name), threadPool);
        }

        CompletableFuture.allOf(creations).join();
    }

    @When("connection delete keyspace(s):")
    public void connection_delete_keyspaces(List<String> names) {
        for (String keyspaceName : names) {
            client.keyspaces().delete(keyspaceName);
        }
    }

    @When("connection delete keyspaces in parallel:")
    public void connection_delete_keyspaces_in_parallel(List<String> names) {
        assertTrue(THREAD_POOL_SIZE >= names.size());

        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        CompletableFuture[] deletions = new CompletableFuture[names.size()];
        int i = 0;
        for (String name : names) {
            deletions[i++] = CompletableFuture.supplyAsync(
                    () -> { client.keyspaces().delete(name); return null; }, threadPool
            );
        }

        CompletableFuture.allOf(deletions).join();
    }

    @Then("connection has keyspace(s):")
    public void connection_has_keyspaces(List<String> names) {
        assertEquals(set(names), set(client.keyspaces().retrieve()));
    }

    @Then("connection does not have keyspace(s):")
    public void connection_does_not_have_keyspaces(List<String> names) {
        Set<String> keyspaces = set(client.keyspaces().retrieve());
        for (String keyspaceName : names) {
            assertFalse(keyspaces.contains(keyspaceName));
        }
    }
}
