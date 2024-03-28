/*
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

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/concept/value/value.hpp"

#include "typedb/connection/transaction.hpp"

#include "../../common/macros.hpp"
#include "../../common/native.hpp"
#include "../concept_factory.hpp"


namespace TypeDB {

Attribute::Attribute(_native::Concept* conceptNative)
    : Thing(ConceptType::ATTRIBUTE, conceptNative) {}


std::unique_ptr<AttributeType> Attribute::getType() {
    ;
    return ConceptFactory::attributeType(getTypeNative());
}

std::unique_ptr<Value> Attribute::getValue() {
    CHECK_NATIVE(conceptNative);
    WRAPPED_NATIVE_CALL(ConceptFactory::value, _native::attribute_get_value(conceptNative.get()));
}

ConceptIterable<Thing> Attribute::getOwners(Transaction& transaction) {
    return getOwners(transaction, nullptr);
}

ConceptIterable<Thing> Attribute::getOwners(Transaction& transaction, const ThingType* ownerType) {
    auto nativeOwnerType = ownerType != nullptr ? ConceptFactory::getNative(ownerType) : nullptr;
    CONCEPTAPI_ITER(Thing, _native::attribute_get_owners(ConceptFactory::getNative(transaction), conceptNative.get(), nativeOwnerType));
}

// protected
_native::Concept* Attribute::getTypeNative() {
    CHECK_NATIVE(conceptNative);
    return _native::attribute_get_type(conceptNative.get());
}

}  // namespace TypeDB
