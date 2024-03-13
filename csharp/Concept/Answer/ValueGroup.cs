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

using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Concept;
using TypeDB.Driver.Common.Validation;

using ConceptError = TypeDB.Driver.Common.Error.Concept;
using QueryError = TypeDB.Driver.Common.Error.Query;

using static TypeDB.Driver.Concept.Concept;

namespace TypeDB.Driver.Concept
{
    public class ValueGroup : NativeObjectWrapper<Pinvoke.ValueGroup>, IValueGroup
    {
        private int _hash = 0;

        public ValueGroup(Pinvoke.ValueGroup nativeValueGroup)
            : base(nativeValueGroup)
        {
        }

        public IConcept Owner
        {
            get { return ConceptOf(Pinvoke.typedb_driver.value_group_get_owner(NativeObject)); }
        }

        public IValue? Value
        {
            get
            {
                Pinvoke.Concept concept = Pinvoke.typedb_driver.value_group_get_value(NativeObject);
                if (concept == null)
                {
                    return null;
                }

                return new Value(concept);
            }
        }

        public override string ToString()
        {
            return Pinvoke.typedb_driver.value_group_to_string(NativeObject);
        }

        public override bool Equals(object? obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || this.GetType() != obj.GetType())
            {
                return false;
            }

            ValueGroup that = (ValueGroup)obj;

            return Pinvoke.typedb_driver.value_group_equals(this.NativeObject, that.NativeObject);
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = ComputeHash();
            }

            return _hash;
        }

        private int ComputeHash()
        {
            return (Owner, Value).GetHashCode();
        }
    }
}
