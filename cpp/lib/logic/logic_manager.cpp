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

#include "typedb/logic/logic_manager.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "typedb/concept/value/value.hpp"

#include "../inc/macros.hpp"

namespace TypeDB {

OptionalRuleFuture LogicManager::getRule(const std::string& label) const {
    CHECK_NATIVE(transaction);
    WRAPPED_NATIVE_CALL(OptionalRuleFuture, _native::logic_manager_get_rule(transaction->getNative(), label.c_str()));
}

RuleIterable LogicManager::getRules() const {
    CHECK_NATIVE(transaction);
    WRAPPED_NATIVE_CALL(RuleIterable, _native::logic_manager_get_rules(transaction->getNative()));
}

RuleFuture LogicManager::putRule(const std::string& label, const std::string& when, const std::string& then) const {
    CHECK_NATIVE(transaction);
    WRAPPED_NATIVE_CALL(RuleFuture, _native::logic_manager_put_rule(transaction->getNative(), label.c_str(), when.c_str(), then.c_str()));
}

LogicManager::LogicManager(TypeDB::Transaction* transaction)
    : transaction(transaction) {}

}  // namespace TypeDB
