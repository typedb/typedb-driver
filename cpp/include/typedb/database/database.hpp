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

#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class DatabaseManager;

class Database {
   public:
    Database(const Database&) = delete;
    Database(Database&&) = default;
    ~Database() = default;

    Database& operator=(const Database&) = delete;
    Database& operator=(Database&&) = default;

    bool operator==(const Database& other);

    std::string name() const;
    void drop();

   private:
    NativePointer<_native::Database> databaseNative;
    Database(_native::Database*) noexcept;

    friend class DatabaseManager;
    friend class TypeDBIteratorHelper<_native::DatabaseIterator, _native::Database, Database>;
};

}  // namespace TypeDB
