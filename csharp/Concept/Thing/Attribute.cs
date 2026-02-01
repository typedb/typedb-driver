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

using System;
using System.Collections.Generic;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    /// <summary>
    /// Represents an attribute instance in TypeDB.
    /// In TypeDB 3.0, attribute instances are read-only data returned from queries.
    /// </summary>
    public class Attribute : Thing, IAttribute
    {
        private IAttributeType? _type;

        public Attribute(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        /// <inheritdoc/>
        public override IAttributeType Type
        {
            get { return _type ?? (_type = new AttributeType(Pinvoke.typedb_driver.attribute_get_type(NativeObject))); }
        }

        /// <summary>
        /// The base class Type property returns IThingType.
        /// </summary>
        IThingType IThing.Type => Type;

        /// <inheritdoc/>
        public IValue Value
        {
            get { return TryGetValue() ?? throw new TypeDBDriverException(InternalError.NULL_NATIVE_VALUE); }
        }

        /// <inheritdoc/>
        public string GetValueType()
        {
            return Value.GetValueType();
        }

        /// <inheritdoc/>
        public bool GetBoolean()
        {
            return Value.GetBoolean();
        }

        /// <inheritdoc/>
        public long GetInteger()
        {
            return Value.GetInteger();
        }

        /// <inheritdoc/>
        public double GetDouble()
        {
            return Value.GetDouble();
        }

        /// <inheritdoc/>
        public decimal GetDecimal()
        {
            return Value.GetDecimal();
        }

        /// <inheritdoc/>
        public string GetString()
        {
            return Value.GetString();
        }

        /// <inheritdoc/>
        public DateOnly GetDate()
        {
            return Value.GetDate();
        }

        /// <inheritdoc/>
        public Datetime GetDatetime()
        {
            return Value.GetDatetime();
        }

        /// <inheritdoc/>
        public DatetimeTZ GetDatetimeTZ()
        {
            return Value.GetDatetimeTZ();
        }

        /// <inheritdoc/>
        public Duration GetDuration()
        {
            return Value.GetDuration();
        }

        /// <inheritdoc/>
        public IReadOnlyDictionary<string, IValue?> GetStruct()
        {
            return Value.GetStruct();
        }

        /// <summary>
        /// Returns this attribute as IAttribute.
        /// </summary>
        public IAttribute AsAttribute()
        {
            return this;
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = Value.GetHashCode();
            }

            return _hash;
        }
    }
}
