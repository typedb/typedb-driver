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
using IValueApi = TypeDB.Driver.Api.IValue;

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
        public Datetime GetDatetime()
        {
            return TryGetDatetime()
                ?? throw new TypeDBDriverException(ConceptError.INVALID_VALUE_RETRIEVAL, "datetime");
        }

        /// <inheritdoc/>
        public DatetimeTZ GetDatetimeTZ()
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
            if (IsDatetime()) return GetDatetime().ToString();
            if (IsDatetimeTZ()) return GetDatetimeTZ().ToString();
            if (IsDuration()) return GetDuration().ToString();
            if (IsStruct()) return GetStruct().ToString() ?? "{}";

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(this, obj)) return true;
            if (obj is not Value other) return false;
            return GetValueType() == other.GetValueType() && Get().Equals(other.Get());
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = Get().GetHashCode();
            }

            return _hash;
        }

        public static IValueApi TryConvertToValue(object value)
        {
            if (value is bool b) return NewBoolean(b);
            if (value is long l) return NewInteger(l);
            if (value is int i) return NewInteger(i);
            if (value is double d) return NewDouble(d);
            if (value is float f) return NewDouble(f);
            if (value is decimal dec) return NewDecimal(dec);
            if (value is string s) return NewString(s);
            if (value is DateOnly date) return NewDate(date);
            if (value is Datetime dt) return NewDatetime(dt);
            if (value is DatetimeTZ dtz) return NewDatetimeTz(dtz);
            if (value is Duration dur) return NewDuration(dur);
            throw new TypeDBDriverException(ConceptError.UNSUPPORTED_VALUE_CONVERSION, value.GetType().Name);
        }

        public static IValueApi NewBoolean(bool value)
            => new Value(Pinvoke.typedb_driver.concept_new_boolean(value));

        public static IValueApi NewInteger(long value)
            => new Value(Pinvoke.typedb_driver.concept_new_integer(value));

        public static IValueApi NewDouble(double value)
            => new Value(Pinvoke.typedb_driver.concept_new_double(value));

        public static IValueApi NewDecimal(decimal value)
        {
            decimal scale = (decimal)Math.Pow(10, IConcept.DecimalScale);
            long integerPart = (long)Math.Floor(value);
            decimal fractional = value - integerPart;
            ulong fractionalPart = (ulong)(fractional * scale);
            return new Value(Pinvoke.typedb_driver.concept_new_decimal(integerPart, fractionalPart));
        }

        public static IValueApi NewString(string value)
            => new Value(Pinvoke.typedb_driver.concept_new_string(value));

        public static IValueApi NewDate(DateOnly value)
        {
            long epochSeconds = new DateTimeOffset(value.ToDateTime(TimeOnly.MinValue), TimeSpan.Zero).ToUnixTimeSeconds();
            return new Value(Pinvoke.typedb_driver.concept_new_date_from_seconds(epochSeconds));
        }

        public static IValueApi NewDatetime(Datetime value)
            => new Value(Pinvoke.typedb_driver.concept_new_datetime(value.Seconds, value.SubsecNanos));

        public static IValueApi NewDatetimeTz(DatetimeTZ value)
        {
            long seconds = value.DateTimeOffset.ToUnixTimeSeconds();
            uint nanos = value.SubsecNanos;
            if (value.IsFixedOffset)
            {
                int offsetSeconds = (int)value.DateTimeOffset.Offset.TotalSeconds;
                return new Value(Pinvoke.typedb_driver.concept_new_datetime_tz_offset(seconds, nanos, offsetSeconds));
            }
            else
            {
                return new Value(Pinvoke.typedb_driver.concept_new_datetime_tz_iana(seconds, nanos, value.ZoneName!));
            }
        }

        public static IValueApi NewDuration(Duration value)
            => new Value(Pinvoke.typedb_driver.concept_new_duration((uint)value.Months, (uint)value.Days, (ulong)value.Nanos));
    }
}
