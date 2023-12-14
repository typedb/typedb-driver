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

class UserManager;

class User {
public:
    User(_native::User*);
    User(User&&) = default;
    User& operator=(User&&) = default;
    ~User() = default;

    std::string username();
    std::optional<int64_t> passwordExpirySeconds();
    void passwordUpdate(const UserManager& userManager, const std::string& passwordOld, const std::string& passwordNew);

private:
    NativePointer<_native::User> userNative;
    User(const User&) = delete;
    User& operator=(const User&) = delete;

    friend class UserManager;
    friend IteratorHelper<_native::UserIterator, _native::User, User>;
};

}  // namespace TypeDB
