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

package com.typedb.driver.concept;

import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.concept.value.ValueImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

/**
 * Factory class to instantiate new <code>Value</code> concepts from raw values.
 * These can then be used in the <code>GivenRows</code> of a query.
 */
public class ValueFactory {
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
        BigDecimal fractional = value.subtract(new BigDecimal(integerPart)).setScale(Concept.DECIMAL_SCALE, RoundingMode.UNNECESSARY);
        BigInteger fractionalPart = fractional.movePointRight(Concept.DECIMAL_SCALE).toBigInteger();
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
}
