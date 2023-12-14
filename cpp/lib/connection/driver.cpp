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

#include "typedb/connection/driver.hpp"

#include "typedb/common/version.hpp"
#include "../common/macros.hpp"
#include "../common/native.hpp"

using namespace TypeDB;

namespace TypeDB {

Credential::Credential(const std::string& username, const std::string& password, bool withTLS, const std::string& customRootCAStr) {
    const char* customRootCA = customRootCAStr != "" ? customRootCAStr.c_str() : nullptr;
    auto p = _native::credential_new(username.c_str(), password.c_str(), customRootCA, withTLS);
    DriverException::check_and_throw();
    credentialNative = NativePointer<_native::Credential>(p, _native::credential_drop);
}

_native::Credential* Credential::getNative() const {
    return credentialNative.get();
}

const char* const DRIVER_NAME = "C++";

Driver Driver::coreDriver(const std::string& coreAddress) {
    auto p = _native::connection_open_core_with_id(coreAddress.c_str(), DRIVER_NAME, TYPEDB_DRIVER_VERSION);
    DriverException::check_and_throw();
    return Driver(p);
}

Driver Driver::cloudDriver(const std::vector<std::string>& cloudAddresses, const Credential& credential) {
    std::vector<const char*> addressesNative;
    for (auto& addr : cloudAddresses)
        addressesNative.push_back(addr.c_str());
    addressesNative.push_back(nullptr);
    auto p = _native::connection_open_cloud_with_id(addressesNative.data(), credential.getNative(), DRIVER_NAME, TYPEDB_DRIVER_VERSION);
    DriverException::check_and_throw();
    return Driver(p);
}

Driver::Driver(_native::Connection* conn) noexcept
    : connectionNative(conn, _native::connection_close),
      databases(this->connectionNative.get()),
      users(this->connectionNative.get()) {}

bool Driver::isOpen() {
    return connectionNative != nullptr && _native::connection_is_open(connectionNative.get());
}

void Driver::close() {
    connectionNative.reset();
}

Session Driver::session(const std::string& database, SessionType sessionType, const Options& options) {
    CHECK_NATIVE(connectionNative);
    return databases.session(database, sessionType, options);
}

User Driver::user() {
    CHECK_NATIVE(connectionNative);
    return users.getCurrentUser();
}

}  // namespace TypeDB
