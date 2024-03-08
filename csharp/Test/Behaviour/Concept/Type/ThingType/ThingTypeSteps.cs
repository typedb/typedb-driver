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

using DataTable = Gherkin.Ast.DataTable; // TODO Remove if not needed
using DocString = Gherkin.Ast.DocString; // TODO Remove if not needed
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private static readonly string ILLEGAL_ARGUMENT = "Illegal argument";

        // TODO: Implement RootLabel
        public static ThingType GetThingType(RootLabel rootLabel, string typeLabel)
        {
            switch (rootLabel)  // TODO: Type?
            {
                case ENTITY:
                    return SingleTransaction.Concepts.getEntityType(typeLabel).Resolve();

                case ATTRIBUTE:
                    return SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();

                case RELATION:
                    return SingleTransaction.Concepts.getRelationType(typeLabel).Resolve();

                default:
                    throw new BehaviourTestException(ILLEGAL_ARGUMENT);
            }
        }

        [When(@"put {} type: {}")]
        public void PutThingType(RootLabel rootLabel, string typeLabel) 
        { // TODO: Parse into rootLabel, typeLabel (string could be different!)
            switch (rootLabel) 
            {
                case ENTITY:
                    SingleTransaction.Concepts.PutEntityType(typeLabel).Resolve();
                    break;

                case RELATION:
                    SingleTransaction.Concepts.PutRelationType(typeLabel).Resolve();
                    break;

                default:
                    throw new IllegalArgumentException(ILLEGAL_ARGUMENT);
            }
        }

        [When(@"delete {root_label} type: {type_label}")]
        public void delete_thing_type(RootLabel rootLabel, string typeLabel)
        {
            GetThingType(rootLabel, typeLabel).delete(SingleTransaction).Resolve();
        }

        [Then(@"delete {root_label} type: {type_label}; throws exception")]
        public void delete_thing_type_throws_exception(RootLabel rootLabel, string typeLabel)
        {
            assertThrows(() => delete_thing_type(rootLabel, typeLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) is null: {bool}")]
        public void thing_type_is_null(RootLabel rootLabel, string typeLabel, boolean isNull)
        {
            assertEquals(isNull, isNull(GetThingType(rootLabel, typeLabel)));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set label: {type_label}")]
        public void thing_type_set_label(RootLabel rootLabel, string typeLabel, string newLabel)
        {
            GetThingType(rootLabel, typeLabel).setLabel(SingleTransaction, newLabel).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get label: {type_label}")]
        public void thing_type_get_label(RootLabel rootLabel, string typeLabel, string getLabel)
        {
            assertEquals(getLabel, GetThingType(rootLabel, typeLabel).getLabel().name());
        }

        [When(@"{root_label}\\( ?{type_label} ?) set abstract: {bool}")]
        public void thing_type_set_abstract(RootLabel rootLabel, string typeLabel, boolean isAbstract)
        {
            ThingType thingType = GetThingType(rootLabel, typeLabel);

            if (isAbstract)
            {
                thingType.setAbstract(SingleTransaction).Resolve();
            }
            else
            {
                thingType.unsetAbstract(SingleTransaction).Resolve();
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set abstract: {bool}; throws exception")]
        public void thing_type_set_abstract_throws_exception(
            RootLabel rootLabel, string typeLabel, boolean isAbstract)
        {
            assertThrows(() => thing_type_set_abstract(rootLabel, typeLabel, isAbstract));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) is abstract: {bool}")]
        public void thing_type_is_abstract(RootLabel rootLabel, string typeLabel, boolean isAbstract)
        {
            assertEquals(isAbstract, GetThingType(rootLabel, typeLabel).isAbstract());
        }

        [When(@"{root_label}\\( ?{type_label} ?) set supertype: {type_label}")]
        public void thing_type_set_supertype(RootLabel rootLabel, string typeLabel, string superLabel)
        {
            switch (rootLabel)
            {
                case ENTITY:
                    EntityType entitySuperType = SingleTransaction.Concepts.getEntityType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .getEntityType(typeLabel).Resolve()
                        .setSupertype(SingleTransaction, entitySuperType).Resolve();
                    break;

                case ATTRIBUTE:
                    AttributeType attributeSuperType = SingleTransaction.Concepts.getAttributeType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .getAttributeType(typeLabel).Resolve()
                        .setSupertype(SingleTransaction, attributeSuperType).Resolve();
                    break;

                case RELATION:
                    RelationType relationSuperType = SingleTransaction.Concepts.getRelationType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .getRelationType(typeLabel).Resolve()
                        .setSupertype(SingleTransaction, relationSuperType).Resolve();
                    break;

                case THING:
                    throw new IllegalArgumentException(ILLEGAL_ARGUMENT);
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set supertype: {type_label}; throws exception")]
        public void thing_type_set_supertype_throws_exception(
            RootLabel rootLabel, string typeLabel, string superLabel)
        {
            assertThrows(() => thing_type_set_supertype(rootLabel, typeLabel, superLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertype: {type_label}")]
        public void thing_type_get_supertype(RootLabel rootLabel, string typeLabel, string superLabel)
        {
            ThingType supertype = GetThingType(rootLabel, superLabel);
            assertEquals(
                supertype,
                GetThingType(rootLabel, typeLabel).getSupertype(SingleTransaction).Resolve());
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertypes contain:")]
        public void thing_type_get_supertypes_contain(
            RootLabel rootLabel, string typeLabel, List<string> superLabels)
        {
            ThingType thing_type = GetThingType(rootLabel, typeLabel);
            Set<string> actuals = thing_type
                .getSupertypes(SingleTransaction)
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(superLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertypes do not contain:")]
        public void thing_type_get_supertypes_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<string> superLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getSupertypes(SingleTransaction)
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string superLabel : superLabels)
            {
                assertFalse(actuals.contains(superLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get subtypes contain:")]
        public void thing_type_get_subtypes_contain(RootLabel rootLabel, string typeLabel, List<string> subLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getSubtypes(SingleTransaction)
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(subLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get subtypes do not contain:")]
        public void thing_type_get_subtypes_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<string> subLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getSubtypes(SingleTransaction)
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string subLabel : subLabels)
            {
                assertFalse(actuals.contains(subLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}, with annotations: {annotations}")]
        public void thing_type_set_owns_attribute_type_with_annotations(
            RootLabel rootLabel, string typeLabel, string attTypeLabel, List<Annotation> annotations)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attTypeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).setOwns(SingleTransaction, attributeType, set(annotations)).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}")]
        public void thing_type_set_owns_attribute_type_as_type_with_annotations(
            RootLabel rootLabel,
            string typeLabel,
            string attTypeLabel,
            string overriddenLabel,
            List<Annotation> annotations)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attTypeLabel).Resolve();
            AttributeType overriddenType = SingleTransaction.Concepts.getAttributeType(overriddenLabel).Resolve();

            GetThingType(rootLabel, typeLabel)
                .setOwns(SingleTransaction, attributeType, overriddenType, set(annotations)).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}, with annotations: {annotations}; throws exception")]
        public void thing_type_set_owns_attribute_type_with_annotations_throws_exception(
            RootLabel rootLabel, string typeLabel, string attributeLabel, List<Annotation> annotations)
        {
            assertThrows(() => thing_type_set_owns_attribute_type_with_annotations(
                rootLabel, typeLabel, attributeLabel, annotations));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}; throws exception")]
        public void thing_type_set_owns_attribute_type_as_type_with_annotations_throws_exception(
            RootLabel rootLabel,
            string typeLabel,
            string attributeLabel,
            string overriddenLabel,
            List<Annotation> annotations)
        {
            assertThrows(() => thing_type_set_owns_attribute_type_as_type_with_annotations(
                rootLabel, typeLabel, attributeLabel, overriddenLabel, annotations));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types, with annotations: {annotations}; contain:")]
        public void thing_type_get_owns_attribute_types_with_annotations_contain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, set(annotations))
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types, with annotations: {annotations}; do not contain:")]
        public void thing_type_get_owns_attribute_types_with_annotations_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, set(annotations))
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                assertFalse(actuals.contains(attributeLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types, with annotations: {annotations}; contain:")]
        public void thing_type_get_owns_explicit_attribute_types_with_annotations_contain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types, with annotations: {annotations}; do not contain:")]
        public void thing_type_get_owns_explicit_attribute_types_with_annotations_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                assertFalse(actuals.contains(attributeLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}")]
        public void thing_type_set_owns_attribute_type(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).setOwns(SingleTransaction, attributeType).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}; throws exception")]
        public void thing_type_set_owns_attribute_type_throws_exception(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            assertThrows(() => thing_type_set_owns_attribute_type(rootLabel, typeLabel, attributeLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}")]
        public void thing_type_set_owns_attribute_type_as(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attributeLabel).Resolve();
            AttributeType overriddenType = SingleTransaction.Concepts.getAttributeType(overriddenLabel).Resolve();
            GetThingType(rootLabel, typeLabel).setOwns(SingleTransaction, attributeType, overriddenType).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}; throws exception")]
        public void thing_type_set_owns_attribute_as_throws_exception(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            assertThrows(() =>
                thing_type_set_owns_attribute_type_as(rootLabel, typeLabel, attributeLabel, overriddenLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}")]
        public void thing_type_unset_owns_attribute_type(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).unsetOwns(SingleTransaction, attributeType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}; throws exception")]
        public void thing_type_unset_owns_attribute_type_throws_exception(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            assertThrows(() => thing_type_unset_owns_attribute_type(rootLabel, typeLabel, attributeLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns overridden attribute\\( ?{type_label} ?) is null: {bool}")]
        public void thing_type_get_owns_overridden_attribute_is_null(
            RootLabel rootLabel, string typeLabel, string attributeLabel, boolean isNull)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .getOwnsOverridden(SingleTransaction, attributeType).Resolve();

            assertEquals(isNull, isNull(ownsOverridden));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns overridden attribute\\( ?{type_label} ?) get label: {type_label}")]
        public void thing_type_get_owns_overridden_attribute_get_label(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string getLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .getOwnsOverridden(SingleTransaction, attributeType).Resolve();

            assertEquals(getLabel, ownsOverridden.Label.Name);
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types contain:")]
        public void thing_type_get_owns_attribute_types_contain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction)
                .map(at => at.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types do not contain:")]
        public void thing_type_get_owns_attribute_types_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction)
                .map(at => at.getLabel().name())
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                assertFalse(actuals.contains(attributeLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types contain:")]
        public void thing_type_get_owns_explicit_attribute_types_contain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, EXPLICIT)
                .map(at => at.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types do not contain:")]
        public void thing_type_get_owns_explicit_attribute_types_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .getOwns(SingleTransaction, EXPLICIT)
                .map(at => at.getLabel().name())
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                assertFalse(actuals.contains(attributeLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}")]
        public void thing_type_set_plays_role(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .getRelationType(roleLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, roleLabel.name()).Resolve();

            GetThingType(rootLabel, typeLabel).setPlays(SingleTransaction, roleType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}; throws exception")]
        public void thing_type_set_plays_role_throws_exception(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            assertThrows(() => thing_type_set_plays_role(rootLabel, typeLabel, roleLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}")]
        public void thing_type_set_plays_role_as(
            RootLabel rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .getRelationType(roleLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, roleLabel.name()).Resolve();

            RoleType overriddenType = SingleTransaction
                .Concepts
                .getRelationType(overriddenLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, overriddenLabel.name()).Resolve();

            GetThingType(rootLabel, typeLabel)
                .setPlays(SingleTransaction, roleType, overriddenType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}; throws exception")]
        public void thing_type_set_plays_role_as_throws_exception(
            RootLabel rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            assertThrows(() => thing_type_set_plays_role_as(rootLabel, typeLabel, roleLabel, overriddenLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}")]
        public void thing_type_unset_plays_role(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .getRelationType(roleLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, roleLabel.name()).Resolve();

            GetThingType(rootLabel, typeLabel).unsetPlays(SingleTransaction, roleType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}; throws exception")]
        public void thing_type_unset_plays_role_throws_exception(
            RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            assertThrows(() => thing_type_unset_plays_role(rootLabel, typeLabel, roleLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles contain:")]
        public void thing_type_get_playing_roles_contain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .getPlays(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());

            assertTrue(actuals.containsAll(roleLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles do not contain:")]
        public void thing_type_get_playing_roles_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .getPlays(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());

            for (Label roleLabel : roleLabels)
            {
                assertFalse(actuals.contains(roleLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles explicit contain:")]
        public void thing_type_get_playing_roles_explicit_contain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .getPlays(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .collect(toSet());

            assertTrue(actuals.containsAll(roleLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles explicit do not contain:")]
        public void thing_type_get_playing_roles_explicit_do_not_contain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .getPlays(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .collect(toSet());

            for (Label roleLabel : roleLabels)
            {
                assertFalse(actuals.contains(roleLabel));
            }
        }
    }
}
