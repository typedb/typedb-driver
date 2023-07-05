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

package com.vaticle.typedb.client.test.behaviour.concept.thing.entity;

import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.common.Label;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;

import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.get;
import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.put;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("CheckReturnValue")
public class EntitySteps {
    @When("{var} = entity\\( ?{type_label} ?) create new instance")
    public void entity_type_create_new_instance(String var, String typeLabel) {
        put(var, tx().concepts().getEntityType(typeLabel).create(tx()));
    }

    @When("entity\\( ?{type_label} ?) create new instance; throws exception")
    public void entity_type_create_new_instance_throws_exception(String typeLabel) {
        assertThrows(() -> tx().concepts().getEntityType(typeLabel).create(tx()));
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, int keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Entity entity = tx().concepts().getEntityType(type).create(tx());
        entity.setHas(tx(), key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, String keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Entity entity = tx().concepts().getEntityType(type).create(tx());
        entity.setHas(tx(), key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, LocalDateTime keyValue) {
        Attribute key = tx().concepts().getAttributeType(keyType).put(tx(), keyValue);
        Entity entity = tx().concepts().getEntityType(type).create(tx());
        entity.setHas(tx(), key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, long keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, String keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, LocalDateTime keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).get(tx(), keyValue).getOwners(tx()).filter(owner -> owner.getType().getLabel().equals(Label.of(type))).findFirst().orElse(null));
    }

    @Then("entity\\( ?{type_label} ?) get instances contain: {var}")
    public void entity_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getEntityType(typeLabel).getInstances(tx()).anyMatch(i -> i.equals(get(var))));
    }

    @Then("entity\\( ?{type_label} ?) get instances is empty")
    public void entity_type_get_instances_is_empty(String typeLabel) {
        assertEquals(0, tx().concepts().getEntityType(typeLabel).getInstances(tx()).count());
    }
}
