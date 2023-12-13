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

using RuleFuture = Future<Rule, _native::RulePromise>;
using OptionalRuleFuture = Future<std::optional<Rule>, _native::RulePromise>;

using RuleIterable = Iterable<_native::RuleIterator, _native::Rule, Rule>;
using RuleIterator = Iterator<_native::RuleIterator, _native::Rule, Rule>;

class Rule {
public:
    Rule(Rule&&) = default;
    Rule& operator=(Rule&&) = default;
    ~Rule() = default;

    std::string label();
    std::string when();
    std::string then();
    std::string toString();

private:
    Rule(_native::Rule*);
    Rule(const Rule&) = delete;
    Rule& operator=(const Rule&) = delete;

    NativePointer<_native::Rule> ruleNative;

    friend class Explanation;
    friend class IteratorHelper<_native::RuleIterator, _native::Rule, Rule>;
    friend class FutureHelper<Rule, _native::RulePromise>;
    friend class FutureHelper<std::optional<Rule>, _native::RulePromise>;
};

}  // namespace TypeDB
