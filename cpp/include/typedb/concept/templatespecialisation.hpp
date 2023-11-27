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
#pragma once


#include "typedb/common/future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

#include "typedb/concept/type/attributetype.hpp"
#include "typedb/concept/type/entitytype.hpp"
#include "typedb/concept/type/relationtype.hpp"
#include "typedb/concept/type/roletype.hpp"
#include "typedb/concept/type/thingtype.hpp"

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"
#include "typedb/concept/thing/thing.hpp"

#include "typedb/concept/value/value.hpp"
namespace TypeDB {

// ConceptFuture
#ifndef _MSC_VER
template <>
std::function<std::unique_ptr<Type>(ConceptFutureWrapper*)> ConceptPtrFuture<Type>::fn_nativePromiseResolve;

template <>
std::function<std::unique_ptr<RoleType>(ConceptFutureWrapper*)> ConceptPtrFuture<RoleType>::fn_nativePromiseResolve;

template <>
std::function<std::unique_ptr<ThingType>(ConceptFutureWrapper*)> ConceptPtrFuture<ThingType>::fn_nativePromiseResolve;

template <>
std::function<std::unique_ptr<EntityType>(ConceptFutureWrapper*)> ConceptPtrFuture<EntityType>::fn_nativePromiseResolve;
template <>
std::function<std::unique_ptr<AttributeType>(ConceptFutureWrapper*)> ConceptPtrFuture<AttributeType>::fn_nativePromiseResolve;
template <>
std::function<std::unique_ptr<RelationType>(ConceptFutureWrapper*)> ConceptPtrFuture<RelationType>::fn_nativePromiseResolve;


template <>
std::function<std::unique_ptr<Thing>(ConceptFutureWrapper*)> ConceptPtrFuture<Thing>::fn_nativePromiseResolve;

template <>
std::function<std::unique_ptr<Entity>(ConceptFutureWrapper*)> ConceptPtrFuture<Entity>::fn_nativePromiseResolve;
template <>
std::function<std::unique_ptr<Attribute>(ConceptFutureWrapper*)> ConceptPtrFuture<Attribute>::fn_nativePromiseResolve;
template <>
std::function<std::unique_ptr<Relation>(ConceptFutureWrapper*)> ConceptPtrFuture<Relation>::fn_nativePromiseResolve;
#endif

}  // namespace TypeDB
