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

#include "typedb/answer/concept_map.hpp"
#include "typedb/common/native.hpp"
#include "typedb/logic/rule.hpp"

namespace TypeDB {

class Transaction;

class LogicManager {
public:
    ~LogicManager() = default;

    OptionalRuleFuture getRule(const std::string& label) const;
    RuleIterable getRules() const;
    [[nodiscard]] RuleFuture putRule(const std::string& label, const std::string& when, const std::string& then) const;

private:
    TypeDB::Transaction* const transaction;
    LogicManager(TypeDB::Transaction*);
    LogicManager(LogicManager&&) noexcept = delete;
    LogicManager& operator=(LogicManager&&) = delete;
    LogicManager(const LogicManager&) = delete;
    LogicManager& operator=(const LogicManager&) = delete;

    friend class TypeDB::Transaction;
};

}  // namespace TypeDB
