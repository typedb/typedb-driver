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

package com.vaticle.typedb.client.test.behaviour.concept.type.attributetype;

import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.ThingType.Annotation;
import com.vaticle.typedb.client.api.concept.value.Value;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;

import static com.vaticle.typedb.client.api.concept.Concept.Transitivity.EXPLICIT;
import static com.vaticle.typedb.client.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.vaticle.typedb.common.collection.Collections.set;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AttributeTypeSteps {

    @When("put attribute type: {type_label}, with value type: {value_type}")
    public void put_attribute_type_with_value_type(String typeLabel, Value.Type valueType) {
        tx().concepts().putAttributeType(typeLabel, valueType);
    }

    @Then("attribute\\( ?{type_label} ?) get value type: {value_type}")
    public void attribute_type_get_value_type(String typeLabel, Value.Type valueType) {
        assertEquals(valueType, tx().concepts().getAttributeType(typeLabel).getValueType());
    }

    @Then("attribute\\( ?{type_label} ?) get supertype value type: {value_type}")
    public void attribute_type_get_supertype_value_type(String typeLabel, Value.Type valueType) {
        AttributeType supertype = tx().concepts().getAttributeType(typeLabel).getSupertype(tx()).asAttributeType();
        assertEquals(valueType, supertype.getValueType());
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes contain:")
    public void attribute_type_as_value_type_get_subtypes_contain(String typeLabel, Value.Type valueType, List<String> subLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getSubtypes(tx(), valueType).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(subLabels));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes do not contain:")
    public void attribute_type_as_value_type_get_subtypes_do_not_contain(String typeLabel, Value.Type valueType, List<String> subLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getSubtypes(tx(), valueType).map(t -> t.getLabel().name()).collect(toSet());
        for (String subLabel : subLabels) {
            assertFalse(actuals.contains(subLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) set regex: {}")
    public void attribute_type_as_value_type_set_regex(String typeLabel, Value.Type valueType, String regex) {
        if (!valueType.equals(Value.Type.STRING)) fail();
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        attributeType.setRegex(tx(), regex);
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) unset regex")
    public void attribute_type_as_value_type_unset_regex(String typeLabel, Value.Type valueType) {
        if (!valueType.equals(Value.Type.STRING)) fail();
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        attributeType.unsetRegex(tx());
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get regex: {}")
    public void attribute_type_as_value_type_get_regex(String typeLabel, Value.Type valueType, String regex) {
        if (!valueType.equals(Value.Type.STRING)) fail();
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        assertEquals(regex, attributeType.getRegex(tx()));
    }

    @Then("attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) does not have any regex")
    public void attribute_type_as_value_type_does_not_have_any_regex(String typeLabel, Value.Type valueType) {
        attribute_type_as_value_type_get_regex(typeLabel, valueType, null);
    }

    @Then("attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; contain:")
    public void attribute_type_get_owners_with_annotations_contain(String typeLabel, List<Annotation> annotations, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), set(annotations)).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; do not contain:")
    public void attribute_type_get_owners_with_annotations_do_not_contain(String typeLabel, List<Annotation> annotations, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), set(annotations)).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; contain:")
    public void attribute_type_get_owners_explicit_with_annotations_contain(String typeLabel, List<Annotation> annotations, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), set(annotations), EXPLICIT).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; do not contain:")
    public void attribute_type_get_owners_explicit_with_annotations_do_not_contain(String typeLabel, List<Annotation> annotations, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), set(annotations), EXPLICIT).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) get owners contain:")
    public void attribute_type_get_owners_contain(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), emptySet()).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get owners do not contain:")
    public void attribute_type_get_owners_do_not_contain(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), emptySet()).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }

    @Then("attribute\\( ?{type_label} ?) get owners explicit contain:")
    public void attribute_type_get_owners_explicit_contain(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), emptySet(), EXPLICIT).map(t -> t.getLabel().name()).collect(toSet());
        assertTrue(actuals.containsAll(ownerLabels));
    }

    @Then("attribute\\( ?{type_label} ?) get owners explicit do not contain:")
    public void attribute_type_get_owners_explicit_do_not_contain(String typeLabel, List<String> ownerLabels) {
        AttributeType attributeType = tx().concepts().getAttributeType(typeLabel);
        Set<String> actuals = attributeType.getOwners(tx(), emptySet(), EXPLICIT).map(t -> t.getLabel().name()).collect(toSet());
        for (String ownerLabel : ownerLabels) {
            assertFalse(actuals.contains(ownerLabel));
        }
    }
}
