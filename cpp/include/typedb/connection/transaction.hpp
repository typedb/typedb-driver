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

#include "typedb/common/native.hpp"
#include "typedb/concept/concept_manager.hpp"
#include "typedb/connection/options.hpp"
#include "typedb/logic/logic_manager.hpp"
#include "typedb/query/query_manager.hpp"

namespace TypeDB {

/**
 * Used to specify the type of transaction.
 *
 * <h3>Examples</h3>
 * <pre>
 * session.transaction(TransactionType.READ);
 * </pre>
 */
enum class TransactionType {
    READ,
    WRITE,
};

// forward declaration for friendship
class Session;
class Concept;


/**
 * \brief A transaction with a TypeDB database.
 */
class Transaction {
private:
    NativePointer<_native::Transaction> transactionNative;
    TypeDB::TransactionType txnType;

public:
    /**
     * The<code></code>QueryManager<code></code> for this Transaction, from which any TypeQL query can be executed.
     */
    const QueryManager query;

    /**
     * The <code>ConceptManager</code> for this transaction, providing access to all Concept API methods.
     */
    const ConceptManager concepts;

    /**
     * The <code>LogicManager</code> for this Transaction, providing access to all Concept API - Logic methods.
     */
    const LogicManager logic;

    Transaction(const Transaction&) = delete;
    Transaction(Transaction&&);
    ~Transaction() = default;

    Transaction& operator=(const Transaction&) = delete;
    Transaction& operator=(Transaction&&);

    /**
     * The transaction’s type (READ or WRITE)
     */
    TypeDB::TransactionType type() const;

    /**
     * Checks whether this transaction is open.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.isOpen();
     * </pre>
     */
    bool isOpen() const;

    /**
     * Closes the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.close()
     * </pre>
     */
    void close();

    /**
     * Closes the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.close()
     * </pre>
     */
    void forceClose();

    /**
     * Commits the changes made via this transaction to the TypeDB database.
     * Whether or not the transaction is commited successfully, it gets closed after the commit call.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.commit()
     * </pre>
     */
    void commit();

    /**
     * Rolls back the uncommitted changes made via this transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.rollback()
     * </pre>
     */
    void rollback();

    /**
     * Registers a callback function which will be executed when this transaction is closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.onClose(function);
     * </pre>
     *
     * @param function The callback function.
     */
    void onClose(std::function<void(const std::optional<DriverException>&)> callback);

private:
    Transaction(_native::Transaction*, TypeDB::TransactionType);
    _native::Transaction* getNative();

    friend class TypeDB::Session;
    friend class TypeDB::QueryManager;
    friend class TypeDB::ConceptManager;
    friend class TypeDB::LogicManager;
    friend class TypeDB::ConceptFactory;
};

}  // namespace TypeDB
