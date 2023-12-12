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

class DatabaseManager {
public:
    DatabaseManager(const DatabaseManager&) = delete;
    DatabaseManager& operator=(const DatabaseManager&) = delete;
    ~DatabaseManager() = default;

    void create(const std::string&) const;
    bool contains(const std::string&) const;
    Database get(const std::string&) const;
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
