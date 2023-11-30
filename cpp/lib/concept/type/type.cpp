
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

#include "typedb/concept/type/type.hpp"
#include "typedb/common/exception.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptiterator.hpp"
#include "inc/conceptfuture.hpp"
#include "inc/macros.hpp"
#include "typedb/connection/transaction.hpp"

namespace TypeDB {

Type::Type(ConceptType conceptType, _native::Concept* conceptNative)
    : Concept(conceptType, conceptNative) {}

ConceptPtrFuture<Type> Type::getSupertype(Transaction& transaction) {
    return ConceptPtrFuture<Type>(getSuperTypeFutureNative(transaction));
}

ConceptIterable<Type> Type::getSupertypes(Transaction& transaction) {
    return ConceptIterable<Type>(getSupertypesIteratorNative(transaction));
}

ConceptIterable<Type> Type::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    return ConceptIterable<Type>(getSubtypesIteratorNative(transaction, transitivity));
}

// Logic to the appropriate native ConceptIterator for each type.
ConceptFutureWrapper* getSuperTypeFutureNativeforRootThingType(Transaction& transaction) {
    return new ConceptFutureWrapperExplicit(nullptr);
}

ConceptIteratorWrapper* getSubtypesIteratorNativeForRootThingType(Transaction& transaction, Transitivity transitivity) {
    _native::Transaction* nativeTransaction = ConceptFactory::getNative(transaction);
    auto rootEntity = _native::concepts_get_root_entity_type();
    auto rootAttribute = _native::concepts_get_root_attribute_type();
    auto rootRelation = _native::concepts_get_root_relation_type();

    auto entitySubtypeIterator = _native::entity_type_get_subtypes(nativeTransaction, rootEntity, (_native::Transitivity)transitivity);
    TypeDBDriverException::check_and_throw();
    auto attributeSubtypeIterator = _native::attribute_type_get_subtypes(nativeTransaction, rootAttribute, (_native::Transitivity)transitivity);
    TypeDBDriverException::check_and_throw();
    auto relationSubtypeIterator = _native::relation_type_get_subtypes(nativeTransaction, rootRelation, (_native::Transitivity)transitivity);
    TypeDBDriverException::check_and_throw();

    _native::concept_drop(rootEntity);
    _native::concept_drop(rootAttribute);
    _native::concept_drop(rootRelation);
    return new ConceptIteratorWrapperChained({relationSubtypeIterator, entitySubtypeIterator, attributeSubtypeIterator});
}

ConceptIteratorWrapper* getSupertypesIteratorNativeForRootThingType(Transaction& transaction) {
    auto rootEntity = _native::concepts_get_root_entity_type();
    auto rootThingTypePromise = _native::entity_type_get_supertype(ConceptFactory::getNative(transaction), rootEntity);
    TypeDBDriverException::check_and_throw();
    _native::concept_drop(rootEntity);
    return new ConceptPromiseWrappingIterator(rootThingTypePromise);
}


_native::ConceptPromise* getSuperTypeFutureNativeFor(_native::Transaction* tx, ConceptType conceptType, _native::Concept* concept) {
    assert(conceptType != ConceptType::ROOT_THING_TYPE);

    switch (conceptType) {
        case ConceptType::ROLE_TYPE:
            return _native::role_type_get_supertype(tx, concept);

        case ConceptType::ATTRIBUTE_TYPE:
            return _native::attribute_type_get_supertype(tx, concept);
        case ConceptType::ENTITY_TYPE:
            return _native::entity_type_get_supertype(tx, concept);
        case ConceptType::RELATION_TYPE:
            return _native::relation_type_get_supertype(tx, concept);
        default:
            ILLEGAL_STATE;
    }
}

_native::ConceptIterator* getSupertypesIteratorNativeFor(_native::Transaction* tx, ConceptType conceptType, _native::Concept* concept) {
    assert(conceptType != ConceptType::ROOT_THING_TYPE);

    switch (conceptType) {
        case ConceptType::ROLE_TYPE:
            return _native::role_type_get_supertypes(tx, concept);

        case ConceptType::ATTRIBUTE_TYPE:
            return _native::attribute_type_get_supertypes(tx, concept);
        case ConceptType::ENTITY_TYPE:
            return _native::entity_type_get_supertypes(tx, concept);
        case ConceptType::RELATION_TYPE:
            return _native::relation_type_get_supertypes(tx, concept);
        default:
            ILLEGAL_STATE;
    }
}
_native::ConceptIterator* getSubtypesIteratorNativeFor(_native::Transaction* tx, ConceptType conceptType, _native::Concept* concept, _native::Transitivity transitivity) {
    assert(conceptType != ConceptType::ROOT_THING_TYPE);

    switch (conceptType) {
        case ConceptType::ROLE_TYPE:
            return _native::role_type_get_subtypes(tx, concept, transitivity);

        case ConceptType::ATTRIBUTE_TYPE:
            return _native::attribute_type_get_subtypes(tx, concept, transitivity);
        case ConceptType::ENTITY_TYPE:
            return _native::entity_type_get_subtypes(tx, concept, transitivity);
        case ConceptType::RELATION_TYPE:
            return _native::relation_type_get_subtypes(tx, concept, transitivity);
        default:
            ILLEGAL_STATE;
    }
}

ConceptFutureWrapper* Type::getSuperTypeFutureNative(Transaction& transaction) {
    CHECK_NATIVE(conceptNative);
    CHECK_NATIVE(ConceptFactory::getNative(transaction));

    if (conceptType == ConceptType::ROOT_THING_TYPE) {
        return getSuperTypeFutureNativeforRootThingType(transaction);
    } else {
        _native::ConceptPromise* promiseNative = getSuperTypeFutureNativeFor(ConceptFactory::getNative(transaction), conceptType, conceptNative.get());
        TypeDBDriverException::check_and_throw();
        return new ConceptFutureWrapperSimple(promiseNative);
    }
}

ConceptIteratorWrapper* Type::getSubtypesIteratorNative(Transaction& transaction, Transitivity transitivity) {
    CHECK_NATIVE(conceptNative);
    CHECK_NATIVE(ConceptFactory::getNative(transaction));

    if (conceptType == ConceptType::ROOT_THING_TYPE) {
        return getSubtypesIteratorNativeForRootThingType(transaction, transitivity);
    } else {
        _native::ConceptIterator* iteratorNative = getSubtypesIteratorNativeFor(ConceptFactory::getNative(transaction), conceptType, conceptNative.get(), (_native::Transitivity)transitivity);
        TypeDBDriverException::check_and_throw();
        return new ConceptIteratorWrapperSimple(iteratorNative);
    }
}

ConceptIteratorWrapper* Type::getSupertypesIteratorNative(Transaction& transaction) {
    CHECK_NATIVE(conceptNative);
    CHECK_NATIVE(ConceptFactory::getNative(transaction));

    if (conceptType == ConceptType::ROOT_THING_TYPE) {
        return getSupertypesIteratorNativeForRootThingType(transaction);
    } else {
        _native::ConceptIterator* iteratorNative = getSupertypesIteratorNativeFor(ConceptFactory::getNative(transaction), conceptType, conceptNative.get());
        TypeDBDriverException::check_and_throw();
        return new ConceptIteratorWrapperSimple(iteratorNative);
    }
}

}  // namespace TypeDB
