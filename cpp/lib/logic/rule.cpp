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

#include "typedb/logic/rule.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../common/utils.hpp"

namespace TypeDB {

Rule::Rule(_native::Rule* ruleNative)
    : ruleNative(ruleNative, _native::rule_drop) {}

std::string Rule::label() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringFromNative(_native::rule_get_label(ruleNative.get()));
}

std::string Rule::when() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringFromNative(_native::rule_get_when(ruleNative.get()));
}

std::string Rule::then() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringFromNative(_native::rule_get_then(ruleNative.get()));
}

template <>
std::optional<Rule> FutureHelper<std::optional<Rule>, _native::RulePromise>::resolve(_native::RulePromise* promiseNative) {
    _native::Rule* ruleNative = _native::rule_promise_resolve(promiseNative);
    DriverException::check_and_throw();
    return (nullptr != ruleNative) ? std::optional<Rule>(Rule(ruleNative)) : std::optional<Rule>();
}

TYPEDB_FUTURE_HELPER_1(Rule, _native::RulePromise, _native::rule_promise_resolve, Rule);

TYPEDB_ITERATOR_HELPER(
    _native::RuleIterator,
    _native::Rule,
    Rule,
    _native::rule_iterator_drop,
    _native::rule_iterator_next,
    _native::rule_drop);
}  // namespace TypeDB
