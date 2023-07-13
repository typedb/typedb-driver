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

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

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

    Attribute put(TypeDBTransaction transaction, String value);

    Attribute put(TypeDBTransaction transaction, long value);

    Attribute put(TypeDBTransaction transaction, double value);

    Attribute put(TypeDBTransaction transaction, boolean value);

    Attribute put(TypeDBTransaction transaction, LocalDateTime value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, Value value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, String value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, long value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, double value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, boolean value);

    @Nullable
    Attribute get(TypeDBTransaction transaction, LocalDateTime value);

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

        ValueType(Class<?> valueClass, boolean isWritable, boolean isKeyable, com.vaticle.typedb.client.jni.ValueType nativeObject) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
            this.nativeObject = nativeObject;
        }

        @CheckReturnValue
        public static ValueType of(com.vaticle.typedb.client.jni.ValueType valueType) {
            for (ValueType type : ValueType.values()) {
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
