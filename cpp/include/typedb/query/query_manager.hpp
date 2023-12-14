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
#include "typedb/common/native.hpp"
#include "typedb/connection/options.hpp"

#include "typedb/answer/concept_map.hpp"
#include "typedb/answer/concept_map_group.hpp"
#include "typedb/answer/json.hpp"
#include "typedb/answer/value_future.hpp"
#include "typedb/answer/value_group.hpp"
#include "typedb/concept/concept.hpp"

#include "typedb/logic/explainable.hpp"
#include "typedb/logic/explanation.hpp"

namespace TypeDB {

using JSONIterable = Iterable<_native::StringIterator, char, JSON>;

class Transaction;

class QueryManager {
public:
    ~QueryManager() = default;
    [[nodiscard]] VoidFuture define(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] VoidFuture undefine(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] ConceptMapIterable get(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] JSONIterable fetch(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] ConceptMapIterable insert(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] VoidFuture matchDelete(const std::string& query, const Options& options = Options()) const;
    [[nodiscard]] ConceptMapIterable update(const std::string& query, const Options& = Options()) const;
    [[nodiscard]] AggregateFuture getAggregate(const std::string& query, const Options& = Options()) const;
    [[nodiscard]] ConceptMapGroupIterable getGroup(const std::string& query, const Options& = Options()) const;
    [[nodiscard]] ValueGroupIterable getGroupAggregate(const std::string& query, const Options& = Options()) const;
    [[nodiscard]] ExplanationIterable explain(const Explainable& explainable, const Options& = Options()) const;

private:
    TypeDB::Transaction* const transaction;
    QueryManager(TypeDB::Transaction*);
    QueryManager(QueryManager&&) noexcept = delete;
    QueryManager& operator=(QueryManager&&) = delete;
    QueryManager(const QueryManager&) = delete;
    QueryManager& operator=(const QueryManager&) = delete;

    friend class TypeDB::Transaction;
};

}  // namespace TypeDB
