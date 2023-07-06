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

package com.vaticle.typedb.client.api.concept.thing;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.Value;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;

public interface Attribute extends Thing {
    DateTimeFormatter ISO_LOCAL_DATE_TIME_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    @CheckReturnValue
    AttributeType getType();

    @Override
    @CheckReturnValue
    default boolean isAttribute() {
        return true;
    }

    @Override
    @CheckReturnValue
    default Attribute asAttribute() {
        return this;
    }

    @CheckReturnValue
    default boolean isBoolean() {
        return getValue().isBoolean();
    }

    @CheckReturnValue
    default boolean isLong() {
        return getValue().isLong();
    }

    @CheckReturnValue
    default boolean isDouble() {
        return getValue().isDouble();
    }

    @CheckReturnValue
    default boolean isString() {
        return getValue().isString();
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return getValue().isDateTime();
    }

    Value getValue();

    @Override
    default JsonObject toJSON() {
        JsonValue value;
        switch (getType().getValueType()) {
            case BOOLEAN: value = Json.value(getValue().asBoolean()); break;
            case LONG: value = Json.value(getValue().asLong()); break;
            case DOUBLE: value = Json.value(getValue().asDouble()); break;
            case STRING: value = Json.value(getValue().asString()); break;
            case DATETIME: value = Json.value(getValue().asDateTime().format(ISO_LOCAL_DATE_TIME_MILLIS)); break;
            default: throw new TypeDBClientException(ILLEGAL_STATE);
        }
        return Json.object()
                .add("type", getType().getLabel().scopedName())
                .add("value_type", getType().getValueType().name().toLowerCase())
                .add("value", value);
    }

    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction);

    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction, ThingType ownerType);
}
