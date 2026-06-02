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

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Concept
{
    /// <summary>
    /// Factory class to instantiate new <c>IValue</c> concepts from raw values.
    /// These can then be used in the <c>GivenRows</c> of a query.
    /// </summary>
    public static class ValueFactory
    {
        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>bool</c> value.</summary>
        public static IValue NewBoolean(bool value)
            => new Value(Pinvoke.typedb_driver.concept_new_boolean(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>long</c> value.</summary>
        public static IValue NewInteger(long value)
            => new Value(Pinvoke.typedb_driver.concept_new_integer(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>double</c> value.</summary>
        public static IValue NewDouble(double value)
            => new Value(Pinvoke.typedb_driver.concept_new_double(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>decimal</c> value.</summary>
        public static IValue NewDecimal(decimal value)
        {
            decimal scale = (decimal)Math.Pow(10, IConcept.DecimalScale);
            long integerPart = (long)Math.Floor(value);
            decimal fractional = value - integerPart;
            ulong fractionalPart = (ulong)(fractional * scale);
            return new Value(Pinvoke.typedb_driver.concept_new_decimal(integerPart, fractionalPart));
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>string</c> value.</summary>
        public static IValue NewString(string value)
            => new Value(Pinvoke.typedb_driver.concept_new_string(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>DateOnly</c> value.</summary>
        public static IValue NewDate(DateOnly value)
        {
            long epochSeconds = new DateTimeOffset(value.ToDateTime(TimeOnly.MinValue), TimeSpan.Zero).ToUnixTimeSeconds();
            return new Value(Pinvoke.typedb_driver.concept_new_date_from_seconds(epochSeconds));
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>Datetime</c> value.</summary>
        public static IValue NewDatetime(Datetime value)
            => new Value(Pinvoke.typedb_driver.concept_new_datetime(value.Seconds, value.SubsecNanos));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>DatetimeTZ</c> value.</summary>
        public static IValue NewDatetimeTz(DatetimeTZ value)
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

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>Duration</c> value.</summary>
        public static IValue NewDuration(Duration value)
            => new Value(Pinvoke.typedb_driver.concept_new_duration((uint)value.Months, (uint)value.Days, (ulong)value.Nanos));
    }
}
