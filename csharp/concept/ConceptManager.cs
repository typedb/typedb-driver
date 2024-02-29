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
using Vaticle.Typedb.Driver.Util;

using DriverError = Vaticle.Typedb.Driver.Common.Error.Driver;
using ConceptError = Vaticle.Typedb.Driver.Common.Error.Concept;

namespace Vaticle.Typedb.Driver.Concept
{
    public class ConceptManager : IConceptManager
    {
        public Pinvoke.Transaction NativeTransaction { get; }

        public ConceptManager(Pinvoke.Transaction nativeTransaction) 
        {
            NativeTransaction = nativeTransaction;
        }

        public IEntityType RootEntityType
        {
            get { return new EntityType(Pinvoke.typedb_driver.concepts_get_root_entity_type()); }
        }

        public IRelationType RootRelationType
        {
            get { return new RelationType(Pinvoke.typedb_driver.concepts_get_root_relation_type()); }
        }

        public IAttributeType RootAttributeType
        {
            get { return new AttributeType(Pinvoke.typedb_driver.concepts_get_root_attribute_type()); }
        }

        public Promise<IEntityType> GetEntityType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IEntityType>.Map<IEntityType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_entity_type(NativeTransaction, label).Resolve,
                obj => new EntityType(obj));
        }

        public Promise<IRelationType> GetRelationType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IRelationType>.Map<IRelationType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_relation_type(NativeTransaction, label).Resolve,
                obj => new RelationType(obj));
        }

        public Promise<IAttributeType> GetAttributeType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IAttributeType>.Map<IAttributeType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_attribute_type(NativeTransaction, label).Resolve,
                obj => new AttributeType(obj));
        }

        public Promise<IEntityType> PutEntityType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IEntityType>.Map<IEntityType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_put_entity_type(NativeTransaction, label).Resolve,
                obj => new EntityType(obj));
        }

        public Promise<IRelationType> PutRelationType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IRelationType>.Map<IRelationType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_put_relation_type(NativeTransaction, label).Resolve,
                obj => new RelationType(obj));
        }

        public Promise<IAttributeType> PutAttributeType(string label, IValue.ValueType valueType)
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IAttributeType>.Map<IAttributeType, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_put_attribute_type(
                    NativeTransaction, label, valueType.NativeObject).Resolve,
                obj => new AttributeType(obj));
        }

        public Promise<IEntity> GetEntity(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IEntity>.Map<IEntity, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_entity(NativeTransaction, iid).Resolve,
                obj => new Entity(obj));
        }

        public Promise<IRelation> GetRelation(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IRelation>.Map<IRelation, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_relation(NativeTransaction, iid).Resolve,
                obj => new Relation(obj));
        }

        public Promise<IAttribute> GetAttribute(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise<IAttribute>.Map<IAttribute, Pinvoke.Concept>(
                Pinvoke.typedb_driver.concepts_get_attribute(NativeTransaction, iid).Resolve,
                obj => new Attribute(obj));
        }

        public IList<TypeDBException> SchemaExceptions
        {
            get 
            {
                if (!NativeTransaction.IsOwned()) // TODO: Change this line to ThrowIfFalse if works! And everywhere else.
                {
                    throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
                }

                try
                {
                    return new NativeEnumerable<Pinvoke.SchemaException>(
                        Pinvoke.typedb_driver.concepts_get_schema_exceptions(NativeTransaction))
                        .Select(e => new TypeDBException(
                            Pinvoke.typedb_driver.schema_exception_code(e),
                            Pinvoke.typedb_driver.schema_exception_message(e)))
                        .ToList<TypeDBException>();
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }
    }
}
