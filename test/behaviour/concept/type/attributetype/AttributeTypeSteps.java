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

package grakn.client.test.behaviour.concept.type.attributetype;

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;

import static grakn.client.common.exception.ErrorMessage.Concept.UNRECOGNISED_CONCEPT;
import static grakn.client.test.behaviour.connection.ConnectionSteps.tx;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Behaviour Steps specific to AttributeTypeSteps
 */
public class AttributeTypeSteps {

    @When("put attribute type: {type_label}, with value type: {value_type}")
    public void put_attribute_type_with_value_type(String typeLabel, ValueType valueType) {
        tx().putAttributeType(typeLabel, valueType);
    }

    @Then("attribute\\( ?{type_label} ?) get value type: {value_type}")
    public void attribute_type_get_value_type(String typeLabel, ValueType valueType) {
        assertEquals(valueType, tx().getAttributeType(typeLabel).getValueType());
    }

    @Then("attribute\\( ?{type_label} ?) get supertype value type: {value_type}")
    public void attribute_type_get_supertype_value_type(String typeLabel, ValueType valueType) {
        Type.Remote supertype = tx().getAttributeType(typeLabel).getSupertype();
        assertEquals(valueType, supertype.asAttributeType().getValueType());
    }

    private AttributeType.Remote attribute_type_as_value_type(String typeLabel, ValueType valueType) {
        final AttributeType.Remote attributeType = tx().getAttributeType(typeLabel);
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
                throw new GraknClientException(UNRECOGNISED_CONCEPT.message(ValueType.class.getCanonicalName(), valueType));
        }
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes contain:")
    public void attribute_type_as_value_type_get_subtypes_contain(String typeLabel, ValueType valueType, List<String> subLabels) {
        AttributeType.Remote attributeType = attribute_type_as_value_type(typeLabel, valueType);
        Set<String> actuals = attributeType.getSubtypes().map(ThingType::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(subLabels));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes do not contain:")
    public void attribute_type_as_value_type_get_subtypes_do_not_contain(String typeLabel, ValueType valueType, List<String> subLabels) {
        AttributeType.Remote attributeType = attribute_type_as_value_type(typeLabel, valueType);
        Set<String> actuals = attributeType.getSubtypes().map(ThingType::getLabel).collect(toSet());
        for (String subLabel : subLabels) {
            assertFalse(actuals.contains(subLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) set regex: {}")
    public void attribute_type_as_value_type_set_regex(String typeLabel, ValueType valueType, String regex) {
        if (!valueType.equals(ValueType.STRING)) fail();
        AttributeType.Remote attributeType = attribute_type_as_value_type(typeLabel, valueType);
        attributeType.asString().setRegex(regex);
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get regex: {}")
    public void attribute_type_as_value_type_get_regex(String typeLabel, ValueType valueType, String regex) {
        if (!valueType.equals(ValueType.STRING)) fail();
        AttributeType.Remote attributeType = attribute_type_as_value_type(typeLabel, valueType);
        assertEquals(regex, attributeType.asString().getRegex());
    }
}
