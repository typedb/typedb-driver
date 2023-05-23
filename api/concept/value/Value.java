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
import com.vaticle.typedb.client.api.concept.type.Type;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.client.api.concept.thing.Attribute.ISO_LOCAL_DATE_TIME_MILLIS;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;

public interface Value<VALUE> extends Concept {

    @CheckReturnValue
    Type.ValueType getValueType();

    @CheckReturnValue
    VALUE getValue();

    @Override
    @CheckReturnValue
    default boolean isValue() {
        return true;
    }

    @CheckReturnValue
    default boolean isBoolean() {
        return false;
    }

    @CheckReturnValue
    default boolean isLong() {
        return false;
    }

    @CheckReturnValue
    default boolean isDouble() {
        return false;
    }

    @CheckReturnValue
    default boolean isString() {
        return false;
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return false;
    }

    @CheckReturnValue
    Value<java.lang.Boolean> asBoolean();

    @CheckReturnValue
    Value<java.lang.Long> asLong();

    @CheckReturnValue
    Value<java.lang.Double> asDouble();

    @CheckReturnValue
    Value<java.lang.String> asString();

    @CheckReturnValue
    Value<java.time.LocalDateTime> asDateTime();

    @Override
    default JsonObject toJSON() {
        JsonValue value;
        switch (getValueType()) {
            case BOOLEAN:
                value = Json.value(asBoolean().getValue());
                break;
            case LONG:
                value = Json.value(asLong().getValue());
                break;
            case DOUBLE:
                value = Json.value(asDouble().getValue());
                break;
            case STRING:
                value = Json.value(asString().getValue());
                break;
            case DATETIME:
                value = Json.value(asDateTime().getValue().format(ISO_LOCAL_DATE_TIME_MILLIS));
                break;
            default:
                throw new TypeDBClientException(ILLEGAL_STATE);
        }
        return Json.object()
                .add("value_type", getValueType().name().toLowerCase())
                .add("value", value);
    }

    interface Boolean extends Value<java.lang.Boolean> {
        @Override
        @CheckReturnValue
        default boolean isBoolean() {
            return true;
        }
    }

    interface Long extends Value<java.lang.Long> {
        @Override
        @CheckReturnValue
        default boolean isLong() {
            return true;
        }
    }

    interface Double extends Value<java.lang.Double> {
        @Override
        @CheckReturnValue
        default boolean isDouble() {
            return true;
        }
    }

    interface String extends Value<java.lang.String> {
        @Override
        @CheckReturnValue
        default boolean isString() {
            return true;
        }
    }

    interface DateTime extends Value<java.time.LocalDateTime> {
        @Override
        @CheckReturnValue
        default boolean isDateTime() {
            return true;
        }
    }
}
