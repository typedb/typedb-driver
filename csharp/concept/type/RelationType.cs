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
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Thing;

namespace Vaticle.Typedb.Driver.Concept.Type
{
    public class RelationType : ThingType, IRelationType 
    {
        public IRelationType(Pinvoke.Concept nativeConcept) 
            : base(nativeConcept)
        {
        }

        public sealed Promise<IRelation> Create(ITypeDBTransaction transaction) 
        {
            return Promise.Map<IRelation, Pinvoke.Concept>(
                Pinvoke.typedb_driver.relation_type_create(
                    NativeTransaction(transaction), NativeObject).Resolve, 
                obj => new Relation(obj));
        }

        public sealed VoidPromise SetSupertype(ITypeDBTransaction transaction, IRelationType relationType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_set_supertype(
                NativeTransaction(transaction),
                NativeObject, 
                ((RelationType)relationType).NativeObject).Resolve);
        }

        public sealed ICollection<IRoleType> GetRelates(ITypeDBTransaction transaction) 
        {
            return GetRelates(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public sealed ICollection<IRoleType> GetRelates(
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

        public sealed Promise<IRoleType> GetRelates(ITypeDBTransaction transaction, string roleLabel) 
        {
            return Promise.Map<IRoleType, Pinvoke.Concept>(Pinvoke.typedb_driver.relation_type_get_relates_for_role_label(
                NativeTransaction(transaction), NativeObject, roleLabel).Resolve,
                obj => new RoleType(obj));
        }

        public Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, IRoleType roleType) 
        {
            return GetRelatesOverridden(transaction, roleType.getLabel().name());
        }

        public sealed Promise<IRoleType> GetRelatesOverridden(ITypeDBTransaction transaction, string roleLabel) 
        {
            return Promise.Map<IRoleType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.relation_type_get_relates_overridden(
                    NativeTransaction(transaction), NativeObject, roleLabel).Resolve, 
                obj => new RoleType(obj));
        }

        public sealed VoidPromise SetRelates(ITypeDBTransaction transaction, string roleLabel) 
        {
            return SetRelates(transaction, roleLabel, (string)null);
        }

        public VoidPromise SetRelates(
            ITypeDBTransaction transaction, string roleLabel, IRoleType overriddenType)
        {
            return SetRelates(transaction, roleLabel, overriddenType.GetLabel().Name);
        }

        public sealed VoidPromise SetRelates(
            ITypeDBTransaction transaction, string roleLabel, string overriddenLabel)
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_set_relates(
                NativeTransaction(transaction), NativeObject, roleLabel, overriddenLabel).Resolve);
        }

        public VoidPromise UnsetRelates(ITypeDBTransaction transaction, IRoleType roleType) 
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_unset_relates(
                NativeTransaction(transaction), NativeObject, roleType.GetLabel().Name).Resolve);
        }

        public sealed VoidPromise UnsetRelates(ITypeDBTransaction transaction, string roleLabel) 
        {
            return new VoidPromise(Pinvoke.typedb_driver.relation_type_unset_relates(
                NativeTransaction(transaction), NativeObject, roleLabel).Resolve);
        }

        public Promise<IRelationType> GetSupertype(ITypeDBTransaction transaction) 
        {
            return Promise.Map<IRelationType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.relation_type_get_supertype(
                    NativeTransaction(transaction), NativeObject).Resolve, 
                obj => new RelationType(obj));
        }

        public sealed ICollection<IRelationType> GetSupertypes(ITypeDBTransaction transaction) 
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

        public sealed ICollection<IRelationType> GetSubtypes(ITypeDBTransaction transaction) 
        {
            return GetSubtypes(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public sealed ICollection<IRelationType> GetSubtypes(ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
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

        public sealed ICollection<IRelation> GetInstances(ITypeDBTransaction transaction) 
        {
            return GetInstances(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public sealed ICollection<IRelation> GetInstances(
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
