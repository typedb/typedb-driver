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

using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Attribute is an instance of the attribute type and has a value.
    /// This value is fixed and unique for every given instance of the attribute type.
    /// Attributes can be uniquely addressed by their type and value.
    /// In TypeDB 3.0, instances are read-only data returned from queries.
    /// </summary>
    public interface IAttribute : IThing
    {
        /// <summary>
        /// The type which this attribute belongs to.
        /// </summary>
        new IAttributeType Type { get; }

        /// <summary>
        /// Retrieves the value which the attribute instance holds.
        /// </summary>
        IValue Value { get; }

        /// <summary>
        /// Retrieves the description of the value type of the value which the attribute instance holds.
        /// </summary>
        string GetValueType();

        /// <summary>
        /// Returns a boolean value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        bool GetBoolean();

        /// <summary>
        /// Returns an integer (long) value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        long GetInteger();

        /// <summary>
        /// Returns a double value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        double GetDouble();

        /// <summary>
        /// Returns a decimal value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        decimal GetDecimal();

        /// <summary>
        /// Returns a string value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        string GetString();

        /// <summary>
        /// Returns a date value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        DateOnly GetDate();

        /// <summary>
        /// Returns a datetime value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        DateTime GetDatetime();

        /// <summary>
        /// Returns a datetime with timezone value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        DatetimeTZ GetDatetimeTZ();

        /// <summary>
        /// Returns a duration value of the value concept that this attribute holds.
        /// If the value has another type, raises an exception.
        /// </summary>
        Duration GetDuration();

        /// <summary>
        /// Returns a struct value of the value concept that this attribute holds
        /// represented as a dictionary from field names to values.
        /// If the value has another type, raises an exception.
        /// </summary>
        IReadOnlyDictionary<string, IValue?> GetStruct();

        /// <inheritdoc/>
        bool IConcept.IsAttribute()
        {
            return true;
        }

        /// <inheritdoc/>
        IAttribute IConcept.AsAttribute()
        {
            return this;
        }
    }
}
