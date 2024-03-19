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
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Api
{
    public interface IValue : IConcept
    {
        /**
         * {@inheritDoc}
         */
        bool IConcept.IsValue()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        IValue IConcept.AsValue()
        {
            return this;
        }

        /**
         * Retrieves the <code>IValue.ValueType</code> of this value concept.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.Type;
         * </pre>
         */
        ValueType Type { get; }

        /**
         * Returns <code>True</code> if the value which this value concept holds is of type <code>bool</code>.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.IsBool();
         * </pre>
         */
        bool IsBool();

        /**
         * Returns <code>True</code> if the value which this value concept holds is of type <code>long</code>.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.IsLong();
         * </pre>
         */
        bool IsLong();

        /**
         * Returns <code>True</code> if the value which this value concept holds is of type <code>double</code>.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.IsDouble();
         * </pre>
         */
        bool IsDouble();

        /**
         * Returns <code>True</code> if the value which this value concept holds is of type <code>string</code>.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.IsString();
         * </pre>
         */
        bool IsString();

        /**
         * Returns <code>True</code> if the value which this value concept holds is of type <code>datetime</code>.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.IsDateTime();
         * </pre>
         */
        bool IsDateTime();

        /**
         * Returns an untyped <code>object</code> value of this value concept.
         * This is useful for value equality or printing without having to switch on the actual contained value.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsUntyped();
         * </pre>
         */
        object AsUntyped();

        /**
         * Returns a <code>bool</code> value of this value concept.
         * If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsBool();
         * </pre>
         */
        bool AsBool();

        /**
         * Returns a <code>long</code> value of this value concept. If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsLong();
         * </pre>
         */
        long AsLong();

        /**
         * Returns a <code>double</code> value of this value concept.
         * If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsDouble();
         * </pre>
         */
        double AsDouble();

        /**
         * Returns a <code>string</code> value of this value concept. If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsString();
         * </pre>
         */
        string AsString();

        /**
         * Returns a <code>datetime</code> value of this value concept.
         * This value contains raw date and time without considering your time zone (Kind = Unspecified).
         * If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsDateTime();
         * </pre>
         */
        System.DateTime AsDateTime();

//        enum ValueType
//        {
//            Object = Pinvoke.ValueType.Object,
//            Bool = Pinvoke.ValueType.Boolean,
//            Long = Pinvoke.ValueType.Long,
//            Double = Pinvoke.ValueType.Double,
//            String = Pinvoke.ValueType.String,
//            DateTime = Pinvoke.ValueType.DateTime,
//        }
//
//        namespace ValueTypeExtension
//        {
//            public static class Extensions
//            {
//                public static System.Type GetValueClass(this ValueType valueType)
//                {
//                    switch (valueType)
//                    {
//                        case Object:
//                            return typeof(object);
//                        case Bool:
//                            return typeof(bool);
//                        case Long:
//                            return typeof(long);
//                        case Double:
//                            return typeof(double);
//                        case String:
//                            return typeof(string);
//                        case DateTime:
//                            return typeof(System.DateTime);
//                        default:
//                            throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
//                    }
//                }
//
//                public static bool IsWritable(this ValueType valueType)
//                {
//                    switch (valueType)
//                    {
//                        case Object:
//                            return false;
//                        case Bool:
//                        case Long:
//                        case Double:
//                        case String:
//                        case DateTime:
//                            return true;
//                        default:
//                            throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
//                    }
//                }
//
//                public static bool IsKeyable(this ValueType valueType)
//                {
//                    switch (valueType)
//                    {
//                        case Object:
//                        case Bool:
//                        case Double:
//                            return false;
//                        case Long:
//                        case String:
//                        case DateTime:
//                            return true;
//                        default:
//                            throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
//                    }
//                }
//            }
//        }

        public class ValueType : NativeObjectWrapper<Pinvoke.ValueType>
        {
            public static readonly ValueType OBJECT = new ValueType(typeof(object), false, false, Pinvoke.ValueType.Object);
            public static readonly ValueType BOOL = new ValueType(typeof(bool), true, false, Pinvoke.ValueType.Boolean);
            public static readonly ValueType LONG = new ValueType(typeof(long), true, true, Pinvoke.ValueType.Long);
            public static readonly ValueType DOUBLE = new ValueType(typeof(double), true, false, Pinvoke.ValueType.Double);
            public static readonly ValueType STRING = new ValueType(typeof(string), true, true, Pinvoke.ValueType.String);
            public static readonly ValueType DATETIME = new ValueType(typeof(System.DateTime), true, true, Pinvoke.ValueType.DateTime);

            public readonly System.Type ValueClass;
            public readonly bool IsWritable;
            public readonly bool IsKeyable;

            private int _hash = 0;

            public static ValueType ValueTypeOf(Pinvoke.ValueType nativeObject)
            {
                ValueType? matchingValueType = Values
                    .Where(value => value.NativeObject.Equals(nativeObject))
                    .FirstOrDefault();

                if (matchingValueType == null)
                {
                    throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
                }

                return matchingValueType;
            }

            private ValueType(
                System.Type valueClass,
                bool isWritable,
                bool isKeyable,
                Pinvoke.ValueType nativeObject)
                : base(nativeObject)
            {
                ValueClass = valueClass;
                IsWritable = isWritable;
                IsKeyable = isKeyable;
            }

            public static IEnumerable<ValueType> Values
            {
                get
                {
                    yield return OBJECT;
                    yield return BOOL;
                    yield return LONG;
                    yield return DOUBLE;
                    yield return STRING;
                    yield return DATETIME;
                }
            }

            public override string ToString()
            {
                if (this == OBJECT) return "object";
                if (this == BOOL) return "bool";
                if (this == LONG) return "long";
                if (this == DOUBLE) return "double";
                if (this == STRING) return "string";
                if (this == DATETIME) return "DateTime";
                throw new System.InvalidOperationException("IValue does not correspond to an acceptable ValueType");
            }

            public override bool Equals(object? obj)
            {
                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }

                if (obj == null || this.GetType() != obj.GetType())
                {
                    return false;
                }

                ValueType typeObj = (ValueType)obj;
                return this.ValueClass == typeObj.ValueClass;
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
                return (ValueClass, IsWritable, IsKeyable).GetHashCode();
            }
        }
    }
}
