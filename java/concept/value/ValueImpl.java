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

package com.typedb.driver.concept.value;

import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.common.exception.ErrorMessage;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_VALUE_RETRIEVAL;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.NULL_CONCEPT_PROPERTY;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.common.util.Objects.className;

import static com.typedb.driver.jni.typedb_driver.concept_new_boolean;
import static com.typedb.driver.jni.typedb_driver.concept_new_date_from_seconds;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime_tz_iana;
import static com.typedb.driver.jni.typedb_driver.concept_new_datetime_tz_offset;
import static com.typedb.driver.jni.typedb_driver.concept_new_decimal;
import static com.typedb.driver.jni.typedb_driver.concept_new_double;
import static com.typedb.driver.jni.typedb_driver.concept_new_duration;
import static com.typedb.driver.jni.typedb_driver.concept_new_integer;
import static com.typedb.driver.jni.typedb_driver.concept_new_string;

public class ValueImpl extends ConceptImpl implements Value {
    private int hash = 0;

    public ValueImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    /**
     * Converts a raw host-language object to a {@link Value} concept.
     * Accepted types: {@code Boolean}, {@code Long}, {@code Integer}, {@code Double}, {@code Float},
     * {@code BigDecimal}, {@code String}, {@code LocalDate}, {@code LocalDateTime}, {@code ZonedDateTime}, {@code Duration}.
     *
     * @throws TypeDBDriverException if the object's type is not supported
     */
    public static Value tryConvertToValue(Object value) {
        if (value instanceof Boolean) return newBoolean((Boolean) value);
        if (value instanceof Long) return newInteger((Long) value);
        if (value instanceof Integer) return newInteger((Integer) value);
        if (value instanceof Double) return newDouble((Double) value);
        if (value instanceof Float) return newDouble(((Float) value).doubleValue());
        if (value instanceof BigDecimal) return newDecimal((BigDecimal) value);
        if (value instanceof String) return newString((String) value);
        if (value instanceof LocalDate) return newDate((LocalDate) value);
        if (value instanceof LocalDateTime) return newDatetime((LocalDateTime) value);
        if (value instanceof ZonedDateTime) return newDatetimeTz((ZonedDateTime) value);
        if (value instanceof Duration) return newDuration((Duration) value);
        throw new TypeDBDriverException(ErrorMessage.Concept.UNSUPPORTED_VALUE_CONVERSION, value.getClass().getName());
    }

    /** Creates a new {@code Value} wrapping the specified {@code boolean} value. */
    public static Value newBoolean(boolean value) {
        return new ValueImpl(concept_new_boolean(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code long} value. */
    public static Value newInteger(long value) {
        return new ValueImpl(concept_new_integer(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code double} value. */
    public static Value newDouble(double value) {
        return new ValueImpl(concept_new_double(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code BigDecimal} value. */
    public static Value newDecimal(BigDecimal value) {
        long integerPart = value.setScale(0, RoundingMode.FLOOR).longValue();
        BigDecimal fractional = value.subtract(new BigDecimal(integerPart)).setScale(com.typedb.driver.api.concept.Concept.DECIMAL_SCALE, RoundingMode.UNNECESSARY);
        BigInteger fractionalPart = fractional.movePointRight(com.typedb.driver.api.concept.Concept.DECIMAL_SCALE).toBigInteger();
        return new ValueImpl(concept_new_decimal(integerPart, fractionalPart));
    }

    /** Creates a new {@code Value} wrapping the specified {@code String} value. */
    public static Value newString(String value) {
        return new ValueImpl(concept_new_string(value));
    }

    /** Creates a new {@code Value} wrapping the specified {@code LocalDate} value. */
    public static Value newDate(LocalDate value) {
        long epochSeconds = value.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        return new ValueImpl(concept_new_date_from_seconds(epochSeconds));
    }

    /** Creates a new {@code Value} wrapping the specified {@code LocalDateTime} value. */
    public static Value newDatetime(LocalDateTime value) {
        long seconds = value.toEpochSecond(ZoneOffset.UTC);
        int nanos = value.getNano();
        return new ValueImpl(concept_new_datetime(seconds, nanos));
    }

    /** Creates a new {@code Value} wrapping the specified {@code ZonedDateTime} value. */
    public static Value newDatetimeTz(ZonedDateTime value) {
        long seconds = value.toInstant().getEpochSecond();
        int nanos = value.getNano();
        if (value.getZone() instanceof ZoneOffset) {
            int offsetSeconds = ((ZoneOffset) value.getZone()).getTotalSeconds();
            return new ValueImpl(concept_new_datetime_tz_offset(seconds, nanos, offsetSeconds));
        } else {
            String zoneName = value.getZone().getId();
            return new ValueImpl(concept_new_datetime_tz_iana(seconds, nanos, zoneName));
        }
    }

    /** Creates a new {@code Value} wrapping the specified {@code Duration} value. */
    public static Value newDuration(Duration value) {
        int months = value.getMonths();
        int days = value.getDays();
        long nanos = value.getTimePart().toNanos();
        return new ValueImpl(concept_new_duration(months, days, BigInteger.valueOf(nanos)));
    }

    @Override
    public String getType() {
        return tryGetValueType().orElseThrow(() -> new TypeDBDriverException(NULL_CONCEPT_PROPERTY, className(this.getClass())));
    }

    @Override
    public Object get() {
        if (isBoolean()) return getBoolean();
        else if (isInteger()) return getInteger();
        else if (isDouble()) return getDouble();
        else if (isDecimal()) return getDecimal();
        else if (isString()) return getString();
        else if (isDate()) return getDate();
        else if (isDatetime()) return getDatetime();
        else if (isDatetimeTZ()) return getDatetimeTZ();
        else if (isDuration()) return getDuration();
        else if (isStruct()) return getStruct();
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public boolean getBoolean() {
        return tryGetBoolean().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "boolean"));
    }

    @Override
    public long getInteger() {
        return tryGetInteger().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "integer"));
    }

    @Override
    public double getDouble() {
        return tryGetDouble().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "double"));
    }

    @Override
    public BigDecimal getDecimal() {
        return tryGetDecimal().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "decimal"));
    }

    @Override
    public String getString() {
        return tryGetString().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "string"));
    }

    @Override
    public LocalDate getDate() {
        return tryGetDate().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "date"));
    }

    @Override
    public LocalDateTime getDatetime() {
        return tryGetDatetime().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "datetime"));
    }

    @Override
    public ZonedDateTime getDatetimeTZ() {
        return tryGetDatetimeTZ().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "datetime-tz"));
    }

    @Override
    public Duration getDuration() {
        return tryGetDuration().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "duration"));
    }

    @Override
    public Map<String, Optional<Value>> getStruct() {
        return tryGetStruct().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "struct"));
    }

    @Override
    public String toString() {
        if (isBoolean()) return Boolean.toString(getBoolean());
        else if (isInteger()) return Long.toString(getInteger());
        else if (isDouble()) return Double.toString(getDouble());
        else if (isDecimal()) return getDecimal().toString();
        else if (isString()) return getString();
        else if (isDate()) return getDate().toString();
        else if (isDatetime()) return getDatetime().toString();
        else if (isDatetimeTZ()) return getDatetimeTZ().toString();
        else if (isDuration()) return getDuration().toString();
        else if (isStruct()) return getStruct().toString();
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return get().hashCode();
    }
}
