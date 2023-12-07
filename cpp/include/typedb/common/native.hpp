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

#include <memory>
#include <utility>

// Including these triggers the guards, Skipping them when we do the include in the _native namespace
#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <functional>

namespace TypeDB {

namespace _native {

// Forward declare all relevant native types
struct Annotation;
struct BoolPromise;
struct Concept;
struct ConceptIterator;
struct ConceptMap;
struct ConceptMapGroup;
struct ConceptMapGroupIterator;
struct ConceptMapIterator;
struct ConceptPromise;
struct Connection;
struct Credential;
struct Database;
struct DatabaseIterator;
struct DatabaseManager;
struct Error;
struct Explainable;
struct Explainables;
struct Explanation;
struct ExplanationIterator;
struct Options;
struct ReplicaInfo;
struct ReplicaInfoIterator;
struct RolePlayer;
struct RolePlayerIterator;
struct Rule;
struct RuleIterator;
struct RulePromise;
struct SchemaException;
struct SchemaExceptionIterator;
struct Session;
struct StringIterator;
struct StringPairIterator;
struct StringPromise;
struct Transaction;
struct User;
struct UserIterator;
struct UserManager;
struct ValueGroup;
struct ValueGroupIterator;
struct VoidPromise;
struct StringPair;

}  // namespace _native

template <typename T>
using NativePointer = std::unique_ptr<T, std::function<void(T*)> >;

}  // namespace TypeDB
