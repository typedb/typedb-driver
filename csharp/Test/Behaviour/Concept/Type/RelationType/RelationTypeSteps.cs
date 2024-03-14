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

using DataTable = Gherkin.Ast.DataTable;
using System;
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IConcept.Transitivity;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [Given(@"relation\(([a-zA-Z0-9-_]+)\) set relates role: ([a-zA-Z0-9-_]+)")]
        [When(@"relation\(([a-zA-Z0-9-_]+)\) set relates role: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeSetRelatesRoleType(string relationLabel, string roleLabel)
        {
            Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .SetRelates(Tx, roleLabel).Resolve();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) set relates role: ([a-zA-Z0-9-_]+); throws exception")]
        public void RelationTypeSetRelatesRoleTypeThrowsException(string relationLabel, string roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => RelationTypeSetRelatesRoleType(relationLabel, roleLabel));
        }

        [When(@"relation\(([a-zA-Z0-9-_]+)\) unset related role: ([a-zA-Z0-9-_]+)")]
        [Then(@"relation\(([a-zA-Z0-9-_]+)\) unset related role: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeUnsetRelatedRoleType(string relationLabel, string roleLabel)
        {
            Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .UnsetRelates(Tx, roleLabel).Resolve();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) unset related role: ([a-zA-Z0-9-_]+); throws exception")]
        public void RelationTypeUnsetRelatedRoleTypeThrowsException(string relationLabel, string roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => RelationTypeUnsetRelatedRoleType(relationLabel, roleLabel));
        }

        [When(@"relation\(([a-zA-Z0-9-_]+)\) set relates role: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+)")]
        public void RelationTypeSetRelatesRoleTypeAs(string relationLabel, string roleLabel, string superRole)
        {
            Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .SetRelates(Tx, roleLabel, superRole).Resolve();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) set relates role: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+); throws exception")]
        public void RelationTypeSetRelatesRoleTypeAsThrowsException(
            string relationLabel, string roleLabel, string superRole)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                RelationTypeSetRelatesRoleTypeAs(relationLabel, roleLabel, superRole));
        }

        [When(@"relation\(([a-zA-Z0-9-_]+)\) remove related role: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeRemoveRelatedRole(string relationLabel, string roleLabel)
        {
            Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .Delete(Tx).Resolve();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) is null: (true|false)")]
        public void RelationTypeGetRoleTypeIsNull(string relationLabel, string roleLabel, bool isNull)
        {
            var roleType = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!;

            Assert.Equal(isNull, roleType == null);
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get overridden role\(([a-zA-Z0-9-_]+)\) is null: (true|false)")]
        public void RelationTypeGetOverriddenRoleTypeIsNull(
            string relationLabel, string roleLabel, bool isNull)
        {
            var overridenType = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelatesOverridden(Tx, roleLabel).Resolve()!;

            Assert.Equal(isNull, overridenType == null);
        }

        [When(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) set label: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeGetRoleTypeSetLabel(string relationLabel, string roleLabel, string newLabel)
        {
            Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .SetLabel(Tx, newLabel).Resolve();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get label: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeGetRoleTypeGetLabel(string relationLabel, string roleLabel, string getLabel)
        {
            var relates = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!;

            Assert.Equal(getLabel, relates.Label.Name);
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get overridden role\(([a-zA-Z0-9-_]+)\) get label: ([a-zA-Z0-9-_]+)")]
        public void RelationTypeGetOverriddenRoleTypeGetLabel(
            string relationLabel, string roleLabel, string getLabel)
        {
            var relates = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelatesOverridden(Tx, roleLabel).Resolve()!;

            Assert.Equal(getLabel, relates.Label.Name);
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) is abstract: (true|false)")]
        public void RelationTypeGetRoleTypeIsAbstract(string relationLabel, string roleLabel, bool isAbstract)
        {
            var isRelatesAbstract = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .IsAbstract();

            Assert.Equal(isAbstract, isRelatesAbstract);
        }

        private HashSet<Label> RelationTypeGetRelatedRoleTypes(string relationLabel)
        {
            return Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get related roles contain:")]
        public void RelationTypeGetRelatedRoleTypesContain(string relationLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRelatedRoleTypes(relationLabel);

            Assert.False(roleLabels.Except(actuals).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get related roles do not contain:")]
        public void RelationTypeGetRelatedRoleTypesDoNotContain(string relationLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRelatedRoleTypes(relationLabel);
            
            foreach (Label label in roleLabels)
            {
                Assert.False(actuals.Contains(label));
            }
        }

        private HashSet<Label> RelationTypeGetRelatedExplicitRoleTypes(string relationLabel)
        {
            return Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, EXPLICIT)
                .Select(obj => obj.Label)
                .ToHashSet();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get related explicit roles contain:")]
        public void RelationTypeGetRelatedExplicitRoleTypesContain(
            string relationLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRelatedExplicitRoleTypes(relationLabel);

            Assert.False(roleLabels.Except(actuals).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get related explicit roles do not contain:")]
        public void RelationTypeGetRelatedExplicitRoleTypesDoNotContain(
            string relationLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRelatedExplicitRoleTypes(relationLabel);

            foreach (Label label in roleLabels)
            {
                Assert.False(actuals.Contains(label));
            }
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get supertype: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void RelationTypeGetRoleTypeGetSupertype(string relationLabel, string roleLabel, string superLabelData)
        {
            Label superLabel = GetScopedLabel(superLabelData);

            IRoleType superLabelType = Tx.Concepts
                .GetRelationType(superLabel.Scope!).Resolve()!
                .GetRelates(Tx, superLabel.Name).Resolve()!;

            IRoleType labelSupertype = Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .GetSupertype(Tx).Resolve()!
                .AsRoleType();

            Assert.Equal(superLabelType, labelSupertype);
        }

        private HashSet<Label> RelationTypeGetRoleTypeGetSupertypes(string relationLabel, string roleLabel) 
        {
            return Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .GetSupertypes(Tx).Select(obj => obj.Label).ToHashSet();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get supertypes contain:")]
        public void RelationTypeGetRoleTypeGetSupertypesContain(
            string relationLabel, string roleLabel, DataTable superLabelsData)
        {
            List<Label> superLabels = Util.ParseDataTableToTypeList<Label>(superLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRoleTypeGetSupertypes(relationLabel, roleLabel);

            Assert.False(superLabels.Except(actuals).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get supertypes do not contain:")]
        public void RelationTypeGetRoleTypeGetSupertypesDoNotContain(
            string relationLabel, string roleLabel, DataTable superLabelsData)
        {
            List<Label> superLabels = Util.ParseDataTableToTypeList<Label>(superLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRoleTypeGetSupertypes(relationLabel, roleLabel);
            
            foreach (Label superLabel in superLabels)
            {
                Assert.False(actuals.Contains(superLabel));
            }
        }

        private HashSet<string> RelationTypeGetRoleTypeGetPlayers(string relationLabel, string roleLabel) 
        {
            return Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .GetPlayerTypes(Tx).Select(t => t.Label.Name)
                .ToHashSet();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get players contain:")]
        public void RelationTypeGetRoleTypeGetPlayersContain(
            string relationLabel, string roleLabel, DataTable playerLabelsData)
        {
            var playerLabels = Util.ParseDataTableToTypeList<string>(playerLabelsData, val => val.ToString());

            HashSet<string> actuals = RelationTypeGetRoleTypeGetPlayers(relationLabel, roleLabel);

            Assert.False(playerLabels.Except(actuals).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get players do not contain:")]
        public void RelationTypeGetRoleTypeGetPlayersDoNotContain(
            string relationLabel, string roleLabel, DataTable playerLabelsData)
        {
            var playerLabels = Util.ParseDataTableToTypeList<string>(playerLabelsData, val => val.ToString());

            HashSet<string> actuals = RelationTypeGetRoleTypeGetPlayers(relationLabel, roleLabel);

            foreach (string superLabel in playerLabels)
            {
                Assert.False(actuals.Contains(superLabel));
            }
        }

        private HashSet<Label> RelationTypeGetRoleTypeGetSubtypes(string relationLabel, string roleLabel) 
        {
            return Tx.Concepts
                .GetRelationType(relationLabel).Resolve()!
                .GetRelates(Tx, roleLabel).Resolve()!
                .GetSubtypes(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get subtypes contain:")]
        public void RelationTypeGetRoleTypeGetSubtypesContain(
            string relationLabel, string roleLabel, DataTable subLabelsData)
        {
            List<Label> subLabels = Util.ParseDataTableToTypeList<Label>(subLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRoleTypeGetSubtypes(relationLabel, roleLabel);

            Assert.False(subLabels.Except(actuals).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get role\(([a-zA-Z0-9-_]+)\) get subtypes do not contain:")]
        public void RelationTypeGetRoleTypeGetSubtypesDoNotContain(
            string relationLabel, string roleLabel, DataTable subLabelsData)
        {
            List<Label> subLabels = Util.ParseDataTableToTypeList<Label>(subLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = RelationTypeGetRoleTypeGetSubtypes(relationLabel, roleLabel);

            foreach (Label subLabel in subLabels)
            {
                Assert.False(actuals.Contains(subLabel));
            }
        }
    }
}
