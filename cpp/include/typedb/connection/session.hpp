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
#include "typedb/connection/transaction.hpp"

namespace TypeDB {

/**
 * Used to specify the type of the session.
 *
 * <h3>Examples</h3>
 * <pre>
 * driver.session(database, TypeDBSession.Type.SCHEMA);
 * </pre>
 */
enum class SessionType {
    DATA,
    SCHEMA,
};

class DatabaseManager;  // forward declaration for friendship

/**
 * \brief A session with a TypeDB database.
 */
class Session {
public:
    Session(const Session&) = delete;
    Session(Session&&) = default;
    ~Session() = default;

    Session& operator=(const Session&) = delete;
    Session& operator=(Session&&) = default;

    /**
     * The current session’s type (SCHEMA or DATA)
     */
    SessionType type() const;

    /**
     * Checks whether this session is open.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.isOpen();
     * </pre>
     */
    bool isOpen() const;

    /**
     * Closes the session. Before opening a new session, the session currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.close();
     * </pre>
     */
    void close();

    /**
     * Returns the name of the database of the session.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.databaseName();
     * </pre>
     */
    std::string databaseName() const;

    /**
     * Opens a transaction to perform read or write queries on the database connected to the session.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.transaction(transactionType, options);
     * </pre>
     *
     * @param type The type of transaction to be created (READ or WRITE)
     * @param options Options for the session
     */
    Transaction transaction(TransactionType type, const Options& options) const;

    /**
     * Registers a callback function which will be executed when this session is closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.onClose(function)
     * </pre>
     *
     * @param function The callback function.
     */
    void onClose(std::function<void()> callback);

    /**
     * Registers a callback function which will be executed when this session is reopened.
     * A session may be closed if it times out, or loses the connection to the database.
     * In such situations, the session is reopened automatically when opening a new transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.onReopen(function)
     * </pre>
     *
     * @param function The callback function.
     */
    void onReopen(std::function<void()> callback);

private:
    NativePointer<_native::Session> sessionNative;
    SessionType sessionType;
    Session(_native::Session*, SessionType);

    friend class TypeDB::DatabaseManager;
};

}  // namespace TypeDB
