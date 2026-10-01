/*
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

package com.typedb.driver.api.answer;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.typedb.driver.common.collection.Pair;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_VALUE_RETRIEVAL;
import static com.typedb.driver.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static com.typedb.driver.common.util.Objects.className;
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
            return new JSON.Number(value);
        } else if (value.isBoolean()) {
            return new JSON.Boolean(value.asBoolean());
        } else if (value.isNull()) {
            return new JSON.Null();
        } else {
            throw new TypeDBDriverException(ILLEGAL_STATE);
        }
    }

    private static java.lang.String indent(java.lang.String string) {
        return Arrays.stream(string.split("\n")).map(s -> "    " + s).collect(joining("\n"));
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

    /** Whether this number is written without a fractional part or an exponent. */
    public boolean isInteger() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public Map<java.lang.String, JSON> asObject() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(Map.class));
    }

    public List<JSON> asArray() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(List.class));
    }

    public double asNumber() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(double.class));
    }

    /** The exact value of an integer number, which a double cannot always hold. */
    public long asInteger() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(long.class));
    }

    public java.lang.String asString() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(java.lang.String.class));
    }

    public boolean asBoolean() {
        throw new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(boolean.class));
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

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JSON.Object that = (JSON.Object) obj;
            return this.object.equals(that.object);
        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }

        @Override
        public java.lang.String toString() {
            Map<java.lang.String, JSON> jsonObject = asObject();
            boolean singleLine = jsonObject.containsKey("root") // this is a type
                    || jsonObject.containsKey("value");  // this is a value or an attribute

            List<java.lang.String> orderedKeys = jsonObject.keySet().stream().sorted((s1, s2) -> {
                if (s1.equals("type")) return 1; // type always comes last
                else if (s2.equals("type")) return -1;
                else return s1.compareTo(s2);
            }).collect(Collectors.toList());

            java.lang.String content = orderedKeys.stream().map(key -> {
                StringBuilder sb = new StringBuilder(Json.value(key).toString()).append(":");
                java.lang.String valueString = jsonObject.get(key).toString();
                sb.append(" ").append(valueString);
                return sb.toString();
            }).collect(joining(singleLine ? ", " : ",\n"));

            StringBuilder sb = new StringBuilder("{");
            if (content.lines().count() > 1) sb.append("\n").append(indent(content)).append("\n");
            else sb.append(" ").append(content).append(" ");
            sb.append("}");

            return sb.toString();
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

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JSON.Array that = (JSON.Array) obj;
            return this.array.equals(that.array);
        }

        @Override
        public int hashCode() {
            return array.hashCode();
        }

        @Override
        public java.lang.String toString() {
            java.lang.String content = asArray().stream().map(JSON::toString).collect(joining(",\n"));

            StringBuilder sb = new StringBuilder("[");
            if (content.lines().count() > 1) sb.append("\n").append(indent(content)).append("\n");
            else sb.append(" ").append(content).append(" ");
            sb.append("]");

            return sb.toString();
        }
    }

    private static class Number extends JSON {
        private final java.lang.String literal;
        private final OptionalLong integer;
        private final double doubleValue;

        Number(JsonValue number) {
            this.literal = number.toString();
            this.integer = parseInteger(number);
            this.doubleValue = number.asDouble();
        }

        private static OptionalLong parseInteger(JsonValue number) {
            try {
                return OptionalLong.of(number.asLong());
            } catch (NumberFormatException fractionalOrOutOfRange) {
                return OptionalLong.empty();
            }
        }

        public boolean isNumber() {
            return true;
        }

        @Override
        public boolean isInteger() {
            return integer.isPresent();
        }

        public double asNumber() {
            return doubleValue;
        }

        @Override
        public long asInteger() {
            return integer.orElseThrow(() -> new TypeDBDriverException(INVALID_VALUE_RETRIEVAL, className(long.class)));
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JSON.Number that = (JSON.Number) obj;
            if (this.integer.isPresent() && that.integer.isPresent()) {
                return this.integer.getAsLong() == that.integer.getAsLong();
            }
            return this.asNumber() == that.asNumber();
        }

        @Override
        public int hashCode() {
            return Double.hashCode(asNumber());
        }

        @Override
        public java.lang.String toString() {
            return literal;
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

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JSON.String that = (JSON.String) obj;
            return this.string.equals(that.string);
        }

        @Override
        public int hashCode() {
            return string.hashCode();
        }

        @Override
        public java.lang.String toString() {
            return Json.value(string).toString();
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

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JSON.Boolean that = (JSON.Boolean) obj;
            return this.aBoolean == that.aBoolean;
        }

        @Override
        public int hashCode() {
            return java.lang.Boolean.hashCode(aBoolean);
        }

        @Override
        public java.lang.String toString() {
            return java.lang.Boolean.toString(aBoolean);
        }
    }

    private static class Null extends JSON {

        @Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            return obj != null && getClass() == obj.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public java.lang.String toString() {
            return "null";
        }
    }
}
