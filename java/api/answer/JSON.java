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

package com.vaticle.typedb.driver.api.answer;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.vaticle.typedb.common.util.Objects.className;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;

public abstract class JSON {
    public static JSON parse(java.lang.String string) {
        return of(Json.parse(string));
    }

    private static JSON of(JsonValue value) {
        if (value.isObject()) {
            return new JSON.Object(StreamSupport.stream(value.asObject().spliterator(), false)
                    .map(member -> new Pair<>(member.getName(), JSON.of(member.getValue()))).collect(Collectors.toMap(Pair::first, Pair::second)));
        } else if (value.isArray()) {
            return new JSON.Array(value.asArray().values().stream().map(JSON::of).collect(Collectors.toList()));
        } else if (value.isString()) {
            return new JSON.String(value.asString());
        } else if (value.isNumber()) {
            return new JSON.Number(value.asFloat());
        } else if (value.isBoolean()) {
            return new JSON.Boolean(value.asBoolean());
        } else {
            throw new TypeDBDriverException(ILLEGAL_STATE);
        }
    }

    public boolean isObject() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public Map<java.lang.String, JSON> asObject() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(Map.class));
    }

    public List<JSON> asArray() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(List.class));
    }

    public float asNumber() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(float.class));
    }

    public java.lang.String asString() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(java.lang.String.class));
    }

    public boolean asBoolean() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(boolean.class));
    }

    private static class Object extends JSON {
        private final Map<java.lang.String, JSON> object;

        Object(Map<java.lang.String, JSON> object) {
            this.object = object;
        }

        public boolean isObject() {
            return true;
        }

        @Override
        public Map<java.lang.String, JSON> asObject() {
            return object;
        }
    }

    private static class Array extends JSON {
        private final List<JSON> array;

        Array(List<JSON> array) {
            this.array = array;
        }

        public boolean isArray() {
            return true;
        }

        public List<JSON> asArray() {
            return array;
        }
    }

    private static class Number extends JSON {
        private final float number;

        Number(float number) {
            this.number = number;
        }

        public boolean isNumber() {
            return true;
        }

        public float asNumber() {
            return number;
        }
    }

    private static class String extends JSON {
        private final java.lang.String string;

        String(java.lang.String string) {
            this.string = string;
        }

        public boolean isString() {
            return false;
        }

        public java.lang.String asString() {
            return string;
        }
    }

    private static class Boolean extends JSON {
        private final boolean aBoolean;

        Boolean(boolean aBoolean) {
            this.aBoolean = aBoolean;
        }

        public boolean isBoolean() {
            return true;
        }

        public boolean asBoolean() {
            return aBoolean;
        }
    }
}
