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
using System.Globalization;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using ConceptError = TypeDB.Driver.Common.Error.Concept;
using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    /// <summary>
    /// Represents a value concept in TypeDB.
    /// In TypeDB 3.0, values are read-only data returned from queries.
    /// </summary>
    public class Value : Concept, IValue
    {
        private int _hash = 0;

        public Value(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        /// <inheritdoc/>
        public string GetValueType()
        {
            return TryGetValueType()
                ?? throw new TypeDBDriverException(InternalError.NULL_NATIVE_VALUE);
        }

        /// <inheritdoc/>
        public object Get()
        {
            if (IsBoolean()) return GetBoolean();
            if (IsInteger()) return GetInteger();
            if (IsDouble()) return GetDouble();
            if (IsDecimal()) return GetDecimal();
            if (IsString()) return GetString();
            if (IsDate()) return GetDate();
            if (IsDatetime()) return GetDatetime();
            if (IsDatetimeTZ()) return GetDatetimeTZ();
            if (IsDuration()) return GetDuration();
            if (IsStruct()) return GetStruct();

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        /// <inheritdoc/>
        public bool GetBoolean()
        {
            return TryGetBoolean()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "boolean");
        }

        /// <inheritdoc/>
        public long GetInteger()
        {
            return TryGetInteger()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "integer");
        }

        /// <inheritdoc/>
        public double GetDouble()
        {
            return TryGetDouble()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "double");
        }

        /// <inheritdoc/>
        public decimal GetDecimal()
        {
            return TryGetDecimal()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "decimal");
        }

        /// <inheritdoc/>
        public string GetString()
        {
            return TryGetString()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "string");
        }

        /// <inheritdoc/>
        public DateOnly GetDate()
        {
            return TryGetDate()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "date");
        }

        /// <inheritdoc/>
        public DateTime GetDatetime()
        {
            return TryGetDatetime()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "datetime");
        }

        /// <inheritdoc/>
        public DateTimeOffset GetDatetimeTZ()
        {
            return TryGetDatetimeTZ()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "datetime-tz");
        }

        /// <inheritdoc/>
        public Duration GetDuration()
        {
            return TryGetDuration()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "duration");
        }

        /// <inheritdoc/>
        public IReadOnlyDictionary<string, IValue?> GetStruct()
        {
            return TryGetStruct()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "struct");
        }

        public override string ToString()
        {
            if (IsBoolean()) return GetBoolean().ToString().ToLower();
            if (IsInteger()) return GetInteger().ToString();
            if (IsDouble())
            {
                var d = GetDouble();
                // Format doubles to show at least one decimal place
                return d.ToString("G17", CultureInfo.InvariantCulture);
            }
            if (IsDecimal())
            {
                var dec = GetDecimal();
                // Format with 19 decimal places and remove trailing zeros
                var formatted = dec.ToString("0.0000000000000000000", CultureInfo.InvariantCulture).TrimEnd('0');
                if (formatted.EndsWith(".")) formatted += "0";
                return formatted + "dec";
            }
            if (IsString()) return GetString();
            if (IsDate()) return GetDate().ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
            if (IsDatetime() || IsDatetimeTZ() || IsDuration())
            {
                // Use the native concept_to_string and extract just the value part
                // The native format wraps values in "Value(<type>: <value>)"
                return ExtractNativeValueString();
            }
            if (IsStruct()) return GetStruct().ToString() ?? "{}";

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        private string ExtractNativeValueString()
        {
            var native = Pinvoke.typedb_driver.concept_to_string(NativeObject);
            // Native format: "Value(<type>: <value>)"
            // Example: "Value(datetime-tz: 2024-09-20T16:40:05.000000001 Europe/London)"
            var colonIndex = native.IndexOf(": ");
            if (native.StartsWith("Value(") && native.EndsWith(")") && colonIndex > 0)
            {
                var value = native.Substring(colonIndex + 2, native.Length - colonIndex - 3);
                // Post-process to match expected TypeDB format:
                // 1. Remove trailing .000000000 (no subsecond nanos)
                value = value.Replace(".000000000", "");
                // 2. Remove colon from fixed offset format (+00:00 -> +0000)
                value = NormalizeTimezoneOffset(value);
                return value;
            }
            return native;
        }

        private static string NormalizeTimezoneOffset(string value)
        {
            // Convert +HH:MM or -HH:MM to +HHMM or -HHMM for fixed offsets
            // But preserve IANA timezone names (e.g., "Europe/London")
            // Fixed offsets appear at the end of datetime-tz values
            if (value.Length >= 6)
            {
                // Check for pattern like +00:00 or -05:30 at the end
                var lastPart = value.Substring(value.Length - 6);
                if ((lastPart[0] == '+' || lastPart[0] == '-') && lastPart[3] == ':')
                {
                    // Remove the colon from the offset
                    return value.Substring(0, value.Length - 3) + value.Substring(value.Length - 2);
                }
            }
            return value;
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = Get().GetHashCode();
            }

            return _hash;
        }
    }
}
