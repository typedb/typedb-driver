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
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Concept;

namespace Vaticle.Typedb.Driver.Concept
{
    public class AttributeType : ThingType, IAttributeType 
    {
        public AttributeType(Pinvoke.Concept nativeConcept) 
            : base(nativeConcept)
        {
        }

        public IValue.ValueType ValueType
        {
            get
            {
                return new IValue.ValueType(
                    Pinvoke.typedb_driver.attribute_type_get_value_type(NativeObject));
            }
        }

        public VoidPromise SetSupertype(ITypeDBTransaction transaction, IAttributeType attributeType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.attribute_type_set_supertype(
                NativeTransaction(transaction),
                NativeObject,
                ((AttributeType)attributeType).NativeObject).Resolve);
        }

        public override Promise<IType> GetSupertype(ITypeDBTransaction transaction)
        {
            return Promise<IType>.Map<Pinvoke.Concept, IType>(
                Pinvoke.typedb_driver.attribute_type_get_supertype(
                    NativeTransaction(transaction), NativeObject).Resolve,
                obj => new AttributeType(obj));
        }

        public override IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_type_get_supertypes(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new AttributeType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction)
        {
            return GetSubtypes(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public IEnumerable<IType> GetSubtypes(
            ITypeDBTransaction transaction, IValue.ValueType valueType) 
        {
            return GetSubtypes(transaction, valueType, IConcept.Transitivity.TRANSITIVE);
        }

        public override IEnumerable<IType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_type_get_subtypes(
                        NativeTransaction(transaction),
                        NativeObject,
                        transitivity.NativeObject))
                    .Select(obj => new AttributeType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IType> GetSubtypes(
            ITypeDBTransaction transaction, 
            IValue.ValueType valueType, 
            IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_type_get_subtypes_with_value_type(
                        NativeTransaction(transaction), 
                        NativeObject, 
                        valueType.NativeObject, 
                        transitivity.NativeObject))
                    .Select(obj => new AttributeType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction)
        {
            return GetInstances(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public override IEnumerable<IThing> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_type_get_instances(
                        NativeTransaction(transaction),
                        NativeObject,
                        transitivity.NativeObject))
                    .Select(obj => new Attribute(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IThingType> GetOwners(ITypeDBTransaction transaction)
        {
            return GetOwners(transaction, new HashSet<IThingType.Annotation>(){});
        }

        public IEnumerable<IThingType> GetOwners(
            ITypeDBTransaction transaction, ICollection<IThingType.Annotation> annotations) 
        {
            return GetOwners(transaction, annotations, IConcept.Transitivity.TRANSITIVE);
        }

        public IEnumerable<IThingType> GetOwners(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            return GetOwners(transaction, new HashSet<IThingType.Annotation>(){}, transitivity);
        }

        public IEnumerable<IThingType> GetOwners(
            ITypeDBTransaction transaction, 
            ICollection<IThingType.Annotation> annotations, 
            IConcept.Transitivity transitivity) 
        {
            Pinvoke.Annotation[] annotationsArray = 
                annotations.Select(obj => obj.NativeObjectValue).ToArray<Pinvoke.Annotation>();
            
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.attribute_type_get_owners(
                        NativeTransaction(transaction),
                        NativeObject,
                        transitivity.NativeObject,
                        annotationsArray))
                    .Select(obj => ThingType.ThingTypeOf(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, string value) 
        {
            return Put(transaction, new Value(value));
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, long value) 
        {
            return Put(transaction, new Value(value));
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, double value) 
        {
            return Put(transaction, new Value(value));
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, bool value) 
        {
            return Put(transaction, new Value(value));
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, System.DateTime value) 
        {
            return Put(transaction, new Value(value));
        }

        public Promise<IAttribute> Put(ITypeDBTransaction transaction, IValue value)
        {
            return Promise<IAttribute>.Map<Pinvoke.Concept, IAttribute>(
                Pinvoke.typedb_driver.attribute_type_put(
                    NativeTransaction(transaction), 
                    NativeObject, 
                    ((Value)value).NativeObject).Resolve,
                obj => new Attribute(obj));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, string value) 
        {
            return Get(transaction, new Value(value));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, long value) 
        {
            return Get(transaction, new Value(value));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, double value)
        {
            return Get(transaction, new Value(value));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, bool value)
        {
            return Get(transaction, new Value(value));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, System.DateTime value)
        {
            return Get(transaction, new Value(value));
        }

        public Promise<IAttribute> Get(ITypeDBTransaction transaction, IValue value)
        {
            return Promise<IAttribute>.Map<Pinvoke.Concept, IAttribute>(
                Pinvoke.typedb_driver.attribute_type_get(
                    NativeTransaction(transaction),
                    NativeObject,
                    ((Value)value).NativeObject).Resolve,
                obj => new Attribute(obj));
        }

        public Promise<string> GetRegex(ITypeDBTransaction transaction)
        {
            return new Promise<string>(Pinvoke.typedb_driver.attribute_type_get_regex(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public VoidPromise SetRegex(ITypeDBTransaction transaction, string regex)
        {
            return new VoidPromise(Pinvoke.typedb_driver.attribute_type_set_regex(
                NativeTransaction(transaction), NativeObject, regex).Resolve);
        }

        public VoidPromise UnsetRegex(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.attribute_type_unset_regex(
                NativeTransaction(transaction), NativeObject).Resolve);
        }
    }
}
