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
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.collection.Pair;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.instance.AttributeImpl;
import com.typedb.driver.concept.instance.EntityImpl;
import com.typedb.driver.concept.instance.RelationImpl;
import com.typedb.driver.concept.type.AttributeTypeImpl;
import com.typedb.driver.concept.type.EntityTypeImpl;
import com.typedb.driver.concept.type.RelationTypeImpl;
import com.typedb.driver.concept.type.RoleTypeImpl;
import com.typedb.driver.concept.value.ValueImpl;

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
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.jni.typedb_driver.concept_equals;
import static com.typedb.driver.jni.typedb_driver.concept_get_boolean;
import static com.typedb.driver.jni.typedb_driver.concept_get_date_as_seconds;
import static com.typedb.driver.jni.typedb_driver.concept_get_datetime;
import static com.typedb.driver.jni.typedb_driver.concept_get_datetime_tz;
import static com.typedb.driver.jni.typedb_driver.concept_get_decimal;
import static com.typedb.driver.jni.typedb_driver.concept_get_double;
import static com.typedb.driver.jni.typedb_driver.concept_get_duration;
import static com.typedb.driver.jni.typedb_driver.concept_get_label;
import static com.typedb.driver.jni.typedb_driver.concept_get_long;
import static com.typedb.driver.jni.typedb_driver.concept_get_string;
import static com.typedb.driver.jni.typedb_driver.concept_get_struct;
import static com.typedb.driver.jni.typedb_driver.concept_is_attribute;
import static com.typedb.driver.jni.typedb_driver.concept_is_attribute_type;
import static com.typedb.driver.jni.typedb_driver.concept_is_boolean;
import static com.typedb.driver.jni.typedb_driver.concept_is_date;
import static com.typedb.driver.jni.typedb_driver.concept_is_datetime;
import static com.typedb.driver.jni.typedb_driver.concept_is_datetime_tz;
import static com.typedb.driver.jni.typedb_driver.concept_is_decimal;
import static com.typedb.driver.jni.typedb_driver.concept_is_double;
import static com.typedb.driver.jni.typedb_driver.concept_is_duration;
import static com.typedb.driver.jni.typedb_driver.concept_is_entity;
import static com.typedb.driver.jni.typedb_driver.concept_is_entity_type;
import static com.typedb.driver.jni.typedb_driver.concept_is_long;
import static com.typedb.driver.jni.typedb_driver.concept_is_relation;
import static com.typedb.driver.jni.typedb_driver.concept_is_relation_type;
import static com.typedb.driver.jni.typedb_driver.concept_is_role_type;
import static com.typedb.driver.jni.typedb_driver.concept_is_string;
import static com.typedb.driver.jni.typedb_driver.concept_is_struct;
import static com.typedb.driver.jni.typedb_driver.concept_is_value;
import static com.typedb.driver.jni.typedb_driver.concept_to_string;
import static com.typedb.driver.jni.typedb_driver.concept_try_get_iid;
import static com.typedb.driver.jni.typedb_driver.concept_try_get_label;
import static com.typedb.driver.jni.typedb_driver.concept_try_get_value;
import static com.typedb.driver.jni.typedb_driver.concept_try_get_value_type;

