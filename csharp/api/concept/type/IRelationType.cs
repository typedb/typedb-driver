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
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api.Concept.Type
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
        override bool IsRelationType()
        {
            return true;
        }
    
        /**
         * {@inheritDoc}
         */
        override IRelationType AsRelationType()
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
         * Retrieves all <code>Relation</code> objects that are instances of this <code>IRelationType</code> or its subtypes.
         * Equivalent to <code>GetInstances(transaction, Transitivity.TRANSITIVE)</code>
         *
         * @see IRelationType#GetInstances(ITypeDBTransaction, Transitivity)
         */
        override ICollection<IRelation> GetInstances(ITypeDBTransaction transaction);
    
    
        /**
         * Retrieves <code>Relation</code>s that are instances of this exact <code>IRelationType</code>, OR
         * this <code>IRelationType</code> and any of its subtypes.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetInstances(transaction, transitivity)
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect instances,
         *                     <code>Transitivity.EXPLICIT</code> for direct relates only
         */
        override ICollection<IRelation> GetInstances(ITypeDBTransaction transaction, Transitivity transitivity);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         *
         * @see IRelationType#getRelates(ITypeDBTransaction, Transitivity)
         */
        ICollection<IRoleType> GetRelates(ITypeDBTransaction transaction);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetRelates(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited relates, <code>Transitivity.EXPLICIT</code> for direct relates only
         */
        ICollection<IRoleType> GetRelates(ITypeDBTransaction transaction, Transitivity transitivity);
    
        /**
         * Retrieves roles that this <code>IRelationType</code> relates to directly or via inheritance.
         * If <code>role_label</code> is given, returns a corresponding <code>RoleType</code> or <code>null</code>.
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
         * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
         *
         * @see IRelationType#GetRelatesOverridden(ITypeDBTransaction, string)
         */
        Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, IRoleType roleType);
    
        /**
         * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
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
        Promise<void> SetRelates(ITypeDBTransaction transaction, string roleLabel);
    
        /**
         * Sets the new role that this <code>IRelationType</code> relates to.
         *
         * @see IRelationType#SetRelates(ITypeDBTransaction, string, string)
         */
        Promise<void> SetRelates(ITypeDBTransaction transaction, string roleLabel, IRoleType overriddenType);
    
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
        Promise<void> SetRelates(ITypeDBTransaction transaction, string roleLabel, string overriddenLabel);
    
        /**
         * Disallows this <code>IRelationType</code> from relating to the given role.
         *
         * @see IRelationType#UnsetRelates(ITypeDBTransaction, string)
         */
        Promise<void> UnsetRelates(ITypeDBTransaction transaction, RoleType roleType);
    
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
        Promise<void> UnsetRelates(ITypeDBTransaction transaction, string roleLabel);
    
        /**
         * Retrieves all direct and indirect subtypes of the <code>IRelationType</code>.
         * Equivalent to <code>GetSubtypes(transaction, Transitivity.TRANSITIVE)</code>
         * 
         * @see IRelationType#GetSubtypes(ITypeDBTransaction, Transitivity)
         */
        override ICollection<IRelationType> GetSubtypes(ITypeDBTransaction transaction);
    
        /**
         * Retrieves all direct and indirect (or direct only) subtypes of the <code>IRelationType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * relationType.GetSubtypes(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
         */
        override ICollection<IRelationType> GetSubtypes(ITypeDBTransaction transaction, Transitivity transitivity);
    
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
        Promise<void> SetSupertype(ITypeDBTransaction transaction, IRelationType superRelationType);
    }
}
