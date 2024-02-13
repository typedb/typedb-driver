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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Thing;

using InternalError = Vaticle.Typedb.Driver.Common.Exception.Error.Internal;

namespace Vaticle.Typedb.Driver.Concept.Type
{
    public abstract class ThingType : Type, IThingType
    {
        ThingType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public ThingType(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity_type(nativeConcept))
                return new EntityType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation_type(nativeConcept))
                return new RelationType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute_type(nativeConcept))
                return new IAttributeType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_root_thing_type(nativeConcept))
                return new Root(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public sealed bool IsRoot()
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

        public sealed bool IsAbstract()
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

        public Label GetLabel()
        {
            try
            {
                return new Label(Pinvoke.typedb_driver.thing_type_get_label(NativeObject));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public VoidPromise Delete(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_delete(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public Promise<bool> IsDeleted(ITypeDBTransaction transaction)
        {
            return new Promise<bool>(Pinvoke.typedb_driver.thing_type_is_deleted(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public sealed VoidPromise SetLabel(ITypeDBTransaction transaction, string label)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_label(
                NativeTransaction(transaction), NativeObject, label));
        }

        public abstract Promise<ThingType> GetSupertype(ITypeDBTransaction transaction);

        public abstract ICollection<IThingType> GetSupertypes(ITypeDBTransaction transaction);

        public abstract ICollection<IThingType> GetSubtypes(ITypeDBTransaction transaction);

        public abstract ICollection<IThingType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        public abstract ICollection<IThing> GetInstances(ITypeDBTransaction transaction);

        public abstract ICollection<IThing> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity);

        public sealed VoidPromise SetAbstract(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_abstract(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public sealed VoidPromise UnsetAbstract(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_abstract(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public sealed VoidPromise SetPlays(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_plays(
                NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject, null).Resolve);
        }

        public sealed VoidPromise SetPlays(
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
            return SetOwns(transaction, attributeType, null, emptySet());
        }

        public VoidPromise SetOwns(
            ITypeDBTransaction transaction, IAttributeType attributeType, ICollection<Annotation> annotations)
        {
            return SetOwns(transaction, attributeType, null, annotations);
        }

        public VoidPromise SetOwns(
            ITypeDBTransaction transaction, IAttributeType attributeType, IAttributeType overriddenType)
        {
            return SetOwns(transaction, attributeType, overriddenType, new HashSet(){});
        }

        public sealed VoidPromise SetOwns(
            ITypeDBTransaction transaction, 
            IAttributeType attributeType, 
            IAttributeType overriddenType, 
            ICollection<Annotation> annotations)
        {
            Pinvoke.Concept overriddenTypeNative = overriddenType != null 
                ? ((IAttributeType)overriddenType).NativeObject 
                : null;
            
            Pinvoke.Annotation[] annotationsArray = 
                annotations.Select(obj => obj.NativeObject).toArray<Pinvoke.Annotation>();
            
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_set_owns(
                NativeTransaction(transaction), 
                NativeObject, 
                ((IAttributeType)attributeType).NativeObject, 
                overriddenTypeNative, 
                annotationsArray));
        }

        public sealed ICollection<IRoleType> GetPlays(ITypeDBTransaction transaction)
        {
            return GetPlays(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public sealed ICollection<IRoleType> GetPlays(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(Pinvoke.typedb_driver.thing_type_get_plays(
                    NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new RoleType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IRoleType> GetPlaysOverridden(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return Promise.Map<IRoleType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.thing_type_get_plays_overridden(
                    NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject).Resolve,
                obj => new RoleType(obj));
        }

        public ICollection<IAttributeType> GetOwns(ITypeDBTransaction transaction)
        {
            return GetOwns(transaction, IConcept.Transitivity.TRANSITIVE, emptySet());
        }

        public ICollection<IAttributeType> GetOwns(ITypeDBTransaction transaction, IValue.ValueType valueType)
        {
            return GetOwns(transaction, valueType, IConcept.Transitivity.TRANSITIVE, emptySet());
        }

        public ICollection<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, IConcept.Transitivity.TRANSITIVE, annotations);
        }

        public sealed ICollection<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType valueType, ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, valueType, IConcept.Transitivity.TRANSITIVE, annotations);
        }

        public ICollection<IIAttributeType> GetOwns(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, transitivity, emptySet());
        }

        public ICollection<IIAttributeType> GetOwns(
            ITypeDBTransaction transaction, IValue.ValueType valueType, IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, valueType, transitivity, emptySet());
        }

        public ICollection<IIAttributeType> GetOwns(
            ITypeDBTransaction transaction, ICollection<Annotation> annotations, IConcept.Transitivity transitivity)
        {
            return GetOwns(transaction, transitivity, annotations);
        }

        public ICollection<IIAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            IValue.ValueType valueType,
            ICollection<Annotation> annotations,
            Transitivity transitivity)
        {
            return GetOwns(transaction, valueType, transitivity, annotations);
        }

        private ICollection<IAttributeType> GetOwns(
            ITypeDBTransaction transaction,
            Transitivity transitivity,
            ICollection<Annotation> annotations)
        {
            return GetOwns(transaction, null, transitivity, annotations);
        }

        private ICollection<IAttributeType> GetOwns(
            ITypeDBTransaction transaction, 
            IValue.ValueType valueType, 
            IConcept.Transitivity transitivity, 
            ICollection<Annotation> annotations) 
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_type_get_owns(
                        NativeTransaction(transaction),
                        NativeObject,
                        valueType == null ? null : valueType.NativeObject,
                        transitivity.NativeObject,
                        Annotations.Select(obj => obj.NativeObject).toArray<Pinvoke.Annotation>()))
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
            return Promise.Map<IAttributeType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.thing_type_get_owns_overridden(
                    NativeTransaction(transaction),
                    NativeObject,
                    ((AttributeType)attributeType).NativeObject).Resolve,
                obj => new AttributeType(obj));
        }

        public sealed VoidPromise UnSetOwns(ITypeDBTransaction transaction, IAttributeType attributeType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_owns(
                NativeTransaction(transaction), NativeObject, ((AttributeType)attributeType).NativeObject).Resolve);
        }

        public sealed VoidPromise UnsetPlays(ITypeDBTransaction transaction, IRoleType roleType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_type_unset_plays(
                NativeTransaction(transaction), NativeObject, ((RoleType)roleType).NativeObject).Resolve);
        }

        public sealed Promise<string> GetSyntax(ITypeDBTransaction transaction)
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

            // TODO: Is GetLabel() the same for Root?

            public Promise<IThingType> GetSupertype(ITypeDBTransaction transaction)
            {
                return new Promise<IThingType>(() => null);
            }

            public ICollection<IThingType> GetSupertypes(ITypeDBTransaction transaction)
            {
                return new List<IThingType>{this};
            }

            public ICollection<IThingType> GetSubtypes(ITypeDBTransaction transaction)
            {
                return new List<IThingType>(){this}
                    .Concat(transaction.AllConcepts.RootEntityType.GetSubtypes(transaction))
                    .Concat(transaction.AllConcepts.RootRelationType.GetSubtypes(transaction))
                    .Concat(transaction.AllConcepts.RootIAttributeType.GetSubtypes(transaction))
                    .Select(obj => (ThingType)obj)
                    .ToList<IThingType>();
            }

            public ICollection<IThingType> GetSubtypes(
                ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
            {
                return new List<IThingType>()
                    {
                        transaction.AllConcepts.RootEntityType,
                        transaction.AllConcepts.RootRelationType,
                        transaction.AllConcepts.RootIAttributeType
                    }.Select(obj => (ThingType)obj);
            }

            public ICollection<IThing> GetInstances(ITypeDBTransaction transaction)
            {
                return new List<IThingType>()
                    {transaction.AllConcepts.RootEntityType.GetInstances(transaction)}
                    .Concat(transaction.AllConcepts.RootRelationType.GetInstances(transaction))
                    .Concat(transaction.AllConcepts.RootIAttributeType.GetInstances(transaction))
                    .Select(obj => (ThingType)obj)
                    .ToList<IThingType>();
            }

            public ICollection<IThing> GetInstances(ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
            {
                return new List<IThing>(){};
            }
        }
    }
}
