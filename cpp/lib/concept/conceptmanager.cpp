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

#include "typedb/concept/conceptmanager.hpp"
#include "typedb/connection/transaction.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptresultwrapper.hpp"
#include "inc/macros.hpp"
#include "inc/utils.hpp"

namespace TypeDB {

#define CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, ...)                                                                                            \
    {                                                                                                                                          \
        CHECK_NATIVE(parentTransaction);                                                                                                       \
        WRAPPED_NATIVE_CALL(ConceptPtrFuture<TYPE>, new ConceptFutureWrapperSimple(NATIVE_FUNC(parentTransaction->getNative(), __VA_ARGS__))); \
    }

#define CONCEPTMANAGER_TYPE(TYPE, NATIVE_FUNC) CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, label.c_str())
#define CONCEPTMANAGER_THING(TYPE, NATIVE_FUNC) CONCEPTMANAGER_CALL(TYPE, NATIVE_FUNC, iid.c_str())


ConceptManager::ConceptManager(TypeDB::Transaction* parentTransaction)
    : parentTransaction(parentTransaction) {}

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

struct SchemaExceptionWrapper {
    std::string code;
    std::string message;
    SchemaExceptionWrapper(_native::SchemaException* e) {
        CHECK_NATIVE(e);
        code = Utils::stringAndFree(_native::schema_exception_code(e));
        message = Utils::stringAndFree(_native::schema_exception_message(e));
        _native::schema_exception_drop(e);
    }
};

using SchemaExceptionIterator = TypeDBIterator<_native::SchemaExceptionIterator, _native::SchemaException, SchemaExceptionWrapper>;
using SchemaExceptionIterable = TypeDBIterable<_native::SchemaExceptionIterator, _native::SchemaException, SchemaExceptionWrapper>;

std::vector<TypeDBDriverException> ConceptManager::getSchemaExceptions() {
    CHECK_NATIVE(parentTransaction);
    std::vector<TypeDBDriverException> exceptions;
    SchemaExceptionIterable exIterable(_native::concepts_get_schema_exceptions(parentTransaction->getNative()));
    for (SchemaExceptionWrapper& exWrapped : exIterable) {
        exceptions.push_back(TypeDBDriverException(exWrapped.code.c_str(), exWrapped.message.c_str()));
    }
    return exceptions;
}

TYPEDB_ITERATOR_HELPER(
    _native::SchemaExceptionIterator,
    _native::SchemaException,
    SchemaExceptionWrapper,
    _native::schema_exception_iterator_drop,
    _native::schema_exception_iterator_next,
    _native::schema_exception_drop);

}  // namespace TypeDB
