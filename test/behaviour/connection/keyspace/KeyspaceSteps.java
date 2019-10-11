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

import grakn.client.test.behaviour.connection.ConnectionSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static grakn.common.util.Collections.set;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyspaceSteps {

    @When("connection create keyspace(s):")
    public void connection_create_keyspaces(List<String> names) {
        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        for (String name : names) {
            ConnectionSteps.client.session(name);
        }
    }

    @When("connection create keyspaces in parallel:")
    public void connection_create_keyspaces_in_parallel(List<String> names) {
        assertTrue(ConnectionSteps.THREAD_POOL_SIZE >= names.size());

        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        CompletableFuture[] creations = new CompletableFuture[names.size()];
        int i = 0;
        for (String name : names) {
            creations[i++] = CompletableFuture.supplyAsync(
                    () -> ConnectionSteps.client.session(name),
                    ConnectionSteps.threadPool
            );
        }

        CompletableFuture.allOf(creations);
    }

    @When("connection delete keyspace(s):")
    public void connection_delete_keyspaces(List<String> names) {
        for (String keyspaceName : names) {
            ConnectionSteps.client.keyspaces().delete(keyspaceName);
        }
    }

    @When("connection delete keyspaces in parallel:")
    public void connection_delete_keyspaces_in_parallel(List<String> names) {
        assertTrue(ConnectionSteps.THREAD_POOL_SIZE >= names.size());

        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        CompletableFuture[] deletions = new CompletableFuture[names.size()];
        int i = 0;
        for (String name : names) {
            deletions[i++] = CompletableFuture.supplyAsync(
                    () -> {
                        ConnectionSteps.client.keyspaces().delete(name);
                        return null;
                    },
                    ConnectionSteps.threadPool
            );
        }

        CompletableFuture.allOf(deletions);
    }

    @Then("connection has keyspace(s):")
    public void connection_has_keyspaces(List<String> names) {
        assertEquals(set(names), set(ConnectionSteps.client.keyspaces().retrieve()));
    }

    @Then("connection does not have keyspace(s):")
    public void connection_does_not_have_keyspaces(List<String> names) {
        Set<String> keyspaces = set(ConnectionSteps.client.keyspaces().retrieve());
        for (String keyspaceName : names) {
            assertFalse(keyspaces.contains(keyspaceName));
        }
    }
}
