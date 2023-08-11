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
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.api.concept.thing.Attribute;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

public interface AttributeType extends ThingType {
    @CheckReturnValue
    Value.Type getValueType();

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
        return getValueType() == Value.Type.BOOLEAN;
    }

    @CheckReturnValue
    default boolean isLong() {
        return getValueType() == Value.Type.LONG;
    }

    @CheckReturnValue
    default boolean isDouble() {
        return getValueType() == Value.Type.DOUBLE;
    }

    @CheckReturnValue
    default boolean isString() {
        return getValueType() == Value.Type.STRING;
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return getValueType() == Value.Type.DATETIME;
    }

    void setSupertype(TypeDBTransaction transaction, AttributeType attributeType);

    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Value.Type valueType);

    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity);

    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstances(TypeDBTransaction transaction);

    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Transitivity transitivity);

    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations, Transitivity transitivity);
}
