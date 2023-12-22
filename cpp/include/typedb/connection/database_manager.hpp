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

#include <memory>
#include <string>

#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"
#include "typedb/connection/options.hpp"
#include "typedb/connection/session.hpp"
#include "typedb/database/database.hpp"

namespace TypeDB {

class Driver;  // Forward declaration for friendship

using DatabaseIterator = Iterator<_native::DatabaseIterator, _native::Database, TypeDB::Database>;

using DatabaseIterable = Iterable<_native::DatabaseIterator, _native::Database, TypeDB::Database>;


/**
 * \brief Provides access to all database management methods.
 */
class DatabaseManager {
public:
    DatabaseManager(const DatabaseManager&) = delete;
    DatabaseManager& operator=(const DatabaseManager&) = delete;
    ~DatabaseManager() = default;

    /**
     * Create a database with the given name
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases.create(name)
     * </pre>
     *
     * @param name The name of the database to be created
     */
    void create(const std::string&) const;

    /**
     * Checks if a database with the given name exists
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases.contains(name)
     * </pre>
     *
     * @param name The database name to be checked
     */
    bool contains(const std::string&) const;

    /**
     * Retrieve the database with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases.get(name)
     * </pre>
     *
     * @param name The name of the database to retrieve
     */
    Database get(const std::string&) const;

    /**
     * Retrieves all databases present on the TypeDB server
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases.all()
     * </pre>
     */
    DatabaseIterable all() const;

private:
    NativePointer<_native::DatabaseManager> databaseManagerNative;

    DatabaseManager(_native::Connection*);
    DatabaseManager(DatabaseManager&&) noexcept = default;
    DatabaseManager& operator=(DatabaseManager&&) = default;

    Session session(const std::string& database, SessionType sessionType, const Options& options);

    friend class TypeDB::Driver;
};

}  // namespace TypeDB
