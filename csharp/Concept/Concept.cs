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

using System;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;
using TypeDB.Driver.Concept;

using DriverError = TypeDB.Driver.Common.Error.Driver;
using InternalError = TypeDB.Driver.Common.Error.Internal;

namespace TypeDB.Driver.Concept
{
    public abstract class Concept : NativeObjectWrapper<Pinvoke.Concept>, IConcept 
    {
        protected Concept(Pinvoke.Concept nativeConcept)
            : base(nativeConcept)
        {
        }

        public static IConcept ConceptOf(Pinvoke.Concept nativeConcept)
        {
            if (Pinvoke.typedb_driver.concept_is_entity_type(nativeConcept))
                return new EntityType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation_type(nativeConcept))
                return new RelationType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute_type(nativeConcept))
                return new AttributeType(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_root_thing_type(nativeConcept))
                return new ThingType.Root(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_entity(nativeConcept))
                return new Entity(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_relation(nativeConcept))
                return new Relation(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_attribute(nativeConcept))
                return new Attribute(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_value(nativeConcept))
                return new Value(nativeConcept);
            if (Pinvoke.typedb_driver.concept_is_role_type(nativeConcept))
                return new RoleType(nativeConcept);

            throw new TypeDBDriverException(InternalError.UNEXPECTED_NATIVE_VALUE);
        }
        
        protected static Pinvoke.Transaction NativeTransaction(ITypeDBTransaction transaction) 
        {
            Pinvoke.Transaction nativeTransaction = ((ConceptManager)transaction.Concepts).NativeTransaction;
            Validator.ThrowIfFalse(nativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);
            
            return nativeTransaction;
        }

        public override string ToString()
        {
            return Pinvoke.typedb_driver.concept_to_string(NativeObject);
        }

        public override bool Equals(object? obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || this.GetType() != obj.GetType())
            {
                return false;
            }

            Concept that = (Concept)obj;
            return Pinvoke.typedb_driver.concept_equals(this.NativeObject, that.NativeObject);
        }

        public abstract override int GetHashCode();
    }
}
