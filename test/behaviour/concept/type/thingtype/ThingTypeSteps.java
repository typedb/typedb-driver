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

package grakn.client.test.behaviour.concept.type.thingtype;

import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.client.test.behaviour.config.Parameters;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static grakn.client.test.behaviour.config.Parameters.RootLabel;
import static grakn.client.test.behaviour.connection.ConnectionSteps.tx;
import static grakn.client.test.behaviour.util.Util.assertThrows;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThingTypeSteps {

    private static final String UNRECOGNISED_VALUE = "Unrecognized value";

    public static ThingType get_thing_type(RootLabel rootLabel, String typeLabel) {
        switch (rootLabel) {
            case ENTITY:
                return tx().concepts().getEntityType(typeLabel);
            case ATTRIBUTE:
                return tx().concepts().getAttributeType(typeLabel);
            case RELATION:
                return tx().concepts().getRelationType(typeLabel);
            default:
                throw new IllegalArgumentException(UNRECOGNISED_VALUE);
        }
    }

    @Then("thing type root get supertypes contain:")
    public void thing_type_root_get_supertypes_contain(List<String> superLabels) {
        Set<String> actuals = tx().concepts().getRootThingType().asRemote(tx()).getSupertypes().map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(superLabels));
    }

    @Then("thing type root get supertypes do not contain:")
    public void thing_type_root_get_supertypes_do_not_contain(List<String> superLabels) {
        Set<String> actuals = tx().concepts().getRootThingType().asRemote(tx()).getSupertypes().map(Type::getLabel).collect(toSet());
        for (String superLabel : superLabels) {
            assertFalse(actuals.contains(superLabel));
        }
    }

    @Then("thing type root get subtypes contain:")
    public void thing_type_root_get_subtypes_contain(List<String> subLabels) {
        Set<String> actuals = tx().concepts().getRootThingType().asRemote(tx()).getSubtypes().map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(subLabels));
    }

    @Then("thing type root get subtypes do not contain:")
    public void thing_type_root_get_subtypes_do_not_contain(List<String> subLabels) {
        Set<String> actuals = tx().concepts().getRootThingType().asRemote(tx()).getSubtypes().map(Type::getLabel).collect(toSet());
        for (String subLabel : subLabels) {
            assertFalse(actuals.contains(subLabel));
        }
    }

    @When("put {root_label} type: {type_label}")
    public void put_thing_type(RootLabel rootLabel, String typeLabel) {
        switch (rootLabel) {
            case ENTITY:
                tx().concepts().putEntityType(typeLabel);
                break;
            case RELATION:
                tx().concepts().putRelationType(typeLabel);
                break;
            default:
                throw new IllegalArgumentException(UNRECOGNISED_VALUE);
        }
    }

    @When("delete {root_label} type: {type_label}")
    public void delete_thing_type(RootLabel rootLabel, String typeLabel) {
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).delete();
    }

    @Then("delete {root_label} type: {type_label}; throws exception")
    public void delete_thing_type_throws_exception(RootLabel rootLabel, String typeLabel) {
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).delete());
    }

    @Then("{root_label}\\( ?{type_label} ?) is null: {bool}")
    public void thing_type_is_null(RootLabel rootLabel, String typeLabel, boolean isNull) {
        assertEquals(isNull, isNull(get_thing_type(rootLabel, typeLabel)));
    }

    @When("{root_label}\\( ?{type_label} ?) set label: {type_label}")
    public void thing_type_set_label(RootLabel rootLabel, String typeLabel, String newLabel) {
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setLabel(newLabel);
    }

    @Then("{root_label}\\( ?{type_label} ?) get label: {type_label}")
    public void thing_type_get_label(RootLabel rootLabel, String typeLabel, String getLabel) {
        assertEquals(getLabel, get_thing_type(rootLabel, typeLabel).getLabel());
    }

    @When("{root_label}\\( ?{type_label} ?) set abstract: {bool}")
    public void thing_type_set_abstract(RootLabel rootLabel, String typeLabel, boolean isAbstract) {
        final ThingType thingType = get_thing_type(rootLabel, typeLabel);
        if (isAbstract) {
            thingType.asRemote(tx()).setAbstract();
        } else {
            thingType.asRemote(tx()).unsetAbstract();
        }
    }

    @Then("{root_label}\\( ?{type_label} ?) is abstract: {bool}")
    public void thing_type_is_abstract(RootLabel rootLabel, String typeLabel, boolean isAbstract) {
        assertEquals(isAbstract, get_thing_type(rootLabel, typeLabel).asRemote(tx()).isAbstract());
    }

    @When("{root_label}\\( ?{type_label} ?) set supertype: {type_label}")
    public void thing_type_set_supertype(RootLabel rootLabel, String typeLabel, String superLabel) {
        switch (rootLabel) {
            case ENTITY:
                EntityType entitySuperType = tx().concepts().getEntityType(superLabel);
                tx().concepts().getEntityType(typeLabel).asRemote(tx()).setSupertype(entitySuperType);
                break;
            case ATTRIBUTE:
                AttributeType attributeSuperType = tx().concepts().getAttributeType(superLabel);
                tx().concepts().getAttributeType(typeLabel).asRemote(tx()).setSupertype(attributeSuperType);
                break;
            case RELATION:
                RelationType relationSuperType = tx().concepts().getRelationType(superLabel);
                tx().concepts().getRelationType(typeLabel).asRemote(tx()).setSupertype(relationSuperType);
                break;
        }
    }

    @Then("{root_label}\\( ?{type_label} ?) set supertype: {type_label}; throws exception")
    public void thing_type_set_supertype_throws_exception(RootLabel rootLabel, String typeLabel, String superLabel) {
        switch (rootLabel) {
            case ENTITY:
                EntityType entitySuperType = tx().concepts().getEntityType(superLabel);
                assertThrows(() -> tx().concepts().getEntityType(typeLabel).asRemote(tx()).setSupertype(entitySuperType));
                break;
            case ATTRIBUTE:
                AttributeType attributeSuperType = tx().concepts().getAttributeType(superLabel);
                assertThrows(() -> tx().concepts().getAttributeType(typeLabel).asRemote(tx()).setSupertype(attributeSuperType));
                break;
            case RELATION:
                RelationType relationSuperType = tx().concepts().getRelationType(superLabel);
                assertThrows(() -> tx().concepts().getRelationType(typeLabel).asRemote(tx()).setSupertype(relationSuperType));
                break;
        }
    }

    @Then("{root_label}\\( ?{type_label} ?) get supertype: {type_label}")
    public void thing_type_get_supertype(RootLabel rootLabel, String typeLabel, String superLabel) {
        ThingType supertype = get_thing_type(rootLabel, superLabel);
        assertEquals(supertype, get_thing_type(rootLabel, typeLabel).asRemote(tx()).getSupertype());
    }

    @Then("{root_label}\\( ?{type_label} ?) get supertypes contain:")
    public void thing_type_get_supertypes_contain(RootLabel rootLabel, String typeLabel, List<String> superLabels) {
        ThingType thing_type = get_thing_type(rootLabel, typeLabel);
        ThingType.Remote remote = thing_type.asRemote(tx());
        Set<String> actuals = remote.getSupertypes().map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(superLabels));
    }

    @Then("{root_label}\\( ?{type_label} ?) get supertypes do not contain:")
    public void thing_type_get_supertypes_do_not_contain(RootLabel rootLabel, String typeLabel, List<String> superLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getSupertypes().map(Type::getLabel).collect(toSet());
        for (String superLabel : superLabels) {
            assertFalse(actuals.contains(superLabel));
        }
    }

    @Then("{root_label}\\( ?{type_label} ?) get subtypes contain:")
    public void thing_type_get_subtypes_contain(RootLabel rootLabel, String typeLabel, List<String> subLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getSubtypes().map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(subLabels));
    }

    @Then("{root_label}\\( ?{type_label} ?) get subtypes do not contain:")
    public void thing_type_get_subtypes_do_not_contain(RootLabel rootLabel, String typeLabel, List<String> subLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getSubtypes().map(Type::getLabel).collect(toSet());
        for (String subLabel : subLabels) {
            assertFalse(actuals.contains(subLabel));
        }
    }

    @When("{root_label}\\( ?{type_label} ?) set owns key type: {type_label}")
    public void thing_type_set_has_key_type(RootLabel rootLabel, String typeLabel, String attTypeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attTypeLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, true);
    }

    @When("{root_label}\\( ?{type_label} ?) set owns key type: {type_label} as {type_label}")
    public void thing_type_set_has_key_type_as(RootLabel rootLabel, String typeLabel, String attTypeLabel, String overriddenLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attTypeLabel);
        AttributeType overriddenType = tx().concepts().getAttributeType(overriddenLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, overriddenType, true);
    }

    @Then("{root_label}\\( ?{type_label} ?) set owns key type: {type_label}; throws exception")
    public void thing_type_set_has_key_type_throws_exception(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, true));
    }

    @Then("{root_label}\\( ?{type_label} ?) set owns key type: {type_label} as {type_label}; throws exception")
    public void thing_type_set_has_key_type_as_throws_exception(RootLabel rootLabel, String typeLabel, String attributeLabel, String overriddenLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        AttributeType overriddenType = tx().concepts().getAttributeType(overriddenLabel);
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, overriddenType, true));
    }

    @When("{root_label}\\( ?{type_label} ?) unset owns key type: {type_label}")
    public void thing_type_remove_has_key_type(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).unsetOwns(attributeType);
    }

    @Then("{root_label}\\( ?{type_label} ?) get owns key types contain:")
    public void thing_type_get_has_key_types_contain(RootLabel rootLabel, String typeLabel, List<String> attributeLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getOwns(true).map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(attributeLabels));
    }

    @Then("{root_label}\\( ?{type_label} ?) get owns key types do not contain:")
    public void thing_type_get_has_key_types_do_not_contain(RootLabel rootLabel, String typeLabel, List<String> attributeLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getOwns(true).map(Type::getLabel).collect(toSet());
        for (String attributeLabel : attributeLabels) {
            assertFalse(actuals.contains(attributeLabel));
        }
    }

    @When("{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}")
    public void thing_type_set_has_attribute_type(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType);
    }

    @Then("{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}; throws exception")
    public void thing_type_set_has_attribute_throws_exception(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType));
    }

    @When("{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}")
    public void thing_type_set_has_attribute_type_as(RootLabel rootLabel, String typeLabel, String attributeLabel, String overriddenLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        AttributeType overriddenType = tx().concepts().getAttributeType(overriddenLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, overriddenType);
    }

    @Then("{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}; throws exception")
    public void thing_type_set_has_attribute_as_throws_exception(RootLabel rootLabel, String typeLabel, String attributeLabel, String overriddenLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        AttributeType overriddenType = tx().concepts().getAttributeType(overriddenLabel);
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setOwns(attributeType, overriddenType));
    }

    @When("{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}")
    public void thing_type_unset_owns_attribute_type(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        AttributeType attributeType = tx().concepts().getAttributeType(attributeLabel);
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).unsetOwns(attributeType);
    }

    @When("{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}; throws exception")
    public void thing_type_unset_owns_attribute_type_throws_exception(RootLabel rootLabel, String typeLabel, String attributeLabel) {
        assertThrows(() -> thing_type_unset_owns_attribute_type(rootLabel, typeLabel, attributeLabel));
    }

    @Then("{root_label}\\( ?{type_label} ?) get owns attribute types contain:")
    public void thing_type_get_has_attribute_types_contain(RootLabel rootLabel, String typeLabel, List<String> attributeLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getOwns().map(Type::getLabel).collect(toSet());
        assertTrue(actuals.containsAll(attributeLabels));
    }

    @Then("{root_label}\\( ?{type_label} ?) get owns attribute types do not contain:")
    public void thing_type_get_has_attribute_types_do_not_contain(RootLabel rootLabel, String typeLabel, List<String> attributeLabels) {
        Set<String> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getOwns().map(Type::getLabel).collect(toSet());
        for (String attributeLabel : attributeLabels) {
            assertFalse(actuals.contains(attributeLabel));
        }
    }

    @When("{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}")
    public void thing_type_set_plays_role(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel) {
        RoleType roleType = tx().concepts().getRelationType(roleLabel.scope()).asRemote(tx()).getRelates(roleLabel.role());
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setPlays(roleType);
    }

    @When("{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}; throws exception")
    public void thing_type_set_plays_role_throws_exception(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel) {
        RoleType roleType = tx().concepts().getRelationType(roleLabel.scope()).asRemote(tx()).getRelates(roleLabel.role());
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setPlays(roleType));
    }

    @When("{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}")
    public void thing_type_set_plays_role_as(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel, Parameters.ScopedLabel overriddenLabel) {
        RoleType roleType = tx().concepts().getRelationType(roleLabel.scope()).asRemote(tx()).getRelates(roleLabel.role());
        RoleType overriddenType = tx().concepts().getRelationType(overriddenLabel.scope()).asRemote(tx()).getRelates(overriddenLabel.role());
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).setPlays(roleType, overriddenType);
    }

    @When("{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}; throws exception")
    public void thing_type_set_plays_role_as_throws_exception(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel, Parameters.ScopedLabel overriddenLabel) {
        RoleType roleType = tx().concepts().getRelationType(roleLabel.scope()).asRemote(tx()).getRelates(roleLabel.role());
        RoleType overriddenType = tx().concepts().getRelationType(overriddenLabel.scope()).asRemote(tx()).getRelates(overriddenLabel.role());
        assertThrows(() -> get_thing_type(rootLabel, typeLabel).asRemote(tx()).setPlays(roleType, overriddenType));
    }

    @When("{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}")
    public void thing_type_unset_plays_role(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel) {
        RoleType roleType = tx().concepts().getRelationType(roleLabel.scope()).asRemote(tx()).getRelates(roleLabel.role());
        get_thing_type(rootLabel, typeLabel).asRemote(tx()).unsetPlays(roleType);
    }

    @When("{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}; throws exception")
    public void thing_type_unset_plays_role_throws_exception(RootLabel rootLabel, String typeLabel, Parameters.ScopedLabel roleLabel) {
        assertThrows(() -> thing_type_unset_plays_role(rootLabel, typeLabel, roleLabel));
    }

    @Then("{root_label}\\( ?{type_label} ?) get playing roles contain:")
    public void thing_type_get_playing_roles_contain(RootLabel rootLabel, String typeLabel, List<Parameters.ScopedLabel> roleLabels) {
        Set<Parameters.ScopedLabel> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getPlays().map(
                r -> new Parameters.ScopedLabel(r.getScope(), r.getLabel())
        ).collect(toSet());
        assertTrue(actuals.containsAll(roleLabels));
    }

    @Then("{root_label}\\( ?{type_label} ?) get playing roles do not contain:")
    public void thing_type_get_playing_roles_do_not_contain(RootLabel rootLabel, String typeLabel, List<Parameters.ScopedLabel> roleLabels) {
        Set<Parameters.ScopedLabel> actuals = get_thing_type(rootLabel, typeLabel).asRemote(tx()).getPlays().map(
                r -> new Parameters.ScopedLabel(r.getScope(), r.getLabel())
        ).collect(toSet());
        for (Parameters.ScopedLabel roleLabel : roleLabels) {
            assertFalse(actuals.contains(roleLabel));
        }
    }
}
