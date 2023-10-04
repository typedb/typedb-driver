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

package com.vaticle.typedb.driver.api.concept.value;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface Value extends Concept {
    DateTimeFormatter ISO_LOCAL_DATE_TIME_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isValue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Value asValue() {
        return this;
    }

    /**
     * Retrieves the <code>Value.Type</code> of this value concept.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getType()
     * </pre>
     */
    Type getType();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isBoolean()
     * </pre>
     */
    boolean isBoolean();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isLong();
     * </pre>
     */
    boolean isLong();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDouble();
     * </pre>
     */
    boolean isDouble();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isString();
     * </pre>
     */
    boolean isString();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>datetime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDatetime();
     * </pre>
     */
    boolean isDateTime();

    /**
     * Returns a <code>boolean</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asBoolean();
     * </pre>
     */
    boolean asBoolean();

    /**
     * Returns a <code>long</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asLong();
     * </pre>
     */
    long asLong();

    /**
     * Returns a <code>double</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDouble();
     * </pre>
     */
    double asDouble();

    /**
     * Returns a <code>string</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asString();
     * </pre>
     */
    String asString();

    /**
     * Returns a <code>datetime</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDatetime();
     * </pre>
     */
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
            default: throw new TypeDBDriverException(ILLEGAL_STATE);
        }
        return Json.object()
                .add("value_type", getType().name().toLowerCase())
                .add("value", value);
    }

    enum Type {
        OBJECT(Object.class, false, false, com.vaticle.typedb.driver.jni.ValueType.Object),
        BOOLEAN(Boolean.class, true, false, com.vaticle.typedb.driver.jni.ValueType.Boolean),
        LONG(Long.class, true, true, com.vaticle.typedb.driver.jni.ValueType.Long),
        DOUBLE(Double.class, true, false, com.vaticle.typedb.driver.jni.ValueType.Double),
        STRING(String.class, true, true, com.vaticle.typedb.driver.jni.ValueType.String),
        DATETIME(LocalDateTime.class, true, true, com.vaticle.typedb.driver.jni.ValueType.DateTime);

        private final Class<?> valueClass;
        private final boolean isWritable;
        private final boolean isKeyable;
        public final com.vaticle.typedb.driver.jni.ValueType nativeObject;

        Type(Class<?> valueClass, boolean isWritable, boolean isKeyable, com.vaticle.typedb.driver.jni.ValueType nativeObject) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
            this.nativeObject = nativeObject;
        }

        @CheckReturnValue
        public static Type of(com.vaticle.typedb.driver.jni.ValueType valueType) {
            for (Type type : Type.values()) {
                if (type.nativeObject == valueType) {
                    return type;
                }
            }
            throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
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
