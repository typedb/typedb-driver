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

using System.Collections.Generic;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Api.Concept.Type
{
    /**
     * Attribute types represent properties that other types can own.
     * <p>IAttribute types have a value type. This value type is fixed and unique for every given instance
     * of the attribute type.
     * <p>Other types can own an attribute type. That means that instances of these other types can own an instance
     * of this attribute type. This usually means that an object in our domain has a property with the matching value.
     * <p>Multiple types can own the same attribute type, and different instances of the same type or different types
     * can share ownership of the same attribute instance.
     */
    public interface IAttributeType : IThingType
    {
        /**
         * The <code>IValue.ValueType</code> of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.ValueType;
         * </pre>
         */
        IValue.ValueType ValueType { get; }

        /**
         * {@inheritDoc}
         */
        new bool IsAttributeType()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        new IAttributeType AsAttributeType()
        {
            return this;
        }

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, IValue value);

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code>
         * with the given <code>string</code> value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, string value);

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code>
         * with the given <code>long</code> value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, long value);

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code>
         * with the given <code>double</code> value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, double value);

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code>
         * with the given <code>bool</code> value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, bool value);

        /**
         * Adds and returns an <code>IAttribute</code> of this <code>IAttributeType</code>
         * with the given <code>LocalDateTime</code> value.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.put(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value New <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Put(ITypeDBTransaction transaction, System.DateTime value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, IValue value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, string value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, long value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, double value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, bool value);

        /**
         * Retrieves an <code>IAttribute</code> of this <code>IAttributeType</code> with the given value
         * if such <code>IAttribute</code> exists. Otherwise, returns <code>None</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Get(transaction, value).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param value <code>IAttribute</code>’s value
         */
        Promise<IAttribute> Get(ITypeDBTransaction transaction, System.DateTime value);

        /**
         * Retrieves the regular expression that is defined for this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetRegex(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<string> GetRegex(ITypeDBTransaction transaction);

        /**
         * ICollections a regular expression as a constraint for this <code>IAttributeType</code>. <code>Values</code>
         * of all <code>IAttribute</code>s of this type (inserted earlier or later) should match this regex.
         * <p>Can only be applied for <code>IAttributeType</code>s with a <code>string</code> value type.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.SetRegex(transaction, regex).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param regex Regular expression
         */
        VoidPromise SetRegex(ITypeDBTransaction transaction, string regex);

        /**
         * Removes the regular expression that is defined for this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.UnsetRegex(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        VoidPromise UnsetRegex(ITypeDBTransaction transaction);

        /**
         * Returns <code>True</code> if the value for attributes of this type is of type <code>bool</code>.
         * Otherwise, returns <code>False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.IsBoolean();
         * </pre>
         */
        bool IsBoolean()
        {
            return ValueType == IValue.ValueType.BOOLEAN;
        }

        /**
         * Returns <code>True</code> if the value for attributes of this type is of type <code>long</code>.
         * Otherwise, returns <code>False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.IsLong();
         * </pre>
         */
        bool IsLong()
        {
            return ValueType == IValue.ValueType.LONG;
        }

        /**
         * Returns <code>True</code> if the value for attributes of this type is of type <code>double</code>.
         * Otherwise, returns <code>False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.IsDouble();
         * </pre>
         */
        bool IsDouble() 
        {
            return ValueType == IValue.ValueType.DOUBLE;
        }

        /**
         * Returns <code>True</code> if the value for attributes of this type is of type <code>string</code>.
         * Otherwise, returns <code>False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.Isstring();
         * </pre>
         */
        bool Isstring() 
        {
            return ValueType == IValue.ValueType.STRING;
        }

        /**
         * Returns <code>True</code> if the value for attributes of this type is of type <code>datetime</code>.
         * Otherwise, returns <code>False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.IsDateTime();
         * </pre>
         */
        bool IsDateTime()
        {
            return ValueType == IValue.ValueType.DATETIME;
        }

        /**
         * ICollections the supplied <code>IAttributeType</code> as the supertype of the current <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.ICollectionSupertype(transaction, superType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attributeType The <code>IAttributeType</code> to set as the supertype of this <code>IAttributeType</code>
         */
        VoidPromise ICollectionSupertype(ITypeDBTransaction transaction, IAttributeType attributeType);

        /**
         * Retrieves all direct and indirect subtypes of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetSubtypes(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        new ICollection<IAttributeType> GetSubtypes(ITypeDBTransaction transaction);

        /**
         * Retrieves all direct and indirect subtypes of this <code>IAttributeType</code>
         * with given <code>IValue.ValueType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetSubtypes(transaction, valueType);
         * </pre>
         *
         * @param transaction The current transaction
         * @param valueType <code>IValue.ValueType</code> for retrieving subtypes
         */
        ICollection<IAttributeType> GetSubtypes(ITypeDBTransaction transaction, IValue.ValueType valueType);

        /**
         * Retrieves all direct and indirect (or direct only) subtypes of this <code>IAttributeType</code>
         * with given <code>IValue.ValueType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetSubtypes(transaction, valueType, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param valueType <code>IValue.ValueType</code> for retrieving subtypes
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
         *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
         */
        ICollection<IAttributeType> GetSubtypes(
            ITypeDBTransaction transaction, 
            IValue.ValueType valueType,
            IConcept.Transitivity transitivity);

        /**
         * Retrieves all direct and indirect (or direct only) subtypes of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetSubtypes(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
         *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
         */
        new ICollection<IAttributeType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Retrieves all direct and indirect <code>IAttributes</code>
         * that are instances of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetInstances(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        new ICollection<IAttribute> GetInstances(ITypeDBTransaction transaction);

        /**
         * Retrieves all direct and indirect (or direct only) <code>IAttributes</code>
         * that are instances of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetInstances(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
         *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
         */
        new ICollection<IAttribute> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Retrieve all <code>Things</code> that own an attribute of this <code>IAttributeType</code>
         * directly or through inheritance.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetOwners(transaction);
         * </pre>
         *
         * @param transaction The current transaction
         */
        ICollection<IThingType> GetOwners(ITypeDBTransaction transaction);

        /**
         * Retrieve all <code>Things</code> that own an attribute of this <code>IAttributeType</code>,
         * filtered by <code>Annotation</code>s, directly or through inheritance.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetOwners(transaction, annotations);
         * </pre>
         *
         * @param transaction The current transaction
         * @param annotations Only retrieve <code>ThingTypes</code> that have an attribute of this
         *                    <code>IAttributeType</code> with all given <code>Annotation</code>s
         */
        ICollection<IThingType> GetOwners(ITypeDBTransaction transaction, ICollection<Annotation> annotations);

        /**
         * Retrieve all <code>Things</code> that own an attribute of this <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetOwners(transaction, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership,
         *                     <code>Transitivity.EXPLICIT</code> for direct ownership only
         */
        ICollection<IThingType> GetOwners(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Retrieve all <code>Things</code> that own an attribute of this <code>IAttributeType</code>,
         * filtered by <code>Annotation</code>s.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetOwners(transaction, annotations, transitivity);
         * </pre>
         *
         * @param transaction The current transaction
         * @param annotations Only retrieve <code>ThingTypes</code> that have an attribute of this
         *                    <code>IAttributeType</code> with all given <code>Annotation</code>s
         * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership,
         *                     <code>Transitivity.EXPLICIT</code> for direct ownership only
         */
        ICollection<IThingType> GetOwners(
            ITypeDBTransaction transaction, 
            ICollection<Annotation> annotations,
            IConcept.Transitivity transitivity);
    }
}
