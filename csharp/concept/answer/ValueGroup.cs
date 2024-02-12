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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api.Answer;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept;
using Vaticle.Typedb.Driver.Util;

using ConceptError = Vaticle.Typedb.Driver.Common.Exception.Error.Concept;
using QueryError = Vaticle.Typedb.Driver.Common.Exception.Error.Query;

namespace Vaticle.Typedb.Driver.Concept.Answer
{
    public class ValueGroupImpl : NativeObjectWrapper<Pinvoke.ValueGroup>, IValueGroup
    {
        private int _hash = 0;

        public ValueGroupImpl(Pinvoke.ValueGroup nativeValueGroup)
            : base(nativeValueGroup)
        {
        }

        public override IConcept Owner
        {
            get { return new Concept(Pinvoke.typedb_driver.value_group_get_owner(NativeObject)); }
        }

        public override IValue? Value
        {
            get
            {
                Pinvoke.Concept concept = Pinvoke.typedb_driver.value_group_get_value(NativeObject);
                if (nativeValue == null)
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

        public override bool Equals(object obj)
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

            return Pinvoke.typedb_driver.value_group_equals(
                this.NativeObject, that.NativeObject);
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
