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

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    /// <summary>
    /// Base class for thing type concepts (EntityType, RelationType, AttributeType).
    /// In TypeDB 3.0, types are read-only data returned from queries.
    /// </summary>
    public abstract class ThingType : Type, IThingType
    {
        internal ThingType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        /// <summary>
        /// Creates the appropriate ThingType subtype from a native concept.
        /// </summary>
        public static IThingType ThingTypeOf(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity_type(nativeConcept))
                return new EntityType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation_type(nativeConcept))
                return new RelationType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute_type(nativeConcept))
                return new AttributeType(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        /// <summary>
        /// Returns this type as IThingType.
        /// </summary>
        public IThingType AsThingType()
        {
            return this;
        }
    }
}
