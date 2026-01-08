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
    /// Base class for instance concepts (Entity, Relation, Attribute).
    /// In TypeDB 3.0, instances are read-only data returned from queries.
    /// </summary>
    public abstract class Thing : Concept, IThing
    {
        protected int _hash = 0;

        internal Thing(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        /// <summary>
        /// Creates the appropriate Thing subtype from a native concept.
        /// </summary>
        public static IThing ThingOf(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity(nativeConcept))
                return new Entity(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation(nativeConcept))
                return new Relation(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute(nativeConcept))
                return new Attribute(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        /// <summary>
        /// Gets the IID of this instance.
        /// </summary>
        public string IID
        {
            get { return TryGetIID() ?? throw new TypeDBDriverException(InternalError.NULL_NATIVE_VALUE); }
        }

        /// <summary>
        /// Gets the type of this instance.
        /// </summary>
        public abstract IThingType Type { get; }

        /// <summary>
        /// Returns this instance as IThing.
        /// </summary>
        public IThing AsThing()
        {
            return this;
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = IID.GetHashCode();
            }

            return _hash;
        }
    }
}
