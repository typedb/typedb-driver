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

using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Concept;
using static TypeDB.Driver.Api.IConcept.Transitivity;
using static TypeDB.Driver.Api.IThingType;

using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    public abstract class ThingType : Type, IThingType
    {
        private Label? _label;

        internal ThingType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public static IThingType ThingTypeOf(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity_type(nativeConcept))
                return new EntityType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation_type(nativeConcept))
                return new RelationType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute_type(nativeConcept))
                return new AttributeType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_root_thing_type(nativeConcept))
                return new Root(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public abstract IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction);

        public abstract IEnumerable<IThing> GetInstances(
            ITypeDBTransaction transaction,
            IConcept.Transitivity transitivity);

        public override bool IsRoot()
        {
            try
            {
                return Pinvoke.typedb_driver.thing_type_is_root(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override bool IsAbstract()
        {
            try
            {
                return Pinvoke.typedb_driver.thing_type_is_abstract(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override Label Label
        {
            get
            {
                if (!_label.HasValue)
                {
                    _label = new Label(Pinvoke.typedb_driver.thing_type_get_label(NativeObject));
                }

                return _label.Value;
            }
        }

        public override VoidPromise Delete(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_delete(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public override Promise<bool> IsDeleted(ITypeDBTransaction transaction)
        {
            return new Promise<bool>(Pinvoke.typedb_driver.thing_type_is_deleted(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public override VoidPromise SetLabel(ITypeDBTransaction transaction, string label)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_label(
                NativeTransaction(transaction), NativeObject, label).Resolve);
        }

        public VoidPromise SetAbstract(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_abstract(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public VoidPromise UnsetAbstract(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_abstract(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public VoidPromise SetPlays(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_plays(
                NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject, null).Resolve);
        }

        public VoidPromise SetPlays(
            ITypeDBTransaction transaction, IRoleType roleType, IRoleType overriddenRoleType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_plays(
                NativeTransaction(transaction),
                NativeObject,
                ((RoleType)roleType).NativeObject, 
                ((RoleType)overriddenRoleType).NativeObject).Resolve);
        }

        public VoidPromise SetOwns(ITypeDBTransaction transaction, IAttributeType attributeType)
        {
            return SetOwns(transaction, attributeType, null, new Annotation[0]);
        }

        public VoidPromise SetOwns(
            ITypeDBTransaction transaction,
            IAttributeType attributeType,
            ICollection<Annotation> annotations)
        {
            return SetOwns(transaction, attributeType, null, annotations);
        }

        public VoidPromise SetOwns(
            ITypeDBTransaction transaction, IAttributeType attributeType, IAttributeType overriddenType)
        {
            return SetOwns(transaction, attributeType, overriddenType, new Annotation[0]);
        }

        public VoidPromise SetOwns(
            ITypeDBTransaction transaction, 
            IAttributeType attributeType, 
            IAttributeType? overriddenType,
            ICollection<Annotation> annotations)
        {
            Pinvoke.Concept? overriddenTypeNative = overriddenType != null
                ? ((AttributeType)overriddenType).NativeObject
                : null;
            
            Pinvoke.Annotation[] annotationsArray = annotations
                .Select(obj => obj.NativeObject)
                .ToArray<Pinvoke.Annotation>();
            
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_owns(
                NativeTransaction(transaction), 
                NativeObject, 
                ((AttributeType)attributeType).NativeObject,
                overriddenTypeNative, 
                annotationsArray).Resolve);
        }

        public IEnumerable<IRoleType> GetPlays(ITypeDBTransaction transaction)
        {
            return GetPlays(transaction, Transitive);
        }

        public IEnumerable<IRoleType> GetPlays(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(Pinvoke.typedb_driver.thing_type_get_plays(
                    NativeTransaction(transaction), NativeObject, (Pinvoke.Transitivity)transitivity))
                    .Select(obj => new RoleType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IRoleType> GetPlaysOverridden(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return Promise<IRoleType>.Map<Pinvoke.Concept, IRoleType>(
                Pinvoke.typedb_driver.thing_type_get_plays_overridden(
                    NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject).Resolve,
                obj => new RoleType(obj));
        }

        public IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction)
        {
            return GetOwns(transaction, Transitive, new Annotation[0]);
        }

        public IEnumerable<IAttributeType> GetOwns(ITypeDBTransaction transaction, IValue.ValueType valueType)
        {
            return GetOwns(
                transaction, valueType, Transitive, new Annotation[0]);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, Transitive, annotations);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType? valueType, ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, valueType, Transitive, annotations);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, transitivity, new Annotation[0]);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType? valueType, IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, valueType, transitivity, new Annotation[0]);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            ICollection<Annotation> annotations,
            IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, transitivity, annotations);
        }

        public IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            IValue.ValueType? valueType,
            ICollection<Annotation> annotations,
            IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, valueType, transitivity, annotations);
        }

        private IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            IConcept.Transitivity transitivity,
            ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, null, transitivity, annotations);
        }

        private IEnumerable<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, 
            IValue.ValueType? valueType,
            IConcept.Transitivity transitivity, 
            ICollection<Annotation> annotations) 
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_type_get_owns(
                        NativeTransaction(transaction),
                        NativeObject,
                        valueType != null ? (Pinvoke.ValueType)valueType : null,
                        (Pinvoke.Transitivity)transitivity,
                        annotations.Select(obj => obj.NativeObject).ToArray<Pinvoke.Annotation>()))
                    .Select(obj => new AttributeType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IAttributeType> GetOwnsOverridden(
            ITypeDBTransaction transaction, IAttributeType attributeType)
        {
            return Promise<IAttributeType>.Map<Pinvoke.Concept, IAttributeType>(
                Pinvoke.typedb_driver.thing_type_get_owns_overridden(
                    NativeTransaction(transaction),
                    NativeObject,
                    ((AttributeType)attributeType).NativeObject).Resolve,
                obj => new AttributeType(obj));
        }

        public VoidPromise UnsetOwns(ITypeDBTransaction transaction, IAttributeType attributeType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_owns(
                NativeTransaction(transaction),
                NativeObject,
                ((AttributeType)attributeType).NativeObject).Resolve);
        }

        public VoidPromise UnsetPlays(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_plays(
                NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject).Resolve);
        }

        public Promise<string> GetSyntax(ITypeDBTransaction transaction)
        {
            return new Promise<string>(Pinvoke.typedb_driver.thing_type_get_syntax(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public class Root : ThingType
        {
            public Root(Pinvoke.Concept nativeConcept)
                : base(nativeConcept)
            {
            }

            public override Promise<IType> GetSupertype(ITypeDBTransaction transaction)
            {
                return new Promise<IType>(() => null);
            }

            public override IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction)
            {
                return new List<IType>{this};
            }

            public override IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction)
            {
                return new List<IType>(){this}
                    .Concat(transaction.Concepts.RootEntityType.GetSubtypes(transaction))
                    .Concat(transaction.Concepts.RootRelationType.GetSubtypes(transaction))
                    .Concat(transaction.Concepts.RootAttributeType.GetSubtypes(transaction));
            }

            public override IEnumerable<IType> GetSubtypes(
                ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
            {
                return new List<IType>()
                    {
                        transaction.Concepts.RootEntityType,
                        transaction.Concepts.RootRelationType,
                        transaction.Concepts.RootAttributeType
                    };
            }

            public override IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction)
            {
                return transaction.Concepts.RootEntityType.GetInstances(transaction)
                    .Concat(transaction.Concepts.RootRelationType.GetInstances(transaction))
                    .Concat(transaction.Concepts.RootAttributeType.GetInstances(transaction))
                    .Cast<IThing>();
            }

            public override IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
            {
                return new List<IThing>(){};
            }
        }
    }
}
