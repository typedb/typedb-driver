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

using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common.Exception;
using InternalError = Vaticle.Typedb.Driver.Common.Exception.Error.Internal;

namespace Vaticle.Typedb.Driver.Api.Concept.Value
{
    public interface IValue : IConcept
    {
        /**
         * {@inheritDoc}
         */
        override bool IsValue
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        override IValue AsValue()
        {
            return true;
        }

        /**
         * Retrieves the <code>IValue.Type</code> of this value concept.
         *
         * <h3>Examples</h3>
         * <pre>
         * value.Type
         * </pre>
         */
        IType Type { get; }

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
         * value.IsSystem.DateTime();
         * </pre>
         */
        bool IsSystem.DateTime();

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
         * value.AsSystem.DateTime();
         * </pre>
         */
        System.DateTime AsSystem.DateTime();

        public class Type
        {
            public static Type OBJECT = new Type(object.GetType(), false, false, Pinvoke.ValueType.Object);
            public static Type BOOLEAN = new Type(bool.GetType(), true, false, Pinvoke.ValueType.Boolean);
            public static Type LONG = new Type(long.GetType(), true, true, Pinvoke.ValueType.Long);
            public static Type DOUBLE = new Type(double.GetType(), true, false, Pinvoke.ValueType.Double);
            public static Type STRING = new Type(string.GetType(), true, true, Pinvoke.ValueType.String);
            public static Type DATETIME = new Type(System.DateTime.GetType(), true, true, Pinvoke.ValueType.DateTime);

            public System.Type ValueClass { get; }
            public bool IsWritable { get; }
            public bool IsKeyable { get; }
            public Pinvoke.ValueType NativeObject { get; }

            private Type(
                System.Type valueClass,
                bool isWritable,
                bool isKeyable,
                Pinvoke.ValueType nativeObject)
            {
                ValueClass = valueClass;
                IsWritable = isWritable;
                IsKeyable = isKeyable;
                NativeObject = nativeObject;
            }

            // TODO: "of" method?
        }
    }
}
