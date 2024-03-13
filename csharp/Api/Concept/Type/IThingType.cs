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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    public interface IThingType : IType
    {
        /**
         * {@inheritDoc}
         */
        bool IConcept.IsThingType()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        IThingType IConcept.AsThingType()
        {
            return this;
        }

        /**
         * Retrieves all <code>IThing</code> objects that are instances of this <code>IThingType</code> or its subtypes.
         * Equivalent to <code>GetInstances(transaction, TRANSITIVE)</code>
         *
         * @see ThingType#GetInstances(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction);

        /**
         * Retrieves <code>IThing</code> objects that are instances of this exact <code>IThingType</code>, OR
         * this <code>IThingType</code> and any of its subtypes
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetInstances(transaction);
         * thingType.GetInstances(transaction, EXPLICIT);
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity <code>EXPLICIT</code> for direct instances only, <code>TRANSITIVE</code> to include instances of subtypes
         */
        IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Set a <code>IThingType</code> to be abstract, meaning it cannot have instances.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.SetAbstract(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @return
         */
        VoidPromise SetAbstract(ITypeDBTransaction transaction);

        /**
         * Set a <code>IThingType</code> to be non-abstract, meaning it can have instances.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.UnsetAbstract(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        VoidPromise UnsetAbstract(ITypeDBTransaction transaction);

        /**
         * Allows the instances of this <code>IThingType</code> to play the given role.
         *
         * @see ThingType#SetPlays(ITypeDBTransaction, IRoleType, IRoleType)
         */
        VoidPromise SetPlays(ITypeDBTransaction transaction, IRoleType roleType);

        /**
         * Allows the instances of this <code>IThingType</code> to play the given role.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.SetPlays(transaction, roleType).Resolve();
         * thingType.SetPlays(transaction, roleType, overriddenType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleType The role to be played by the instances of this type
         * @param overriddenType The role type that this role overrides, if applicable
         */
        VoidPromise SetPlays(ITypeDBTransaction transaction, IRoleType roleType, IRoleType overriddenType);

        /**
         * Allows the instances of this <code>IThingType</code> to own the given <code>IAttributeType</code>.
         * Optionally, overriding a previously declared ownership.
         * Optionally, adds annotations to the ownership.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.SetOwns(transaction, attributeType).Resolve();
         * thingType.SetOwns(transaction, attributeType, overriddenType, new []{NewKey()}).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attributeType The <code>IAttributeType</code> to be owned by the instances of this type.
         * @param overriddenType The <code>IAttributeType</code> that this attribute ownership overrides, if applicable.
         * @param annotations Adds annotations to the ownership.
         */
        VoidPromise SetOwns(
            ITypeDBTransaction transaction, 
            IAttributeType attributeType, 
            IAttributeType? overriddenType,
            ICollection<Annotation> annotations);

        /**
         * Allows the instances of this <code>IThingType</code> to own the given <code>IAttributeType</code>,
         *
         * @see ThingType#SetOwns(ITypeDBTransaction, IAttributeType, IAttributeType, Set)
         */
        VoidPromise SetOwns(
            ITypeDBTransaction transaction, IAttributeType attributeType, IAttributeType overriddenType);

        /**
         * Allows the instances of this <code>IThingType</code> to own the given <code>IAttributeType</code>.
         *
         * @see ThingType#SetOwns(ITypeDBTransaction, IAttributeType, IAttributeType, Set)
         */
        VoidPromise SetOwns(ITypeDBTransaction transaction, IAttributeType attributeType, ICollection<Annotation> annotations);

        /**
         * Allows the instances of this <code>IThingType</code> to own the given <code>IAttributeType</code>.
         *
         * @see ThingType#SetOwns(ITypeDBTransaction, IAttributeType, IAttributeType, Set)
         */
        VoidPromise SetOwns(ITypeDBTransaction transaction, IAttributeType attributeType);

        /**
         * Retrieves all direct and inherited roles that are allowed
         * to be played by the instances of this <code>IThingType</code>.
         *
         * @see ThingType#GetPlays(ITypeDBTransaction, IConcept.Transitivity)
         */
        IEnumerable<IRoleType> GetPlays(ITypeDBTransaction transaction);

        /**
         * Retrieves all direct and inherited (or direct only) roles that are allowed
         * to be played by the instances of this <code>IThingType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetPlays(transaction).Resolve();
         * thingType.GetPlays(transaction, EXPLICIT).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param transitivity transitivity: <code>TRANSITIVE</code> for direct and indirect playing,
         *                                   <code>EXPLICIT</code> for direct playing only
         */
        IEnumerable<IRoleType> GetPlays(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Retrieves a <code>IRoleType</code> that is overridden by the given
         * <code>role_type</code> for this <code>IThingType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetPlaysOverridden(transaction, roleType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleType The <code>IRoleType</code> that overrides an inherited role
         */
        Promise<IRoleType> GetPlaysOverridden(ITypeDBTransaction transaction, IRoleType roleType);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction, IValue.ValueType valueType);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction, ICollection<Annotation> annotations);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType?, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType? valueType, ICollection<Annotation> annotations);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType?, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType? valueType, IConcept.Transitivity transitivity);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * @see ThingType#GetOwns(ITypeDBTransaction, IValue.ValueType, ICollection&lt;Annotation&gt;, IConcept.Transitivity)
         */
        IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, ICollection<Annotation> annotations, IConcept.Transitivity transitivity);

        /**
         * Retrieves <code>IAttributeType</code> that the instances of this
         * <code>IThingType</code> are allowed to own directly or via inheritance.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetOwns(transaction);
         * thingType.GetOwns(transaction, valueType, EXPLICIT, new []{NewKey()}));
         * </pre>
         *
         * @param transaction The current transaction
         * @param valueType If specified, only attribute types of this <code>ValueType</code> will be retrieved.
         * @param transitivity <code>TRANSITIVE</code> for direct and inherited ownership,
         *                     <code>EXPLICIT</code> for direct ownership only
         * @param annotations Only retrieve attribute types owned with annotations.
         */
        IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            IValue.ValueType? valueType,
            ICollection<Annotation> annotations,
            IConcept.Transitivity transitivity);

        /**
         * Retrieves an <code>IAttributeType</code>, ownership of which is overridden
         * for this <code>IThingType</code> by a given <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetOwnsOverridden(transaction, attributeType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attributeType The <code>IAttributeType</code> that overrides requested <code>IAttributeType</code>
         */
        Promise<IAttributeType> GetOwnsOverridden(ITypeDBTransaction transaction, IAttributeType attributeType);

        /**
         * Disallows the instances of this <code>IThingType</code> from playing the given role.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.UnsetPlays(transaction, roleType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param roleType The role to not be played by the instances of this type.
         */
        VoidPromise UnsetPlays(ITypeDBTransaction transaction, IRoleType roleType);

        /**
         * Disallows the instances of this <code>IThingType</code> from owning the given <code>IAttributeType</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.UnsetOwns(transaction, attributeType).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         * @param attributeType The <code>IAttributeType</code> to not be owned by the type.
         */
        VoidPromise UnsetOwns(ITypeDBTransaction transaction, IAttributeType attributeType);

        /**
         * Produces a pattern for creating this <code>IThingType</code> in a <code>define</code> query.
         *
         * <h3>Examples</h3>
         * <pre>
         * thingType.GetSyntax(transaction).Resolve();
         * </pre>
         *
         * @param transaction The current transaction
         */
        Promise<string> GetSyntax(ITypeDBTransaction transaction);

        /**
         * Annotation
         */
        public class Annotation : NativeObjectWrapper<Pinvoke.Annotation>
        {
            /**
             * @hidden
             */
            private readonly int _hash;

            private Annotation(Pinvoke.Annotation annotation)
                : base(annotation)
            {
                _hash = (IsKey(), IsUnique()).GetHashCode();
            }

            /**
             * Produces a <code>@key</code> annotation.
             *
             * <h3>Examples</h3>
             * <pre>
             * using static TypeDB.Driver.Api.IThingType.Annotation;
             * NewKey();
             * </pre>
             */
            public static Annotation NewKey()
            {
                return new Annotation(Pinvoke.typedb_driver.annotation_new_key());
            }

            /**
             * Produces a <code>@unique</code> annotation.
             *
             * <h3>Examples</h3>
             * <pre>
             * Annotation.NewUnique();
             * </pre>
             */
            public static Annotation NewUnique()
            {
                return new Annotation(Pinvoke.typedb_driver.annotation_new_unique());
            }

            /**
             * Checks if this <code>Annotation</code> is a <code>@key</code> annotation.
             *
             * <h3>Examples</h3>
             * <pre>
             * annotation.IsKey();
             * </pre>
             */
            public bool IsKey()
            {
                return Pinvoke.typedb_driver.annotation_is_key(NativeObject);
            }

            /**
             * Checks if this <code>Annotation</code> is a <code>@unique</code> annotation.
             *
             * <h3>Examples</h3>
             * <pre>
             * annotation.IsUnique();
             * </pre>
             */
            public bool IsUnique()
            {
                return Pinvoke.typedb_driver.annotation_is_unique(NativeObject);
            }

            /**
             * Retrieves a string representation of this <code>Annotation</code>.
             *
             * <h3>Examples</h3>
             * <pre>
             * annotation.ToString();
             * </pre>
             */
            public override string ToString()
            {
                return Pinvoke.typedb_driver.annotation_to_string(NativeObject);
            }

            /**
             * Checks if this <code>Annotation</code> is equal to another object.
             *
             * <h3>Examples</h3>
             * <pre>
             * annotation.Equals(obj);
             * </pre>
             *
             * @param obj Object to compare with
             */
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

                Annotation that = (Annotation)obj;

                return Pinvoke.typedb_driver.annotation_equals(this.NativeObject, that.NativeObject);
            }

            /**
             * @hidden
             */
            public override int GetHashCode()
            {
                return _hash;
            }
        }
    }
}
