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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;

using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

using InternalError = TypeDB.Driver.Common.Error.Internal;
using ConceptError = TypeDB.Driver.Common.Error.Concept;

namespace TypeDB.Driver.Answer
{
    /// <summary>
    /// Represents a JSON value returned from a fetch query.
    /// </summary>
    public abstract class JSON : IJSON
    {
        /// <summary>
        /// Parses a JSON string into a JSON object.
        /// </summary>
        /// <param name="jsonString">The JSON string to parse.</param>
        /// <returns>The parsed JSON object.</returns>
        public static JSON Parse(string jsonString)
        {
            using JsonDocument doc = JsonDocument.Parse(jsonString);
            return Of(doc.RootElement.Clone());
        }

        private static JSON Of(JsonElement element)
        {
            return element.ValueKind switch
            {
                JsonValueKind.Object => new JSONObject(
                    element.EnumerateObject()
                        .ToDictionary(p => p.Name, p => Of(p.Value))),
                JsonValueKind.Array => new JSONArray(
                    element.EnumerateArray()
                        .Select(Of)
                        .ToList()),
                JsonValueKind.String => new JSONString(element.GetString()!),
                JsonValueKind.Number => new JSONNumber(element.GetDouble()),
                JsonValueKind.True => new JSONBoolean(true),
                JsonValueKind.False => new JSONBoolean(false),
                JsonValueKind.Null => new JSONNull(),
                _ => throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, element.ValueKind)
            };
        }

        // Base class implementations of IJSON members - can be overridden by subclasses
        public virtual bool IsObject => false;
        public virtual bool IsArray => false;
        public virtual bool IsNumber => false;
        public virtual bool IsString => false;
        public virtual bool IsBoolean => false;
        public virtual bool IsNull => false;

        public virtual IReadOnlyDictionary<string, IJSON> AsObject()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Object");
        }

        public virtual IReadOnlyList<IJSON> AsArray()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Array");
        }

        public virtual double AsNumber()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Number");
        }

        public virtual string AsString()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "String");
        }

        public virtual bool AsBoolean()
        {
            throw new TypeDBDriverException(ConceptError.INVALID_CONCEPT_CASTING,
                GetType().Name, "Boolean");
        }

        private class JSONObject : JSON
        {
            private readonly Dictionary<string, JSON> _object;

            public JSONObject(Dictionary<string, JSON> obj)
            {
                _object = obj;
            }

            public override bool IsObject => true;

            public override IReadOnlyDictionary<string, IJSON> AsObject() =>
                _object.ToDictionary(kvp => kvp.Key, kvp => (IJSON)kvp.Value);

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                if (obj is not JSONObject other) return false;
                if (_object.Count != other._object.Count) return false;
                return _object.All(kvp =>
                    other._object.TryGetValue(kvp.Key, out var value) &&
                    kvp.Value.Equals(value));
            }

            public override int GetHashCode() => _object.GetHashCode();

            public override string ToString()
            {
                var content = string.Join(", ", _object.Select(kvp => $"\"{kvp.Key}\": {kvp.Value}"));
                return $"{{ {content} }}";
            }
        }

        private class JSONArray : JSON
        {
            private readonly List<JSON> _array;

            public JSONArray(List<JSON> array)
            {
                _array = array;
            }

            public override bool IsArray => true;

            public override IReadOnlyList<IJSON> AsArray() => _array.Cast<IJSON>().ToList();

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                if (obj is not JSONArray other) return false;
                return _array.SequenceEqual(other._array);
            }

            public override int GetHashCode() => _array.GetHashCode();

            public override string ToString()
            {
                var content = string.Join(", ", _array);
                return $"[ {content} ]";
            }
        }

        private class JSONNumber : JSON
        {
            private readonly double _number;

            public JSONNumber(double number)
            {
                _number = number;
            }

            public override bool IsNumber => true;

            public override double AsNumber() => _number;

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                if (obj is not JSONNumber other) return false;
                return _number == other._number;
            }

            public override int GetHashCode() => _number.GetHashCode();

            public override string ToString() => _number.ToString("0.0#####");
        }

        private class JSONString : JSON
        {
            private readonly string _str;

            public JSONString(string str)
            {
                _str = str;
            }

            public override bool IsString => true;

            public override string AsString() => _str;

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                if (obj is not JSONString other) return false;
                return _str == other._str;
            }

            public override int GetHashCode() => _str.GetHashCode();

            public override string ToString() => $"\"{_str}\"";
        }

        private class JSONBoolean : JSON
        {
            private readonly bool _value;

            public JSONBoolean(bool value)
            {
                _value = value;
            }

            public override bool IsBoolean => true;

            public override bool AsBoolean() => _value;

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                if (obj is not JSONBoolean other) return false;
                return _value == other._value;
            }

            public override int GetHashCode() => _value.GetHashCode();

            public override string ToString() => _value.ToString().ToLower();
        }

        private class JSONNull : JSON
        {
            public override bool IsNull => true;

            public override bool Equals(object? obj)
            {
                if (ReferenceEquals(this, obj)) return true;
                return obj is JSONNull;
            }

            public override int GetHashCode() => 0;

            public override string ToString() => "null";
        }
    }
}
