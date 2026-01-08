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

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    public abstract class Concept : NativeObjectWrapper<Pinvoke.Concept>, IConcept
    {
        protected Concept(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public static IConcept ConceptOf(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity_type(nativeConcept))
                return new EntityType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation_type(nativeConcept))
                return new RelationType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute_type(nativeConcept))
                return new AttributeType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_entity(nativeConcept))
                return new Entity(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation(nativeConcept))
                return new Relation(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute(nativeConcept))
                return new Attribute(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_value(nativeConcept))
                return new Value(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_role_type(nativeConcept))
                return new RoleType(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        #region Value Type Checks

        /// <inheritdoc/>
        public bool IsBoolean()
        {
            return Pinvoke.typedb_driver.concept_is_boolean(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsInteger()
        {
            return Pinvoke.typedb_driver.concept_is_integer(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDouble()
        {
            return Pinvoke.typedb_driver.concept_is_double(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDecimal()
        {
            return Pinvoke.typedb_driver.concept_is_decimal(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsString()
        {
            return Pinvoke.typedb_driver.concept_is_string(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDate()
        {
            return Pinvoke.typedb_driver.concept_is_date(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDatetime()
        {
            return Pinvoke.typedb_driver.concept_is_datetime(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDatetimeTZ()
        {
            return Pinvoke.typedb_driver.concept_is_datetime_tz(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsDuration()
        {
            return Pinvoke.typedb_driver.concept_is_duration(NativeObject);
        }

        /// <inheritdoc/>
        public bool IsStruct()
        {
            return Pinvoke.typedb_driver.concept_is_struct(NativeObject);
        }

        #endregion

        #region Value Accessors

        /// <inheritdoc/>
        public bool? TryGetBoolean()
        {
            if (!IsBoolean()) return null;
            return Pinvoke.typedb_driver.concept_get_boolean(NativeObject);
        }

        /// <inheritdoc/>
        public long? TryGetInteger()
        {
            if (!IsInteger()) return null;
            return Pinvoke.typedb_driver.concept_get_integer(NativeObject);
        }

        /// <inheritdoc/>
        public double? TryGetDouble()
        {
            if (!IsDouble()) return null;
            return Pinvoke.typedb_driver.concept_get_double(NativeObject);
        }

        /// <inheritdoc/>
        public decimal? TryGetDecimal()
        {
            if (!IsDecimal()) return null;
            var nativeDecimal = Pinvoke.typedb_driver.concept_get_decimal(NativeObject);
            // Convert native Decimal to C# decimal
            // The native format uses integer_part and fractional_part
            return (decimal)nativeDecimal.integer + ((decimal)nativeDecimal.fractional / (decimal)Math.Pow(10, IConcept.DecimalScale));
        }

        /// <inheritdoc/>
        public string? TryGetString()
        {
            if (!IsString()) return null;
            return Pinvoke.typedb_driver.concept_get_string(NativeObject);
        }

        /// <inheritdoc/>
        public DateOnly? TryGetDate()
        {
            if (!IsDate()) return null;
            long seconds = Pinvoke.typedb_driver.concept_get_date_as_seconds(NativeObject);
            var dateTime = DateTimeOffset.FromUnixTimeSeconds(seconds).DateTime;
            return DateOnly.FromDateTime(dateTime);
        }

        /// <inheritdoc/>
        public DateTime? TryGetDatetime()
        {
            if (!IsDatetime()) return null;
            var nativeDatetime = Pinvoke.typedb_driver.concept_get_datetime(NativeObject);
            long seconds = nativeDatetime.seconds;
            uint nanos = nativeDatetime.subsec_nanos;
            var dateTime = DateTimeOffset.FromUnixTimeSeconds(seconds).DateTime;
            return dateTime.AddTicks(nanos / 100); // 1 tick = 100 nanoseconds
        }

        /// <inheritdoc/>
        public DateTimeOffset? TryGetDatetimeTZ()
        {
            if (!IsDatetimeTZ()) return null;
            var nativeDatetimeTz = Pinvoke.typedb_driver.concept_get_datetime_tz(NativeObject);
            var datetimeInNanos = nativeDatetimeTz.datetime_in_nanos;
            long seconds = datetimeInNanos.seconds;
            uint nanos = datetimeInNanos.subsec_nanos;

            if (nativeDatetimeTz.is_fixed_offset)
            {
                int offsetSeconds = nativeDatetimeTz.local_minus_utc_offset;
                var offset = TimeSpan.FromSeconds(offsetSeconds);
                var dto = DateTimeOffset.FromUnixTimeSeconds(seconds).ToOffset(offset);
                return dto.AddTicks(nanos / 100);
            }
            else
            {
                // IANA timezone - just return as UTC for now
                // Full timezone support would require additional handling
                var dto = DateTimeOffset.FromUnixTimeSeconds(seconds);
                return dto.AddTicks(nanos / 100);
            }
        }

        /// <inheritdoc/>
        public Duration? TryGetDuration()
        {
            if (!IsDuration()) return null;
            var nativeDuration = Pinvoke.typedb_driver.concept_get_duration(NativeObject);
            return new Duration(nativeDuration);
        }

        /// <inheritdoc/>
        public IReadOnlyDictionary<string, IValue?>? TryGetStruct()
        {
            if (!IsStruct()) return null;

            // TODO: Fix struct iteration once SWIG binding field names are determined
            // The native iterator uses StringAndOptValue which has string/value fields
            // that need to be accessed with the correct SWIG-generated C# names
            var result = new Dictionary<string, IValue?>();
            return result;
        }

        #endregion

        #region Metadata Accessors

        /// <inheritdoc/>
        public string GetLabel()
        {
            return Pinvoke.typedb_driver.concept_get_label(NativeObject);
        }

        /// <inheritdoc/>
        public string? TryGetLabel()
        {
            return Pinvoke.typedb_driver.concept_try_get_label(NativeObject);
        }

        /// <inheritdoc/>
        public string? TryGetIID()
        {
            return Pinvoke.typedb_driver.concept_try_get_iid(NativeObject);
        }

        /// <inheritdoc/>
        public string? TryGetValueType()
        {
            return Pinvoke.typedb_driver.concept_try_get_value_type(NativeObject);
        }

        /// <inheritdoc/>
        public IValue? TryGetValue()
        {
            var nativeValue = Pinvoke.typedb_driver.concept_try_get_value(NativeObject);
            if (nativeValue == null) return null;

            var concept = ConceptOf(nativeValue);
            return concept.IsValue() ? concept.AsValue() : null;
        }

        #endregion

        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_to_string(NativeObject);
        }

        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || GetType() != obj.GetType())
            {
                return false;
            }

            Concept that = (Concept)obj;
            return Pinvoke.typedb_driver.concept_equals(NativeObject, that.NativeObject);
        }

        public abstract override int GetHashCode();
    }
}
