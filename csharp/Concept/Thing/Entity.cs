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
using TypeDB.Driver.Concept;

namespace TypeDB.Driver.Concept
{
    public class Entity : Thing, IEntity
    {
        public Entity(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public override IEntityType Type
        {
            get { return new EntityType(Pinvoke.typedb_driver.entity_get_type(NativeObject)); }
        }

        public IEntity AsEntity()
        {
            return this;
        }
    }
}