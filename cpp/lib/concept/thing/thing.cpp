
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

#include "typedb/concept/thing/thing.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "../concept_factory.hpp"
#include "../../common/macros.hpp"
#include "../../common/utils.hpp"

namespace TypeDB {

Thing::Thing(ConceptType conceptType, _native::Concept* conceptNative)
    : Concept(conceptType, conceptNative) {}

std::string Thing::getIID() {
    CHECK_NATIVE(conceptNative);
    return Utils::stringFromNative(_native::thing_get_iid(conceptNative.get()));
}

std::unique_ptr<ThingType> Thing::getType() {
    return ConceptFactory::thingType(getTypeNative());
}

bool Thing::isInferred() {
    CHECK_NATIVE(conceptNative);
    return _native::thing_get_is_inferred(conceptNative.get());
}

VoidFuture Thing::drop(Transaction& transaction){
    CONCEPTAPI_CALL(VoidFuture, _native::thing_delete(ConceptFactory::getNative(transaction), conceptNative.get()))}

BoolFuture Thing::isDeleted(Transaction& transaction){
    CONCEPTAPI_CALL(BoolFuture, _native::thing_is_deleted(ConceptFactory::getNative(transaction), conceptNative.get()))}

ConceptIterable<Attribute> Thing::getHas(Transaction& transaction) {
    return getHas(transaction, Annotation::NONE);
}

ConceptIterable<Attribute> Thing::getHas(Transaction& transaction, const AttributeType* attribute) {
    std::vector<const AttributeType*> attrTypeVector;
    attrTypeVector.push_back(attribute);
    return getHas(transaction, attrTypeVector);
}

ConceptIterable<Attribute> Thing::getHas(Transaction& transaction, const std::vector<const AttributeType*>& attributeTypes) {
    const _native::Annotation* nativeAnnotations[1] = {nullptr};
    CONCEPTAPI_ITER(Attribute, _native::thing_get_has(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::toNativeArray(attributeTypes).data(), nativeAnnotations));
}

ConceptIterable<Attribute> Thing::getHas(Transaction& transaction, const std::vector<std::unique_ptr<AttributeType>>& attributeTypes) {
    const _native::Annotation* nativeAnnotations[1] = {nullptr};
    CONCEPTAPI_ITER(Attribute, _native::thing_get_has(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::toNativeArray(attributeTypes).data(), nativeAnnotations));
}


ConceptIterable<Attribute> Thing::getHas(Transaction& transaction, const std::vector<Annotation>& annotations) {
    const _native::Concept* nativeConcepts[1] = {nullptr};
    CONCEPTAPI_ITER(Attribute, _native::thing_get_has(ConceptFactory::getNative(transaction), conceptNative.get(), nativeConcepts, ConceptFactory::toNativeArray(annotations).data()));
}

VoidFuture Thing::setHas(Transaction& transaction, Attribute* attribute) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_set_has(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(attribute)));
}

VoidFuture Thing::unsetHas(Transaction& transaction, Attribute* attribute) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_unset_has(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(attribute)));
}


ConceptIterable<Relation> Thing::getRelations(Transaction& transaction) {
    return getRelations(transaction, std::vector<RoleType*>());
}

ConceptIterable<Relation> Thing::getRelations(Transaction& transaction, const std::vector<RoleType*>& roleTypes) {
    CONCEPTAPI_ITER(Relation, _native::thing_get_relations(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::toNativeArray(roleTypes).data()));
}

ConceptIterable<Relation> Thing::getRelations(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes) {
    CONCEPTAPI_ITER(Relation, _native::thing_get_relations(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::toNativeArray(roleTypes).data()));
}

ConceptIterable<RoleType> Thing::getPlaying(Transaction& transaction) {
    CONCEPTAPI_ITER(RoleType, _native::thing_get_playing(ConceptFactory::getNative(transaction), conceptNative.get()));
}

}  // namespace TypeDB
