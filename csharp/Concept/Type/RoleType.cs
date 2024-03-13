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
using TypeDB.Driver.Concept;
using static TypeDB.Driver.Api.IConcept.Transitivity;

namespace TypeDB.Driver.Concept
{
    public class RoleType : Type, IRoleType
    {
        public RoleType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public override bool IsRoot()
        {
            return Pinvoke.typedb_driver.role_type_is_root(NativeObject);
        }

        public override bool IsAbstract()
        {
            return Pinvoke.typedb_driver.role_type_is_abstract(NativeObject);
        }

        public override Label Label
        {
            get
            {
                return new Label(
                    Pinvoke.typedb_driver.role_type_get_scope(NativeObject),
                    Pinvoke.typedb_driver.role_type_get_name(NativeObject));
            }
        }

        public override VoidPromise Delete(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.role_type_delete(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public override Promise<bool> IsDeleted(ITypeDBTransaction transaction)
        {
            return new Promise<bool>(Pinvoke.typedb_driver.role_type_is_deleted(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public override VoidPromise SetLabel(ITypeDBTransaction transaction, string label)
        {
            return new VoidPromise(Pinvoke.typedb_driver.role_type_set_label(
                NativeTransaction(transaction), NativeObject, label).Resolve);
        }

        public override Promise<IType> GetSupertype(ITypeDBTransaction transaction)
        {
            return Promise<IType>.Map<Pinvoke.Concept, IType>(
                Pinvoke.typedb_driver.role_type_get_supertype(
                    NativeTransaction(transaction), NativeObject).Resolve,
                obj => new RoleType(obj));
        }

        public override IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_supertypes(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new RoleType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction)
        {
            return GetSubtypes(transaction, TRANSITIVE);
        }

        public override IEnumerable<IType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_subtypes(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new RoleType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IRelationType> GetRelationType(ITypeDBTransaction transaction)
        {
            return Promise<IRelationType>.Map<Pinvoke.Concept, IRelationType>(
                Pinvoke.typedb_driver.role_type_get_relation_type(
                    NativeTransaction(transaction), NativeObject).Resolve,
                obj => new RelationType(obj));
        }

        public IEnumerable<IRelationType> GetRelationTypes(ITypeDBTransaction transaction)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_relation_types(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new RelationType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IThingType> GetPlayerTypes(ITypeDBTransaction transaction)
        {
            return GetPlayerTypes(transaction, TRANSITIVE);
        }

        public IEnumerable<IThingType> GetPlayerTypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_player_types(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => ThingType.ThingTypeOf(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IRelation> GetRelationInstances(ITypeDBTransaction transaction)
        {
            return GetRelationInstances(transaction, TRANSITIVE);
        }

        public IEnumerable<IRelation> GetRelationInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_relation_instances(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new Relation(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IEnumerable<IThing> GetPlayerInstances(ITypeDBTransaction transaction)
        {
            return GetPlayerInstances(transaction, TRANSITIVE);
        }

        public IEnumerable<IThing> GetPlayerInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.role_type_get_player_instances(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => Thing.ThingOf(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
