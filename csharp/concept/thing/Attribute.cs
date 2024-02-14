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

using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Value;
using Vaticle.Typedb.Driver.Concept.Type;

namespace Vaticle.Typedb.Driver.Concept.Thing
{
    public class Attribute : Thing, IAttribute
    {
        public Attribute(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public IAttributeType Type
        {
            get { return new AttributeType(Pinvoke.typedb_driver.attribute_get_type(NativeObject)); }
        }

        public IValue Value
        {
            get { return new Value(Pinvoke.typedb_driver.attribute_get_value(NativeObject)); }
        }

        public sealed ICollection<IThing> GetOwners(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_get_owners(
                        NativeTransaction(transaction), NativeObject, null))
                    .Select(obj => new Thing(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ICollection<IThing> GetOwners(ITypeDBTransaction transaction, IThingType ownerType)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_get_owners(
                        NativeTransaction(transaction),
                        NativeObject,
                        ((ThingType)ownerType).NativeObject))
                    .Select(obj => new Thing(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
