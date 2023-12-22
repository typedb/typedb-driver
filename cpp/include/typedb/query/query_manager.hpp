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
#include "typedb/answer/explainable.hpp"
#include "typedb/answer/json.hpp"
#include "typedb/answer/value_future.hpp"
#include "typedb/answer/value_group.hpp"
#include "typedb/concept/concept.hpp"
#include "typedb/logic/explanation.hpp"

namespace TypeDB {

using JSONIterable = Iterable<_native::StringIterator, char, JSON>;

class Transaction;

/**
 * \brief Provides methods for executing TypeQL queries in the transaction.
 */
class QueryManager {
public:
    ~QueryManager() = default;

    /**
     * Performs a TypeQL Define query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.define(query, options).get()
     * </pre>
     *
     * @param query The TypeQL Define query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] VoidFuture define(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Undefine query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.undefine(query, options).get()
     * </pre>
     *
     * @param query The TypeQL Undefine query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] VoidFuture undefine(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Insert query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.insert(query, options)
     * </pre>
     *
     * @param query The TypeQL Insert query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] ConceptMapIterable insert(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Delete query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.matchDelete(query, options).get()
     * </pre>
     *
     * @param query The TypeQL Delete query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] VoidFuture matchDelete(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Update query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.update(query, options)
     * </pre>
     *
     * @param query The TypeQL Update query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] ConceptMapIterable update(const std::string& query, const Options& = Options()) const;

    /**
     * Performs a TypeQL Get query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.get(query, options)
     * </pre>
     *
     * @param query The TypeQL Get query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] ConceptMapIterable get(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Fetch query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.fetch(query, options)
     * </pre>
     *
     * @param query The TypeQL Fetch query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] JSONIterable fetch(const std::string& query, const Options& options = Options()) const;

    /**
     * Performs a TypeQL Get Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.getAggregate(query, options).get()
     * </pre>
     *
     * @param query The TypeQL Get Aggregate query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] AggregateFuture getAggregate(const std::string& query, const Options& = Options()) const;

    /**
     * Performs a TypeQL Get Group query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.getGroup(query, options)
     * </pre>
     *
     * @param query The TypeQL Get Group query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] ConceptMapGroupIterable getGroup(const std::string& query, const Options& = Options()) const;

    /**
     * Performs a TypeQL Get Group Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.getGroupAggregate(query, options)
     * </pre>
     *
     * @param query The TypeQL Get Group Aggregate query to be executed
     * @param options Specify query options
     */
    [[nodiscard]] ValueGroupIterable getGroupAggregate(const std::string& query, const Options& = Options()) const;

    /**
     * Performs a TypeQL Explain query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query.explain(explainable, options)
     * </pre>
     *
     * @param explainable The Explainable to be explained
     * @param options Specify query options
     */
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
