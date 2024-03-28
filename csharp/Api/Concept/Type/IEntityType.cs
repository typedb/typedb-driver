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
     * Entity types represent the classification of independent objects in the data model of the business domain.
     */
    public interface IEntityType : IThingType
    {
        /**
         * {@inheritDoc}
         */
        bool IConcept.IsEntityType()
        {
            return true;
        }
    
        /**
         * {@inheritDoc}
         */
        IEntityType IConcept.AsEntityType()
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
