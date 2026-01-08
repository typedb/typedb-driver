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

namespace TypeDB.Driver.Concept
{
    /// <summary>
    /// Represents a relation instance in TypeDB.
    /// In TypeDB 3.0, relation instances are read-only data returned from queries.
    /// </summary>
    public class Relation : Thing, IRelation
    {
        private IRelationType? _type;

        public Relation(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        /// <summary>
        /// Gets the type of this relation.
        /// </summary>
        public override IRelationType Type
        {
            get { return _type ?? (_type = new RelationType(Pinvoke.typedb_driver.relation_get_type(NativeObject))); }
        }

        /// <summary>
        /// The base class Type property returns IThingType.
        /// </summary>
        IThingType IThing.Type => Type;

        /// <summary>
        /// Returns this relation as IRelation.
        /// </summary>
        public IRelation AsRelation()
        {
            return this;
        }
    }
}
