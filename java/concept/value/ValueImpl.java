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
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_VALUE_RETRIEVAL;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.NULL_CONCEPT_PROPERTY;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.common.util.Objects.className;

public class ValueImpl extends ConceptImpl implements Value {
    private int hash = 0;

    public ValueImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public String getType() {
        return tryGetValueType().orElseThrow(() -> new TypeDBDriverException(NULL_CONCEPT_PROPERTY, className(this.getClass())));
    }

    @Override
    public Object get() {
        if (isBoolean()) return getBoolean();
        else if (isLong()) return getLong();
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
    public long getLong() {
        return tryGetLong().orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "long"));
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
        else if (isLong()) return Long.toString(getLong());
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
