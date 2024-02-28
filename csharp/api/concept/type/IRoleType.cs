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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api
{
    /**
     * Roles are special internal types used by relations. We can not create an instance of a role in a database. But we can set an instance of another type (role player) to play a role in a particular instance of a relation type.
     * Roles allow a schema to enforce logical constraints on types of role players.
     */
    public interface IRoleType : IType 
    {
        /**
         * {@inheritDoc}
         */
        new bool IsRoleType()
        {
            return true;
        }
    
        /**
         * {@inheritDoc}
         */
        new IRoleType AsRoleType()
        {
            return this;
        }

        /**
         * Retrieves the <code>RelationType</code> that this role is directly related to.
         *
         * <h3>Examples</h3>
         * <pre>
         * roleType.GetRelationType(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        new Promise<IRelationType> GetRelationType(ITypeDBTransaction transaction);
    
        /**
         * Retrieves <code>RelationType</code>s that this role is related to (directly or indirectly).
         *
         * <h3>Examples</h3>
         * <pre>
         * roleType.GetRelationTypes(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        IEnumerable<IRelationType> GetRelationTypes(ITypeDBTransaction transaction);
    
        /**
         * Retrieves the <code>ThingType</code>s whose instances play this role.
         * Equivalent to <code>GetPlayerTypes(transaction, Transitivity.TRANSITIVE)</code>.
         *
         * @see IRoleType#GetPlayerTypes(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IThingType> GetPlayerTypes(ITypeDBTransaction transaction);
    
        /**
         * Retrieves the <code>ThingType</code>s whose instances play this role.
         *
         * <h3>Examples</h3>
         * <pre>
         * roleType.GetPlayerTypes(transaction, transitivity)
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect playing,
         *                     <code>Transitivity.EXPLICIT</code> for direct playing only
         */
        IEnumerable<IThingType> GetPlayerTypes(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    
        /**
         * Retrieves the <code>Relation</code> instances that this role is related to.
         * Equivalent to <code>GetRelationInstances(transaction, Transitivity.TRANSITIVE)</code>
         *
         * @see IRoleType#GetRelationInstances(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IRelation> GetRelationInstances(ITypeDBTransaction transaction);
    
        /**
         * Retrieves the <code>Relation</code> instances that this role is related to.
         *
         * <h3>Examples</h3>
         * <pre>
         * roleType.GetRelationInstances(transaction, transitivity)
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect relation, <code>Transitivity.EXPLICIT</code> for direct relation only
         */
        IEnumerable<IRelation> GetRelationInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    
        /**
         * Retrieves the <code>Thing</code> instances that play this role.
         *
         * @see IRoleType#GetPlayerInstances(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IThing> GetPlayerInstances(ITypeDBTransaction transaction);
    
        /**
         * Retrieves the <code>Thing</code> instances that play this role.
         *
         * <h3>Examples</h3>
         * <pre>
         * roleType.GetPlayerInstances(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect playing,
         *                     <code>Transitivity.EXPLICIT</code> for direct playing only
         */
        IEnumerable<IThing> GetPlayerInstances(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    }
}
