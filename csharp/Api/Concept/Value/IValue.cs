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

        /**
         * Used to specify the type of the value.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetOwns(transaction, IValue.ValueType.String);
         * </pre>
         */
        public enum ValueType
        {
            Object = Pinvoke.ValueType.Object,
            Bool = Pinvoke.ValueType.Boolean,
            Long = Pinvoke.ValueType.Long,
            Double = Pinvoke.ValueType.Double,
            String = Pinvoke.ValueType.String,
            DateTime = Pinvoke.ValueType.DateTime,
        }
    }

    /**
     * Extension class with additional methods describing characteristics of ValueType enum values. // TODO: Is not generated in doxygen
     */
    public static class ValueTypeExtensions
    {
        /**
         * Returns a <code>System.Type</code> equivalent of this value concept for this programming language.
         *
         * <h3>Examples</h3>
         * <pre>
         * valueType.GetValueClass();
         * </pre>
         */
        public static System.Type GetValueClass(this IValue.ValueType valueType)
        {
            switch (valueType)
            {
                case IValue.ValueType.Object:
                    return typeof(object);
                case IValue.ValueType.Bool:
                    return typeof(bool);
                case IValue.ValueType.Long:
                    return typeof(long);
                case IValue.ValueType.Double:
                    return typeof(double);
                case IValue.ValueType.String:
                    return typeof(string);
                case IValue.ValueType.DateTime:
                    return typeof(System.DateTime);
                default:
                    throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
            }
        }

        /**
         * Returns <code>true</code> if this value concept can be written to a database.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * valueType.IsWritable();
         * </pre>
         */
        public static bool IsWritable(this IValue.ValueType valueType)
        {
            switch (valueType)
            {
                case IValue.ValueType.Object:
                    return false;
                case IValue.ValueType.Bool:
                case IValue.ValueType.Long:
                case IValue.ValueType.Double:
                case IValue.ValueType.String:
                case IValue.ValueType.DateTime:
                    return true;
                default:
                    throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
            }
        }

        /**
         * Returns <code>true</code> if this value concept can be used as a key via the @key annotation.
         * Otherwise, returns <code>false</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * valueType.IsKeyable();
         * </pre>
         */
        public static bool IsKeyable(this ValueType valueType)
        {
            switch (valueType)
            {
                case IValue.ValueType.Object:
                case IValue.ValueType.Bool:
                case IValue.ValueType.Double:
                    return false;
                case IValue.ValueType.Long:
                case IValue.ValueType.String:
                case IValue.ValueType.DateTime:
                    return true;
                default:
                    throw new TypeDBDriverException(InternalError.UNEXPECTED_INTERNAL_VALUE, valueType);
            }
        }
    }
}
