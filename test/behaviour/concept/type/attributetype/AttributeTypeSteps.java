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

package grakn.client.test.behaviour.concept.type.attributetype;

import grakn.client.api.concept.type.AttributeType;
import grakn.client.api.concept.type.AttributeType.ValueType;
import grakn.client.api.concept.type.Type;
import grakn.client.common.exception.GraknClientException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_VALUE_TYPE;
import static grakn.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AttributeTypeSteps {

    @When("put attribute type: {type_label}, with value type: {value_type}")
    public void put_attribute_type_with_value_type(String typeLabel, ValueType valueType) {
        tx().concepts().putAttributeType(typeLabel, valueType);
    }

    @Then("attribute\\( ?{type_label} ?) get value type: {value_type}")
    public void attribute_type_get_value_type(String typeLabel, ValueType valueType) {
        assertEquals(valueType, tx().concepts().getAttributeType(typeLabel).getValueType());
    }

    @Then("attribute\\( ?{type_label} ?) get supertype value type: {value_type}")
    public void attribute_type_get_supertype_value_type(String typeLabel, ValueType valueType) {
        Type supertype = tx().concepts().getAttributeType(typeLabel).asRemote(tx()).getSupertype();
        assertEquals(valueType, supertype.asAttributeType().getValueType());
    }

    private AttributeType attribute_type_as_value_type(String typeLabel, ValueType valueType) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        switch (valueType) {
            case OBJECT:
                return attributeType;
            case BOOLEAN:
                return attributeType.asBoolean();
            case LONG:
                return attributeType.asLong();
            case DOUBLE:
                return attributeType.asDouble();
            case STRING:
                return attributeType.asString();
            case DATETIME:
                return attributeType.asDateTime();
            default:
                throw new GraknClientException(BAD_VALUE_TYPE, valueType);
        }
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes contain:")
    public void attribute_type_as_value_type_get_subtypes_contain(String typeLabel, ValueType valueType, List<String> subLabels) {
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        Set<String> actuals = attributeType.asRemote(tx()).getSubtypes().map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(subLabels));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes do not contain:")
    public void attribute_type_as_value_type_get_subtypes_do_not_contain(String typeLabel, ValueType valueType, List<String> subLabels) {
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        Set<String> actuals = attributeType.asRemote(tx()).getSubtypes().map(t -> t.getLabel().name()).collect(toSet());
        for (String subLabel : subLabels) {
            assertFalse(actuals.contains(subLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) set regex: {}")
    public void attribute_type_as_value_type_set_regex(String typeLabel, ValueType valueType, String regex) {
        if (!valueType.equals(ValueType.STRING)) fail();
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        attributeType.asString().asRemote(tx()).setRegex(regex);
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) unset regex")
    public void attribute_type_as_value_type_unset_regex(String typeLabel, AttributeType.ValueType valueType) {
        if (!valueType.equals(AttributeType.ValueType.STRING)) fail();
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        attributeType.asString().asRemote(tx()).setRegex(null);
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get regex: {}")
    public void attribute_type_as_value_type_get_regex(String typeLabel, ValueType valueType, String regex) {
        if (!valueType.equals(ValueType.STRING)) fail();
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        assertEquals(regex, attributeType.asString().asRemote(tx()).getRegex());
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) does not have any regex")
    public void attribute_type_as_value_type_does_not_have_any_regex(String typeLabel, AttributeType.ValueType valueType) {
        if (!valueType.equals(AttributeType.ValueType.STRING)) fail();
        AttributeType attributeType = attribute_type_as_value_type(typeLabel, valueType);
        assertNull(attributeType.asString().asRemote(tx()).getRegex());
    }

    @Then("attribute\\( ?{type_label} ?) get key owners contain:")
    public void attribute_type_get_owners_as_key_contains(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.asRemote(tx()).getOwners(true).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get key owners do not contain:")
    public void attribute_type_get_owners_as_key_do_not_contains(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.asRemote(tx()).getOwners(true).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) get attribute owners contain:")
    public void attribute_type_get_owners_as_attribute_contains(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.asRemote(tx()).getOwners(false).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get attribute owners do not contain:")
    public void attribute_type_get_owners_as_attribute_do_not_contains(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.asRemote(tx()).getOwners(false).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }
}
