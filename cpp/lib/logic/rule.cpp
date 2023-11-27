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

#include "inc/macros.hpp"
#include "inc/utils.hpp"

namespace TypeDB {

Rule::Rule(_native::Rule* ruleNative)
    : ruleNative(ruleNative, _native::rule_drop) {}

Rule::Rule(Rule&& from) {
    *this = std::move(from);
}

Rule& Rule::operator=(Rule&& from) {
    ruleNative = std::move(from.ruleNative);
    return *this;
}

std::string Rule::label() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringAndFree(_native::rule_get_label(ruleNative.get()));
}

std::string Rule::when() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringAndFree(_native::rule_get_when(ruleNative.get()));
}

std::string Rule::then() {
    CHECK_NATIVE(ruleNative);
    return Utils::stringAndFree(_native::rule_get_then(ruleNative.get()));
}

Rule ruleFutureResolve(_native::RulePromise* promiseNative) {
    _native::Rule* ruleNative = _native::rule_promise_resolve(promiseNative);
    TypeDBDriverException::check_and_throw();
    return Rule(ruleNative);
}

std::unique_ptr<Rule> rulePtrFutureResolve(_native::RulePromise* promiseNative) {
    _native::Rule* ruleNative = _native::rule_promise_resolve(promiseNative);
    TypeDBDriverException::check_and_throw();
    return (nullptr != ruleNative) ? std::unique_ptr<Rule>(new Rule(ruleNative)) : nullptr;
}

template <>
std::function<Rule(_native::RulePromise*)> RuleFuture::fn_nativePromiseResolve = ruleFutureResolve;
template <>
std::function<std::unique_ptr<Rule>(_native::RulePromise*)> RulePtrFuture::fn_nativePromiseResolve = rulePtrFutureResolve;

TYPEDB_ITERATOR_HELPER(
    _native::RuleIterator,
    _native::Rule,
    Rule,
    _native::rule_iterator_drop,
    _native::rule_iterator_next,
    _native::rule_drop);

}  // namespace TypeDB
