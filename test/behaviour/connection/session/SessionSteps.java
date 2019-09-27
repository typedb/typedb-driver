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

package grakn.client.test.behaviour.connection.session;

import grakn.client.GraknClient;
import grakn.client.test.behaviour.connection.ConnectionSteps;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;

public class SessionSteps {



    @Then("session(s) is/are null: {boolean}")
    public void sessions_are_null(Boolean isNull) {
        Collection<GraknClient.Session> sessions =
                ConnectionSteps.sessionsList.isEmpty() ?
                        ConnectionSteps.sessionsMap.values() :
                        ConnectionSteps.sessionsList;

        for (GraknClient.Session session : sessions) {
            assertEquals(isNull(session), isNull);
        }
    }

    @Then("session(s) is/are open: {boolean}")
    public void sessions_are_open(Boolean isOpen) {
        Collection<GraknClient.Session> sessions =
                ConnectionSteps.sessionsList.isEmpty() ?
                        ConnectionSteps.sessionsMap.values() :
                        ConnectionSteps.sessionsList;

        for (GraknClient.Session session : sessions) {
            assertEquals(session.isOpen(), isOpen);
        }
    }

    @Then("session(s) has/have keyspace: {word}")
    public void sessions_have_keyspace(String name) {
        for (GraknClient.Session session : ConnectionSteps.sessionsList) {
            assertEquals(session.keyspace().name(), name);
        }
    }

    @Then("sessions have keyspaces:")
    public void sessions_have_keyspaces(Map<String, String> names) {
        for (Map.Entry<String, String> name : names.entrySet()) {
            assertEquals(ConnectionSteps.sessionsMap.get(name.getKey()).keyspace().name(), name.getValue());
        }
    }
}
