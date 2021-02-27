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

package grakn.client.test.behaviour.concept.thing.relation;

import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.thing.Thing;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static grakn.client.test.behaviour.concept.thing.ThingSteps.get;
import static grakn.client.test.behaviour.concept.thing.ThingSteps.put;
import static grakn.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static grakn.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelationSteps {

    @When("{var} = relation\\( ?{type_label} ?) create new instance")
    public void relation_type_create_new_instance(String var, String typeLabel) {
        put(var, tx().concepts().getRelationType(typeLabel).asRemote(tx()).create());
    }

    @Then("relation\\( ?{type_label} ?) create new instance; throws exception")
    public void relation_type_create_new_instance_throws_exception(String typeLabel) {
        assertThrows(() -> tx().concepts().getRelationType(typeLabel).asRemote(tx()).create());
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, int keyValue) {
        Attribute.Long key = tx().concepts().getAttributeType(keyType).asLong().asRemote(tx()).put(keyValue);
        Relation relation = tx().concepts().getRelationType(type).asRemote(tx()).create();
        relation.asRemote(tx()).setHas(key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, String keyValue) {
        Attribute.String key = tx().concepts().getAttributeType(keyType).asString().asRemote(tx()).put(keyValue);
        Relation relation = tx().concepts().getRelationType(type).asRemote(tx()).create();
        relation.asRemote(tx()).setHas(key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, LocalDateTime keyValue) {
        Attribute.DateTime key = tx().concepts().getAttributeType(keyType).asDateTime().asRemote(tx()).put(keyValue);
        Relation relation = tx().concepts().getRelationType(type).asRemote(tx()).create();
        relation.asRemote(tx()).setHas(key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, long keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asLong().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.asRemote(tx()).getType().equals(tx().concepts().getRelationType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, String keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asString().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.asRemote(tx()).getType().equals(tx().concepts().getRelationType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, LocalDateTime keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asDateTime().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.asRemote(tx()).getType().equals(tx().concepts().getRelationType(type)))
                .findFirst().orElse(null));
    }

    @Then("relation\\( ?{type_label} ?) get instances contain: {var}")
    public void relation_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getRelationType(typeLabel).asRemote(tx()).getInstances().anyMatch(i -> i.equals(get(var))));
    }

    @Then("relation\\( ?{type_label} ?) get instances do not contain: {var}")
    public void relation_type_get_instances_do_not_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getRelationType(typeLabel).asRemote(tx()).getInstances().noneMatch(i -> i.equals(get(var))));
    }

    @Then("relation\\( ?{type_label} ?) get instances is empty")
    public void relation_type_get_instances_is_empty(String typeLabel) {
        assertEquals(0, tx().concepts().getRelationType(typeLabel).asRemote(tx()).getInstances().count());
    }

    @When("relation {var} add player for role\\( ?{type_label} ?): {var}")
    public void relation_add_player_for_role(String var1, String roleTypeLabel, String var2) {
        get(var1).asRelation().asRemote(tx()).addPlayer(get(var1).asRelation().asRemote(tx()).getType().asRemote(tx()).getRelates(roleTypeLabel), get(var2));
    }

    @When("relation {var} add player for role\\( ?{type_label} ?): {var}; throws exception")
    public void relation_add_player_for_role_throws_exception(String var1, String roleTypeLabel, String var2) {
        assertThrows(() -> get(var1).asRelation().asRemote(tx()).addPlayer(get(var1).asRelation().asRemote(tx()).getType().asRemote(tx()).getRelates(roleTypeLabel), get(var2)));
    }

    @When("relation {var} remove player for role\\( ?{type_label} ?): {var}")
    public void relation_remove_player_for_role(String var1, String roleTypeLabel, String var2) {
        get(var1).asRelation().asRemote(tx()).removePlayer(get(var1).asRelation().asRemote(tx()).getType().asRemote(tx()).getRelates(roleTypeLabel), get(var2));
    }

    @Then("relation {var} get players contain:")
    public void relation_get_players_contain(String var, Map<String, String> players) {
        Relation relation = get(var).asRelation();
        players.forEach((rt, var2) -> assertTrue(relation.asRemote(tx()).getPlayersByRoleType().get(relation.asRemote(tx()).getType().asRemote(tx()).getRelates(rt)).contains(get(var2.substring(1)))));
    }

    @Then("relation {var} get players do not contain:")
    public void relation_get_players_do_not_contain(String var, Map<String, String> players) {
        Relation relation = get(var).asRelation();
        players.forEach((rt, var2) -> {
            List<? extends Thing> p;
            if ((p = relation.asRemote(tx()).getPlayersByRoleType().get(relation.asRemote(tx()).getType().asRemote(tx()).getRelates(rt))) != null) {
                assertFalse(p.contains(get(var2.substring(1))));
            }
        });
    }

    @Then("relation {var} get players contain: {var}")
    public void relation_get_players_contain(String var1, String var2) {
        assertTrue(get(var1).asRelation().asRemote(tx()).getPlayers().anyMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players do not contain: {var}")
    public void relation_get_players_do_not_contain(String var1, String var2) {
        assertTrue(get(var1).asRelation().asRemote(tx()).getPlayers().noneMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players for role\\( ?{type_label} ?) contain: {var}")
    public void relation_get_player_for_role_contain(String var1, String roleTypeLabel, String var2) {
        assertTrue(get(var1).asRelation().asRemote(tx())
                           .getPlayers(get(var1).asRelation().asRemote(tx()).getType().asRemote(tx()).getRelates(roleTypeLabel))
                           .anyMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players for role\\( ?{type_label} ?) do not contain: {var}")
    public void relation_get_player_for_role_do_not_contain(String var1, String roleTypeLabel, String var2) {
        assertTrue(get(var1).asRelation().asRemote(tx())
                           .getPlayers(get(var1).asRelation().asRemote(tx()).getType().asRemote(tx()).getRelates(roleTypeLabel))
                           .noneMatch(p -> p.equals(get(var2))));
    }
}
