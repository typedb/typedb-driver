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

using System.Collections.Generic;
using System.Linq;

using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Type;

namespace Vaticle.Typedb.Driver.Concept.Thing
{
    public class Relation : Thing, IRelation
    {
        public Relation(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public override IRelationType Type
        {
            get { return new RelationType(Pinvoke.typedb_driver.relation_get_type(NativeObject)); }
        }

        public VoidPromise AddPlayer(
            ITypeDBTransaction transaction, IRoleType roleType, IThing player)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_add_role_player(
                NativeTransaction(transaction),
                NativeObject,
                ((RoleType)roleType).NativeObject,
                ((Thing)player).NativeObject).Resolve);
        }

        public VoidPromise RemovePlayer(
            ITypeDBTransaction transaction, IRoleType roleType, IThing player)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_remove_role_player(
                NativeTransaction(transaction),
                NativeObject,
                ((RoleType)roleType).NativeObject,
                ((Thing)player).NativeObject).Resolve);
        }

        public ICollection<IThing> GetPlayersByRoleType(
            ITypeDBTransaction transaction, params IRoleType[] roleTypes)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_get_players_by_role_type(
                        NativeTransaction(transaction),
                        NativeObject,
                        roleTypes.Select(obj => ((RoleType)obj).NativeObject).toArray<Pinvoke.Concept>()))
                    .Select(obj => new Thing(obj));

            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Dictionary<IRoleType, ICollection<IThing>> GetPlayers(ITypeDBTransaction transaction)
        {
            var rolePlayers = new Dictionary<IRoleType, ICollection<IThing>>();

            try
            {
                new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_get_role_players(
                        NativeTransaction(transaction), NativeObject))
                    .ForEach(rolePlayer =>
                        {
                            RoleType role =
                                new RoleType(Pinvoke.typedb_driver.role_player_get_role_type(rolePlayer));
                            Thing player =
                                new Thing(Pinvoke.typedb_driver.role_player_get_player(rolePlayer));
                            if (rolePlayers.ContainsKey(role))
                            {
                                rolePlayers[role].Add(player);
                            }
                            else
                            {
                                rolePlayers.Put(role, new List<IThing>(){player});
                            }
                        });
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }

            return rolePlayers;
        }

        public ICollection<IRoleType> GetRelating(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_get_relating(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new RoleType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
