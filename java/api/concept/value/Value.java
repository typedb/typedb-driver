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

package com.vaticle.typedb.client.api.concept.value;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface Value extends Concept {
    DateTimeFormatter ISO_LOCAL_DATE_TIME_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    default boolean isValue() {
        return true;
    }

    @Override
    default Value asValue() {
        return this;
    }

    Type getType();

    boolean isBoolean();

    boolean isLong();

    boolean isDouble();

    boolean isString();

    boolean isDateTime();

    boolean asBoolean();

    long asLong();

    double asDouble();

    String asString();

    LocalDateTime asDateTime();

    @Override
    default JsonObject toJSON() {
        JsonValue value;
        switch (getType()) {
            case BOOLEAN: value = Json.value(asBoolean()); break;
            case LONG: value = Json.value(asLong()); break;
            case DOUBLE: value = Json.value(asDouble()); break;
            case STRING: value = Json.value(asString()); break;
            case DATETIME: value = Json.value(asDateTime().format(ISO_LOCAL_DATE_TIME_MILLIS)); break;
            default: throw new TypeDBClientException(ILLEGAL_STATE);
        }
        return Json.object()
                .add("value_type", getType().name().toLowerCase())
                .add("value", value);
    }

    enum Type {
        OBJECT(Object.class, false, false, com.vaticle.typedb.client.jni.ValueType.Object),
        BOOLEAN(Boolean.class, true, false, com.vaticle.typedb.client.jni.ValueType.Boolean),
        LONG(Long.class, true, true, com.vaticle.typedb.client.jni.ValueType.Long),
        DOUBLE(Double.class, true, false, com.vaticle.typedb.client.jni.ValueType.Double),
        STRING(String.class, true, true, com.vaticle.typedb.client.jni.ValueType.String),
        DATETIME(LocalDateTime.class, true, true, com.vaticle.typedb.client.jni.ValueType.DateTime);

        private final Class<?> valueClass;
        private final boolean isWritable;
        private final boolean isKeyable;
        public final com.vaticle.typedb.client.jni.ValueType nativeObject;

        Type(Class<?> valueClass, boolean isWritable, boolean isKeyable, com.vaticle.typedb.client.jni.ValueType nativeObject) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
            this.nativeObject = nativeObject;
        }

        @CheckReturnValue
        public static Type of(com.vaticle.typedb.client.jni.ValueType valueType) {
            for (Type type : Type.values()) {
                if (type.nativeObject == valueType) {
                    return type;
                }
            }
            throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
        }

        @CheckReturnValue
        public Class<?> valueClass() {
            return valueClass;
        }

        @CheckReturnValue
        public boolean isWritable() {
            return isWritable;
        }

        @CheckReturnValue
        public boolean isKeyable() {
            return isKeyable;
        }
    }

}
