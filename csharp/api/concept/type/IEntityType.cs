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
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api.Concept.Type
{
    /**
     * Entity types represent the classification of independent objects in the data model of the business domain.
     */
    public interface IEntityType : IThingType
    {
        /**
         * {@inheritDoc}
         */
        new bool IsEntityType()
        {
            return true;
        }
    
        /**
         * {@inheritDoc}
         */
        new IEntityType AsEntityType()
        {
            return this;
        }
    
        /**
         * Creates and returns a new instance of this <code>IEntityType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * entityType.Create(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<IEntity> Create(ITypeDBTransaction transaction);
    
        /**
         * Retrieves all <code>Entity</code> objects that are instances of this <code>IEntityType</code> or its subtypes.
         * Equivalent to <code>GetInstances(transaction, Transitivity.TRANSITIVE)</code>
         *
         * @see IEntityType#GetInstances(ITypeDBTransaction, IConcept.Transitivity)
         */
        new ICollection<IEntity> GetInstances(ITypeDBTransaction transaction);
    
        /**
         * Retrieves <code>Entity</code> objects that are instances of this exact <code>IEntityType</code> OR
         * this <code>IEntityType</code> and any of its subtypes.
         *
         * <h3>Examples</h3>
         * <pre>
         * entityType.GetInstances(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.EXPLICIT</code> for direct instances only,
         *                     <code>Transitivity.TRANSITIVE</code> to include subtypes
         */
        new ICollection<IEntity> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    
        /**
         * Retrieves all (direct and indirect) subtypes of the <code>IEntityType</code>.
         * Equivalent to <code>GetSubtypes(transaction, Transitivity.TRANSITIVE)</code>
         *
         * @see IEntityType#GetSubtypes(ITypeDBTransaction, Transitivity)
         */
        new ICollection<IEntityType> GetSubtypes(ITypeDBTransaction transaction);
    
        /**
         * Retrieves all direct and indirect (or direct only) subtypes of the <code>IEntityType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * entityType.GetSubtypes(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
         *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
         */
        new ICollection<IEntityType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);
    
        /**
         * Sets the supplied <code>IEntityType</code> as the supertype of the current <code>IEntityType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * entityType.SetSupertype(transaction, entityType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param superEntityType The <code>IEntityType</code> to set as the supertype of this <code>IEntityType</code>
         */
        VoidPromise SetSupertype(ITypeDBTransaction transaction, IEntityType superEntityType);
    }
}
