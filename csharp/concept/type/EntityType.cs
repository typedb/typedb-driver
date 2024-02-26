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
using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Thing;

namespace Vaticle.Typedb.Driver.Concept.Type
{
    public class EntityType : ThingType, IEntityType
    {
        public EntityType(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public Promise<IEntity> Create(ITypeDBTransaction transaction)
        {
            return Promise.Map<IEntity, Pinvoke.Concept>(
                Pinvoke.typedb_driver.entity_type_create(
                    NativeTransaction(transaction), NativeObject).Resolve, 
                obj => new Entity(obj));
        }

        public VoidPromise SetSupertype(ITypeDBTransaction transaction, IEntityType entityType)
        {
            return new VoidPromise(Pinvoke.typedb_driver.entity_type_set_supertype(
                NativeTransaction(transaction), NativeObject, ((EntityType)entityType).NativeObject));
        }

        public Promise<IEntity> GetSupertype(ITypeDBTransaction transaction) 
        {
            return Promise.Map<IEntity, Pinvoke.Concept>(
                Pinvoke.typedb_driver.entity_type_get_supertype(
                    NativeTransaction(transaction), NativeObject).Resolve,
                obj => new EntityType(obj));
        }

        public ICollection<IEntityType> GetSupertypes(ITypeDBTransaction transaction)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.entity_type_get_supertypes(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new EntityType(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ICollection<IEntityType> GetSubtypes(ITypeDBTransaction transaction)
        {
            return GetSubtypes(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public ICollection<IEntityType> GetSubtypes(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity) 
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.entity_type_get_subtypes(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new EntityType(obj));
            }
             catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ICollection<IEntity> GetInstances(ITypeDBTransaction transaction)
        {
            return GetInstances(transaction, IConcept.Transitivity.TRANSITIVE);
        }

        public ICollection<IEntity> GetInstances(
            ITypeDBTransaction transaction, IConcept.Transitivity transitivity)
        {
            try 
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.entity_type_get_instances(
                        NativeTransaction(transaction), NativeObject, transitivity.NativeObject))
                    .Select(obj => new Entity(obj));
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
