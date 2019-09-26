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

package grakn.client.test.behaviour.connection.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;

import static grakn.client.common.util.Collections.set;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Keyspace {

    @Given("connection has been opened")
    public void connection_has_been_opened() {
        Connection.has_been_opened();
    }

    @Given("connection delete all keyspaces")
    public void connection_delete_all_keyspaces() {
        Connection.delete_all_keyspace();
    }
    @Given("connection does not have any keyspace")
    public void connection_does_not_have_any_keyspace() {
        Connection.does_not_have_any_keyspace();
    }

    @When("connection create one keyspace: {word}")
    public void connection_create_one_keyspace(String keyspaceName) {
        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        Connection.client.session(keyspaceName);
    }

    @When("connection create multiple keyspaces:")
    public void connection_create_multiple_keyspaces(List<String> keyspaceNames) {
        // TODO: This step should be rewritten once we can create keypsaces without opening sessions
        for (String name : keyspaceNames) {
            Connection.client.session(name);
        }
    }

    @When("connection delete one keyspace: {word}")
    public void connection_delete_one_keyspace(String keyspaceName) {
        Connection.client.keyspaces().delete(keyspaceName);
    }

    @When("connection delete multiple keyspaces:")
    public void connection_delete_multiple_keyspaces(List<String> keyspaceNames) {
        for (String keyspaceName : keyspaceNames) {
            Connection.client.keyspaces().delete(keyspaceName);
        }
    }

    @Then("connection has one keyspace: {word}")
    public void connection_has_one_keyspace(String keyspaceName) {
        assertEquals(Connection.client.keyspaces().retrieve(), singletonList(keyspaceName));
    }

    @Then("connection has multiple keyspaces:")
    public void connection_has_multiple_keyspaces(List<String> keyspaceName) {
        assertEquals(set(Connection.client.keyspaces().retrieve()), set(keyspaceName));
    }

    @Then("connection does not have one keyspace: {word}")
    public void connection_does_not_have_on_keyspace(String keyspaceName) {
        assertFalse(Connection.client.keyspaces().retrieve().contains(keyspaceName));
    }

    @Then("connection does not have multiple keyspaces:")
    public void connection_does_not_have_keyspaces(List<String> keyspaceNames) {
        Set<String> keyspaces = set(Connection.client.keyspaces().retrieve());
        for (String keyspaceName : keyspaceNames) {
            assertFalse(keyspaces.contains(keyspaceName));
        }
    }
}
