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

#include "typedb/concept/concept_manager.hpp"
#include "typedb/connection/transaction.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../common/utils.hpp"
#include "./concept_factory.hpp"
#include "./future.hpp"

namespace TypeDB {

#define CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, ...)                                                                                      \
    {                                                                                                                                    \
        CHECK_NATIVE(transaction);                                                                                                       \
        WRAPPED_NATIVE_CALL(ConceptPtrFuture<TYPE>, new ConceptFutureWrapperSimple(NATIVE_FUNC(transaction->getNative(), __VA_ARGS__))); \
    }

#define CONCEPTMANAGER_TYPE(TYPE, NATIVE_FUNC) CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, label.c_str())
#define CONCEPTMANAGER_THING(TYPE, NATIVE_FUNC) CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, iid.c_str())


ConceptManager::ConceptManager(TypeDB::Transaction* transaction)
    : transaction(transaction) {}

std::unique_ptr<EntityType> ConceptManager::getRootEntityType() const {
    return ConceptFactory::entityType(_native::concepts_get_root_entity_type());
}

std::unique_ptr<RelationType> ConceptManager::getRootRelationType() const {
    return ConceptFactory::relationType(_native::concepts_get_root_entity_type());
}

std::unique_ptr<AttributeType> ConceptManager::getRootAttributeType() const {
    return ConceptFactory::attributeType(_native::concepts_get_root_entity_type());
}

ConceptPtrFuture<EntityType> ConceptManager::getEntityType(const std::string& label) const {
    CONCEPTMANAGER_TYPE(EntityType, concepts_get_entity_type);
}

ConceptPtrFuture<RelationType> ConceptManager::getRelationType(const std::string& label) const {
    CONCEPTMANAGER_TYPE(RelationType, _native::concepts_get_relation_type);
}

ConceptPtrFuture<AttributeType> ConceptManager::getAttributeType(const std::string& label) const {
    CONCEPTMANAGER_TYPE(AttributeType, _native::concepts_get_attribute_type);
}

ConceptPtrFuture<EntityType> ConceptManager::putEntityType(const std::string& label) const {
    CONCEPTMANAGER_TYPE(EntityType, _native::concepts_put_entity_type);
}

ConceptPtrFuture<RelationType> ConceptManager::putRelationType(const std::string& label) const {
    CONCEPTMANAGER_TYPE(RelationType, _native::concepts_put_relation_type);
}

ConceptPtrFuture<AttributeType> ConceptManager::putAttributeType(const std::string& label, ValueType valueType) const {
    CONCEPTMANAGER_CALL(AttributeType, _native::concepts_put_attribute_type, label.c_str(), (_native::ValueType)valueType);
}

ConceptPtrFuture<Entity> ConceptManager::getEntity(const std::string& iid) const {
    CONCEPTMANAGER_THING(Entity, _native::concepts_get_entity);
}

ConceptPtrFuture<Relation> ConceptManager::getRelation(const std::string& iid) const {
    CONCEPTMANAGER_THING(Relation, _native::concepts_get_relation);
}

ConceptPtrFuture<Attribute> ConceptManager::getAttribute(const std::string& iid) const {
    CONCEPTMANAGER_THING(Attribute, _native::concepts_get_attribute);
}

using SchemaExceptionIterator = TypeDBIterator<_native::SchemaExceptionIterator, _native::SchemaException, TypeDBDriverException>;
using SchemaExceptionIterable = TypeDBIterable<_native::SchemaExceptionIterator, _native::SchemaException, TypeDBDriverException>;

std::vector<TypeDBDriverException> ConceptManager::getSchemaExceptions() {
    CHECK_NATIVE(transaction);
    std::vector<TypeDBDriverException> exceptions;
    SchemaExceptionIterable exIterable(_native::concepts_get_schema_exceptions(transaction->getNative()));
    for (TypeDBDriverException& ex : exIterable) {
        exceptions.push_back(ex);
    }
    return exceptions;
}

TYPEDB_ITERATOR_HELPER(
    _native::SchemaExceptionIterator,
    _native::SchemaException,
    TypeDBDriverException,
    _native::schema_exception_iterator_drop,
    _native::schema_exception_iterator_next,
    _native::schema_exception_drop);

}  // namespace TypeDB
