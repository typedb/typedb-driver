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
        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label}")]
        public void relation_type_set_relates_role_type(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .setRelates(SingleTransaction, roleLabel).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label}; throws exception")]
        public void relation_type_set_relates_role_type_throws_exception(string relationLabel, string roleLabel)
        {
            assertThrows(() => relation_type_set_relates_role_type(relationLabel, roleLabel));
        }

        [When(@"relation\\( ?{type_label} ?) unset related role: {type_label}")]
        public void relation_type_unset_related_role_type(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .unsetRelates(SingleTransaction, roleLabel).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) unset related role: {type_label}; throws exception")]
        public void relation_type_unset_related_role_type_throws_exception(string relationLabel, string roleLabel)
        {
            assertThrows(() => relation_type_unset_related_role_type(relationLabel, roleLabel));
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label} as {type_label}")]
        public void relation_type_set_relates_role_type_as(string relationLabel, string roleLabel, string superRole)
        {
            SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .setRelates(SingleTransaction, roleLabel, superRole).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label} as {type_label}; throws exception")]
        public void relation_type_set_relates_role_type_as_throws_exception(
            string relationLabel, string roleLabel, string superRole)
        {
            assertThrows(() =>
                relation_type_set_relates_role_type_as(relationLabel, roleLabel, superRole));
        }

        [When(@"relation\\( ?{type_label} ?) remove related role: {type_label}")]
        public void relation_type_remove_related_role(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .Delete(SingleTransaction).Resolve();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) is null: {bool}")]
        public void relation_type_get_role_type_is_null(string relationLabel, string roleLabel, boolean isNull)
        {
            var roleType = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve();

            assertEquals(isNull, isNull(roleType));
        }

        [Then(@"relation\\( ?{type_label} ?) get overridden role\\( ?{type_label} ?) is null: {bool}")]
        public void relation_type_get_overridden_role_type_is_null(
            string relationLabel, string roleLabel, boolean isNull)
        {
            var overridenType = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelatesOverridden(SingleTransaction, roleLabel).Resolve();

            assertEquals(isNull, isNull(overridenType));
        }

        [When(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) set label: {type_label}")]
        public void relation_type_get_role_type_set_label(string relationLabel, string roleLabel, string newLabel)
        {
            SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .setLabel(SingleTransaction, newLabel).Resolve();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get label: {type_label}")]
        public void relation_type_get_role_type_get_label(string relationLabel, string roleLabel, string getLabel)
        {
            var relatesLabelName = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .Label
                .Name;

            assertEquals(getLabel, relatesLabelName);
        }

        [Then(@"relation\\( ?{type_label} ?) get overridden role\\( ?{type_label} ?) get label: {type_label}")]
        public void relation_type_get_overridden_role_type_get_label(
            string relationLabel, string roleLabel, string getLabel)
        {
            var relatesLabelName = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelatesOverridden(SingleTransaction, roleLabel).Resolve()
                .Label
                .Name;

            assertEquals(getLabel, relatesLabelName);
        }

        [When(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) is abstract: {bool}")]
        public void relation_type_get_role_type_is_abstract(string relationLabel, string roleLabel, boolean isAbstract)
        {
            var isRelatesAbstract = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .isAbstract();

            assertEquals(isAbstract, isRelatesAbstract);
        }

        private Set<Label> relation_type_get_related_role_types(string relationLabel)
        {
            return SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());
        }

        [Then(@"relation\\( ?{type_label} ?) get related roles contain:")]
        public void relation_type_get_related_role_types_contain(string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = relation_type_get_related_role_types(relationLabel);
            assertTrue(actuals.containsAll(roleLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get related roles do not contain:")]
        public void relation_type_get_related_role_types_do_not_contain(string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = relation_type_get_related_role_types(relationLabel);
            
            for (Label label : roleLabels) 
            {
                assertFalse(actuals.contains(label));
            }
        }

        private Set<Label> relation_type_get_related_explicit_role_types(string relationLabel)
        {
            return SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get related explicit roles contain:")]
        public void relation_type_get_related_explicit_role_types_contain(
            string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = relation_type_get_related_explicit_role_types(relationLabel);
            assertTrue(actuals.containsAll(roleLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get related explicit roles do not contain:")]
        public void relation_type_get_related_explicit_role_types_do_not_contain(
            string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = relation_type_get_related_explicit_role_types(relationLabel);

            for (Label label : roleLabels)
            {
                assertFalse(actuals.contains(label));
            }
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertype: {scoped_label}")]
        public void relation_type_get_role_type_get_supertype(string relationLabel, string roleLabel, Label superLabel)
        {
            RoleType superLabelType = SingleTransaction
                .Concepts
                .getRelationType(superLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, superLabel.name()).Resolve();

            RoleType labelSupertype = SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .getSupertype(SingleTransaction).Resolve();

            assertEquals(superLabelType, labelSupertype);
        }

        private Set<Label> relation_type_get_role_type_get_supertypes(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .getSupertypes(SingleTransaction).map(Type::getLabel).collect(toSet());
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertypes contain:")]
        public void relation_type_get_role_type_get_supertypes_contain(
            string relationLabel, string roleLabel, List<Label> superLabels)
        {
            Set<Label> actuals = relation_type_get_role_type_get_supertypes(relationLabel, roleLabel);
            assertTrue(actuals.containsAll(superLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertypes do not contain:")]
        public void relation_type_get_role_type_get_supertypes_do_not_contain(
            string relationLabel, string roleLabel, List<Label> superLabels)
        {
            Set<Label> actuals = relation_type_get_role_type_get_supertypes(relationLabel, roleLabel);
            
            for (Label superLabel : superLabels) 
            {
                assertFalse(actuals.contains(superLabel));
            }
        }

        private Set<string> relation_type_get_role_type_get_players(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .getPlayerTypes(SingleTransaction).map(t => t.Label.name())
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get players contain:")]
        public void relation_type_get_role_type_get_players_contain(
            string relationLabel, string roleLabel, List<string> playerLabels)
        {
            Set<string> actuals = relation_type_get_role_type_get_players(relationLabel, roleLabel);
            assertTrue(actuals.containsAll(playerLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get players do not contain:")]
        public void relation_type_get_role_type_get_players_do_not_contain(
            string relationLabel, string roleLabel, List<string> playerLabels)
        {
            Set<string> actuals = relation_type_get_role_type_get_players(relationLabel, roleLabel);

            for (string superLabel : playerLabels)
            {
                assertFalse(actuals.contains(superLabel));
            }
        }

        private Set<Label> relation_type_get_role_type_get_subtypes(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .getRelationType(relationLabel).Resolve()
                .getRelates(SingleTransaction, roleLabel).Resolve()
                .getSubtypes(SingleTransaction)
                .Select(obj => obj.Label)
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get subtypes contain:")]
        public void relation_type_get_role_type_get_subtypes_contain(
            string relationLabel, string roleLabel, List<Label> subLabels)
        {
            Set<Label> actuals = relation_type_get_role_type_get_subtypes(relationLabel, roleLabel);
            assertTrue(actuals.containsAll(subLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get subtypes do not contain:")]
        public void relation_type_get_role_type_get_subtypes_do_not_contain(
            string relationLabel, string roleLabel, List<Label> subLabels)
        {
            Set<Label> actuals = relation_type_get_role_type_get_subtypes(relationLabel, roleLabel);

            for (Label subLabel : subLabels)
            {
                assertFalse(actuals.contains(subLabel));
            }
        }
    }
}
