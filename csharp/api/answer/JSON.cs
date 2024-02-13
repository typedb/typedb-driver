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

// TODO: Implement!
namespace Vaticle.Typedb.Driver.Api.Answer
{
//    public abstract class JSON {
//        public static JSON parse(java.lang.String string) {
//            return of(Json.parse(string));
//        }
//
//        private static JSON of(JsonValue value) {
//            if (value.isObject()) {
//                return new JSON.Object(StreamSupport.stream(value.asObject().spliterator(), false)
//                        .map(member -> new Pair<>(member.getName(), JSON.of(member.getValue()))).collect(Collectors.toMap(Pair::first, Pair::second)));
//            } else if (value.isArray()) {
//                return new JSON.Array(value.asArray().values().stream().map(JSON::of).collect(Collectors.toList()));
//            } else if (value.isString()) {
//                return new JSON.String(value.asString());
//            } else if (value.isNumber()) {
//                return new JSON.Number(value.asFloat());
//            } else if (value.isbool()) {
//                return new JSON.bool(value.asbool());
//            } else if (value.isNull()) {
//                return new JSON.Null();
//            } else {
//                throw new TypeDBDriverException(ILLEGAL_STATE);
//            }
//        }
//
//        public bool isObject() {
//            return false;
//        }
//
//        public bool isArray() {
//            return false;
//        }
//
//        public bool isNumber() {
//            return false;
//        }
//
//        public bool isString() {
//            return false;
//        }
//
//        public bool isbool() {
//            return false;
//        }
//
//        public Map<java.lang.String, JSON> asObject() {
//            throw new TypeDBDriverException(ILLEGAL_CAST, className(Map.class));
//        }
//
//        public List<JSON> asArray() {
//            throw new TypeDBDriverException(ILLEGAL_CAST, className(List.class));
//        }
//
//        public double asNumber() {
//            throw new TypeDBDriverException(ILLEGAL_CAST, className(double.class));
//        }
//
//        public java.lang.String asString() {
//            throw new TypeDBDriverException(ILLEGAL_CAST, className(java.lang.String.class));
//        }
//
//        public bool asbool() {
//            throw new TypeDBDriverException(ILLEGAL_CAST, className(bool.class));
//        }
//
//        private static java.lang.String indent(java.lang.String string) {
//            return Arrays.stream(string.split("\n")).map(s -> "    " + s).collect(joining("\n"));
//        }
//
//        private static class Object extends JSON {
//            private sealed Map<java.lang.String, JSON> object;
//
//            Object(Map<java.lang.String, JSON> object) {
//                this.object = object;
//            }
//
//            public bool isObject() {
//                return true;
//            }
//
//            @Override
//            public Map<java.lang.String, JSON> asObject() {
//                return object;
//            }
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                if (obj == null || getClass() != obj.getClass()) return false;
//                JSON.Object that = (JSON.Object) obj;
//                return this.object.equals(that.object);
//            }
//
//            @Override
//            public int hashCode() {
//                return object.hashCode();
//            }
//
//            @Override
//            public java.lang.String toString() {
//                Map<java.lang.String, JSON> jsonObject = asObject();
//                bool singleLine = jsonObject.containsKey("root") // this is a type
//                        || jsonObject.containsKey("value");  // this is a value or an attribute
//
//                List<java.lang.String> orderedKeys = jsonObject.keySet().stream().sorted((s1, s2) -> {
//                    if (s1.equals("type")) return 1; // type always comes last
//                    else if (s2.equals("type")) return -1;
//                    else return s1.compareTo(s2);
//                }).collect(Collectors.toList());
//
//                java.lang.String content = orderedKeys.stream().map(key -> {
//                    StringBuilder sb = new StringBuilder("\"").append(key).append("\":");
//                    java.lang.String valueString = jsonObject.get(key).toString();
//                    sb.append(" ").append(valueString);
//                    return sb.toString();
//                }).collect(joining(singleLine ? ", " : ",\n"));
//
//                StringBuilder sb = new StringBuilder("{");
//                if (content.lines().count() > 1) sb.append("\n").append(indent(content)).append("\n");
//                else sb.append(" ").append(content).append(" ");
//                sb.append("}");
//
//                return sb.toString();
//            }
//        }
//
//        private static class Array extends JSON {
//            private sealed List<JSON> array;
//
//            Array(List<JSON> array) {
//                this.array = array;
//            }
//
//            public bool isArray() {
//                return true;
//            }
//
//            public List<JSON> asArray() {
//                return array;
//            }
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                if (obj == null || getClass() != obj.getClass()) return false;
//                JSON.Array that = (JSON.Array) obj;
//                return this.array.equals(that.array);
//            }
//
//            @Override
//            public int hashCode() {
//                return array.hashCode();
//            }
//
//            @Override
//            public java.lang.String toString() {
//                java.lang.String content = asArray().stream().map(JSON::toString).collect(joining(",\n"));
//
//                StringBuilder sb = new StringBuilder("[");
//                if (content.lines().count() > 1) sb.append("\n").append(indent(content)).append("\n");
//                else sb.append(" ").append(content).append(" ");
//                sb.append("]");
//
//                return sb.toString();
//            }
//        }
//
//        private static class Number extends JSON {
//            private sealed double number;
//
//            Number(double number) {
//                this.number = number;
//            }
//
//            public bool isNumber() {
//                return true;
//            }
//
//            public double asNumber() {
//                return number;
//            }
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                if (obj == null || getClass() != obj.getClass()) return false;
//                JSON.Number that = (JSON.Number) obj;
//                return this.number == that.number;
//            }
//
//            @Override
//            public int hashCode() {
//                return Double.hashCode(number);
//            }
//
//            @Override
//            public java.lang.String toString() {
//                long integerPart = (long) number;
//                if ((double) integerPart == number) {
//                    return Long.toString(integerPart);
//                } else {
//                    return Double.toString(number);
//                }
//            }
//        }
//
//        private static class String extends JSON {
//            private sealed java.lang.String string;
//
//            String(java.lang.String string) {
//                this.string = string;
//            }
//
//            public bool isString() {
//                return true;
//            }
//
//            public java.lang.String asString() {
//                return string;
//            }
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                if (obj == null || getClass() != obj.getClass()) return false;
//                JSON.String that = (JSON.String) obj;
//                return this.string.equals(that.string);
//            }
//
//            @Override
//            public int hashCode() {
//                return string.hashCode();
//            }
//
//            @Override
//            public java.lang.String toString() {
//                return '"' + string + '"';
//            }
//        }
//
//        private static class bool extends JSON {
//            private sealed bool abool;
//
//            bool(bool abool) {
//                this.abool = abool;
//            }
//
//            public bool isbool() {
//                return true;
//            }
//
//            public bool asbool() {
//                return abool;
//            }
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                if (obj == null || getClass() != obj.getClass()) return false;
//                JSON.bool that = (JSON.bool) obj;
//                return this.abool == that.abool;
//            }
//
//            @Override
//            public int hashCode() {
//                return java.lang.bool.hashCode(abool);
//            }
//
//            @Override
//            public java.lang.String toString() {
//                return java.lang.bool.toString(abool);
//            }
//        }
//
//        private static class Null extends JSON {
//
//            @Override
//            public bool equals(java.lang.Object obj) {
//                if (obj == this) return true;
//                return obj != null && getClass() == obj.getClass();
//            }
//
//            @Override
//            public int hashCode() {
//                return 0;
//            }
//
//            @Override
//            public java.lang.String toString() {
//                return "null";
//            }
//        }
//    }
}
