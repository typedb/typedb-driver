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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.vaticle.typedb.common.util.Objects.className;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static java.util.stream.Collectors.joining;

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

    public double asNumber() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(double.class));
    }

    public java.lang.String asString() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(java.lang.String.class));
    }

    public boolean asBoolean() {
        throw new TypeDBDriverException(ILLEGAL_CAST, className(boolean.class));
    }

    @Override
    public java.lang.String toString() {
        if (isBoolean()) return java.lang.Boolean.toString(asBoolean());
        else if (isNumber()) return Double.toString(asNumber());
        else if (isString()) return '"' + asString() + '"';
        else if (isArray()) {
            java.lang.String content = asArray().stream().map(JSON::toString).collect(joining(",\n"));

            return "[\n" + Arrays.stream(content.split("\n")).map(s -> "    " + s).collect(joining("\n")) + "\n]";
        } else if (isObject()) {
            java.lang.String content = asObject().keySet().stream().map(key -> {
                StringBuilder sb = new StringBuilder("\"").append(key).append("\":");
                var valueString = asObject().get(key).toString();
                sb.append(" ").append(valueString);
                return sb.toString();
            }).collect(joining(",\n"));
            return "{\n" + Arrays.stream(content.split("\n")).map(s -> "    " + s).collect(joining("\n")) + "\n}";
        } else throw new TypeDBDriverException(ILLEGAL_STATE);
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JSON that = (JSON)obj;
        if (isBoolean() && that.isBoolean()) return asBoolean() == that.asBoolean();
        else if (isNumber() && that.isNumber()) return asNumber() == that.asNumber();
        else if (isString() && that.isString()) return asString().equals(that.asString());
        else if (isArray() && that.isArray()) return asArray().equals(that.asArray());
        else if (isObject() && that.isObject()) return asObject().equals(that.asObject());
        else return false;
    }

    @Override
    public int hashCode() {
        if (isBoolean()) return java.lang.Boolean.hashCode(asBoolean());
        else if (isNumber()) return Double.hashCode(asNumber());
        else if (isString()) return asString().hashCode();
        else if (isArray()) return asArray().hashCode();
        else if (isObject()) return asObject().hashCode();
        else throw new TypeDBDriverException(ILLEGAL_STATE);
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
        private final double number;

        Number(double number) {
            this.number = number;
        }

        public boolean isNumber() {
            return true;
        }

        public double asNumber() {
            return number;
        }
    }

    private static class String extends JSON {
        private final java.lang.String string;

        String(java.lang.String string) {
            this.string = string;
        }

        public boolean isString() {
            return true;
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
