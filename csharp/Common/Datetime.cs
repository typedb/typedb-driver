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

namespace TypeDB.Driver.Common
{
    /// <summary>
    /// Represents a datetime with nanosecond precision.
    /// C#'s <see cref="System.DateTime"/> only has tick precision (100ns, 7 fractional digits),
    /// losing the last 2 digits of TypeDB's 9-digit nanosecond precision.
    /// This class stores the full-precision seconds and subsecond nanoseconds separately,
    /// similar to Java's <c>Instant</c>.
    /// </summary>
    public class Datetime : IEquatable<Datetime>
    {
        private int _hash = 0;

        /// <summary>
        /// Creates a Datetime from Unix epoch seconds and subsecond nanoseconds.
        /// </summary>
        /// <param name="seconds">Seconds since Unix epoch (1970-01-01T00:00:00Z).</param>
        /// <param name="subsecNanos">Nanoseconds within the second (0–999,999,999).</param>
        public Datetime(long seconds, uint subsecNanos)
        {
            Seconds = seconds;
            SubsecNanos = subsecNanos;
            DateTime = DateTimeOffset.FromUnixTimeSeconds(seconds)
                .AddTicks(subsecNanos / 100)
                .DateTime;
        }

        /// <summary>
        /// Gets the Unix epoch seconds component.
        /// </summary>
        public long Seconds { get; }

        /// <summary>
        /// Gets the subsecond nanoseconds component (0–999,999,999).
        /// </summary>
        public uint SubsecNanos { get; }

        /// <summary>
        /// Gets a <see cref="System.DateTime"/> derived from this value.
        /// Note: this has only tick precision (100ns) and may lose the last 2 nanosecond digits.
        /// </summary>
        public DateTime DateTime { get; }

        /// <summary>
        /// Parses a datetime string in TypeDB format, preserving all 9 fractional digits.
        /// Example: "2024-09-20T16:40:05.123456789"
        /// </summary>
        public static Datetime Parse(string value)
        {
            uint nanos = 0;
            string datetimePart = value;

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

                var fractionalStr = value.Substring(dotIdx + 1, fractionalEnd - dotIdx - 1);
                var padded = fractionalStr.PadRight(9, '0');
                if (padded.Length > 9) padded = padded.Substring(0, 9);
                nanos = uint.Parse(padded);

                // Truncate to 7 digits for C# DateTime parsing
                var truncatedFractional = fractionalStr.Length > 7
                    ? fractionalStr.Substring(0, 7)
                    : fractionalStr;
                datetimePart = value.Substring(0, dotIdx + 1) + truncatedFractional
                    + value.Substring(fractionalEnd);
            }

            var dt = DateTime.Parse(datetimePart, CultureInfo.InvariantCulture);
            var dto = new DateTimeOffset(DateTime.SpecifyKind(dt, DateTimeKind.Utc));
            long seconds = dto.ToUnixTimeSeconds();

            return new Datetime(seconds, nanos);
        }

        /// <summary>
        /// Formats the datetime as an ISO-8601 string with nanosecond precision.
        /// Subsecond precision is omitted if zero, otherwise trailing zeros are trimmed.
        /// </summary>
        public override string ToString()
        {
            string formatted = DateTime.ToString("yyyy-MM-dd'T'HH:mm:ss", CultureInfo.InvariantCulture);

            if (SubsecNanos != 0)
            {
                string fractional = SubsecNanos.ToString("D9").TrimEnd('0');
                formatted += "." + fractional;
            }

            return formatted;
        }

        /// <inheritdoc/>
        public bool Equals(Datetime? other)
        {
            if (other is null) return false;
            if (ReferenceEquals(this, other)) return true;
            return Seconds == other.Seconds && SubsecNanos == other.SubsecNanos;
        }

        /// <inheritdoc/>
        public override bool Equals(object? obj)
        {
            return Equals(obj as Datetime);
        }

        /// <inheritdoc/>
        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = HashCode.Combine(Seconds, SubsecNanos);
            }
            return _hash;
        }

        /// <summary>
        /// Equality operator.
        /// </summary>
        public static bool operator ==(Datetime? left, Datetime? right)
        {
            if (left is null) return right is null;
            return left.Equals(right);
        }

        /// <summary>
        /// Inequality operator.
        /// </summary>
        public static bool operator !=(Datetime? left, Datetime? right)
        {
            return !(left == right);
        }
    }
}
