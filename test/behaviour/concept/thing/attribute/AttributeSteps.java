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

package com.vaticle.typedb.client.test.behaviour.concept.thing.attribute;

import com.vaticle.typedb.client.api.concept.Concept.ValueType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;

import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.get;
import static com.vaticle.typedb.client.test.behaviour.concept.thing.ThingSteps.put;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttributeSteps {

    @When("attribute\\( ?{type_label} ?) get instances contain: {var}")
    public void attribute_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().concepts().getAttributeType(typeLabel).asRemote(tx()).getInstances().anyMatch(i -> i.equals(get(var))));
    }

    @Then("attribute {var} get owners contain: {var}")
    public void attribute_get_owners_contain(String var1, String var2) {
        assertTrue(get(var1).asAttribute().asRemote(tx()).getOwners().anyMatch(o -> o.equals(get(var2))));
    }

    @Then("attribute {var} get owners do not contain: {var}")
    public void attribute_get_owners_do_not_contain(String var1, String var2) {
        assertTrue(get(var1).asAttribute().asRemote(tx()).getOwners().noneMatch(o -> o.equals(get(var2))));
    }

    @Then("attribute {var} has value type: {value_type}")
    public void attribute_has_value_type(String var, ValueType valueType) {
        assertEquals(valueType, get(var).asAttribute().getType().getValueType());
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}")
    public void attribute_type_as_boolean_put(String var, String typeLabel, boolean value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asBoolean().asRemote(tx()).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}; throws exception")
    public void attribute_type_as_boolean_put_throws_exception(String typeLabel, boolean value) {
        assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asBoolean().asRemote(tx()).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}")
    public void attribute_type_as_long_put(String var, String typeLabel, long value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asLong().asRemote(tx()).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}; throws exception")
    public void attribute_type_as_long_put_throws_exception(String typeLabel, long value) {
        assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asLong().asRemote(tx()).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}")
    public void attribute_type_as_double_put(String var, String typeLabel, double value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asDouble().asRemote(tx()).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}; throws exception")
    public void attribute_type_as_double_put_throws_exception(String typeLabel, double value) {
        assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asDouble().asRemote(tx()).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}")
    public void attribute_type_as_string_put(String var, String typeLabel, String value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asString().asRemote(tx()).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}; throws exception")
    public void attribute_type_as_string_put_throws_exception(String typeLabel, String value) {
        assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asString().asRemote(tx()).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}")
    public void attribute_type_as_datetime_put(String var, String typeLabel, LocalDateTime value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asDateTime().asRemote(tx()).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}; throws exception")
    public void attribute_type_as_datetime_put_throws_exception(String typeLabel, LocalDateTime value) {
        assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asDateTime().asRemote(tx()).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) get: {bool}")
    public void attribute_type_as_boolean_get(String var, String typeLabel, boolean value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asBoolean().asRemote(tx()).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) get: {int}")
    public void attribute_type_as_long_get(String var, String typeLabel, long value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asLong().asRemote(tx()).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) get: {double}")
    public void attribute_type_as_double_get(String var, String typeLabel, double value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asDouble().asRemote(tx()).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) get: {word}")
    public void attribute_type_as_string_get(String var, String typeLabel, String value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asString().asRemote(tx()).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) get: {datetime}")
    public void attribute_type_as_datetime_get(String var, String typeLabel, LocalDateTime value) {
        put(var, tx().concepts().getAttributeType(typeLabel).asDateTime().asRemote(tx()).get(value));
    }

    @Then("attribute {var} has boolean value: {bool}")
    public void attribute_has_boolean_value(String var, boolean value) {
        assertEquals(value, get(var).asAttribute().asBoolean().getValue());
    }

    @Then("attribute {var} has long value: {long}")
    public void attribute_has_long_value(String var, long value) {
        assertEquals(value, get(var).asAttribute().asLong().getValue().longValue());
    }

    @Then("attribute {var} has double value: {double}")
    public void attribute_has_double_value(String var, double value) {
        assertEquals(value, get(var).asAttribute().asDouble().getValue(), 0.0001);
    }

    @Then("attribute {var} has string value: {word}")
    public void attribute_has_string_value(String var, String value) {
        assertEquals(value, get(var).asAttribute().asString().getValue());
    }

    @Then("attribute {var} has datetime value: {datetime}")
    public void attribute_has_datetime_value(String var, LocalDateTime value) {
        assertEquals(value, get(var).asAttribute().asDateTime().getValue());
    }
}
