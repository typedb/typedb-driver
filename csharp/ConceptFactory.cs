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
using System.Linq;
using TypeDB.Driver.Api;

using TypeDB.Driver.Common;
using TypeDB.Driver.Connection;

namespace TypeDB.Driver
{
    /// <summary>
    /// Factory class to instantiate new <c>IValue</c> concepts and build <c>IGivenRows</c> for use as query inputs.
    /// </summary>
    public static class ConceptFactory
    {
        /// <summary>
        /// Constructs an <c>IGivenRows</c> instance for use as inputs to queries (index-based).
        /// </summary>
        /// <param name="givenVariables">The variables describing the columns of <paramref name="givenRows"/>.</param>
        /// <param name="givenRows">Input rows; each inner list is one row indexed by <paramref name="givenVariables"/>, <c>null</c> entries represent empty variables.</param>
        public static IGivenRows BuildGivenRowsFrom(List<string> givenVariables, List<List<IConcept?>> givenRows)
        {
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)givenVariables.Count);
            foreach (var variable in givenVariables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)givenRows.Count);
            foreach (var row in givenRows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                for (int i = 0; i < row.Count; i++)
                {
                    var concept = row[i];
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_empty(rowsBuilder, (uint)i);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_concept(rowsBuilder, (uint)i, ((Concept.Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRows(Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released()));
        }

        /// <summary>
        /// Constructs an <c>IGivenRows</c> instance from dict-based rows for use as inputs to queries.
        /// </summary>
        /// <param name="givenRows">Input rows; each row is a map from variable name to concept, <c>null</c> values represent empty variables.</param>
        public static IGivenRows BuildGivenRowsFrom(List<Dictionary<string, IConcept?>> givenRows)
        {
            var variables = givenRows.SelectMany(r => r.Keys).Distinct().ToList();
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)variables.Count);
            foreach (var variable in variables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)givenRows.Count);
            foreach (var row in givenRows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                foreach (var (variable, concept) in row)
                {
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_empty(rowsBuilder, variable);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_concept(rowsBuilder, variable, ((Concept.Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRows(Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released()));
        }

        /// <summary>
        /// Constructs an <c>IGivenRows</c> instance from rows containing raw .NET values or concepts.
        /// Values that are already <c>IConcept</c> instances are used directly. Other values are
        /// converted via <see cref="TryConvertToValue"/>. <c>null</c> entries represent empty variables.
        /// </summary>
        /// <param name="givenRows">Input rows; each row maps variable names to concepts or raw values.</param>
        public static IGivenRows BuildGivenRowsFromObjects(List<Dictionary<string, object?>> givenRows)
        {
            var converted = givenRows.Select(row =>
                row.ToDictionary(
                    kv => kv.Key,
                    kv => kv.Value == null ? null :
                          kv.Value is IConcept concept ? concept :
                          (IConcept?)TryConvertToValue(kv.Value)
                )
            ).ToList();
            return BuildGivenRowsFrom(converted);
        }

        /// <summary>
        /// Converts a raw .NET value to an <c>IValue</c> concept.
        /// Accepted types: <c>bool</c>, <c>long</c>, <c>int</c>, <c>double</c>, <c>float</c>,
        /// <c>decimal</c>, <c>string</c>, <c>DateOnly</c>, <c>Datetime</c>, <c>DatetimeTZ</c>, <c>Duration</c>.
        /// </summary>
        /// <param name="value">The value to convert.</param>
        /// <exception cref="TypeDBDriverException">if the type is not supported.</exception>
        public static IValue TryConvertToValue(object value)
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
            throw new TypeDBDriverException(Error.Concept.UNSUPPORTED_VALUE_CONVERSION, value.GetType().Name);
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>bool</c> value.</summary>
        public static IValue NewBoolean(bool value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_boolean(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>long</c> value.</summary>
        public static IValue NewInteger(long value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_integer(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>double</c> value.</summary>
        public static IValue NewDouble(double value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_double(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>decimal</c> value.</summary>
        public static IValue NewDecimal(decimal value)
        {
            decimal scale = (decimal)Math.Pow(10, IConcept.DecimalScale);
            long integerPart = (long)Math.Floor(value);
            decimal fractional = value - integerPart;
            ulong fractionalPart = (ulong)(fractional * scale);
            return new Concept.Value(Pinvoke.typedb_driver.concept_new_decimal(integerPart, fractionalPart));
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>string</c> value.</summary>
        public static IValue NewString(string value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_string(value));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>DateOnly</c> value.</summary>
        public static IValue NewDate(DateOnly value)
        {
            long epochSeconds = new DateTimeOffset(value.ToDateTime(TimeOnly.MinValue), TimeSpan.Zero).ToUnixTimeSeconds();
            return new Concept.Value(Pinvoke.typedb_driver.concept_new_date_from_seconds(epochSeconds));
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>Datetime</c> value.</summary>
        public static IValue NewDatetime(Datetime value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_datetime(value.Seconds, value.SubsecNanos));

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>DatetimeTZ</c> value.</summary>
        public static IValue NewDatetimeTz(DatetimeTZ value)
        {
            long seconds = value.DateTimeOffset.ToUnixTimeSeconds();
            uint nanos = value.SubsecNanos;
            if (value.IsFixedOffset)
            {
                int offsetSeconds = (int)value.DateTimeOffset.Offset.TotalSeconds;
                return new Concept.Value(Pinvoke.typedb_driver.concept_new_datetime_tz_offset(seconds, nanos, offsetSeconds));
            }
            else
            {
                return new Concept.Value(Pinvoke.typedb_driver.concept_new_datetime_tz_iana(seconds, nanos, value.ZoneName!));
            }
        }

        /// <summary>Creates a new <c>IValue</c> wrapping the specified <c>Duration</c> value.</summary>
        public static IValue NewDuration(Duration value)
            => new Concept.Value(Pinvoke.typedb_driver.concept_new_duration((uint)value.Months, (uint)value.Days, (ulong)value.Nanos));
    }
}
