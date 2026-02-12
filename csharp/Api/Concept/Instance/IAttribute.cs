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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Represents an attribute instance in TypeDB.
    /// Attribute instances represent properties with values that can be owned by other instances.
    /// In TypeDB 3.0, instances are read-only data returned from queries.
    /// </summary>
    public interface IAttribute : IInstance
    {
        /// <summary>
        /// The attribute type which this attribute instance belongs to.
        /// </summary>
        new IAttributeType Type { get; }

        /// <inheritdoc/>
        IType IInstance.Type => Type;

        /// <inheritdoc/>
        bool IConcept.IsAttribute()
        {
            return true;
        }

        /// <inheritdoc/>
        IAttribute IConcept.AsAttribute()
        {
            return this;
        }
    }
}
