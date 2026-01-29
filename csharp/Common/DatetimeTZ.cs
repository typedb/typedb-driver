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
    /// Represents a datetime with timezone information.
    /// C#'s <see cref="DateTimeOffset"/> cannot carry IANA timezone names (e.g., "Europe/London"),
    /// so this class wraps a <see cref="DateTimeOffset"/> alongside the original zone identifier.
    /// This mirrors Java's <c>ZonedDateTime</c> which preserves the zone ID.
    /// </summary>
    public class DatetimeTZ : IEquatable<DatetimeTZ>
    {
        private int _hash = 0;

        /// <summary>
        /// Creates a DatetimeTZ with an IANA timezone name.
        /// </summary>
        /// <param name="dateTimeOffset">The date, time, and UTC offset.</param>
        /// <param name="zoneName">The IANA timezone name (e.g., "Europe/London").</param>
        public DatetimeTZ(DateTimeOffset dateTimeOffset, string zoneName)
        {
            DateTimeOffset = dateTimeOffset;
            ZoneName = zoneName;
            IsFixedOffset = false;
        }

        /// <summary>
        /// Creates a DatetimeTZ with a fixed UTC offset (no IANA zone name).
        /// </summary>
        /// <param name="dateTimeOffset">The date, time, and UTC offset.</param>
        public DatetimeTZ(DateTimeOffset dateTimeOffset)
        {
            DateTimeOffset = dateTimeOffset;
            ZoneName = null;
            IsFixedOffset = true;
        }

        /// <summary>
        /// Gets the underlying <see cref="System.DateTimeOffset"/> value.
        /// </summary>
        public DateTimeOffset DateTimeOffset { get; }

        /// <summary>
        /// Gets the IANA timezone name (e.g., "Europe/London"), or null for fixed offsets.
        /// </summary>
        public string? ZoneName { get; }

        /// <summary>
        /// Gets whether this datetime-tz uses a fixed UTC offset (true) or an IANA zone name (false).
        /// </summary>
        public bool IsFixedOffset { get; }

        /// <summary>
        /// Formats the datetime-tz as an ISO-8601 string.
        /// Fixed offsets produce: "2024-09-20T16:40:05+01:00"
        /// IANA zones produce: "2024-09-20T16:40:05+01:00 Europe/London"
        /// Subsecond precision is omitted if zero, otherwise trailing zeros are trimmed.
        /// </summary>
        public override string ToString()
        {
            var dt = DateTimeOffset;
            string formatted = dt.ToString("yyyy-MM-dd'T'HH:mm:ss", CultureInfo.InvariantCulture);

            long ticks = dt.Ticks % TimeSpan.TicksPerSecond;
            if (ticks != 0)
            {
                string fractional = ticks.ToString("D7").TrimEnd('0');
                formatted += "." + fractional;
            }

            formatted += FormatOffset(dt.Offset);

            if (!IsFixedOffset && ZoneName != null)
            {
                formatted += " " + ZoneName;
            }

            return formatted;
        }

        private static string FormatOffset(TimeSpan offset)
        {
            if (offset == TimeSpan.Zero)
            {
                return "+00:00";
            }

            string sign = offset < TimeSpan.Zero ? "-" : "+";
            TimeSpan abs = offset < TimeSpan.Zero ? offset.Negate() : offset;
            return $"{sign}{abs.Hours:D2}:{abs.Minutes:D2}";
        }

        /// <inheritdoc/>
        public bool Equals(DatetimeTZ? other)
        {
            if (other is null) return false;
            if (ReferenceEquals(this, other)) return true;
            return DateTimeOffset.Equals(other.DateTimeOffset)
                && IsFixedOffset == other.IsFixedOffset
                && ZoneName == other.ZoneName;
        }

        /// <inheritdoc/>
        public override bool Equals(object? obj)
        {
            return Equals(obj as DatetimeTZ);
        }

        /// <inheritdoc/>
        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = HashCode.Combine(DateTimeOffset, IsFixedOffset, ZoneName);
            }
            return _hash;
        }

        /// <summary>
        /// Equality operator.
        /// </summary>
        public static bool operator ==(DatetimeTZ? left, DatetimeTZ? right)
        {
            if (left is null) return right is null;
            return left.Equals(right);
        }

        /// <summary>
        /// Inequality operator.
        /// </summary>
        public static bool operator !=(DatetimeTZ? left, DatetimeTZ? right)
        {
            return !(left == right);
        }
    }
}
