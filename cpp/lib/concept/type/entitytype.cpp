
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

#include "typedb/concept/type/entitytype.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptresultwrapper.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

EntityType::EntityType(_native::Concept* conceptNative)
    : ThingType(ConceptType::ENTITY_TYPE, conceptNative) {}

ConceptPtrFuture<Entity> EntityType::create(Transaction& transaction) {
    CONCEPTAPI_FUTURE(Entity, _native::entity_type_create(ConceptFactory::getNative(transaction), conceptNative.get()));
}

ConceptIterable<Entity> EntityType::getInstances(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(Entity, _native::entity_type_get_instances(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptIterable<EntityType> EntityType::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(EntityType, _native::entity_type_get_subtypes(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

VoidFuture EntityType::setSupertype(Transaction& transaction, EntityType* superEntityType) {
    CONCEPTAPI_CALL(VoidFuture, _native::entity_type_set_supertype(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(superEntityType)));
}

}  // namespace TypeDB
