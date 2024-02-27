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

using Vaticle.Typedb.Driver.Api;

namespace Vaticle.Typedb.Driver.Api
{
    /**
     * <p>Attribute is an instance of the attribute type and has a value.
     * This value is fixed and unique for every given instance of the attribute type.</p>
     * <p>Attributes can be uniquely addressed by their type and value.</p>
     */
    public interface IAttribute : IThing
    {
        /**
         * The type which this <code>IAttribute</code> belongs to.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.Type;
         * </pre>
         */
        new IAttributeType Type { get; }

        /**
         * Checks if the concept is an <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.IsAttribute();
         * </pre>
         */
        new bool IsAttribute() 
        {
            return true;
        }

        /**
         * Casts the concept to <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.AsAttribute();
         * </pre>
         */
        new IAttribute AsAttribute() 
        {
            return this;
        }

        /**
         * Retrieves the value which the <code>IAttribute</code> instance holds.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.Value;
         * </pre>
         */
        IValue Value { get; }

        /**
         * Retrieves the instances that own this <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.GetOwners(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        ICollection<IThing> GetOwners(ITypeDBTransaction transaction);

        /**
         * Retrieves the instances that own this <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attribute.GetOwners(transaction, ownerType);
         * </pre>
         *
         * @param transaction The current transaction
         * @param ownerType Filter results for only owners of the given type
         */
        ICollection<IThing> GetOwners(ITypeDBTransaction transaction, IThingType ownerType);
    }
}
