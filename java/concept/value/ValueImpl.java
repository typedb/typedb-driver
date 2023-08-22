/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.concept.value;

import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_VALUE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_date_time_as_millis;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_get_string;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_date_time;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_is_string;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_boolean;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_date_time_from_millis;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_double;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_long;
import static com.vaticle.typedb.client.jni.typedb_client.value_new_string;

public class ValueImpl extends ConceptImpl implements Value {
    private int hash = 0;

    public ValueImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    public static Value of(boolean value) {
        return new ValueImpl(value_new_boolean(value));
    }

    public static Value of(long value) {
        return new ValueImpl(value_new_long(value));
    }

    public static Value of(double value) {
        return new ValueImpl(value_new_double(value));
    }

    public static Value of(String value) {
        if (value == null) throw new TypeDBClientException(MISSING_VALUE);
        return new ValueImpl(value_new_string(value));
    }

    public static Value of(LocalDateTime value) {
        if (value == null) throw new TypeDBClientException(MISSING_VALUE);
        return new ValueImpl(value_new_date_time_from_millis(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
    }

    @Override
    public Type getType() {
        if (isBoolean()) return Type.BOOLEAN;
        else if (isLong()) return Type.LONG;
        else if (isDouble()) return Type.DOUBLE;
        else if (isString()) return Type.STRING;
        else if (isDateTime()) return Type.DATETIME;
        else throw new TypeDBClientException(ILLEGAL_STATE);
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
    public boolean isString() {
        return value_is_string(nativeObject);
    }

    @Override
    public boolean isDateTime() {
        return value_is_date_time(nativeObject);
    }

    @Override
    public boolean asBoolean() {
        return value_get_boolean(nativeObject);
    }

    @Override
    public long asLong() {
        return value_get_long(nativeObject);
    }

    @Override
    public double asDouble() {
        return value_get_double(nativeObject);
    }

    @Override
    public String asString() {
        return value_get_string(nativeObject);
    }

    @Override
    public LocalDateTime asDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value_get_date_time_as_millis(nativeObject)), ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        if (isBoolean()) return Boolean.toString(asBoolean());
        else if (isLong()) return Long.toString(asLong());
        else if (isDouble()) return Double.toString(asDouble());
        else if (isString()) return asString();
        else if (isDateTime()) return asDateTime().toString();
        throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    private int computeHash() {
        if (isBoolean()) return Boolean.hashCode(asBoolean());
        else if (isLong()) return Long.hashCode(asLong());
        else if (isDouble()) return Double.hashCode(asDouble());
        else if (isString()) return asString().hashCode();
        else if (isDateTime()) return asDateTime().hashCode();
        return -1;
    }
}
