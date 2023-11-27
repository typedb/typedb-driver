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

#include "typedb/user/user.hpp"
#include "typedb/user/usermanager.hpp"

#include "inc/macros.hpp"
#include "inc/utils.hpp"

using namespace TypeDB;

namespace TypeDB {

User::User(_native::User* userNative)
    : userNative(userNative, _native::user_drop) {}

User::User(User&& from) noexcept {
    *this = std::move(from);
}

User& User::operator=(User&& from) {
    userNative = std::move(from.userNative);

    return *this;
}

std::string User::username() {
    CHECK_NATIVE(userNative);
    WRAPPED_NATIVE_CALL(Utils::stringAndFree, _native::user_get_username(userNative.get()));
}

std::optional<int64_t> User::passwordExpirySeconds() {
    CHECK_NATIVE(userNative);
    int64_t expiryNative = _native::user_get_password_expiry_seconds(userNative.get());
    TypeDBDriverException::check_and_throw();
    return expiryNative >= 0 ? std::optional<int64_t>(expiryNative) : std::optional<int64_t>();
}

void User::passwordUpdate(const UserManager& userManager, const std::string& passwordOld, const std::string& passwordNew) {
    CHECK_NATIVE(userNative);
    _native::user_password_update(userNative.get(), userManager.getNative(), passwordOld.c_str(), passwordNew.c_str());
    TypeDBDriverException::check_and_throw();
}

}  // namespace TypeDB
