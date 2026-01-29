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
using System.Globalization;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Behaviour
{
    /// <summary>
    /// Shared test helper for parsing expected value strings from Gherkin steps
    /// and comparing them against actual IValue instances.
    /// Consolidates logic previously duplicated across ConceptSteps and QuerySteps.
    /// </summary>
    internal static class TestValueHelper
    {
        /// <summary>
        /// Infers the canonical value type name from a type string returned by GetValueType().
        /// Normalizes variations like "long" → "integer", "datetime_tz" → "datetime-tz".
        /// </summary>
        public static string InferValueType(string typeStr)
        {
            var lower = typeStr.ToLower().Replace("_", "-").Replace(" ", "-");
            switch (lower)
            {
                case "boolean": return "boolean";
                case "integer":
                case "long": return "integer";
                case "double": return "double";
                case "decimal": return "decimal";
                case "string": return "string";
                case "date": return "date";
                case "datetime": return "datetime";
                case "datetime-tz":
                case "datetimetz": return "datetime-tz";
                case "duration": return "duration";
                default: return "struct";
            }
        }

        /// <summary>
        /// Parses an expected value string (from a Gherkin step) into the appropriate
        /// native C# type for comparison.
        /// </summary>
        public static object ParseExpectedValue(string rawValue, string valueType)
        {
            switch (valueType.ToLower().Trim())
            {
                case "boolean":
                    return bool.Parse(rawValue);
                case "integer":
                    return long.Parse(rawValue);
                case "double":
                    return double.Parse(rawValue, CultureInfo.InvariantCulture);
                case "decimal":
                    return ParseDecimal(rawValue);
                case "string":
                    var str = rawValue;
                    if (str.StartsWith("\"") && str.EndsWith("\""))
                        str = str.Substring(1, str.Length - 2);
                    str = str.Replace("\\\"", "\"");
                    return str;
                case "date":
                    return DateOnly.Parse(rawValue, CultureInfo.InvariantCulture);
                case "datetime":
                    return ParseDatetime(rawValue);
                case "datetime-tz":
                    return DatetimeTZ.Parse(rawValue);
                case "duration":
                    return Duration.Parse(rawValue);
                case "struct":
                    return rawValue; // Compare as string representation
                default:
                    throw new BehaviourTestException($"Unknown value type for parsing: {valueType}");
            }
        }

        /// <summary>
        /// Parses a decimal string, stripping the "dec" suffix if present
        /// (TypeDB uses "123.456dec" format in Gherkin steps).
        /// </summary>
        public static decimal ParseDecimal(string s)
        {
            if (s.EndsWith("dec", StringComparison.OrdinalIgnoreCase))
                s = s.Substring(0, s.Length - 3);
            return decimal.Parse(s, CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Parses a datetime string, handling high-precision fractional seconds.
        /// C# DateTime has tick precision (100ns, 7 fractional digits).
        /// TypeDB supports nanosecond precision (9 digits).
        /// Truncates to 7 digits if needed for C# parsing.
        /// </summary>
        public static DateTime ParseDatetime(string value)
        {
            var dotIdx = value.IndexOf('.');
            if (dotIdx >= 0)
            {
                var fractionalEnd = value.Length;
                for (int i = dotIdx + 1; i < value.Length; i++)
                {
                    if (!char.IsDigit(value[i]))
                    {
                        fractionalEnd = i;
                        break;
                    }
                }
                var fractionalLen = fractionalEnd - dotIdx - 1;
                if (fractionalLen > 7)
                {
                    value = value.Substring(0, dotIdx + 8) + value.Substring(fractionalEnd);
                }
            }
            return DateTime.Parse(value, CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Compares two doubles with a combined relative/absolute tolerance.
        /// Uses relative tolerance of 1e-10 with an absolute floor of 1e-25
        /// for very small numbers.
        /// </summary>
        public static bool CompareDoubles(double a, double b)
        {
            return Math.Abs(a - b) < Math.Max(Math.Abs(a) * 1e-10, 1e-25);
        }

        /// <summary>
        /// Compares an IValue against an expected string using the appropriate type.
        /// For doubles, uses approximate comparison.
        /// For other types, parses the expected string and compares by value equality.
        /// </summary>
        public static bool CompareValues(IValue actual, string expectedStr, string? valueTypeHint)
        {
            var actualType = InferValueType(actual.GetValueType());
            var valueType = valueTypeHint ?? actualType;

            if (valueType == "double")
            {
                var expected = (double)ParseExpectedValue(expectedStr, valueType);
                var actualDouble = Convert.ToDouble(GetValueAs(actual, actualType));
                return CompareDoubles(expected, actualDouble);
            }

            var expectedVal = ParseExpectedValue(expectedStr, valueType);
            var actualVal = GetValueAs(actual, actualType);
            return expectedVal.Equals(actualVal);
        }

        /// <summary>
        /// Gets a specific typed value from an IValue (e.g., GetBoolean, GetInteger).
        /// Returns the unwrapped value as an object.
        /// </summary>
        private static object GetValueAs(IValue value, string valueType)
        {
            switch (valueType.ToLower().Trim())
            {
                case "boolean": return value.GetBoolean();
                case "integer": return value.GetInteger();
                case "double": return value.GetDouble();
                case "decimal": return value.GetDecimal();
                case "string": return value.GetString();
                case "date": return value.GetDate();
                case "datetime": return value.GetDatetime();
                case "datetime-tz": return value.GetDatetimeTZ();
                case "duration": return value.GetDuration();
                case "struct": return value.GetStruct().ToString()!;
                default:
                    throw new BehaviourTestException($"Unknown value type: {valueType}");
            }
        }
    }
}
