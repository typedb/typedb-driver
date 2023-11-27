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

#include "inc/macros.hpp"

using namespace TypeDB;

namespace TypeDB {

Credential::Credential(const std::string& username, const std::string& password, bool withTLS, const std::string& customRootCAStr) {
    const char* customRootCA = customRootCAStr != "" ? customRootCAStr.c_str() : nullptr;
    auto p = _native::credential_new(username.c_str(), password.c_str(), customRootCA, withTLS);
    TypeDBDriverException::check_and_throw();
    credentialNative = std::move(NativePointer<_native::Credential>(p, _native::credential_drop));
}

_native::Credential* Credential::getNative() const {
    return credentialNative.get();
}


_native::Connection* connectToCore(const std::string& coreAddress) {
    auto p = _native::connection_open_core(coreAddress.c_str());
    TypeDBDriverException::check_and_throw();
    return p;
}

_native::Connection* connectToEnterprise(const std::vector<std::string>& enterpriseAddresses, _native::Credential* nativeCredential) {
    std::vector<const char*> addressesNative;
    for (auto& addr : enterpriseAddresses)
        addressesNative.push_back(addr.c_str());
    addressesNative.push_back(nullptr);
    auto p = _native::connection_open_enterprise(addressesNative.data(), nativeCredential);
    TypeDBDriverException::check_and_throw();
    return p;
}

Driver::Driver()
    : Driver(nullptr) {}

Driver::Driver(const std::string& coreAddress)
    : Driver(connectToCore(coreAddress)) {
    TypeDBDriverException::check_and_throw();
}

Driver::Driver(const std::vector<std::string>& enterpriseAddresses, const Credential& credential)
    : Driver(connectToEnterprise(enterpriseAddresses, credential.getNative())) {
    TypeDBDriverException::check_and_throw();
}

Driver::Driver(_native::Connection* conn) noexcept
    : connectionNative(conn, _native::connection_close),
      databases(this->connectionNative.get()),
      users(this->connectionNative.get()) {}

Driver::Driver(Driver&& from)
    : connectionNative(std::move(from.connectionNative)),
      databases(std::move(from.databases)),
      users(std::move(from.users)) {}

Driver& Driver::operator=(Driver&& from) {
    connectionNative = std::move(from.connectionNative);
    from.connectionNative = nullptr;
    databases = std::move(from.databases);
    users = std::move(from.users);
    return *this;
}

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
