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
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using ConceptError = TypeDB.Driver.Common.Error.Concept;
using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Api
{
    public interface IConcept
    {
        /**
         * Checks if the concept is a <code>IType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsType();
         * </pre>
         */
        bool IsType()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IThingType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsThingType();
         * </pre>
         */
        bool IsThingType()
        {
            return false;
        }

        /**
         * Checks if the concept is an <code>IEntityType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsEntityType();
         * </pre>
         */
        bool IsEntityType()
        {
            return false;
        }

        /**
         * Checks if the concept is an <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsAttributeType();
         * </pre>
         */
        bool IsAttributeType()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IRelationType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsRelationType();
         * </pre>
         */
        bool IsRelationType()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IRoleType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsRoleType();
         * </pre>
         */
        bool IsRoleType()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsThing();
         * </pre>
         */
        bool IsThing()
        {
            return false;
        }

        /**
         * Checks if the concept is an <code>IEntity</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsEntity();
         * </pre>
         */
        bool IsEntity()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsRelation();
         * </pre>
         */
        bool IsRelation()
        {
            return false;
        }

        /**
         * Checks if the concept is an <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsAttribute();
         * </pre>
         */
        bool IsAttribute()
        {
            return false;
        }

        /**
         * Checks if the concept is a <code>IValue</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.IsValue();
         * </pre>
         */
        bool IsValue()
        {
            return false;
        }

        /**
         * Casts the concept to <code>IType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsType();
         * </pre>
         */
        IType AsType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IType).Name);
        }

        /**
         * Casts the concept to <code>IThingType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsThingType();
         * </pre>
         */
        IThingType AsThingType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IThingType).Name);
        }

        /**
         * Casts the concept to <code>IEntityType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsEntityType();
         * </pre>
         */
        IEntityType AsEntityType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IEntityType).Name);
        }

        /**
         * Casts the concept to <code>IRelationType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsRelationType();
         * </pre>
         */
        IRelationType AsRelationType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IRelationType).Name);
        }

        /**
         * Casts the concept to <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsAttributeType();
         * </pre>
         */
        IAttributeType AsAttributeType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IAttributeType).Name);
        }

        /**
         * Casts the concept to <code>IRoleType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsRoleType();
         * </pre>
         */
        IRoleType AsRoleType()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IRoleType).Name);
        }

        /**
         * Casts the concept to <code>IThing</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsThing();
         * </pre>
         */
        IThing AsThing()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IThing).Name);
        }

        /**
         * Casts the concept to <code>IEntity</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsEntity();
         * </pre>
         */
        IEntity AsEntity()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IEntity).Name);
        }

        /**
         * Casts the concept to <code>IRelation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsRelation();
         * </pre>
         */
        IRelation AsRelation()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IRelation).Name);
        }

        /**
         * Casts the concept to <code>IAttribute</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsAttribute();
         * </pre>
         */
        IAttribute AsAttribute()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IAttribute).Name);
        }

        /**
         * Casts the concept to <code>IValue</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * concept.AsValue();
         * </pre>
         */
        IValue AsValue()
        {
            throw new TypeDBDriverException(
                ConceptError.INVALID_CONCEPT_CASTING, this.GetType().Name, typeof(IValue).Name);
        }

        /**
         * This class is used for specifying whether we need explicit or transitive subtyping, instances, etc.
         *
         * <h3>Examples</h3>
         * <pre>
         * attributeType.GetOwners(transaction, annotation, EXPLICIT);
         * </pre>
         */
        public class Transitivity : NativeObjectWrapper<Pinvoke.Transitivity>
        {
            public static readonly Transitivity TRANSITIVE = new Transitivity(Pinvoke.Transitivity.Transitive);
            public static readonly Transitivity EXPLICIT = new Transitivity(Pinvoke.Transitivity.Explicit);

            private Transitivity(Pinvoke.Transitivity nativeObject)
                : base(nativeObject)
            {
            }
        }
    }
}
