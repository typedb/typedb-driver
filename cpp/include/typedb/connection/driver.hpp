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
#include "typedb/connection/database_manager.hpp"
#include "typedb/connection/session.hpp"
#include "typedb/user/user_manager.hpp"

#include <string>

namespace TypeDB {

class Driver;

class Credential {
public:
    Credential(const std::string& username, const std::string& password, bool withTLS, const std::string& customRootCAStr = "");
    Credential(const Credential&) = delete;
    Credential& operator=(const Credential&) = delete;
    Credential(Credential&&) = default;
    Credential& operator=(Credential&&) = default;

private:
    NativePointer<_native::Credential> credentialNative;

    _native::Credential* getNative() const;

    friend class Driver;
};

class Driver {
private:
    NativePointer<_native::Connection> connectionNative;  // Remains on top for construction order

public:
    DatabaseManager databases;
    UserManager users;

    static void initLogging();
    static Driver coreDriver(const std::string& coreAddress);
    static Driver cloudDriver(const std::vector<std::string>& cloudAddresses, const Credential& credential);

    Driver(const Driver&) = delete;
    Driver(Driver&& from) = default;
    ~Driver() = default;

    Driver& operator=(const Driver& from) = delete;
    Driver& operator=(Driver&& from) = default;

    bool isOpen();
    void close();
    Session session(const std::string& database, SessionType sessionType, const Options& options);
    User user();

private:
    Driver(TypeDB::_native::Connection* conn) noexcept;
};

}  // namespace TypeDB
