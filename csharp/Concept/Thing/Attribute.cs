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
using System.Linq;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Concept;
using static TypeDB.Driver.Concept.Thing;

namespace TypeDB.Driver.Concept
{
    public class Attribute : Thing, IAttribute
    {
        public Attribute(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public override IAttributeType Type
        {
            get { return new AttributeType(Pinvoke.typedb_driver.attribute_get_type(NativeObject)); }
        }

        public IValue Value
        {
            get { return new Value(Pinvoke.typedb_driver.attribute_get_value(NativeObject)); }
        }

        public IEnumerable<IThing> GetOwners(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_get_owners(
                        NativeTransaction(transaction), NativeObject, null))
                    .Select(obj => ThingOf(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IThing> GetOwners(ITypeDBTransaction transaction, IThingType ownerType)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_get_owners(
                        NativeTransaction(transaction),
                        NativeObject,
                        ((ThingType)ownerType).NativeObject))
                    .Select(obj => ThingOf(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
