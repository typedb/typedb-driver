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
        public void relation_type_create_new_instance(string var, string typeLabel)
        {
            put(
                var,
                SingleTransaction.Concepts.getRelationType(typeLabel).Resolve().create(SingleTransaction).Resolve());
        }

        [Then(@"relation\\( ?{type_label} ?) create new instance; throws exception")]
        public void relation_type_create_new_instance_throws_exception(string typeLabel)
        {
            assertThrows(() =>
                SingleTransaction.Concepts.getRelationType(typeLabel).Resolve().create(SingleTransaction).Resolve());
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")]
        public void relation_type_create_new_instance_with_key(string var, string type, string keyType, int keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .getRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.setHas(SingleTransaction, key).Resolve();
            put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")]
        public void relation_type_create_new_instance_with_key(
            string var, string type, string keyType, string keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .getRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.setHas(SingleTransaction, key).Resolve();
            put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")]
        public void relation_type_create_new_instance_with_key(
            string var, string type, string keyType, LocalDateTime keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Relation relation = SingleTransaction
                .Concepts
                .getRelationType(type).Resolve()
                .create(SingleTransaction).Resolve();

            relation.setHas(SingleTransaction, key).Resolve();
            put(var, relation);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")]
        public void relation_type_get_instance_with_key(string var1, string type, string keyType, long keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var1, owner);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")]
        public void relation_type_get_instance_with_key(string var1, string type, string keyType, string keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var1, owner);
        }

        [When(@"{var} = relation\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")]
        public void relation_type_get_instance_with_key(
            string var1, string type, string keyType, LocalDateTime keyValue)
        {
            var owner = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var1, owner);
        }

        [Then(@"relation\\( ?{type_label} ?) get instances contain: {var}")]
        public void relation_type_get_instances_contain(string typeLabel, string var)
        {
            var instances = SingleTransaction
                .Concepts
                .getRelationType(typeLabel).Resolve()
                .getInstances(SingleTransaction);

            assertTrue(instances.anyMatch(i => i.equals(get(var))));
        }

        [Then(@"relation\\( ?{type_label} ?) get instances do not contain: {var}")]
        public void relation_type_get_instances_do_not_contain(string typeLabel, string var)
        {
            var instances = SingleTransaction
                .Concepts
                .getRelationType(typeLabel).Resolve()
                .getInstances(SingleTransaction);

            assertTrue(instances.noneMatch(i => i.equals(get(var))));
        }

        [Then(@"relation\\( ?{type_label} ?) get instances is empty")]
        public void relation_type_get_instances_is_empty(string typeLabel)
        {
            var instances = SingleTransaction
                .Concepts
                .getRelationType(typeLabel).Resolve()
                .getInstances(SingleTransaction);

            assertEquals(0, instances.Count);
        }

        [When(@"relation {var} add player for role\\( ?{type_label} ?): {var}")]
        public void relation_add_player_for_role(string var1, string roleTypeLabel, string var2)
        {
            var relates = get(var1)
                .asRelation()
                .getType()
                .getRelates(SingleTransaction, roleTypeLabel).Resolve();

            get(var1)
                .asRelation()
                .addPlayer(SingleTransaction, relates, get(var2)).Resolve();
        }

        [When(@"relation {var} add player for role\\( ?{type_label} ?): {var}; throws exception")]
        public void relation_add_player_for_role_throws_exception(string var1, string roleTypeLabel, string var2)
        {
            var relates = get(var1)
                .asRelation()
                .getType()
                .getRelates(SingleTransaction, roleTypeLabel).Resolve();

            assertThrows(() => get(var1)
                .asRelation()
                .addPlayer(SingleTransaction, relates, get(var2)).Resolve());
        }

        [When(@"relation {var} remove player for role\\( ?{type_label} ?): {var}")]
        public void relation_remove_player_for_role(string var1, string roleTypeLabel, string var2)
        {
            var relates = get(var1)
                .asRelation()
                .getType()
                .getRelates(SingleTransaction, roleTypeLabel).Resolve();

            get(var1)
                .asRelation()
                .removePlayer(SingleTransaction, relates, get(var2)).Resolve();
        }

        [Then(@"relation {var} get players contain:")]
        public void relation_get_players_contain(string var, Map<string, string> players)
        {
            Relation relation = get(var).asRelation();

            players.forEach((rt, var2) =>
                {
                    var players = relation
                        .getPlayers(SingleTransaction)
                        .get(relation.getType().getRelates(SingleTransaction, rt).Resolve();

                    assertTrue(players.contains(get(var2.substring(1))));
                });
        }

        [Then(@"relation {var} get players do not contain:")]
        public void relation_get_players_do_not_contain(string var, Map<string, string> players)
        {
            Relation relation = get(var).asRelation();

            players.forEach((rt, var2) =>
                {
                    var players = relation
                        .getPlayers(SingleTransaction)
                        .get(relation.getType().getRelates(SingleTransaction, rt).Resolve());

                    if (players != null)
                    {
                        assertFalse(p.contains(get(var2.substring(1))));
                    }
                });
        }

        [Then(@"relation {var} get players contain: {var}")]
        public void relation_get_players_contain(string var1, string var2)
        {
            var players = get(var1).asRelation().getPlayersByRoleType(SingleTransaction);
            assertTrue(players.anyMatch(p => p.equals(get(var2))));
        }

        [Then(@"relation {var} get players do not contain: {var}")]
        public void relation_get_players_do_not_contain(string var1, string var2)
        {
            var players = get(var1).asRelation().getPlayersByRoleType(SingleTransaction);
            assertTrue(players.noneMatch(p => p.equals(get(var2))));
        }

        [Then(@"relation {var} get players for role\\( ?{type_label} ?) contain: {var}")]
        public void relation_get_players_for_role_contain(string var1, string roleTypeLabel, string var2)
        {
            var relates = get(var1)
                .asRelation()
                .getType()
                .getRelates(SingleTransaction, roleTypeLabel).Resolve();

            var players = get(var1)
                .asRelation()
                .getPlayersByRoleType(SingleTransaction, relates);

            assertTrue(players.anyMatch(p => p.equals(get(var2))));
        }

        [Then(@"relation {var} get players for role\\( ?{type_label} ?) do not contain: {var}")]
        public void relation_get_players_for_role_do_not_contain(string var1, string roleTypeLabel, string var2)
        {
            var relates = get(var1)
                .asRelation()
                .getType()
                .getRelates(SingleTransaction, roleTypeLabel).Resolve();

            var players = get(var1)
                .asRelation()
                .getPlayersByRoleType(SingleTransaction, relates);

            assertTrue(players.noneMatch(p => p.equals(get(var2))));
        }
    }
}
