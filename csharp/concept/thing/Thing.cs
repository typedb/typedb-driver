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
using Vaticle.Typedb.Driver.Api.Concept.Thing;
using Vaticle.Typedb.Driver.Api.Concept.Type;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept;
using Vaticle.Typedb.Driver.Concept.Type.AttributeTypeImpl;

using InternalError = Vaticle.Typedb.Driver.Common.Exception.Error.Internal;

namespace Vaticle.Typedb.Driver.Concept.Thing
{
    public abstract class Thing : Concept, IThing
    {
        private int hash = 0;

        ThingImpl(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public static IThing Of(Pinvoke.Concept nativeConcept) 
        {
            if (Pinvoke.typedb_driver.concept_is_entity(nativeConcept))
                return new Entity(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation(nativeConcept))
                return new Relation(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute(nativeConcept))
                return new Attribute(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }

        public sealed string IID 
        {
            get { return Pinvoke.typedb_driver.thing_get_iid(NativeObject); }
        }

        public abstract IThingType Type { get; }

        public bool IsInferred()
        {
            return Pinvoke.typedb_driver.thing_get_is_inferred(NativeObject);
        }

        public IThing AsThing()
        {
            return this;
        }

        public sealed ICollection<IAttribute> GetHas(
            ITypeDBTransaction transaction, params IAttributeType[] attributeTypes)
        {
            Pinvoke.Concept[] attributeTypesArray = attributeTypes
                .Select(obj -> ((AttributeType)obj).NativeObject)
                .toArray<Pinvoke.Concept>();

            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_get_has(
                        NativeTransaction(transaction),
                        NativeObject,
                        attributeTypesArray,
                        new Pinvoke.Annotation[0]))
                    .Select(obj => new Attribute(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public sealed ICollection<IAttribute> GetHas(
            ITypeDBTransaction transaction, ICollection<Annotation> annotations)
        {
            Pinvoke.Annotation[] annotationsArray =
                annotations.Select(obj => obj.NativeObject).toArray<Pinvoke.Annotation>();

            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_get_has(
                        NativeTransaction(transaction),
                        NativeObject,
                        new Pinvoke.Concept[0],
                        annotationsArray))
                    .Select(obj => new Attribute(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public sealed ICollection<IRelation> GetRelations(
            ITypeDBTransaction transaction, params IRoleType[] roleTypes)
        {
            Pinvoke.Concept[] roleTypesArray =
                roleTypes.Select(obj => ((RoleType)obj).NativeObject).toArray<Pinvoke.Concept>();
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_get_relations(
                        NativeTransaction(transaction), NativeObject, roleTypesArray))
                    .Select(obj => new Relation(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public sealed ICollection<IRoleType> GetPlaying(ITypeDBTransaction transaction)
        {
            try
            {
                return new NativeEnumerable<Pinvoke.Concept>(
                    Pinvoke.typedb_driver.thing_get_playing(
                        NativeTransaction(transaction), NativeObject))
                    .Select(obj => new RoleType(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public sealed VoidPromise SetHas(ITypeDBTransaction transaction, IAttribute attribute)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_set_has(
                NativeTransaction(transaction),
                NativeObject,
                ((Attribute)attribute).NativeObject).Resolve);
        }

        public sealed VoidPromise UnsetHas(ITypeDBTransaction transaction, IAttribute attribute)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_unset_has(
                NativeTransaction(transaction),
                NativeObject,
                ((Attribute)attribute).NativeObject).Resolve);
        }

        public sealed VoidPromise Delete(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.thing_delete(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public sealed Promise<bool> IsDeleted(ITypeDBTransaction transaction)
        {
            return new Promise<bool>(Pinvoke.typedb_driver.thing_is_deleted(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = ComputeHash();
            }

            return _hash;
        }

        private int ComputeHash()
        {
            return IID.GetHashCode();
        }
    }
}