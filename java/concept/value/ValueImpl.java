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

import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.collection.Pair;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.typedb.driver.common.collection.Collections.pair;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.jni.typedb_driver.value_get_boolean;
import static com.typedb.driver.jni.typedb_driver.value_get_date_as_seconds;
import static com.typedb.driver.jni.typedb_driver.value_get_datetime;
import static com.typedb.driver.jni.typedb_driver.value_get_datetime_tz;
import static com.typedb.driver.jni.typedb_driver.value_get_decimal;
import static com.typedb.driver.jni.typedb_driver.value_get_double;
import static com.typedb.driver.jni.typedb_driver.value_get_duration;
import static com.typedb.driver.jni.typedb_driver.value_get_long;
import static com.typedb.driver.jni.typedb_driver.value_get_string;
import static com.typedb.driver.jni.typedb_driver.value_get_struct;
import static com.typedb.driver.jni.typedb_driver.value_get_value_type;
import static com.typedb.driver.jni.typedb_driver.value_is_boolean;
import static com.typedb.driver.jni.typedb_driver.value_is_date;
import static com.typedb.driver.jni.typedb_driver.value_is_datetime;
import static com.typedb.driver.jni.typedb_driver.value_is_datetime_tz;
import static com.typedb.driver.jni.typedb_driver.value_is_decimal;
import static com.typedb.driver.jni.typedb_driver.value_is_double;
import static com.typedb.driver.jni.typedb_driver.value_is_duration;
import static com.typedb.driver.jni.typedb_driver.value_is_long;
import static com.typedb.driver.jni.typedb_driver.value_is_string;
import static com.typedb.driver.jni.typedb_driver.value_is_struct;

public class ValueImpl extends ConceptImpl implements Value {
    private int hash = 0;

    public ValueImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public String getType() {
        return value_get_value_type(nativeObject);
    }

    @Override
    public boolean isBoolean() {
        return value_is_boolean(nativeObject);
    }

    @Override
    public boolean isLong() {
        return value_is_long(nativeObject);
    }

    @Override
    public boolean isDouble() {
        return value_is_double(nativeObject);
    }

    @Override
    public boolean isDecimal() {
        return value_is_decimal(nativeObject);
    }

    @Override
    public boolean isString() {
        return value_is_string(nativeObject);
    }

    @Override
    public boolean isDate() {
        return value_is_date(nativeObject);
    }

    @Override
    public boolean isDatetime() {
        return value_is_datetime(nativeObject);
    }

    @Override
    public boolean isDatetimeTZ() {
        return value_is_datetime_tz(nativeObject);
    }

    @Override
    public boolean isDuration() {
        return value_is_duration(nativeObject);
    }

    @Override
    public boolean isStruct() {
        return value_is_struct(nativeObject);
    }

    @Override
    public Object asUntyped() {
        if (isBoolean()) return asBoolean();
        else if (isLong()) return asLong();
        else if (isDouble()) return asDouble();
        else if (isDecimal()) return asDecimal();
        else if (isString()) return asString();
        else if (isDate()) return asDate();
        else if (isDatetime()) return asDatetime();
        else if (isDatetimeTZ()) return asDatetimeTZ();
        else if (isDuration()) return asDuration();
        else if (isStruct()) return asStruct();
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public boolean asBoolean() {
        if (!isBoolean()) throw new TypeDBDriverException(ILLEGAL_CAST, "boolean");
        return value_get_boolean(nativeObject);
    }

    @Override
    public long asLong() {
        if (!isLong()) throw new TypeDBDriverException(ILLEGAL_CAST, "long");
        return value_get_long(nativeObject);
    }

    @Override
    public double asDouble() {
        if (!isDouble()) throw new TypeDBDriverException(ILLEGAL_CAST, "double");
        return value_get_double(nativeObject);
    }

    @Override
    public BigDecimal asDecimal() {
        if (!isDecimal()) throw new TypeDBDriverException(ILLEGAL_CAST, "decimal");
        com.typedb.driver.jni.Decimal nativeDecimal = value_get_decimal(nativeObject);
        BigInteger nativeFractional = nativeDecimal.getFractional();
        BigDecimal integerPart = new BigDecimal(nativeDecimal.getInteger());
        BigDecimal fractionalPart = new BigDecimal(nativeFractional)
                .setScale(DECIMAL_SCALE, RoundingMode.UNNECESSARY)
                .divide(BigDecimal.TEN.pow(DECIMAL_SCALE), RoundingMode.UNNECESSARY);
        return integerPart.add(fractionalPart);
    }

    @Override
    public String asString() {
        if (!isString()) throw new TypeDBDriverException(ILLEGAL_CAST, "string");
        return value_get_string(nativeObject);
    }

    @Override
    public LocalDate asDate() {
        if (!isDate()) throw new TypeDBDriverException(ILLEGAL_CAST, "date");
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(value_get_date_as_seconds(nativeObject)), ZoneOffset.UTC).toLocalDate();
    }

    @Override
    public LocalDateTime asDatetime() {
        if (!isDatetime()) throw new TypeDBDriverException(ILLEGAL_CAST, "datetime");
        return LocalDateTime.ofInstant(instantFromNativeDatetime(value_get_datetime(nativeObject)), ZoneOffset.UTC);
    }

    @Override
    public ZonedDateTime asDatetimeTZ() {
        if (!isDatetimeTZ()) throw new TypeDBDriverException(ILLEGAL_CAST, "datetime-tz");
        com.typedb.driver.jni.DatetimeAndZoneId nativeDatetime = value_get_datetime_tz(nativeObject);
        return instantFromNativeDatetime(nativeDatetime.getDatetime_in_nanos()).atZone(ZoneId.of(nativeDatetime.getZone_id()));
    }

    @Override
    public Duration asDuration() {
        if (!isDuration()) throw new TypeDBDriverException(ILLEGAL_CAST, "duration");
        return new Duration(value_get_duration(nativeObject));
    }

    @Override
    public Map<String, Optional<Value>> asStruct() {
        if (!isStruct()) throw new TypeDBDriverException(ILLEGAL_CAST, "struct");
        return new NativeIterator<>(value_get_struct(nativeObject)).stream().map(fieldAndValue -> {
            String fieldName = fieldAndValue.getString();
            com.typedb.driver.jni.Concept nativeValue = fieldAndValue.getValue();
            Optional<Value> resultValue;
            if (nativeValue != null) {
                Concept value = ConceptImpl.of(nativeValue);
                if (!value.isValue()) throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
                resultValue = Optional.of(value.asValue());
            } else {
                resultValue = Optional.empty();
            }
            return pair(fieldName, resultValue);
        }).collect(Collectors.toMap(Pair::first, Pair::second));
    }

    private Instant instantFromNativeDatetime(com.typedb.driver.jni.DatetimeInNanos nativeDatetime) {
        return Instant.ofEpochSecond(nativeDatetime.getSeconds(), nativeDatetime.getSubsec_nanos());
    }

    @Override
    public String toString() {
        if (isBoolean()) return Boolean.toString(asBoolean());
        else if (isLong()) return Long.toString(asLong());
        else if (isDouble()) return Double.toString(asDouble());
        else if (isDecimal()) return asDecimal().toString();
        else if (isString()) return asString();
        else if (isDate()) return asDate().toString();
        else if (isDatetime()) return asDatetime().toString();
        else if (isDatetimeTZ()) return asDatetimeTZ().toString();
        else if (isDuration()) return asDuration().toString();
        else if (isStruct()) return asStruct().toString();
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        return asUntyped().hashCode();
    }
}
