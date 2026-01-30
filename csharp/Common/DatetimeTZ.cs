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
using System.Text.RegularExpressions;

namespace TypeDB.Driver.Common
{
    /// <summary>
    /// Represents a datetime with timezone information and nanosecond precision.
    /// C#'s <see cref="DateTimeOffset"/> cannot carry IANA timezone names (e.g., "Europe/London"),
    /// so this class wraps a <see cref="DateTimeOffset"/> alongside the resolved <see cref="TimeZoneInfo"/>.
    /// Additionally, <see cref="DateTimeOffset"/> only has tick precision (100ns),
    /// so this class stores subsecond nanoseconds separately to preserve TypeDB's full 9-digit precision.
    /// This mirrors Java's <c>ZonedDateTime</c> which preserves both zone ID and nanosecond precision.
    /// </summary>
    public class DatetimeTZ : IEquatable<DatetimeTZ>
    {
        private int _hash = 0;

        /// <summary>
        /// Creates a DatetimeTZ with an IANA timezone from a UTC datetime.
        /// The local time and offset are computed from the zone's rules at the given UTC instant.
        /// This mirrors Java's <c>ZonedDateTime.ofInstant(Instant, ZoneId)</c>.
        /// </summary>
        /// <param name="utcDateTime">The UTC datetime (Kind should be Utc or Unspecified).</param>
        /// <param name="zoneName">The IANA timezone name (e.g., "Europe/London").</param>
        /// <param name="subsecNanos">Subsecond nanoseconds (0–999,999,999). Defaults to 0.</param>
        public DatetimeTZ(DateTime utcDateTime, string zoneName, uint subsecNanos = 0)
        {
            ZoneName = zoneName;
            ZoneInfo = TimeZoneInfo.FindSystemTimeZoneById(zoneName);
            var offset = ZoneInfo.GetUtcOffset(utcDateTime);
            DateTimeOffset = new DateTimeOffset(DateTime.SpecifyKind(utcDateTime, DateTimeKind.Utc)).ToOffset(offset);
            IsFixedOffset = false;
            SubsecNanos = subsecNanos;
        }

        /// <summary>
        /// Creates a DatetimeTZ with a fixed UTC offset (no IANA zone).
        /// </summary>
        /// <param name="dateTimeOffset">The date, time, and UTC offset.</param>
        /// <param name="subsecNanos">Subsecond nanoseconds (0–999,999,999). Defaults to 0.</param>
        public DatetimeTZ(DateTimeOffset dateTimeOffset, uint subsecNanos = 0)
        {
            DateTimeOffset = dateTimeOffset;
            ZoneName = null;
            ZoneInfo = null;
            IsFixedOffset = true;
            SubsecNanos = subsecNanos;
        }

        /// <summary>
        /// Gets the local date and time with its UTC offset.
        /// For IANA zones, the offset is derived from the zone's rules at the stored instant.
        /// Note: this has only tick precision (100ns) and may lose the last 2 nanosecond digits.
        /// </summary>
        public DateTimeOffset DateTimeOffset { get; }

        /// <summary>
        /// Gets the subsecond nanoseconds component (0–999,999,999).
        /// This preserves TypeDB's full nanosecond precision beyond what
        /// <see cref="DateTimeOffset"/> can represent.
        /// </summary>
        public uint SubsecNanos { get; }

        /// <summary>
        /// Gets the IANA timezone name (e.g., "Europe/London"), or null for fixed offsets.
        /// This is the original name from the database, independent of platform timezone ID conventions.
        /// </summary>
        public string? ZoneName { get; }

        /// <summary>
        /// Gets the resolved <see cref="TimeZoneInfo"/>, or null for fixed offsets.
        /// </summary>
        public TimeZoneInfo? ZoneInfo { get; }

        /// <summary>
        /// Gets whether this datetime-tz uses a fixed UTC offset (true) or an IANA zone (false).
        /// </summary>
        public bool IsFixedOffset { get; }

        /// <summary>
        /// Parses a datetime-tz string in TypeDB format, preserving all 9 fractional digits.
        /// IANA zones: "2024-09-20T16:40:05.000000001 Europe/London" (local time + zone name)
        /// Fixed offsets: "2024-09-20T16:40:05.000000001+0100" (local time + offset without colon)
        /// UTC shorthand: "2024-09-20T16:40:05Z"
        /// </summary>
        public static DatetimeTZ Parse(string value)
        {
            // Try to detect IANA zone: a space followed by a non-offset zone name
            // Fixed offsets end with Z or +/-HHMM (digits only after the sign)
            var zoneMatch = Regex.Match(value, @"^(.+?)\s+([A-Za-z].*)$");
            if (zoneMatch.Success)
            {
                var datetimeStr = zoneMatch.Groups[1].Value;
                var zoneName = zoneMatch.Groups[2].Value;
                uint nanos = ExtractNanos(datetimeStr);
                var localDt = ParseLocalDatetime(datetimeStr);
                var zoneInfo = TimeZoneInfo.FindSystemTimeZoneById(zoneName);
                var utcDt = TimeZoneInfo.ConvertTimeToUtc(
                    DateTime.SpecifyKind(localDt, DateTimeKind.Unspecified), zoneInfo);
                return new DatetimeTZ(utcDt, zoneName, nanos);
            }

            // Fixed offset: parse with DateTimeOffset
            uint fixedNanos = ExtractNanos(value);
            // Normalize offset format: +0100 → +01:00 for DateTimeOffset.Parse
            var truncated = TruncateFractional(value);
            var normalized = Regex.Replace(truncated, @"([+-])(\d{2})(\d{2})$", "$1$2:$3");
            var dto = DateTimeOffset.Parse(normalized, CultureInfo.InvariantCulture);
            return new DatetimeTZ(dto, fixedNanos);
        }

        /// <summary>
        /// Extracts the full nanosecond value from fractional seconds in a datetime string.
        /// </summary>
        private static uint ExtractNanos(string value)
        {
            var dotIdx = value.IndexOf('.');
            if (dotIdx < 0) return 0;

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
            return uint.Parse(padded);
        }

        /// <summary>
        /// Truncates fractional seconds to 7 digits for C# DateTime/DateTimeOffset parsing.
        /// </summary>
        private static string TruncateFractional(string value)
        {
            var dotIdx = value.IndexOf('.');
            if (dotIdx < 0) return value;

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
                return value.Substring(0, dotIdx + 8) + value.Substring(fractionalEnd);
            }
            return value;
        }

        private static DateTime ParseLocalDatetime(string value)
        {
            var truncated = TruncateFractional(value);
            return DateTime.Parse(truncated, CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Formats the datetime-tz as an ISO-8601 string with nanosecond precision.
        /// Fixed offsets produce: "2024-09-20T16:40:05.123456789+01:00"
        /// IANA zones produce: "2024-09-20T16:40:05.123456789+01:00 Europe/London"
        /// Subsecond precision is omitted if zero, otherwise trailing zeros are trimmed.
        /// </summary>
        public override string ToString()
        {
            var dt = DateTimeOffset;
            string formatted = dt.ToString("yyyy-MM-dd'T'HH:mm:ss", CultureInfo.InvariantCulture);

            if (SubsecNanos != 0)
            {
                string fractional = SubsecNanos.ToString("D9").TrimEnd('0');
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
                && SubsecNanos == other.SubsecNanos
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
                _hash = HashCode.Combine(DateTimeOffset, SubsecNanos, IsFixedOffset, ZoneName);
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
