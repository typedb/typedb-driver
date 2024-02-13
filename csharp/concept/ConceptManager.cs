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

using Vaticle.Typedb.Driver.Api.Concept;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Thing;
using Vaticle.Typedb.Driver.Concept.Type;
using Vaticle.Typedb.Driver.Util;

using DriverError = Vaticle.Typedb.Driver.Common.Exception.Error.Driver;
using ConceptError = Vaticle.Typedb.Driver.Common.Exception.Error.Concept;

namespace Vaticle.Typedb.Driver.Concept
{
    public sealed class ConceptManager : IConceptManager
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

            return Promise.Map<IEntityType, Pinvoke.EntityType>(
                Pinvoke.typedb_driver.concepts_get_entity_type(NativeTransaction, label), 
                obj => new EntityType(obj));
        }

        public Promise<IRelationType> GetRelationType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IRelationType, Pinvoke.RelationType>(
                Pinvoke.typedb_driver.concepts_get_relation_type(NativeTransaction, label), 
                obj => new RelationType(obj));
        }

        public Promise<IAttributeType> GetAttributeType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IAttributeType, Pinvoke.AttributeType>(
                Pinvoke.typedb_driver.concepts_get_attribute_type(NativeTransaction, label), 
                obj => new AttributeType(obj));
        }

        public Promise<IEntityType> PutEntityType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IEntityType, Pinvoke.EntityType>(
                Pinvoke.typedb_driver.concepts_put_entity_type(NativeTransaction, label), 
                obj => new EntityType(obj));
        }

        public Promise<IRelationType> PutRelationType(string label) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IRelationType, Pinvoke.RelationType>(
                Pinvoke.typedb_driver.concepts_put_relation_type(NativeTransaction, label), 
                obj => new RelationType(obj));
        }

        public Promise<IAttributeType> PutAttributeType(string label, IValue.Type valueType) 
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IAttributeType, Pinvoke.AttributeType>(
                Pinvoke.typedb_driver.concepts_put_attribute_type(NativeTransaction, label, valueType.NativeObject), 
                obj => new AttributeType(obj));
        }

        public Promise<IEntity> GetEntity(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IEntity, Pinvoke.Entity>(
                Pinvoke.typedb_driver.concepts_get_entity(NativeTransaction, iid), 
                obj => new Entity(obj));
        }

        public Promise<IRelation> GetRelation(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IRelation, Pinvoke.Relation>(
                Pinvoke.typedb_driver.concepts_get_relation(NativeTransaction, iid), 
                obj => new Relation(obj));
        }

        public Promise<IAttribute> GetAttribute(string iid) 
        {
            InputChecker.NonEmptyString(iid, ConceptError.MISSING_IID);
            InputChecker.ThrowIfFalse(NativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return Promise.Map<IAttribute, Pinvoke.Attribute>(
                Pinvoke.typedb_driver.concepts_get_attribute(NativeTransaction, iid), 
                obj => new Attribute(obj));
        }

        public ICollection<TypeDBException> SchemaExceptions
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
