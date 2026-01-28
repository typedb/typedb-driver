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
using System.Text.RegularExpressions;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Common
{
    /// <summary>
    /// Represents a duration with date and time components.
    /// TypeDB uses a custom duration format with months, days, and nanoseconds components.
    /// </summary>
    public class Duration : IEquatable<Duration>
    {
        private const int MonthsInYear = 12;
        private int _hash = 0;

        /// <summary>
        /// Creates a Duration from a native Duration object.
        /// </summary>
        public Duration(Pinvoke.Duration nativeDuration)
        {
            if (nativeDuration == null)
            {
                throw new TypeDBDriverException(InternalError.NULL_NATIVE_VALUE);
            }

            Months = (int)nativeDuration.months;
            Days = (int)nativeDuration.days;
            Nanos = (long)nativeDuration.nanos;
        }

        /// <summary>
        /// Creates a Duration with the specified components.
        /// </summary>
        /// <param name="months">Number of months.</param>
        /// <param name="days">Number of days.</param>
        /// <param name="nanos">Number of nanoseconds.</param>
        public Duration(int months, int days, long nanos)
        {
            Months = months;
            Days = days;
            Nanos = nanos;
        }

        /// <summary>
        /// Gets the number of months in this duration.
        /// </summary>
        public int Months { get; }

        /// <summary>
        /// Gets the number of days in this duration.
        /// </summary>
        public int Days { get; }

        /// <summary>
        /// Gets the number of nanoseconds in this duration.
        /// </summary>
        public long Nanos { get; }

        /// <summary>
        /// Gets the number of whole seconds in the nanoseconds component.
        /// </summary>
        public long Seconds => Nanos / 1_000_000_000;

        /// <summary>
        /// Gets the nanoseconds within the current second.
        /// </summary>
        public long NanosOfSecond => Nanos % 1_000_000_000;

        /// <summary>
        /// Parses a Duration from an ISO 8601 duration string.
        /// </summary>
        /// <param name="durationString">The duration string (e.g., "P1Y10M7DT15H44M5.00394892S" or "P55W").</param>
        /// <returns>The parsed Duration.</returns>
        /// <exception cref="FormatException">If the string cannot be parsed.</exception>
        public static Duration Parse(string durationString)
        {
            if (string.IsNullOrEmpty(durationString) || !durationString.StartsWith("P"))
            {
                throw new FormatException($"Invalid duration format: {durationString}");
            }

            // Handle week format (PnW)
            var weekMatch = Regex.Match(durationString, @"^P(\d+)W$");
            if (weekMatch.Success)
            {
                int weeks = int.Parse(weekMatch.Groups[1].Value);
                return new Duration(0, weeks * 7, 0);
            }

            int months = 0;
            int days = 0;
            long nanos = 0;

            // Split by T to separate date and time parts
            string[] parts = durationString.Substring(1).Split('T');
            string datePart = parts[0];
            string? timePart = parts.Length > 1 ? parts[1] : null;

            // Parse date part (years, months, days)
            if (!string.IsNullOrEmpty(datePart))
            {
                var yearMatch = Regex.Match(datePart, @"(\d+)Y");
                var monthMatch = Regex.Match(datePart, @"(\d+)M");
                var dayMatch = Regex.Match(datePart, @"(\d+)D");

                if (yearMatch.Success)
                {
                    months += int.Parse(yearMatch.Groups[1].Value) * MonthsInYear;
                }
                if (monthMatch.Success)
                {
                    months += int.Parse(monthMatch.Groups[1].Value);
                }
                if (dayMatch.Success)
                {
                    days = int.Parse(dayMatch.Groups[1].Value);
                }
            }

            // Parse time part (hours, minutes, seconds)
            if (!string.IsNullOrEmpty(timePart))
            {
                var hourMatch = Regex.Match(timePart, @"(\d+)H");
                var minMatch = Regex.Match(timePart, @"(\d+)M");
                var secMatch = Regex.Match(timePart, @"([\d.]+)S");

                if (hourMatch.Success)
                {
                    nanos += long.Parse(hourMatch.Groups[1].Value) * 3600L * 1_000_000_000L;
                }
                if (minMatch.Success)
                {
                    nanos += long.Parse(minMatch.Groups[1].Value) * 60L * 1_000_000_000L;
                }
                if (secMatch.Success)
                {
                    double seconds = double.Parse(secMatch.Groups[1].Value);
                    nanos += (long)(seconds * 1_000_000_000L);
                }
            }

            return new Duration(months, days, nanos);
        }

        /// <inheritdoc/>
        public override string ToString()
        {
            var datePart = $"P{(Months / MonthsInYear > 0 ? $"{Months / MonthsInYear}Y" : "")}" +
                          $"{(Months % MonthsInYear > 0 ? $"{Months % MonthsInYear}M" : "")}" +
                          $"{(Days > 0 ? $"{Days}D" : "")}";

            if (Nanos == 0)
            {
                return datePart.Length > 1 ? datePart : "P0D";
            }

            long totalSeconds = Nanos / 1_000_000_000;
            long nanosOfSecond = Nanos % 1_000_000_000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            var timePart = "T" +
                          $"{(hours > 0 ? $"{hours}H" : "")}" +
                          $"{(minutes > 0 ? $"{minutes}M" : "")}" +
                          $"{(seconds > 0 || nanosOfSecond > 0 ? FormatSeconds(seconds, nanosOfSecond) : "")}";

            return (datePart.Length > 1 ? datePart : "P") + (timePart.Length > 1 ? timePart : "");
        }

        private static string FormatSeconds(long seconds, long nanosOfSecond)
        {
            if (nanosOfSecond == 0)
            {
                return $"{seconds}S";
            }
            return $"{seconds}.{nanosOfSecond:D9}S".TrimEnd('0');
        }

        /// <inheritdoc/>
        public bool Equals(Duration? other)
        {
            if (other is null) return false;
            if (ReferenceEquals(this, other)) return true;
            return Months == other.Months && Days == other.Days && Nanos == other.Nanos;
        }

        /// <inheritdoc/>
        public override bool Equals(object? obj)
        {
            return Equals(obj as Duration);
        }

        /// <inheritdoc/>
        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = HashCode.Combine(Months, Days, Nanos);
            }
            return _hash;
        }

        /// <summary>
        /// Equality operator.
        /// </summary>
        public static bool operator ==(Duration? left, Duration? right)
        {
            if (left is null) return right is null;
            return left.Equals(right);
        }

        /// <summary>
        /// Inequality operator.
        /// </summary>
        public static bool operator !=(Duration? left, Duration? right)
        {
            return !(left == right);
        }
    }
}
