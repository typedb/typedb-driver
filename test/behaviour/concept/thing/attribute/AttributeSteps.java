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

package grakn.client.test.behaviour.concept.thing.attribute;

import grakn.client.concept.ValueType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;

import static grakn.client.test.behaviour.concept.thing.ThingSteps.get;
import static grakn.client.test.behaviour.concept.thing.ThingSteps.put;
import static grakn.client.test.behaviour.connection.ConnectionSteps.tx;
import static grakn.client.test.behaviour.util.Util.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttributeSteps {

    @When("attribute\\( ?{type_label} ?) get instances contain: {var}")
    public void attribute_type_get_instances_contain(String typeLabel, String var) {
        assertTrue(tx().getAttributeType(typeLabel).instances().anyMatch(i -> i.equals(get(var))));
    }

    @Then("attribute {var} get owners contain: {var}")
    public void attribute_get_owners_contain(String var1, String var2) {
        assertTrue(get(var1).asAttribute().owners().anyMatch(o -> o.equals(get(var2))));
    }

    @Then("attribute {var} get owners do not contain: {var}")
    public void attribute_get_owners_do_not_contain(String var1, String var2) {
        assertTrue(get(var1).asAttribute().owners().noneMatch(o -> o.equals(get(var2))));
    }

    @Then("attribute {var} has value type: {value_type}")
    public void attribute_has_value_type(String var, ValueType valueType) {
        assertEquals(valueType, get(var).asAttribute().getType().valueType());
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}")
    public void attribute_type_as_boolean_put(String var, String typeLabel, boolean value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.BOOLEAN).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?boolean ?) put: {bool}; throws exception")
    public void attribute_type_as_boolean_put_throws_exception(String typeLabel, boolean value) {
        assertThrows(() -> tx().getAttributeType(typeLabel).asAttributeType(ValueType.BOOLEAN).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}")
    public void attribute_type_as_long_put(String var, String typeLabel, long value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.LONG).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?long ?) put: {int}; throws exception")
    public void attribute_type_as_long_put_throws_exception(String typeLabel, long value) {
        assertThrows(() -> tx().getAttributeType(typeLabel).asAttributeType(ValueType.LONG).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}")
    public void attribute_type_as_double_put(String var, String typeLabel, double value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.DOUBLE).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?double ?) put: {double}; throws exception")
    public void attribute_type_as_double_put_throws_exception(String typeLabel, double value) {
        assertThrows(() -> tx().getAttributeType(typeLabel).asAttributeType(ValueType.DOUBLE).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}")
    public void attribute_type_as_string_put(String var, String typeLabel, String value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.STRING).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?string ?) put: {word}; throws exception")
    public void attribute_type_as_string_put_throws_exception(String typeLabel, String value) {
        assertThrows(() -> tx().getAttributeType(typeLabel).asAttributeType(ValueType.STRING).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}")
    public void attribute_type_as_datetime_put(String var, String typeLabel, LocalDateTime value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.DATETIME).put(value));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?datetime ?) put: {datetime}; throws exception")
    public void attribute_type_as_datetime_put_throws_exception(String typeLabel, LocalDateTime value) {
        assertThrows(() -> tx().getAttributeType(typeLabel).asAttributeType(ValueType.DATETIME).put(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?boolean ?) get: {bool}")
    public void attribute_type_as_boolean_get(String var, String typeLabel, boolean value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.BOOLEAN).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?long ?) get: {int}")
    public void attribute_type_as_long_get(String var, String typeLabel, long value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.LONG).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?double ?) get: {double}")
    public void attribute_type_as_double_get(String var, String typeLabel, double value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.DOUBLE).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?string ?) get: {word}")
    public void attribute_type_as_string_get(String var, String typeLabel, String value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.STRING).get(value));
    }

    @When("{var} = attribute\\( ?{type_label} ?) as\\( ?datetime ?) get: {datetime}")
    public void attribute_type_as_datetime_get(String var, String typeLabel, LocalDateTime value) {
        put(var, tx().getAttributeType(typeLabel).asAttributeType(ValueType.DATETIME).get(value));
    }

    @Then("attribute {var} has boolean value: {bool}")
    public void attribute_has_boolean_value(String var, boolean value) {
        assertEquals(value, get(var).asAttribute().asAttribute(ValueType.BOOLEAN).value());
    }

    @Then("attribute {var} has long value: {long}")
    public void attribute_has_long_value(String var, long value) {
        assertEquals(value, get(var).asAttribute().asAttribute(ValueType.LONG).value().longValue());
    }

    @Then("attribute {var} has double value: {double}")
    public void attribute_has_double_value(String var, double value) {
        assertEquals(value, get(var).asAttribute().asAttribute(ValueType.DOUBLE).value(), 0.0001);
    }

    @Then("attribute {var} has string value: {word}")
    public void attribute_has_string_value(String var, String value) {
        assertEquals(value, get(var).asAttribute().asAttribute(ValueType.STRING).value());
    }

    @Then("attribute {var} has datetime value: {datetime}")
    public void attribute_has_datetime_value(String var, LocalDateTime value) {
        assertEquals(value, get(var).asAttribute().asAttribute(ValueType.DATETIME).value());
    }
}
