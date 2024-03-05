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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;
using InternalError = Vaticle.Typedb.Driver.Common.Error.Internal;

namespace Vaticle.Typedb.Driver.Api
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
         * value.Type
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
         * If the value has another type, raises an exception.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.AsDateTime();
         * </pre>
         */
        System.DateTime AsDateTime();

        public class ValueType : NativeObjectWrapper<Pinvoke.ValueType>
        {
            public static readonly ValueType OBJECT = new ValueType(typeof(object), false, false, Pinvoke.ValueType.Object);
            public static readonly ValueType BOOL = new ValueType(typeof(bool), true, false, Pinvoke.ValueType.Boolean);
            public static readonly ValueType LONG = new ValueType(typeof(long), true, true, Pinvoke.ValueType.Long);
            public static readonly ValueType DOUBLE = new ValueType(typeof(double), true, false, Pinvoke.ValueType.Double);
            public static readonly ValueType STRING = new ValueType(typeof(string), true, true, Pinvoke.ValueType.String);
            public static readonly ValueType DATETIME = new ValueType(typeof(System.DateTime), true, true, Pinvoke.ValueType.DateTime);

            public System.Type ValueClass { get; }
            public bool IsWritable { get; }
            public bool IsKeyable { get; }

            public ValueType(Pinvoke.ValueType nativeObject)
                : base(nativeObject)
            {
                foreach (var type in ALL_VALUE_TYPES)
                {
                    if (type.NativeObject == nativeObject)
                    {
                        ValueClass = type.ValueClass;
                        IsWritable = type.IsWritable;
                        IsKeyable = type.IsKeyable;

                        return;
                    }
                }

                throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
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

            private static readonly ValueType[] ALL_VALUE_TYPES =
            {
                OBJECT,
                BOOL,
                LONG,
                DOUBLE,
                STRING,
                DATETIME,
            };

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
        }
    }
}
