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
    /// Represents an entity type in TypeDB.
    /// Entity types represent the classification of independent objects in the data model.
    /// In TypeDB 3.0, types are read-only data returned from queries.
    /// </summary>
    public interface IEntityType : IThingType
    {
        /// <inheritdoc/>
        bool IConcept.IsEntityType()
        {
            return true;
        }

        /// <inheritdoc/>
        IEntityType IConcept.AsEntityType()
        {
            return this;
        }
    }
}
