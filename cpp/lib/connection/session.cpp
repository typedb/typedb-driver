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

#include <atomic>
#include <unordered_map>

#include "typedb/common/exception.hpp"
#include "typedb/connection/session.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../common/utils.hpp"

namespace TypeDB {

static_assert(static_cast<int>(SessionType::DATA) == _native::Data);
static_assert(static_cast<int>(SessionType::SCHEMA) == _native::Schema);

std::atomic_uintptr_t nextSessionCallbackId = 0;
std::unordered_map<std::uintptr_t, std::function<void()>> sessionCallbacks;

static void session_callback_execute(void* ID) {
    try {
        sessionCallbacks.at(reinterpret_cast<std::uintptr_t>(ID))();
    } catch (std::exception const& e) {
        throw Utils::exception(DriverError::CALLBACK_EXCEPTION, e.what());
    }
}

static void session_callback_erase(void* ID) {
    try {
        sessionCallbacks.erase(reinterpret_cast<std::uintptr_t>(ID));
    } catch (std::exception const& e) {
        throw Utils::exception(DriverError::CALLBACK_EXCEPTION, e.what());
    }
}

uintptr_t registerCallback(std::function<void()> callback) {
    std::uintptr_t id = ++nextSessionCallbackId;
    sessionCallbacks.emplace(id, callback);
    return id;
}

Session::Session(_native::Session* sessionNative, SessionType type)
    : sessionNative(sessionNative, _native::session_close), sessionType(type) {}

SessionType Session::type() const {
    return sessionType;
}

bool Session::isOpen() const {
    return sessionNative != nullptr && _native::session_is_open(sessionNative.get());
}

void Session::close() {
    sessionNative.reset();
}

std::string Session::databaseName() const {
    CHECK_NATIVE(sessionNative);
    return Utils::stringFromNative(_native::session_get_database_name(sessionNative.get()));
}

Transaction Session::transaction(TransactionType type, const Options& options) const {
    CHECK_NATIVE(sessionNative);
    _native::Transaction* p = _native::transaction_new(sessionNative.get(), (_native::TransactionType)type, options.getNative());
    DriverException::check_and_throw();
    return Transaction(p, type);  // No std::move for copy-elision
}

void Session::onClose(std::function<void()> callback) {
    CHECK_NATIVE(sessionNative);
    std::uintptr_t id = registerCallback(callback);
    _native::session_on_close(sessionNative.get(), reinterpret_cast<void*>(id), &session_callback_execute, &session_callback_erase);
    DriverException::check_and_throw();
}

void Session::onReopen(std::function<void()> callback) {
    CHECK_NATIVE(sessionNative);
    std::uintptr_t id = registerCallback(callback);
    _native::session_on_reopen(sessionNative.get(), reinterpret_cast<void*>(id), &session_callback_execute, &session_callback_erase);
    DriverException::check_and_throw();
}

}  // namespace TypeDB
