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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /**
     * Relation types (or subtypes of the relation root type) represent relationships between types. Relation types have roles.
     * Other types can play roles in relations if it’s mentioned in their definition.
     * A relation type must specify at least one role.
     */
    public interface IRelationType : IThingType
    {
        /**
         * {@inheritDoc}
         */
        bool IConcept.IsRelationType()
        {
            return true;
        }
    
        /**
         * {@inheritDoc}
         */
        IRelationType IConcept.AsRelationType()
        {
            return this;
        }
    
        /**
         * Creates and returns an instance of this <code>IRelationType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.Create(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<IRelation> Create(ITypeDBTransaction transaction);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         *
         * @see IRelationType#getRelates(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IRoleType> GetRelates(ITypeDBTransaction transaction);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetRelates(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitive</code> for direct and inherited relates,
         *                     <code>Explicit</code> for direct relates only
         */
        IEnumerable<IRoleType> GetRelates(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         * If <code>role_label</code> is given, returns a corresponding <code>IRoleType</code> or <code>null</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetRelates(transaction, roleLabel).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleLabel Label of the role we wish to retrieve
         */
        Promise<IRoleType> GetRelates(ITypeDBTransaction transaction, string roleLabel);

        /**
         * Retrieves a <code>IRoleType</code> that is overridden by the role with the <code>role_label</code>.
         *
         * @see IRelationType#GetRelatesOverridden(ITypeDBTransaction, string)
         */
        Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, IRoleType roleType);
    
        /**
         * Retrieves a <code>IRoleType</code> that is overridden by the role with the <code>role_label</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetRelatesOverridden(transaction, roleLabel).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleLabel Label of the role that overrides an inherited role
         */
        Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, string roleLabel);
    
        /**
         * Sets the new role that this <code>IRelationType</code> relates to.
         *
         * @see IRelationType#SetRelates(ITypeDBTransaction, string, string)
         */
        VoidPromise SetRelates(ITypeDBTransaction transaction, string roleLabel);
    
        /**
         * Sets the new role that this <code>IRelationType</code> relates to.
         *
         * @see IRelationType#SetRelates(ITypeDBTransaction, string, string)
         */
        VoidPromise SetRelates(ITypeDBTransaction transaction, string roleLabel, IRoleType overriddenType);
    
        /**
         * Sets the new role that this <code>IRelationType</code> relates to.
         * If we are setting an overriding type this way, we have to also pass the overridden type as a second argument.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.SetRelates(transaction, roleLabel).Resolve();
         * relationType.SetRelates(transaction, roleLabel, overriddenLabel).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleLabel The new role for the <code>IRelationType</code> to relate to
         * @param overriddenLabel The label being overridden, if applicable
         */
        VoidPromise SetRelates(ITypeDBTransaction transaction, string roleLabel, string? overriddenLabel);
    
        /**
         * Disallows this <code>IRelationType</code> from relating to the given role.
         *
         * @see IRelationType#UnsetRelates(ITypeDBTransaction, string)
         */
        VoidPromise UnsetRelates(ITypeDBTransaction transaction, IRoleType roleType);
    
        /**
         * Disallows this <code>IRelationType</code> from relating to the given role.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.UnsetRelates(transaction, roleLabel).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleLabel The role to not relate to the relation type.
         */
        VoidPromise UnsetRelates(ITypeDBTransaction transaction, string roleLabel);

        /**
         * Sets the supplied <code>IRelationType</code> as the supertype of the current <code>IRelationType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.SetSupertype(transaction, superRelationType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param superRelationType The <code>IRelationType</code> to set as the supertype of this <code>IRelationType</code>
         */
        VoidPromise SetSupertype(ITypeDBTransaction transaction, IRelationType superRelationType);
    }
}
