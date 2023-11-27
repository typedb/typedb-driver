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

#include "typedb/user/usermanager.hpp"
#include "inc/macros.hpp"
#include "typedb/common/exception.hpp"

using namespace TypeDB;

namespace TypeDB {

UserManager::UserManager(const _native::Connection* connectionNative) {
    userManagerNative =
        connectionNative
            ? NativePointer<_native::UserManager>(_native::user_manager_new(connectionNative), _native::user_manager_drop)
            : NativePointer<_native::UserManager>(nullptr);
}

UserManager::UserManager(UserManager&& from) noexcept {
    *this = std::move(from);
}

UserManager& UserManager::operator=(UserManager&& from) {
    userManagerNative = std::move(from.userManagerNative);
    return *this;
}

bool UserManager::contains(const std::string& username) const {
    CHECK_NATIVE(userManagerNative);
    WRAPPED_NATIVE_CALL(RETURN_IDENTITY, _native::users_contains(userManagerNative.get(), username.c_str()));
}

void UserManager::create(const std::string& username, const std::string& password) const {
    CHECK_NATIVE(userManagerNative);
    _native::users_create(userManagerNative.get(), username.c_str(), password.c_str());
    TypeDBDriverException::check_and_throw();
}

void UserManager::drop(const std::string& username) const {
    CHECK_NATIVE(userManagerNative);
    _native::users_delete(userManagerNative.get(), username.c_str());
    TypeDBDriverException::check_and_throw();
}

UserIterable UserManager::all() const {
    CHECK_NATIVE(userManagerNative);
    WRAPPED_NATIVE_CALL(UserIterable, _native::users_all(userManagerNative.get()));
}

std::unique_ptr<User> UserManager::get(const std::string& username) const {
    CHECK_NATIVE(userManagerNative);
    auto p = _native::users_get(userManagerNative.get(), username.c_str());
    TypeDBDriverException::check_and_throw();
    return std::unique_ptr<User>(new User(p));
}

void UserManager::passwordSet(const std::string& username, const std::string& password) const {
    CHECK_NATIVE(userManagerNative);
    _native::users_set_password(userManagerNative.get(), username.c_str(), password.c_str());
    TypeDBDriverException::check_and_throw();
}

User UserManager::getCurrentUser() const {
    CHECK_NATIVE(userManagerNative);
    WRAPPED_NATIVE_CALL(User, _native::users_current_user(userManagerNative.get()));
}

_native::UserManager* UserManager::getNative() const {
    return userManagerNative.get();
}

// UserIterator
TYPEDB_ITERATOR_HELPER(
    _native::UserIterator,
    _native::User,
    User,
    _native::user_iterator_drop,
    _native::user_iterator_next,
    _native::user_drop);

}  // namespace TypeDB
