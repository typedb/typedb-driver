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

package grakn.client.test.behaviour.concept.thing.entity;

import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Entity;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;

import static grakn.client.test.behaviour.concept.thing.ThingSteps.get;
import static grakn.client.test.behaviour.concept.thing.ThingSteps.put;
import static grakn.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static grakn.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntitySteps {

    @When("{var} = entity\\( ?{type_label} ?) create new instance")
    public void entity_type_create_new_instance(String var, String typeLabel) {
        put(var, tx().concepts().getEntityType(typeLabel).asRemote(tx()).create());
    }

    @When("entity\\( ?{type_label} ?) create new instance; throws exception")
    public void entity_type_create_new_instance_throws_exception(String typeLabel) {
        assertThrows(() -> tx().concepts().getEntityType(typeLabel).asRemote(tx()).create());
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, int keyValue) {
        Attribute.Long key = tx().concepts().getAttributeType(keyType).asLong().asRemote(tx()).put(keyValue);
        Entity entity = tx().concepts().getEntityType(type).asRemote(tx()).create();
        entity.asRemote(tx()).setHas(key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, String keyValue) {
        Attribute.String key = tx().concepts().getAttributeType(keyType).asString().asRemote(tx()).put(keyValue);
        Entity entity = tx().concepts().getEntityType(type).asRemote(tx()).create();
        entity.asRemote(tx()).setHas(key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, LocalDateTime keyValue) {
        Attribute.DateTime key = tx().concepts().getAttributeType(keyType).asDateTime().asRemote(tx()).put(keyValue);
        Entity entity = tx().concepts().getEntityType(type).asRemote(tx()).create();
        entity.asRemote(tx()).setHas(key);
        put(var, entity);
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, long keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asLong().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.getType().getLabel().equals(type)).findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, String keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asString().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.getType().getLabel().equals(type)).findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, LocalDateTime keyValue) {
        put(var1, tx().concepts().getAttributeType(keyType).asDateTime().asRemote(tx()).get(keyValue).asRemote(tx()).getOwners()
                .filter(owner -> owner.getType().getLabel().equals(type)).findFirst().orElse(null));
    }

    @Then("entity\\( ?{type_label} ?) get instances contain: {var}")
    public void entity_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getEntityType(typeLabel).asRemote(tx()).getInstances().anyMatch(i -> i.equals(get(var))));
    }

    @Then("entity\\( ?{type_label} ?) get instances is empty")
    public void entity_type_get_instances_is_empty(String typeLabel) {
        assertEquals(0, tx().concepts().getEntityType(typeLabel).asRemote(tx()).getInstances().count());
    }
}
