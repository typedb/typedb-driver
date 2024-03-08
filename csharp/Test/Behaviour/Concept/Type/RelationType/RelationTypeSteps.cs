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
        public void RelationTypeSetRelatesRoleType(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .SetRelates(SingleTransaction, roleLabel).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label}; throws exception")]
        public void RelationTypeSetRelatesRoleTypeThrowsException(string relationLabel, string roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => RelationTypeSetRelatesRoleType(relationLabel, roleLabel));
        }

        [When(@"relation\\( ?{type_label} ?) unset related role: {type_label}")]
        public void RelationTypeUnsetRelatedRoleType(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .unsetRelates(SingleTransaction, roleLabel).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) unset related role: {type_label}; throws exception")]
        public void RelationTypeUnsetRelatedRoleTypeThrowsException(string relationLabel, string roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => RelationTypeUnsetRelatedRoleType(relationLabel, roleLabel));
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label} as {type_label}")]
        public void RelationTypeSetRelatesRoleTypeAs(string relationLabel, string roleLabel, string superRole)
        {
            SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .SetRelates(SingleTransaction, roleLabel, superRole).Resolve();
        }

        [When(@"relation\\( ?{type_label} ?) set relates role: {type_label} as {type_label}; throws exception")]
        public void RelationTypeSetRelatesRoleTypeAsThrowsException(
            string relationLabel, string roleLabel, string superRole)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                RelationTypeSetRelatesRoleTypeAs(relationLabel, roleLabel, superRole));
        }

        [When(@"relation\\( ?{type_label} ?) remove related role: {type_label}")]
        public void RelationTypeRemoveRelatedRole(string relationLabel, string roleLabel)
        {
            SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .Delete(SingleTransaction).Resolve();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) is null: {bool}")]
        public void RelationTypeGetRoleTypeIsNull(string relationLabel, string roleLabel, bool isNull)
        {
            var roleType = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve();

            Assert.Equals(isNull, isNull(roleType));
        }

        [Then(@"relation\\( ?{type_label} ?) get overridden role\\( ?{type_label} ?) is null: {bool}")]
        public void RelationTypeGetOverriddenRoleTypeIsNull(
            string relationLabel, string roleLabel, bool isNull)
        {
            var overridenType = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelatesOverridden(SingleTransaction, roleLabel).Resolve();

            Assert.Equals(isNull, isNull(overridenType));
        }

        [When(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) set label: {type_label}")]
        public void RelationTypeGetRoleTypeSetLabel(string relationLabel, string roleLabel, string newLabel)
        {
            SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .SetLabel(SingleTransaction, newLabel).Resolve();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get label: {type_label}")]
        public void RelationTypeGetRoleTypeGetLabel(string relationLabel, string roleLabel, string getLabel)
        {
            var relatesLabelName = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .Label
                .Name;

            Assert.Equals(getLabel, relatesLabelName);
        }

        [Then(@"relation\\( ?{type_label} ?) get overridden role\\( ?{type_label} ?) get label: {type_label}")]
        public void RelationTypeGetOverriddenRoleTypeGetLabel(
            string relationLabel, string roleLabel, string getLabel)
        {
            var relatesLabelName = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelatesOverridden(SingleTransaction, roleLabel).Resolve()
                .Label
                .Name;

            Assert.Equals(getLabel, relatesLabelName);
        }

        [When(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) is abstract: {bool}")]
        public void RelationTypeGetRoleTypeIsAbstract(string relationLabel, string roleLabel, bool isAbstract)
        {
            var isRelatesAbstract = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .isAbstract();

            Assert.Equals(isAbstract, isRelatesAbstract);
        }

        private Set<Label> RelationTypeGetRelatedRoleTypes(string relationLabel)
        {
            return SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());
        }

        [Then(@"relation\\( ?{type_label} ?) get related roles contain:")]
        public void RelationTypeGetRelatedRoleTypesContain(string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = RelationTypeGetRelatedRoleTypes(relationLabel);
            Assert.True(actuals.containsAll(roleLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get related roles do not contain:")]
        public void RelationTypeGetRelatedRoleTypesDoNotContain(string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = RelationTypeGetRelatedRoleTypes(relationLabel);
            
            for (Label label : roleLabels) 
            {
                Assert.False(actuals.contains(label));
            }
        }

        private Set<Label> RelationTypeGetRelatedExplicitRoleTypes(string relationLabel)
        {
            return SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get related explicit roles contain:")]
        public void RelationTypeGetRelatedExplicitRoleTypesContain(
            string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = RelationTypeGetRelatedExplicitRoleTypes(relationLabel);
            Assert.True(actuals.containsAll(roleLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get related explicit roles do not contain:")]
        public void RelationTypeGetRelatedExplicitRoleTypesDoNotContain(
            string relationLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = RelationTypeGetRelatedExplicitRoleTypes(relationLabel);

            for (Label label : roleLabels)
            {
                Assert.False(actuals.contains(label));
            }
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertype: {scoped_label}")]
        public void RelationTypeGetRoleTypeGetSupertype(string relationLabel, string roleLabel, Label superLabel)
        {
            RoleType superLabelType = SingleTransaction
                .Concepts
                .GetRelationType(superLabel.scope().Get()).Resolve()
                .GetRelates(SingleTransaction, superLabel.Name).Resolve();

            RoleType labelSupertype = SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .GetSupertype(SingleTransaction).Resolve();

            Assert.Equals(superLabelType, labelSupertype);
        }

        private Set<Label> RelationTypeGetRoleTypeGetSupertypes(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .GetSupertypes(SingleTransaction).map(Type::getLabel).collect(toSet());
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertypes contain:")]
        public void RelationTypeGetRoleTypeGetSupertypesContain(
            string relationLabel, string roleLabel, List<Label> superLabels)
        {
            Set<Label> actuals = RelationTypeGetRoleTypeGetSupertypes(relationLabel, roleLabel);
            Assert.True(actuals.containsAll(superLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get supertypes do not contain:")]
        public void RelationTypeGetRoleTypeGetSupertypesDoNotContain(
            string relationLabel, string roleLabel, List<Label> superLabels)
        {
            Set<Label> actuals = RelationTypeGetRoleTypeGetSupertypes(relationLabel, roleLabel);
            
            for (Label superLabel : superLabels) 
            {
                Assert.False(actuals.contains(superLabel));
            }
        }

        private Set<string> RelationTypeGetRoleTypeGetPlayers(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .GetPlayerTypes(SingleTransaction).map(t => t.Label.Name)
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get players contain:")]
        public void RelationTypeGetRoleTypeGetPlayersContain(
            string relationLabel, string roleLabel, List<string> playerLabels)
        {
            Set<string> actuals = RelationTypeGetRoleTypeGetPlayers(relationLabel, roleLabel);
            Assert.True(actuals.containsAll(playerLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get players do not contain:")]
        public void RelationTypeGetRoleTypeGetPlayersDoNotContain(
            string relationLabel, string roleLabel, List<string> playerLabels)
        {
            Set<string> actuals = RelationTypeGetRoleTypeGetPlayers(relationLabel, roleLabel);

            for (string superLabel : playerLabels)
            {
                Assert.False(actuals.contains(superLabel));
            }
        }

        private Set<Label> RelationTypeGetRoleTypeGetSubtypes(string relationLabel, string roleLabel) 
        {
            return SingleTransaction
                .Concepts
                .GetRelationType(relationLabel).Resolve()
                .GetRelates(SingleTransaction, roleLabel).Resolve()
                .GetSubtypes(SingleTransaction)
                .Select(obj => obj.Label)
                .ToHashSet();
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get subtypes contain:")]
        public void RelationTypeGetRoleTypeGetSubtypesContain(
            string relationLabel, string roleLabel, List<Label> subLabels)
        {
            Set<Label> actuals = RelationTypeGetRoleTypeGetSubtypes(relationLabel, roleLabel);
            Assert.True(actuals.containsAll(subLabels));
        }

        [Then(@"relation\\( ?{type_label} ?) get role\\( ?{type_label} ?) get subtypes do not contain:")]
        public void RelationTypeGetRoleTypeGetSubtypesDoNotContain(
            string relationLabel, string roleLabel, List<Label> subLabels)
        {
            Set<Label> actuals = RelationTypeGetRoleTypeGetSubtypes(relationLabel, roleLabel);

            for (Label subLabel : subLabels)
            {
                Assert.False(actuals.contains(subLabel));
            }
        }
    }
}
