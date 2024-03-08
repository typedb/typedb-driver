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
        [When(@"{var} = relation\\( ?{type_label} ?) create new instance")]
        public void RelationTypeCreateNewInstance(string var, string typeLabel)
        {
            Put(
                var,
                SingleTransaction.Concepts.GetRelationType(typeLabel).Resolve().create(SingleTransaction).Resolve());
        }

        [Then(@"relation\\( ?{type_label} ?) create new instance; throws exception")]
        public void RelationTypeCreateNewInstanceThrowsException(string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                SingleTransaction.Concepts.GetRelationType(typeLabel).Resolve().create(SingleTransaction).Resolve());
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")]
        public void RelationTypeCreateNewInstanceWithKey(string var, string type, string keyType, int keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .GetRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.SetHas(SingleTransaction, key).Resolve();
            Put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")]
        public void RelationTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, string keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .GetRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.SetHas(SingleTransaction, key).Resolve();
            Put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")]
        public void RelationTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, DateTime keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .GetRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.SetHas(SingleTransaction, key).Resolve();
            Put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")]
        public void RelationTypeGetInstanceWithKey(string var1, string type, string keyType, long keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var1, owner);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")]
        public void RelationTypeGetInstanceWithKey(string var1, string type, string keyType, string keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var1, owner);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")]
        public void RelationTypeGetInstanceWithKey(
            string var1, string type, string keyType, DateTime keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var1, owner);
        }

        [Then(@"relation\\( ?{type_label} ?) get instances contain: {var}")]
        public void RelationTypeGetInstancesContain(string typeLabel, string var)
        {
            var instances = SingleTransaction
                .Concepts
                .GetRelationType(typeLabel).Resolve()
                .GetInstances(SingleTransaction);

            Assert.True(instances.anyMatch(i => i.equals(Get(var))));
        }

        [Then(@"relation\\( ?{type_label} ?) get instances do not contain: {var}")]
        public void RelationTypeGetInstancesDoNotContain(string typeLabel, string var)
        {
            var instances = SingleTransaction
                .Concepts
                .GetRelationType(typeLabel).Resolve()
                .GetInstances(SingleTransaction);

            Assert.True(instances.noneMatch(i => i.equals(Get(var))));
        }

        [Then(@"relation\\( ?{type_label} ?) get instances is empty")]
        public void RelationTypeGetInstancesIsEmpty(string typeLabel)
        {
            var instances = SingleTransaction
                .Concepts
                .GetRelationType(typeLabel).Resolve()
                .GetInstances(SingleTransaction);

            Assert.Equals(0, instances.Count);
        }

        [When(@"relation {var} add player for role\\( ?{type_label} ?): {var}")]
        public void RelationAddPlayerForRole(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)
                .asRelation()
                .GetType()
                .GetRelates(SingleTransaction, roleTypeLabel).Resolve();

            Get(var1)
                .asRelation()
                .addPlayer(SingleTransaction, relates, Get(var2)).Resolve();
        }

        [When(@"relation {var} add player for role\\( ?{type_label} ?): {var}; throws exception")]
        public void RelationAddPlayerForRoleThrowsException(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)
                .asRelation()
                .GetType()
                .GetRelates(SingleTransaction, roleTypeLabel).Resolve();

            Assert.Throws<TypeDBDriverException>(() => Get(var1)
                .asRelation()
                .addPlayer(SingleTransaction, relates, Get(var2)).Resolve());
        }

        [When(@"relation {var} remove player for role\\( ?{type_label} ?): {var}")]
        public void RelationRemovePlayerForRole(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)
                .asRelation()
                .GetType()
                .GetRelates(SingleTransaction, roleTypeLabel).Resolve();

            Get(var1)
                .asRelation()
                .removePlayer(SingleTransaction, relates, Get(var2)).Resolve();
        }

        [Then(@"relation {var} get players contain:")]
        public void RelationGetPlayersContain(string var, Map<string, string> players)
        {
            Relation relation = Get(var).asRelation();

            players.forEach((rt, var2) =>
                {
                    var players = relation
                        .GetPlayers(SingleTransaction)
                        .Get(relation.GetType().GetRelates(SingleTransaction, rt).Resolve();

                    Assert.True(players.contains(Get(var2.substring(1))));
                });
        }

        [Then(@"relation {var} get players do not contain:")]
        public void RelationGetPlayersDoNotContain(string var, Map<string, string> players)
        {
            Relation relation = Get(var).asRelation();

            players.forEach((rt, var2) =>
                {
                    var players = relation
                        .GetPlayers(SingleTransaction)
                        .Get(relation.GetType().GetRelates(SingleTransaction, rt).Resolve());

                    if (players != null)
                    {
                        Assert.False(p.contains(Get(var2.substring(1))));
                    }
                });
        }

        [Then(@"relation {var} get players contain: {var}")]
        public void RelationGetPlayersContain(string var1, string var2)
        {
            var players = Get(var1).asRelation().GetPlayersByRoleType(SingleTransaction);
            Assert.True(players.anyMatch(p => p.equals(Get(var2))));
        }

        [Then(@"relation {var} get players do not contain: {var}")]
        public void RelationGetPlayersDoNotContain(string var1, string var2)
        {
            var players = Get(var1).asRelation().GetPlayersByRoleType(SingleTransaction);
            Assert.True(players.noneMatch(p => p.equals(Get(var2))));
        }

        [Then(@"relation {var} get players for role\\( ?{type_label} ?) contain: {var}")]
        public void RelationGetPlayersForRoleContain(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)
                .asRelation()
                .GetType()
                .GetRelates(SingleTransaction, roleTypeLabel).Resolve();

            var players = Get(var1)
                .asRelation()
                .GetPlayersByRoleType(SingleTransaction, relates);

            Assert.True(players.anyMatch(p => p.equals(Get(var2))));
        }

        [Then(@"relation {var} get players for role\\( ?{type_label} ?) do not contain: {var}")]
        public void RelationGetPlayersForRoleDoNotContain(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)
                .asRelation()
                .GetType()
                .GetRelates(SingleTransaction, roleTypeLabel).Resolve();

            var players = Get(var1)
                .asRelation()
                .GetPlayersByRoleType(SingleTransaction, relates);

            Assert.True(players.noneMatch(p => p.equals(Get(var2))));
        }
    }
}
