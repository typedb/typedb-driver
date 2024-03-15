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
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) create new instance")]
        [Then(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) create new instance")]
        public void RelationTypeCreateNewInstance(string var, string typeLabel)
        {
            Put(
                var,
                Tx.Concepts.GetRelationType(typeLabel).Resolve()!.Create(Tx).Resolve()!);
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) create new instance; throws exception")]
        public void RelationTypeCreateNewInstanceThrowsException(string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                Tx.Concepts.GetRelationType(typeLabel).Resolve()!.Create(Tx).Resolve()!);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) create new instance with key\(([a-zA-Z0-9-_]+)\): {int}")]
        public void RelationTypeCreateNewInstanceWithKey(string var, string type, string keyType, int keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Put(Tx, keyValue).Resolve()!;

            IRelation relation = Tx.Concepts
                .GetRelationType(type).Resolve()!
                .Create(Tx).Resolve()!;

            relation.SetHas(Tx, key).Resolve();
            Put(var, relation);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) create new instance with key\(([a-zA-Z0-9-_]+)\): {word}")]
        public void RelationTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, string keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Put(Tx, keyValue).Resolve()!;

            IRelation relation = Tx.Concepts
                .GetRelationType(type).Resolve()!
                .Create(Tx).Resolve()!;

            relation.SetHas(Tx, key).Resolve();
            Put(var, relation);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) create new instance with key\(([a-zA-Z0-9-_]+)\): (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})")]
        public void RelationTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, DateTime keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Put(Tx, keyValue).Resolve()!;

            IRelation relation = Tx.Concepts
                .GetRelationType(type).Resolve()!
                .Create(Tx).Resolve()!;

            relation.SetHas(Tx, key).Resolve();
            Put(var, relation);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) get instance with key\(([a-zA-Z0-9-_]+)\): {int}")]
        public void RelationTypeGetInstanceWithKey(string var1, string type, string keyType, long keyValue)
        {
            var owner = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Get(Tx, keyValue).Resolve()!
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .FirstOrDefault();

            Put(var1, owner);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) get instance with key\(([a-zA-Z0-9-_]+)\): {word}")]
        public void RelationTypeGetInstanceWithKey(string var1, string type, string keyType, string keyValue)
        {
            var owner = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Get(Tx, keyValue).Resolve()!
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .FirstOrDefault();

            Put(var1, owner);
        }

        [When(@"\$([a-zA-Z0-9]+) = relation\(([a-zA-Z0-9-_]+)\) get instance with key\(([a-zA-Z0-9-_]+)\): (\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})")]
        public void RelationTypeGetInstanceWithKey(
            string var1, string type, string keyType, DateTime keyValue)
        {
            var owner = Tx.Concepts
                .GetAttributeType(keyType).Resolve()!
                .Get(Tx, keyValue).Resolve()!
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .FirstOrDefault();

            Put(var1, owner);
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get instances contain: \$([a-zA-Z0-9]+)")]
        public void RelationTypeGetInstancesContain(string typeLabel, string var)
        {
            var instances = Tx.Concepts
                .GetRelationType(typeLabel).Resolve()!
                .GetInstances(Tx);

            Assert.True(instances.Where(i => i.Equals(Get(var)!)).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get instances do not contain: \$([a-zA-Z0-9]+)")]
        public void RelationTypeGetInstancesDoNotContain(string typeLabel, string var)
        {
            var instances = Tx.Concepts
                .GetRelationType(typeLabel).Resolve()!
                .GetInstances(Tx);

            Assert.False(instances.Where(i => i.Equals(Get(var)!)).Any());
        }

        [Then(@"relation\(([a-zA-Z0-9-_]+)\) get instances is empty")]
        public void RelationTypeGetInstancesIsEmpty(string typeLabel)
        {
            var instances = Tx.Concepts
                .GetRelationType(typeLabel).Resolve()!
                .GetInstances(Tx);

            Assert.Equal(0, instances.Count());
        }

        [When(@"relation \$([a-zA-Z0-9]+) add player for role\(([a-zA-Z0-9-_]+)\): \$([a-zA-Z0-9]+)")]
        public void RelationAddPlayerForRole(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)!
                .AsRelation()
                .Type.AsRelationType()
                .GetRelates(Tx, roleTypeLabel).Resolve()!;

            Get(var1)!
                .AsRelation()
                .AddPlayer(Tx, relates, Get(var2)!).Resolve();
        }

        [When(@"relation \$([a-zA-Z0-9]+) add player for role\(([a-zA-Z0-9-_]+)\): \$([a-zA-Z0-9]+); throws exception")]
        public void RelationAddPlayerForRoleThrowsException(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)!
                .AsRelation()
                .Type.AsRelationType()
                .GetRelates(Tx, roleTypeLabel).Resolve()!;

            Assert.Throws<TypeDBDriverException>(() => Get(var1)!
                .AsRelation()
                .AddPlayer(Tx, relates, Get(var2)!).Resolve());
        }

        [When(@"relation \$([a-zA-Z0-9]+) remove player for role\(([a-zA-Z0-9-_]+)\): \$([a-zA-Z0-9]+)")]
        public void RelationRemovePlayerForRole(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)!
                .AsRelation()
                .Type.AsRelationType()
                .GetRelates(Tx, roleTypeLabel).Resolve()!;

            Get(var1)!
                .AsRelation()
                .RemovePlayer(Tx, relates, Get(var2)!).Resolve();
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players contain:")]
        public void RelationGetPlayersContain(string var, DataTable playersData)
        {
            Dictionary<string, string> players = Util.ParseDataTableToDictionary(playersData);
            IRelation relation = Get(var)!.AsRelation();

            foreach (var (rt, var2) in players)
            {
                var relationPlayers = relation
                    .GetPlayers(Tx)[relation.Type.AsRelationType().GetRelates(Tx, rt).Resolve()!];

                Assert.True(relationPlayers.Contains(Get(var2.Substring(1))!));
            }
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players do not contain:")]
        public void RelationGetPlayersDoNotContain(string var, DataTable playersData)
        {
            Dictionary<string, string> expectedPlayers = Util.ParseDataTableToDictionary(playersData);

            IRelation relation = Get(var)!.AsRelation();

            foreach (var (rt, var2) in expectedPlayers)
            {
                var players = relation.GetPlayers(Tx);
                var relates = relation.Type.AsRelationType().GetRelates(Tx, rt).Resolve()!;

                if (players.ContainsKey(relates))
                {
                    var relatesPlayers = players[relates];
                    Assert.False(relatesPlayers.Contains(Get(var2.Substring(1))!));
                }
            }
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players contain: \$([a-zA-Z0-9]+)")]
        public void RelationGetPlayersContain(string var1, string var2)
        {
            var players = Get(var1)!.AsRelation().GetPlayersByRoleType(Tx);
            Assert.True(players.Where(p => p.Equals(Get(var2)!)).Any());
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players do not contain: \$([a-zA-Z0-9]+)")]
        public void RelationGetPlayersDoNotContain(string var1, string var2)
        {
            var players = Get(var1)!.AsRelation().GetPlayersByRoleType(Tx);

            Assert.False(players.Where(p => p.Equals(Get(var2)!)).Any());
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players for role\(([a-zA-Z0-9-_]+)\) contain: \$([a-zA-Z0-9]+)")]
        public void RelationGetPlayersForRoleContain(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)!
                .AsRelation()
                .Type.AsRelationType()
                .GetRelates(Tx, roleTypeLabel).Resolve()!;

            var players = Get(var1)!
                .AsRelation()
                .GetPlayersByRoleType(Tx, relates);

            Assert.True(players.Where(p => p.Equals(Get(var2)!)).Any());
        }

        [Then(@"relation \$([a-zA-Z0-9]+) get players for role\(([a-zA-Z0-9-_]+)\) do not contain: \$([a-zA-Z0-9]+)")]
        public void RelationGetPlayersForRoleDoNotContain(string var1, string roleTypeLabel, string var2)
        {
            var relates = Get(var1)!
                .AsRelation()
                .Type.AsRelationType()
                .GetRelates(Tx, roleTypeLabel).Resolve()!;

            var players = Get(var1)!
                .AsRelation()
                .GetPlayersByRoleType(Tx, relates);

            Assert.False(players.Where(p => p.Equals(Get(var2)!)).Any());
        }
    }
}
