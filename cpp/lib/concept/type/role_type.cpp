
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

#include "typedb/concept/type/role_type.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "../../common/macros.hpp"
#include "../../common/native.hpp"
#include "../../common/utils.hpp"
#include "../concept_factory.hpp"

namespace TypeDB {

RoleType::RoleType(_native::Concept* conceptNative)
    : Type(ConceptType::ROLE_TYPE, conceptNative) {}

std::string RoleType::getName() {
    CHECK_NATIVE(conceptNative);
    return Utils::stringFromNative(_native::role_type_get_name(conceptNative.get()));
}

std::string RoleType::getScope() {
    CHECK_NATIVE(conceptNative);
    return Utils::stringFromNative(_native::role_type_get_scope(conceptNative.get()));
}

std::string RoleType::getLabel() {
    return getScope() + ":" + getName();
}

VoidFuture RoleType::setLabel(Transaction& transaction, const std::string& newLabel) {
    CONCEPTAPI_CALL(VoidFuture, _native::role_type_set_label(ConceptFactory::getNative(transaction), conceptNative.get(), newLabel.c_str()));
}

bool RoleType::isAbstract() {
    CHECK_NATIVE(conceptNative);
    return _native::role_type_is_abstract(conceptNative.get());
}

VoidFuture RoleType::drop(Transaction& transaction) {
    CONCEPTAPI_CALL(VoidFuture, _native::role_type_delete(ConceptFactory::getNative(transaction), conceptNative.get()));
}

BoolFuture RoleType::isDeleted(Transaction& transaction) {
    CONCEPTAPI_CALL(BoolFuture, _native::role_type_is_deleted(ConceptFactory::getNative(transaction), conceptNative.get()));
}

// "Inherited"
ConceptPtrFuture<RoleType> RoleType::getSupertype(Transaction& transaction) {
    return ConceptPtrFuture<RoleType>(getSuperTypeFutureNative(transaction));
}

ConceptIterable<RoleType> RoleType::getSupertypes(Transaction& transaction) {
    return ConceptIterable<RoleType>(getSupertypesIteratorNative(transaction));
}

ConceptIterable<RoleType> RoleType::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    return ConceptIterable<RoleType>(getSubtypesIteratorNative(transaction, transitivity));
}

ConceptPtrFuture<RelationType> RoleType::getRelationType(Transaction& transaction){
    CONCEPTAPI_FUTURE(RelationType, _native::role_type_get_relation_type(ConceptFactory::getNative(transaction), conceptNative.get()))}

ConceptIterable<RelationType> RoleType::getRelationTypes(Transaction& transaction) {
    CONCEPTAPI_ITER(RelationType, _native::role_type_get_relation_types(ConceptFactory::getNative(transaction), conceptNative.get()));
}

ConceptIterable<ThingType> RoleType::getPlayerTypes(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(ThingType, _native::role_type_get_player_types(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptIterable<Relation> RoleType::getRelationInstances(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(Relation, _native::role_type_get_relation_instances(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptIterable<Thing> RoleType::getPlayerInstances(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(Thing, _native::role_type_get_player_instances(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

}  // namespace TypeDB
