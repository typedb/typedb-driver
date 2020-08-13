/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.client.test.behaviour.concept.thing.entity;

import grakn.client.concept.ValueType;
import grakn.client.concept.thing.Attribute;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;

import static grakn.client.test.behaviour.util.Util.assertThrows;
import static grakn.client.test.behaviour.concept.thing.ThingSteps.get;
import static grakn.client.test.behaviour.concept.thing.ThingSteps.put;
import static grakn.client.test.behaviour.connection.ConnectionSteps.tx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntitySteps {

    @When("{var} = entity\\( ?{type_label} ?) create new instance")
    public void entity_type_create_new_instance(String var, String typeLabel) {
        put(var, tx().getEntityType(typeLabel).create());
    }

    @When("entity\\( ?{type_label} ?) create new instance; throws exception")
    public void entity_type_create_new_instance_throws_exception(String typeLabel) {
        assertThrows(() -> tx().getEntityType(typeLabel).create());
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {bool}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, boolean keyValue) {
        Attribute.Remote<Boolean> key = tx().getAttributeType(keyType).asAttributeType(ValueType.BOOLEAN).put(keyValue);
        put(var, tx().getEntityType(type).create().setHas(key));
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, int keyValue) {
        Attribute.Remote<Long> key = tx().getAttributeType(keyType).asAttributeType(ValueType.LONG).put((long) keyValue);
        put(var, tx().getEntityType(type).create().setHas(key));
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {double}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, double keyValue) {
        Attribute.Remote<Double> key = tx().getAttributeType(keyType).asAttributeType(ValueType.DOUBLE).put(keyValue);
        put(var, tx().getEntityType(type).create().setHas(key));
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, String keyValue) {
        Attribute.Remote<String> key = tx().getAttributeType(keyType).asAttributeType(ValueType.STRING).put(keyValue);
        put(var, tx().getEntityType(type).create().setHas(key));
    }

    @When("{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_create_new_instance_with_key(String var, String type, String keyType, LocalDateTime keyValue) {
        Attribute.Remote<LocalDateTime> key = tx().getAttributeType(keyType).asAttributeType(ValueType.DATETIME).put(keyValue);
        put(var, tx().getEntityType(type).create().setHas(key));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {bool}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, boolean keyValue) {
        put(var1, tx().getAttributeType(keyType).asAttributeType(ValueType.BOOLEAN).get(keyValue).getOwners()
                .filter(owner -> owner.getType().equals(tx().getEntityType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, long keyValue) {
        put(var1, tx().getAttributeType(keyType).asAttributeType(ValueType.LONG).get(keyValue).getOwners()
                .filter(owner -> owner.getType().equals(tx().getEntityType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {double}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, double keyValue) {
        put(var1, tx().getAttributeType(keyType).asAttributeType(ValueType.DOUBLE).get(keyValue).getOwners()
                .filter(owner -> owner.getType().equals(tx().getEntityType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, String keyValue) {
        put(var1, tx().getAttributeType(keyType).asAttributeType(ValueType.STRING).get(keyValue).getOwners()
                .filter(owner -> owner.getType().equals(tx().getEntityType(type)))
                .findFirst().orElse(null));
    }

    @When("{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")
    public void entity_type_get_instance_with_key(String var1, String type, String keyType, LocalDateTime keyValue) {
        put(var1, tx().getAttributeType(keyType).asAttributeType(ValueType.DATETIME).get(keyValue).getOwners()
                .filter(owner -> owner.getType().equals(tx().getEntityType(type)))
                .findFirst().orElse(null));
    }

    @Then("entity\\( ?{type_label} ?) get instances contain: {var}")
    public void entity_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().getEntityType(typeLabel).getInstances().anyMatch(i -> i.equals(get(var))));
    }

    @Then("entity\\( ?{type_label} ?) get instances is empty")
    public void entity_type_get_instances_is_empty(String typeLabel) {
        assertEquals(0, tx().getEntityType(typeLabel).getInstances().count());
    }
}
