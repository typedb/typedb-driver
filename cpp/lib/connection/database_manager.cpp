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

#include "typedb/connection/database_manager.hpp"
#include "typedb/common/exception.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"

namespace TypeDB {

// DatabaseIterator
TYPEDB_ITERATOR_HELPER(
    _native::DatabaseIterator,
    _native::Database,
    TypeDB::Database,
    _native::database_iterator_drop,
    _native::database_iterator_next,
    _native::database_close);

DatabaseManager::DatabaseManager(_native::Connection* connectionNative) {
    databaseManagerNative = connectionNative ? NativePointer<_native::DatabaseManager>(_native::database_manager_new(connectionNative), _native::database_manager_drop) : NativePointer<_native::DatabaseManager>(nullptr);
}

void DatabaseManager::create(const std::string& name) const {
    CHECK_NATIVE(databaseManagerNative);
    _native::databases_create(databaseManagerNative.get(), name.c_str());
    TypeDBDriverException::check_and_throw();
}

bool DatabaseManager::contains(const std::string& name) const {
    CHECK_NATIVE(databaseManagerNative);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::databases_contains(databaseManagerNative.get(), name.c_str()));
}

Database DatabaseManager::get(const std::string& name) const {
    CHECK_NATIVE(databaseManagerNative);
    WRAPPED_NATIVE_CALL(Database, _native::databases_get(databaseManagerNative.get(), name.c_str()));
}

DatabaseIterable DatabaseManager::all() const {
    CHECK_NATIVE(databaseManagerNative);
    WRAPPED_NATIVE_CALL(DatabaseIterable, _native::databases_all(databaseManagerNative.get()));
}

// Private
Session DatabaseManager::session(const std::string& database, SessionType sessionType, const Options& options) {
    CHECK_NATIVE(databaseManagerNative);
    WRAPPED_NATIVE_CALL(Session, _native::session_new(databaseManagerNative.get(), database.c_str(), (_native::SessionType)sessionType, options.getNative()));
}

}  // namespace TypeDB
