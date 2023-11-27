
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

#include "typedb/concept/type/relationtype.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/concept/thing/relation.hpp"

#include "typedb/connection/transaction.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptresultwrapper.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

RelationType::RelationType(_native::Concept* conceptNative)
    : ThingType(ConceptType::RELATION_TYPE, conceptNative) {}

ConceptPtrFuture<Relation> RelationType::create(Transaction& transaction) {
    CONCEPTAPI_FUTURE(Relation, _native::relation_type_create(ConceptFactory::getNative(transaction), conceptNative.get()));
}

ConceptIterable<Relation> RelationType::getInstances(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(Relation, _native::relation_type_get_instances(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptIterable<RoleType> RelationType::getRelates(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(RoleType, _native::relation_type_get_relates(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}
ConceptPtrFuture<RoleType> RelationType::getRelates(Transaction& transaction, const std::string& roleLabel) {
    CONCEPTAPI_FUTURE(RoleType, _native::relation_type_get_relates_for_role_label(ConceptFactory::getNative(transaction), conceptNative.get(), roleLabel.c_str()));
}
ConceptPtrFuture<RoleType> RelationType::getRelatesOverridden(Transaction& transaction, RoleType* roleType) {
    CONCEPTAPI_FUTURE(RoleType, _native::relation_type_get_relates_overridden(ConceptFactory::getNative(transaction), conceptNative.get(), roleType->getName().c_str()));
}

ConceptPtrFuture<RoleType> RelationType::getRelatesOverridden(Transaction& transaction, const std::string& roleLabel) {
    CONCEPTAPI_FUTURE(RoleType, _native::relation_type_get_relates_overridden(ConceptFactory::getNative(transaction), conceptNative.get(), roleLabel.c_str()));
}

VoidFuture RelationType::setRelates(Transaction& transaction, const std::string& roleLabel) {
    return setRelatesNative(transaction, roleLabel.c_str(), nullptr);
}

VoidFuture RelationType::setRelates(Transaction& transaction, const std::string& roleLabel, RoleType* overriddenType) {
    return setRelatesNative(transaction, roleLabel.c_str(), overriddenType->getName().c_str());
}

VoidFuture RelationType::setRelates(Transaction& transaction, const std::string& roleLabel, const std::string& overriddenLabel) {
    return setRelatesNative(transaction, roleLabel.c_str(), overriddenLabel.c_str());
}

VoidFuture RelationType::unsetRelates(Transaction& transaction, RoleType* roleType) {
    CONCEPTAPI_CALL(VoidFuture, _native::relation_type_unset_relates(ConceptFactory::getNative(transaction), conceptNative.get(), roleType->getName().c_str()));
}

VoidFuture RelationType::unsetRelates(Transaction& transaction, const std::string& roleLabel) {
    CONCEPTAPI_CALL(VoidFuture, _native::relation_type_unset_relates(ConceptFactory::getNative(transaction), conceptNative.get(), roleLabel.c_str()));
}

ConceptIterable<RelationType> RelationType::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(RelationType, _native::relation_type_get_subtypes(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

VoidFuture RelationType::setSupertype(Transaction& transaction, RelationType* superRelationType) {
    // CHECK_NATIVE(superRelationType);
    CONCEPTAPI_CALL(VoidFuture, _native::relation_type_set_supertype(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(superRelationType)));
}

// privates
VoidFuture RelationType::setRelatesNative(Transaction& transaction, const char* roleLabel, const char* overriddenRoleLabel) {
    CONCEPTAPI_CALL(VoidFuture, _native::relation_type_set_relates(ConceptFactory::getNative(transaction), conceptNative.get(), roleLabel, overriddenRoleLabel));
}

}  // namespace TypeDB