public abstract class ConceptImpl extends NativeObject<com.typedb.driver.jni.Concept> implements Concept {
    protected ConceptImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    public static ConceptImpl of(com.typedb.driver.jni.Concept concept) {
        if (concept_is_entity_type(concept)) return new EntityTypeImpl(concept);
        else if (concept_is_relation_type(concept)) return new RelationTypeImpl(concept);
        else if (concept_is_attribute_type(concept)) return new AttributeTypeImpl(concept);
        else if (concept_is_entity(concept)) return new EntityImpl(concept);
        else if (concept_is_relation(concept)) return new RelationImpl(concept);
        else if (concept_is_attribute(concept)) return new AttributeImpl(concept);
        else if (concept_is_value(concept)) return new ValueImpl(concept);
        else if (concept_is_role_type(concept)) return new RoleTypeImpl(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public String getLabel() {
        return concept_get_label(nativeObject);
    }

    @Override
    public Optional<String> tryGetLabel() {
        return Optional.ofNullable(concept_try_get_label(nativeObject));
    }

    @Override
    public final Optional<String> tryGetIID() {
        return Optional.ofNullable(concept_try_get_iid(nativeObject));
    }

    @Override
    public final Optional<String> tryGetValueType() {
        return Optional.ofNullable(concept_try_get_value_type(nativeObject));
    }

    @Override
    public final Optional<Value> tryGetValue() {
        com.typedb.driver.jni.Concept nativeValue = concept_try_get_value(nativeObject);
        return nativeValue == null ? Optional.empty() : Optional.of(new ValueImpl(nativeValue));
    }

    @Override
    public boolean isBoolean() {
        return concept_is_boolean(nativeObject);
    }

    @Override
    public boolean isLong() {
        return concept_is_long(nativeObject);
    }

    @Override
    public boolean isDouble() {
        return concept_is_double(nativeObject);
    }

    @Override
    public boolean isDecimal() {
        return concept_is_decimal(nativeObject);
    }

    @Override
    public boolean isString() {
        return concept_is_string(nativeObject);
    }

    @Override
    public boolean isDate() {
        return concept_is_date(nativeObject);
    }

    @Override
    public boolean isDatetime() {
        return concept_is_datetime(nativeObject);
    }

    @Override
    public boolean isDatetimeTZ() {
        return concept_is_datetime_tz(nativeObject);
    }

    @Override
    public boolean isDuration() {
        return concept_is_duration(nativeObject);
    }

    @Override
    public boolean isStruct() {
        return concept_is_struct(nativeObject);
    }

    @Override
    public Optional<Boolean> tryGetBoolean() {
        if (isType() || !isBoolean()) return Optional.empty();
        return Optional.of(concept_get_boolean(nativeObject));
    }

    @Override
    public Optional<Long> tryGetLong() {
        if (isType() || !isLong()) return Optional.empty();
        return Optional.of(concept_get_long(nativeObject));
    }

    @Override
    public Optional<Double> tryGetDouble() {
        if (isType() || !isDouble()) return Optional.empty();
        return Optional.of(concept_get_double(nativeObject));
    }

    @Override
    public Optional<BigDecimal> tryGetDecimal() {
        if (isType() || !isDecimal()) return Optional.empty();
        com.typedb.driver.jni.Decimal nativeDecimal = concept_get_decimal(nativeObject);
        BigInteger nativeFractional = nativeDecimal.getFractional();
        BigDecimal integerPart = new BigDecimal(nativeDecimal.getInteger());
        BigDecimal fractionalPart = new BigDecimal(nativeFractional)
                .setScale(DECIMAL_SCALE, RoundingMode.UNNECESSARY)
                .divide(BigDecimal.TEN.pow(DECIMAL_SCALE), RoundingMode.UNNECESSARY);
        return Optional.of(integerPart.add(fractionalPart));
    }

    @Override
    public Optional<String> tryGetString() {
        if (isType() || !isString()) return Optional.empty();
        return Optional.of(concept_get_string(nativeObject));
    }

    @Override
    public Optional<LocalDate> tryGetDate() {
        if (isType() || !isDate()) return Optional.empty();
        return Optional.of(LocalDateTime.ofInstant(Instant.ofEpochSecond(concept_get_date_as_seconds(nativeObject)), ZoneOffset.UTC).toLocalDate());
    }

    @Override
    public Optional<LocalDateTime> tryGetDatetime() {
        if (isType() || !isDatetime()) return Optional.empty();
        return Optional.of(LocalDateTime.ofInstant(instantFromNativeDatetime(concept_get_datetime(nativeObject)), ZoneOffset.UTC));
    }

    @Override
    public Optional<ZonedDateTime> tryGetDatetimeTZ() {
        if (isType() || !isDatetimeTZ()) return Optional.empty();
        com.typedb.driver.jni.DatetimeAndTimeZone nativeDatetime = concept_get_datetime_tz(nativeObject);
        Instant naiveDatetime = instantFromNativeDatetime(nativeDatetime.getDatetime_in_nanos());
        if (nativeDatetime.getIs_fixed_offset()) {
            return Optional.of(naiveDatetime.atZone(ZoneOffset.ofTotalSeconds(nativeDatetime.getLocal_minus_utc_offset())));
        } else {
            assert nativeDatetime.getZone_name() != null;
            return Optional.of(naiveDatetime.atZone(ZoneId.of(nativeDatetime.getZone_name())));
        }
    }

    @Override
    public Optional<Duration> tryGetDuration() {
        if (isType() || !isDuration()) return Optional.empty();
        return Optional.of(new Duration(concept_get_duration(nativeObject)));
    }

    @Override
    public Optional<Map<String, Optional<Value>>> tryGetStruct() {
        if (isType() || !isStruct()) return Optional.empty();
        return Optional.of(new NativeIterator<>(concept_get_struct(nativeObject)).stream().map(fieldAndValue -> {
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
        }).collect(Collectors.toMap(Pair::first, Pair::second)));
    }

    private Instant instantFromNativeDatetime(com.typedb.driver.jni.DatetimeInNanos nativeDatetime) {
        return Instant.ofEpochSecond(nativeDatetime.getSeconds(), nativeDatetime.getSubsec_nanos());
    }

    @Override
    public String toString() {
        return concept_to_string(nativeObject);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConceptImpl that = (ConceptImpl) obj;
        return concept_equals(this.nativeObject, that.nativeObject);
    }

    @Override
    public abstract int hashCode();
}
