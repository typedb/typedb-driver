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

using System;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;
using TypeDB.Driver.Concept;

using ConceptError = TypeDB.Driver.Common.Error.Concept;
using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    public class Value : Concept, IValue
    {
        private int _hash = 0;

        public Value(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public Value(bool value)
            : this(Pinvoke.typedb_driver.value_new_boolean(value))
        {
        }

        public Value(long value)
            : this(Pinvoke.typedb_driver.value_new_long(value))
        {
        }

        public Value(double value) 
            : this(Pinvoke.typedb_driver.value_new_double(value))
        {
        }

        public Value(string value)
            : this(Pinvoke.typedb_driver.value_new_string(value))
        {
        }

        public Value(System.DateTime value)
            : this(Pinvoke.typedb_driver.value_new_date_time_from_millis(
                new System.DateTimeOffset(
                    System.DateTime.SpecifyKind(value, DateTimeKind.Unspecified)).ToUnixTimeMilliseconds()))
        {
        }

        public IValue.ValueType Type
        {
            get
            {
                if (IsBool()) return IValue.ValueType.Bool;
                if (IsLong()) return IValue.ValueType.Long;
                if (IsDouble()) return IValue.ValueType.Double;
                if (IsString()) return IValue.ValueType.String;
                if (IsDateTime()) return IValue.ValueType.DateTime;
                
                throw new TypeDBDriverException(InternalError.ILLEGAL_STATE);
            }
        }

        public bool IsBool()
        {
            return Pinvoke.typedb_driver.value_is_boolean(NativeObject);
        }

        public bool IsLong() 
        {
            return Pinvoke.typedb_driver.value_is_long(NativeObject);
        }

        public bool IsDouble() 
        {
            return Pinvoke.typedb_driver.value_is_double(NativeObject);
        }

        public bool IsString() 
        {
            return Pinvoke.typedb_driver.value_is_string(NativeObject);
        }

        public bool IsDateTime() 
        {
            return Pinvoke.typedb_driver.value_is_date_time(NativeObject);
        }

        public object AsUntyped() 
        {
            if (IsBool()) return AsBool();
            if (IsLong()) return AsLong();
            if (IsDouble()) return AsDouble();
            if (IsString()) return AsString();
            if (IsDateTime()) return AsDateTime();
            
            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public bool AsBool() 
        {
            Validator.ThrowIfFalse(IsBool, InternalError.ILLEGAL_CAST, "bool");

            return Pinvoke.typedb_driver.value_get_boolean(NativeObject);
        }

        public long AsLong() 
        {
            Validator.ThrowIfFalse(IsLong, InternalError.ILLEGAL_CAST, "long");

            return Pinvoke.typedb_driver.value_get_long(NativeObject);
        }

        public double AsDouble() 
        {
            Validator.ThrowIfFalse(IsDouble, InternalError.ILLEGAL_CAST, "double");

            return Pinvoke.typedb_driver.value_get_double(NativeObject);
        }

        public string AsString() 
        {
            Validator.ThrowIfFalse(IsString, InternalError.ILLEGAL_CAST, "string");

            return Pinvoke.typedb_driver.value_get_string(NativeObject);
        }

        public System.DateTime AsDateTime()
        {
            Validator.ThrowIfFalse(IsDateTime, InternalError.ILLEGAL_CAST, "DateTime");

            return System.DateTimeOffset.FromUnixTimeMilliseconds(
                Pinvoke.typedb_driver.value_get_date_time_as_millis(NativeObject)).DateTime;
        }

        public override string ToString()
        {
            if (IsBool())
            {
                var str = AsBool().ToString();
                return str[0].ToString().ToLower() + str.Substring(1);
            }

            if (IsLong())
            {
                return AsLong().ToString();
            }

            if (IsDouble())
            {
                return AsDouble().ToString("0.0#####");
            }

            if (IsString())
            {
                return AsString();
            }

            if (IsDateTime())
            {
                return AsDateTime().ToString();
            }

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = ComputeHash();
            }

            return _hash;
        }

        private int ComputeHash()
        {
            return AsUntyped().GetHashCode();
        }
    }
}
