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

using TypeDB.Driver.Common;

using ConceptError = TypeDB.Driver.Common.Error.Concept;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// The base interface for all concepts in TypeDB.
    /// </summary>
    public interface IConcept
    {
        /// <summary>
        /// The scale used for decimal values.
        /// </summary>
        const int DecimalScale = 19;

        #region Type Checking - Concept Types

        /// <summary>
        /// Checks if the concept is a <see cref="IType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsType()
        /// </code>
        /// </example>
        bool IsType() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IThingType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsThingType()
        /// </code>
        /// </example>
        bool IsThingType() => false;

        /// <summary>
        /// Checks if the concept is an <see cref="IEntityType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsEntityType()
        /// </code>
        /// </example>
        bool IsEntityType() => false;

        /// <summary>
        /// Checks if the concept is an <see cref="IAttributeType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsAttributeType()
        /// </code>
        /// </example>
        bool IsAttributeType() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IRelationType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsRelationType()
        /// </code>
        /// </example>
        bool IsRelationType() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IRoleType"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsRoleType()
        /// </code>
        /// </example>
        bool IsRoleType() => false;

        /// <summary>
        /// Checks if the concept is an instance (entity, relation, or attribute).
        /// This is the 3.0 name for what was previously called "Thing".
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsInstance()
        /// </code>
        /// </example>
        bool IsInstance() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IThing"/>.
        /// Note: In 3.0, this is also known as IsInstance().
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsThing()
        /// </code>
        /// </example>
        bool IsThing() => IsInstance();

        /// <summary>
        /// Checks if the concept is an <see cref="IEntity"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsEntity()
        /// </code>
        /// </example>
        bool IsEntity() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IRelation"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsRelation()
        /// </code>
        /// </example>
        bool IsRelation() => false;

        /// <summary>
        /// Checks if the concept is an <see cref="IAttribute"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsAttribute()
        /// </code>
        /// </example>
        bool IsAttribute() => false;

        /// <summary>
        /// Checks if the concept is a <see cref="IValue"/>.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsValue()
        /// </code>
        /// </example>
        bool IsValue() => false;

        #endregion

        #region Type Checking - Value Types

        /// <summary>
        /// Returns true if the value which this Concept holds is of type boolean
        /// or if this Concept is an AttributeType of type boolean.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsBoolean()
        /// </code>
        /// </example>
        bool IsBoolean();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type integer
        /// or if this Concept is an AttributeType of type integer.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsInteger()
        /// </code>
        /// </example>
        bool IsInteger();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type double
        /// or if this Concept is an AttributeType of type double.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDouble()
        /// </code>
        /// </example>
        bool IsDouble();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type decimal
        /// or if this Concept is an AttributeType of type decimal.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDecimal()
        /// </code>
        /// </example>
        bool IsDecimal();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type string
        /// or if this Concept is an AttributeType of type string.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsString()
        /// </code>
        /// </example>
        bool IsString();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type date
        /// or if this Concept is an AttributeType of type date.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDate()
        /// </code>
        /// </example>
        bool IsDate();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type datetime
        /// or if this Concept is an AttributeType of type datetime.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDatetime()
        /// </code>
        /// </example>
        bool IsDatetime();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type datetime-tz
        /// or if this Concept is an AttributeType of type datetime-tz.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDatetimeTZ()
        /// </code>
        /// </example>
        bool IsDatetimeTZ();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type duration
        /// or if this Concept is an AttributeType of type duration.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsDuration()
        /// </code>
        /// </example>
        bool IsDuration();

        /// <summary>
        /// Returns true if the value which this Concept holds is of type struct
        /// or if this Concept is an AttributeType of type struct.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.IsStruct()
        /// </code>
        /// </example>
        bool IsStruct();

        #endregion

        #region Concept Casting

        /// <summary>
        /// Casts the concept to <see cref="IType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a type.</exception>
        /// <example>
        /// <code>
        /// concept.AsType()
        /// </code>
        /// </example>
        IType AsType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IThingType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a thing type.</exception>
        /// <example>
        /// <code>
        /// concept.AsThingType()
        /// </code>
        /// </example>
        IThingType AsThingType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IThingType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IEntityType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not an entity type.</exception>
        /// <example>
        /// <code>
        /// concept.AsEntityType()
        /// </code>
        /// </example>
        IEntityType AsEntityType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IEntityType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IRelationType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a relation type.</exception>
        /// <example>
        /// <code>
        /// concept.AsRelationType()
        /// </code>
        /// </example>
        IRelationType AsRelationType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IRelationType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IAttributeType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not an attribute type.</exception>
        /// <example>
        /// <code>
        /// concept.AsAttributeType()
        /// </code>
        /// </example>
        IAttributeType AsAttributeType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IAttributeType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IRoleType"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a role type.</exception>
        /// <example>
        /// <code>
        /// concept.AsRoleType()
        /// </code>
        /// </example>
        IRoleType AsRoleType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IRoleType));
        }

        /// <summary>
        /// Casts the concept to <see cref="IThing"/> (instance).
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a thing/instance.</exception>
        /// <example>
        /// <code>
        /// concept.AsThing()
        /// </code>
        /// </example>
        IThing AsThing()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IThing));
        }

        /// <summary>
        /// Casts the concept to <see cref="IEntity"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not an entity.</exception>
        /// <example>
        /// <code>
        /// concept.AsEntity()
        /// </code>
        /// </example>
        IEntity AsEntity()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IEntity));
        }

        /// <summary>
        /// Casts the concept to <see cref="IRelation"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a relation.</exception>
        /// <example>
        /// <code>
        /// concept.AsRelation()
        /// </code>
        /// </example>
        IRelation AsRelation()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IRelation));
        }

        /// <summary>
        /// Casts the concept to <see cref="IAttribute"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not an attribute.</exception>
        /// <example>
        /// <code>
        /// concept.AsAttribute()
        /// </code>
        /// </example>
        IAttribute AsAttribute()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IAttribute));
        }

        /// <summary>
        /// Casts the concept to <see cref="IValue"/>.
        /// </summary>
        /// <exception cref="TypeDBDriverException">If the concept is not a value.</exception>
        /// <example>
        /// <code>
        /// concept.AsValue()
        /// </code>
        /// </example>
        IValue AsValue()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, GetType().Name, nameof(IValue));
        }

        #endregion

        #region Value Accessors

        /// <summary>
        /// Returns a boolean value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetBoolean()
        /// </code>
        /// </example>
        bool? TryGetBoolean();

        /// <summary>
        /// Returns an integer value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetInteger()
        /// </code>
        /// </example>
        long? TryGetInteger();

        /// <summary>
        /// Returns a double value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDouble()
        /// </code>
        /// </example>
        double? TryGetDouble();

        /// <summary>
        /// Returns a decimal value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDecimal()
        /// </code>
        /// </example>
        decimal? TryGetDecimal();

        /// <summary>
        /// Returns a string value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetString()
        /// </code>
        /// </example>
        string? TryGetString();

        /// <summary>
        /// Returns a date value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDate()
        /// </code>
        /// </example>
        DateOnly? TryGetDate();

        /// <summary>
        /// Returns a datetime value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDatetime()
        /// </code>
        /// </example>
        DateTime? TryGetDatetime();

        /// <summary>
        /// Returns a datetime-tz value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDatetimeTZ()
        /// </code>
        /// </example>
        DateTimeOffset? TryGetDatetimeTZ();

        /// <summary>
        /// Returns a duration value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetDuration()
        /// </code>
        /// </example>
        Duration? TryGetDuration();

        /// <summary>
        /// Returns a struct value of this Concept.
        /// If it's not a Value or it has another type, returns null.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetStruct()
        /// </code>
        /// </example>
        IReadOnlyDictionary<string, IValue?>? TryGetStruct();

        #endregion

        #region Metadata Accessors

        /// <summary>
        /// Retrieves the unique label of the concept.
        /// If this is an Instance, returns the label of the type of this instance ("unknown" if type fetching is disabled).
        /// If this is a Value, returns the label of the value type.
        /// If this is a Type, returns the label of the type.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.GetLabel()
        /// </code>
        /// </example>
        string GetLabel();

        /// <summary>
        /// Retrieves the unique label of the concept, or null if type fetching is disabled for instances.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetLabel()
        /// </code>
        /// </example>
        string? TryGetLabel();

        /// <summary>
        /// Retrieves the unique id (IID) of the Concept. Returns null if absent.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetIID()
        /// </code>
        /// </example>
        string? TryGetIID();

        /// <summary>
        /// Retrieves the string describing the value type of this Concept.
        /// Returns null if absent.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetValueType()
        /// </code>
        /// </example>
        string? TryGetValueType();

        /// <summary>
        /// Retrieves the value which this Concept holds.
        /// Returns null if this Concept does not hold any value.
        /// </summary>
        /// <example>
        /// <code>
        /// concept.TryGetValue()
        /// </code>
        /// </example>
        IValue? TryGetValue();

        #endregion

        /// <summary>
        /// Used to specify whether we need explicit or transitive subtyping, instances, etc.
        /// </summary>
        /// <example>
        /// <code>
        /// attributeType.GetOwners(transaction, annotation, Transitivity.Explicit)
        /// </code>
        /// </example>
        public enum Transitivity
        {
            Transitive = 0,
            Explicit = 1,
        }
    }
}
