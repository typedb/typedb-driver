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
#include "typedb/user/user.hpp"

namespace TypeDB {

class Driver;  // forward declaration for friendship

using UserIterator = Iterator<_native::UserIterator, _native::User, User>;
using UserIterable = Iterable<_native::UserIterator, _native::User, User>;

class UserManager {
public:
    ~UserManager() = default;

    bool contains(const std::string& username) const;
    void create(const std::string& username, const std::string& password) const;
    void deleteUser(const std::string& username) const;
    UserIterable all() const;
    std::unique_ptr<User> get(const std::string& username) const;
    void passwordSet(const std::string& username, const std::string& password) const;
    User getCurrentUser() const;

private:
    NativePointer<_native::UserManager> userManagerNative;

    UserManager(const _native::Connection*);
    UserManager(UserManager&&) = default;
    UserManager& operator=(UserManager&&) = default;
    UserManager(const UserManager&) = delete;
    UserManager& operator=(const UserManager&) = delete;

    _native::UserManager* getNative() const;

    friend class TypeDB::Driver;
    friend class User;
};

}  // namespace TypeDB
