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

package com.vaticle.typedb.client.api.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Value;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.BAD_ENUM_VALUE;

public interface AttributeType extends ThingType {
    @CheckReturnValue
    ValueType getValueType();

    @Override
    @CheckReturnValue
    default boolean isAttributeType() {
        return true;
    }

    @Override
    @CheckReturnValue
    default AttributeType asAttributeType() {
        return this;
    }

    Attribute put(TypeDBTransaction transaction, Value value);

    default Attribute put(TypeDBTransaction transaction, String value) {
        return put(transaction, Value.of(value));
    }

    default Attribute put(TypeDBTransaction transaction, long value) {
        return put(transaction, Value.of(value));
    }

    default Attribute put(TypeDBTransaction transaction, double value) {
        return put(transaction, Value.of(value));
    }

    default Attribute put(TypeDBTransaction transaction, boolean value) {
        return put(transaction, Value.of(value));
    }

    default Attribute put(TypeDBTransaction transaction, LocalDateTime value) {
        return put(transaction, Value.of(value));
    }

    @Nullable
    Attribute get(TypeDBTransaction transaction, Value value);

    @Nullable
    default Attribute get(TypeDBTransaction transaction, String value) {
        return get(transaction, Value.of(value));
    }

    @Nullable
    default Attribute get(TypeDBTransaction transaction, long value) {
        return get(transaction, Value.of(value));
    }

    @Nullable
    default Attribute get(TypeDBTransaction transaction, double value) {
        return get(transaction, Value.of(value));
    }

    @Nullable
    default Attribute get(TypeDBTransaction transaction, boolean value) {
        return get(transaction, Value.of(value));
    }

    @Nullable
    default Attribute get(TypeDBTransaction transaction, LocalDateTime value) {
        return get(transaction, Value.of(value));
    }

    String getRegex(TypeDBTransaction transaction);

    void setRegex(TypeDBTransaction transaction, String regex);

    void unsetRegex(TypeDBTransaction transaction);

    @CheckReturnValue
    default boolean isBoolean() {
        return getValueType() == ValueType.BOOLEAN;
    }

    @CheckReturnValue
    default boolean isLong() {
        return getValueType() == ValueType.LONG;
    }

    @CheckReturnValue
    default boolean isDouble() {
        return getValueType() == ValueType.DOUBLE;
    }

    @CheckReturnValue
    default boolean isString() {
        return getValueType() == ValueType.STRING;
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return getValueType() == ValueType.DATETIME;
    }

    enum ValueType {
        OBJECT(Object.class, false, false),
        BOOLEAN(Boolean.class, true, false),
        LONG(Long.class, true, true),
        DOUBLE(Double.class, true, false),
        STRING(String.class, true, true),
        DATETIME(LocalDateTime.class, true, true);

        private final Class<?> valueClass;
        private final boolean isWritable;
        private final boolean isKeyable;

        ValueType(Class<?> valueClass, boolean isWritable, boolean isKeyable) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
        }

        @CheckReturnValue
        public static ValueType of(com.vaticle.typedb.client.jni.ValueType valueType) {
            switch (valueType) {
                case Boolean: return BOOLEAN;
                case Long: return LONG;
                case Double: return DOUBLE;
                case String: return STRING;
                case DateTime: return DATETIME;
                case Object: return OBJECT;
            }
            throw new TypeDBClientException(BAD_ENUM_VALUE);
        }

        @CheckReturnValue
        public com.vaticle.typedb.client.jni.ValueType asJNI(){
            switch (this) {
                case BOOLEAN: return com.vaticle.typedb.client.jni.ValueType.Boolean;
                case LONG: return com.vaticle.typedb.client.jni.ValueType.Long;
                case DOUBLE: return com.vaticle.typedb.client.jni.ValueType.Double;
                case STRING: return com.vaticle.typedb.client.jni.ValueType.String;
                case DATETIME: return com.vaticle.typedb.client.jni.ValueType.DateTime;
                case OBJECT: return com.vaticle.typedb.client.jni.ValueType.Object;
            }
            throw new TypeDBClientException(BAD_ENUM_VALUE);
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

    void setSupertype(TypeDBTransaction transaction, AttributeType attributeType);

    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, ValueType valueType);

    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypesExplicit(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstances(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstancesExplicit(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations);

    @CheckReturnValue
    Stream<? extends ThingType> getOwnersExplicit(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends ThingType> getOwnersExplicit(TypeDBTransaction transaction, Set<Annotation> annotations);
}
