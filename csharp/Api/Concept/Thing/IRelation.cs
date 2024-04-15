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

using System.Collections.Generic;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /**
     * Relation is an instance of a relation type and can be uniquely addressed
     * by a combination of its type, owned attributes and role players.
     */
    public interface IRelation : IThing
    {
        /**
         * Checks if the concept is a <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.IsRelation();
         * </pre>
         */
        bool IConcept.IsRelation()
        {
            return true;
        }

        /**
         * Casts the concept to <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.AsRelation();
         * </pre>
         */
        IRelation IConcept.AsRelation()
        {
            return this;
        }

        /**
         * Adds a new role player to play the given role in this <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.AddPlayer(transaction, roleType, player).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleType The role to be played by the <code>player</code>
         * @param player The thing to play the role
         */
        VoidPromise AddPlayer(ITypeDBTransaction transaction, IRoleType roleType, IThing player);

        /**
         * Removes the association of the given instance that plays the given role in this <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.RemovePlayer(transaction, roleType, player).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleType The role to no longer be played by the thing in this <code>IRelation</code>
         * @param player The instance to no longer play the role in this <code>IRelation</code>
         */
        VoidPromise RemovePlayer(ITypeDBTransaction transaction, IRoleType roleType, IThing player);

        /**
         * Retrieves all role players of this <code>IRelation</code>, optionally filtered by given role types.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.GetPlayersByRoleType(transaction, roleTypes);
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleTypes 0 or more role types
         */
        IEnumerable<IThing> GetPlayersByRoleType(ITypeDBTransaction transaction, params IRoleType[] roleTypes);

        /**
         * Retrieves a mapping of all instances involved in the <code>IRelation</code> and the role each play.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.GetPlayers(transaction)
         * </pre>
         *
         * @param transaction The current transaction
         */
        Dictionary<IRoleType, ICollection<IThing>> GetPlayers(ITypeDBTransaction transaction);

        /**
         * Retrieves all role types currently played in this <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relation.GetRelating(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        IEnumerable<IRoleType> GetRelating(ITypeDBTransaction transaction);
    }
}
