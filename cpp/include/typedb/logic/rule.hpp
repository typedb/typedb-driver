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

#include <string>

#include "typedb/common/future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class Explanation;  // Forward declaration for friendship
class Rule;

using RuleFuture = TypeDBFuture<Rule, _native::RulePromise>;
using RulePtrFuture = TypeDBFuture<std::unique_ptr<Rule>, _native::RulePromise>;

using RuleIterable = TypeDBIterable<_native::RuleIterator, _native::Rule, Rule>;
using RuleIterator = TypeDBIterator<_native::RuleIterator, _native::Rule, Rule>;

Rule ruleFutureResolve(_native::RulePromise* promiseNative);
std::unique_ptr<Rule> rulePtrFutureResolve(_native::RulePromise* promiseNative);

class Rule {
   public:
    Rule(Rule&&);
    Rule& operator=(Rule&&);

    std::string label();
    std::string when();
    std::string then();

   private:
    Rule(_native::Rule*);
    Rule(const Rule&) = delete;
    Rule& operator=(const Rule&) = delete;

    NativePointer<_native::Rule> ruleNative;

    friend class Explanation;
    friend class TypeDBIteratorHelper<_native::RuleIterator, _native::Rule, Rule>;
    friend class TypeDBFuture<Rule, _native::RulePromise>;
    friend Rule ruleFutureResolve(_native::RulePromise* promiseNative);
    friend std::unique_ptr<Rule> rulePtrFutureResolve(_native::RulePromise* promiseNative);
};

#ifndef _MSC_VER
template <>
std::function<Rule(_native::RulePromise*)> RuleFuture::fn_nativePromiseResolve;
template <>
std::function<std::unique_ptr<Rule>(_native::RulePromise*)> RulePtrFuture::fn_nativePromiseResolve;
#endif

}  // namespace TypeDB
