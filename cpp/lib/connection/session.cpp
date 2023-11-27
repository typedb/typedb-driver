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

#include "typedb/connection/session.hpp"
#include "typedb/common/exception.hpp"

#include "inc/macros.hpp"
#include "inc/utils.hpp"

namespace TypeDB {

Session::Session()
    : Session(nullptr) {}

Session::Session(_native::Session* sessionNative)
    : sessionNative(sessionNative, _native::session_close) {}


Session::Session(Session&& from) {
    *this = std::move(from);
}

Session& Session::operator=(Session&& from) {
    sessionNative = std::move(from.sessionNative);
    return *this;
}

bool Session::isOpen() const {
    return sessionNative != nullptr && _native::session_is_open(sessionNative.get());
}

void Session::close() {
    sessionNative.reset();
}

std::string Session::databaseName() const {
    CHECK_NATIVE(sessionNative);
    return Utils::stringAndFree(_native::session_get_database_name(sessionNative.get()));
}

Transaction Session::transaction(TransactionType type, const Options& options) const {
    CHECK_NATIVE(sessionNative);
    _native::Transaction* p = _native::transaction_new(sessionNative.get(), (_native::TransactionType)type, options.getNative());
    TypeDBDriverException::check_and_throw();
    return Transaction(p, type);  // No std::move for copy-elision
}

}  // namespace TypeDB
