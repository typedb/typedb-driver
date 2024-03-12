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
using static Vaticle.Typedb.Driver.Api.IConcept.Transitivity;

namespace Vaticle.Typedb.Driver.Concept
{
    public class RelationType : ThingType, IRelationType
    {
        public RelationType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public Promise<IRelation> Create(ITypeDBTransaction transaction)
        {
            return Promise<IRelation>.Map<Pinvoke.Concept, IRelation>(
                Pinvoke.typedb_driver.relation_type_create(
                    NativeTransaction(transaction), NativeObject).Resolve, 
                obj => new Relation(obj));
        }

        public VoidPromise SetSupertype(ITypeDBTransaction transaction, IRelationType relationType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_set_supertype(
                NativeTransaction(transaction),
                NativeObject, 
                ((RelationType)relationType).NativeObject).Resolve);
        }

        public IEnumerable<IRoleType> GetRelates(ITypeDBTransaction transaction)
        {
            return GetRelates(transaction, TRANSITIVE);
        }

        public IEnumerable<IRoleType> GetRelates(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(Pinvoke.typedb_driver.relation_type_get_relates(
                    NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new RoleType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public Promise<IRoleType> GetRelates(ITypeDBTransaction transaction, string roleLabel)
        {
            return Promise<IRoleType>.Map<Pinvoke.Concept, IRoleType>(
                Pinvoke.typedb_driver.relation_type_get_relates_for_role_label(
                    NativeTransaction(transaction), NativeObject, roleLabel).Resolve,
                obj => new RoleType(obj));
        }

        public Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, IRoleType roleType) 
        {
            return GetRelatesOverridden(transaction, roleType.Label.Name);
        }

        public Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, string roleLabel)
        {
            return Promise<IRoleType>.Map<Pinvoke.Concept, IRoleType>(
                Pinvoke.typedb_driver.relation_type_get_relates_overridden(
                    NativeTransaction(transaction), NativeObject, roleLabel).Resolve, 
                obj => new RoleType(obj));
        }

        public VoidPromise SetRelates(ITypeDBTransaction transaction, string roleLabel)
        {
            return SetRelates(transaction, roleLabel, (string)null);
        }

        public VoidPromise SetRelates(
            ITypeDBTransaction transaction, string roleLabel, IRoleType overriddenType)
        {
            return SetRelates(transaction, roleLabel, overriddenType.Label.Name);
        }

        public VoidPromise SetRelates(
            ITypeDBTransaction transaction, string roleLabel, string overriddenLabel)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_set_relates(
                NativeTransaction(transaction), NativeObject, roleLabel, overriddenLabel).Resolve);
        }

        public VoidPromise UnsetRelates(ITypeDBTransaction transaction, IRoleType roleType) 
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_unset_relates(
                NativeTransaction(transaction), NativeObject, roleType.Label.Name).Resolve);
        }

        public VoidPromise UnsetRelates(ITypeDBTransaction transaction, string roleLabel)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_unset_relates(
                NativeTransaction(transaction), NativeObject, roleLabel).Resolve);
        }

        public override Promise<IType> GetSupertype(ITypeDBTransaction transaction)
        {
            return Promise<IType>.Map<Pinvoke.Concept, IType>(
                Pinvoke.typedb_driver.relation_type_get_supertype(
                    NativeTransaction(transaction), NativeObject).Resolve,
                obj => new RelationType(obj));
        }

        public override IEnumerable<IType> GetSupertypes(ITypeDBTransaction transaction)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_type_get_supertypes(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new RelationType(obj));
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

        public override IEnumerable<IType> GetSubtypes(ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_type_get_subtypes(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new RelationType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override IEnumerable<IThing> GetInstances(ITypeDBTransaction transaction)
        {
            return GetInstances(transaction, TRANSITIVE);
        }

        public override IEnumerable<IThing> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.relation_type_get_instances(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new Relation(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
