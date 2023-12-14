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

#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/concept/thing/attribute.hpp"
#include "typedb/connection/transaction.hpp"

#include "../../common/macros.hpp"
#include "../../common/native.hpp"
#include "../concept_factory.hpp"

namespace TypeDB {

ConceptPtrFuture<Attribute> putNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative);
ConceptPtrFuture<Attribute> putNativeAndFree(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative);
ConceptPtrFuture<Attribute> getNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative);
ConceptPtrFuture<Attribute> getNativeAndFree(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative);

// AttributeType
AttributeType::AttributeType(_native::Concept* conceptNative)
    : ThingType(ConceptType::ATTRIBUTE_TYPE, conceptNative) {}

ValueType AttributeType::getValueType() {
    CHECK_NATIVE(conceptNative);
    return (ValueType)_native::attribute_type_get_value_type(conceptNative.get());
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, Value* value) {
    CHECK_NATIVE(value);
    return putNative(conceptNative, transaction, ConceptFactory::getNative(value));
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, const std::string& value) {
    return putNativeAndFree(conceptNative, transaction, _native::value_new_string(value.c_str()));
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, int64_t value) {
    return putNativeAndFree(conceptNative, transaction, _native::value_new_long(value));
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, double value) {
    return putNativeAndFree(conceptNative, transaction, _native::value_new_double(value));
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, bool value) {
    return putNativeAndFree(conceptNative, transaction, _native::value_new_boolean(value));
}

ConceptPtrFuture<Attribute> AttributeType::put(Transaction& transaction, DateTime value) {
    return putNativeAndFree(conceptNative, transaction, _native::value_new_date_time_from_millis(value.time_since_epoch().count()));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, Value* value) {
    return getNative(conceptNative, transaction, ConceptFactory::getNative(value));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, const std::string& value) {
    return getNativeAndFree(conceptNative, transaction, _native::value_new_string(value.c_str()));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, int64_t value) {
    return getNativeAndFree(conceptNative, transaction, _native::value_new_long(value));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, double value) {
    return getNativeAndFree(conceptNative, transaction, _native::value_new_double(value));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, bool value) {
    return getNativeAndFree(conceptNative, transaction, _native::value_new_boolean(value));
}

ConceptPtrFuture<Attribute> AttributeType::get(Transaction& transaction, DateTime value) {
    return getNativeAndFree(conceptNative, transaction, _native::value_new_date_time_from_millis(value.time_since_epoch().count()));
}

OptionalStringFuture AttributeType::getRegex(Transaction& transaction) {
    CONCEPTAPI_CALL(OptionalStringFuture, _native::attribute_type_get_regex(ConceptFactory::getNative(transaction), conceptNative.get()));
}

VoidFuture AttributeType::setRegex(Transaction& transaction, const std::string& regex) {
    CONCEPTAPI_CALL(VoidFuture, _native::attribute_type_set_regex(ConceptFactory::getNative(transaction), conceptNative.get(), regex.c_str()));
}

VoidFuture AttributeType::unsetRegex(Transaction& transaction) {
    CONCEPTAPI_CALL(VoidFuture, _native::attribute_type_unset_regex(ConceptFactory::getNative(transaction), conceptNative.get()));
}

VoidFuture AttributeType::setSupertype(Transaction& transaction, AttributeType* attributeType) {
    CONCEPTAPI_CALL(VoidFuture, _native::attribute_type_set_supertype(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(attributeType)));
}

ConceptIterable<AttributeType> AttributeType::getSubtypes(Transaction& transaction, ValueType valueType, Transitivity transitivity) {
    CONCEPTAPI_ITER(AttributeType,
                    _native::attribute_type_get_subtypes_with_value_type(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::ValueType)valueType, (_native::Transitivity)transitivity));
}

// "Inherited"
ConceptIterable<AttributeType> AttributeType::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    return ConceptIterable<AttributeType>(getSubtypesIteratorNative(transaction, transitivity));
}

ConceptIterable<Attribute> AttributeType::getInstances(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(Attribute,
                    _native::attribute_type_get_instances(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptIterable<ThingType> AttributeType::getOwners(Transaction& transaction, Transitivity transitivity) {
    return getOwners(transaction, {}, transitivity);
}

ConceptIterable<ThingType> AttributeType::getOwners(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity) {
    CONCEPTAPI_ITER(ThingType,
                    _native::attribute_type_get_owners(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity, ConceptFactory::nativeAnnotationArray(&annotations).data()));
}

ConceptIterable<ThingType> AttributeType::getOwners(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity) {
    CONCEPTAPI_ITER(ThingType,
                    _native::attribute_type_get_owners(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity, ConceptFactory::nativeAnnotationArray(&annotations).data()));
}

ConceptPtrFuture<Attribute> putNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative) {
    CONCEPTAPI_FUTURE(Attribute, _native::attribute_type_put(ConceptFactory::getNative(transaction), conceptNative.get(), valueNative));
}

ConceptPtrFuture<Attribute> putNativeAndFree(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative) {
    auto ret = putNative(conceptNative, transaction, valueNative);
    _native::concept_drop(valueNative);
    return ret;
}
ConceptPtrFuture<Attribute> getNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative) {
    CONCEPTAPI_FUTURE(Attribute, _native::attribute_type_get(ConceptFactory::getNative(transaction), conceptNative.get(), valueNative));
}

ConceptPtrFuture<Attribute> getNativeAndFree(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, _native::Concept* valueNative) {
    auto ret = getNative(conceptNative, transaction, valueNative);
    _native::concept_drop(valueNative);
    return ret;
}

}  // namespace TypeDB
