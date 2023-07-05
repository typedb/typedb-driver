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

package com.vaticle.typedb.client.test.behaviour.concept.thing.relation;

import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.common.Label;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.get;
import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.put;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("CheckReturnValue")
public class RelationSteps {
    @When("{var} = relation\\( ?{type_label} ?) create new instance")
    public void relation_type_create_new_instance(String var, String typeLabel) {
        put(var, tx().concepts().getRelationType(typeLabel).create(tx()));
    }

    @Then("relation\\( ?{type_label} ?) create new instance; throws exception")
    public void relation_type_create_new_instance_throws_exception(String typeLabel) {
        assertThrows(() -> tx().concepts().getRelationType(typeLabel).create(tx()));
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, int keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Relation relation = tx().concepts().getRelationType(type).create(tx());
        relation.setHas(tx(), key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, String keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Relation relation = tx().concepts().getRelationType(type).create(tx());
        relation.setHas(tx(), key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")
    public void relation_type_create_new_instance_with_key(String var, String type, String keyType, LocalDateTime keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Relation relation = tx().concepts().getRelationType(type).create(tx());
        relation.setHas(tx(), key);
        put(var, relation);
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, long keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, String keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @When("{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")
    public void relation_type_get_instance_with_key(String var1, String type, String keyType, LocalDateTime keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @Then("relation\\( ?{type_label} ?) get instances contain: {var}")
    public void relation_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getRelationType(typeLabel).getInstances(tx()).anyMatch(i -> i.equals(get(var))));
    }

    @Then("relation\\( ?{type_label} ?) get instances do not contain: {var}")
    public void relation_type_get_instances_do_not_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getRelationType(typeLabel).getInstances(tx()).noneMatch(i -> i.equals(get(var))));
    }

    @Then("relation\\( ?{type_label} ?) get instances is empty")
    public void relation_type_get_instances_is_empty(String typeLabel) {
        assertEquals(0, tx().concepts().getRelationType(typeLabel).getInstances(tx()).count());
    }

    @When("relation {var} add player for role\\( ?{type_label} ?): {var}")
    public void relation_add_player_for_role(String var1, String roleTypeLabel, String var2) {
        get(var1).asRelation().addPlayer(tx(), get(var1).asRelation().getType().getRelates(tx(), roleTypeLabel), get(var2));
    }

    @When("relation {var} add player for role\\( ?{type_label} ?): {var}; throws exception")
    public void relation_add_player_for_role_throws_exception(String var1, String roleTypeLabel, String var2) {
        assertThrows(() -> get(var1).asRelation().addPlayer(tx(), get(var1).asRelation().getType().getRelates(tx(), roleTypeLabel), get(var2)));
    }

    @When("relation {var} remove player for role\\( ?{type_label} ?): {var}")
    public void relation_remove_player_for_role(String var1, String roleTypeLabel, String var2) {
        get(var1).asRelation().removePlayer(tx(), get(var1).asRelation().getType().getRelates(tx(), roleTypeLabel), get(var2));
    }

    @Then("relation {var} get players contain:")
    public void relation_get_players_contain(String var, Map<String, String> players) {
        Relation relation = get(var).asRelation();
        players.forEach((rt, var2) -> assertTrue(relation.getPlayersByRoleType(tx()).get(relation.getType().getRelates(tx(), rt)).contains(get(var2.substring(1)))));
    }

    @Then("relation {var} get players do not contain:")
    public void relation_get_players_do_not_contain(String var, Map<String, String> players) {
        Relation relation = get(var).asRelation();
        players.forEach((rt, var2) -> {
            List<? extends Thing> p;
            if ((p = relation.getPlayersByRoleType(tx()).get(relation.getType().getRelates(tx(), rt))) != null) {
                assertFalse(p.contains(get(var2.substring(1))));
            }
        });
    }

    @Then("relation {var} get players contain: {var}")
    public void relation_get_players_contain(String var1, String var2) {
        assertTrue(get(var1).asRelation().getPlayers(tx()).anyMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players do not contain: {var}")
    public void relation_get_players_do_not_contain(String var1, String var2) {
        assertTrue(get(var1).asRelation().getPlayers(tx()).noneMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players for role\\( ?{type_label} ?) contain: {var}")
    public void relation_get_players_for_role_contain(String var1, String roleTypeLabel, String var2) {
        assertTrue(get(var1).asRelation().getPlayers(tx(), get(var1).asRelation().getType().getRelates(tx(), roleTypeLabel)).anyMatch(p -> p.equals(get(var2))));
    }

    @Then("relation {var} get players for role\\( ?{type_label} ?) do not contain: {var}")
    public void relation_get_players_for_role_do_not_contain(String var1, String roleTypeLabel, String var2) {
        assertTrue(get(var1).asRelation().getPlayers(tx(), get(var1).asRelation().getType().getRelates(tx(), roleTypeLabel)).noneMatch(p -> p.equals(get(var2))));
    }
}
