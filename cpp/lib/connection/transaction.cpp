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

#include "typedb/common/exception.hpp"
#include "typedb/common/future.hpp"
#include "typedb/connection/transaction.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"

namespace TypeDB {

static_assert(static_cast<int>(TransactionType::READ) == _native::Read);
static_assert(static_cast<int>(TransactionType::WRITE) == _native::Write);

std::atomic_uintptr_t nextTxCallbackId = 0;
std::unordered_map<std::uintptr_t, std::function<void(const std::optional<DriverException>&)>> transactionOnCloseCallbacks;

void transaction_callback_execute(std::uintptr_t ID, _native::Error* error) {
    try {
        transactionOnCloseCallbacks.at(ID)(error != nullptr ? std::optional<DriverException>(DriverException(error)) : std::optional<DriverException>());
        transactionOnCloseCallbacks.erase(ID);
    } catch (std::exception const& e) {
        throw Utils::exception(DriverError::CALLBACK_EXCEPTION, e.what());
    }
}

uintptr_t registerCallback(std::function<void(const std::optional<DriverException>&)> callback) {
    std::uintptr_t id = ++nextTxCallbackId;
    transactionOnCloseCallbacks.emplace(id, callback);
    return id;
}

Transaction::Transaction(_native::Transaction* transactionNative, TypeDB::TransactionType txnType)
    : transactionNative(transactionNative, _native::transaction_close), txnType(txnType), query(this), concepts(this), logic(this) {}


Transaction::Transaction(Transaction&& from)
    : query(this), concepts(this), logic(this) {  // This might need testing
    *this = std::move(from);
}

Transaction& Transaction::operator=(Transaction&& from) {
    transactionNative = std::move(from.transactionNative);
    this->txnType = from.txnType;
    return *this;
}

_native::Transaction* Transaction::getNative() {
    return transactionNative.get();
}

TypeDB::TransactionType Transaction::type() const {
    return txnType;
}

bool Transaction::isOpen() const {
    return transactionNative != nullptr && _native::transaction_is_open(transactionNative.get());
}

void Transaction::close() {
    if (transactionNative != nullptr) _native::transaction_close(transactionNative.release());
}

void Transaction::forceClose() {
    CHECK_NATIVE(transactionNative);
    _native::transaction_force_close(transactionNative.release());
}

void Transaction::commit() {
    CHECK_NATIVE(transactionNative);
    VoidFuture p = _native::transaction_commit(transactionNative.release());
    DriverException::check_and_throw();
    p.wait();
    DriverException::check_and_throw();
}

void Transaction::rollback() {
    CHECK_NATIVE(transactionNative);
    VoidFuture p = _native::transaction_rollback(transactionNative.release());
    DriverException::check_and_throw();
    p.wait();
    DriverException::check_and_throw();
}

void Transaction::onClose(std::function<void(const std::optional<DriverException>&)> callback) {
    CHECK_NATIVE(transactionNative);
    std::uintptr_t id = registerCallback(callback);
    _native::transaction_on_close(transactionNative.get(), id, &transaction_callback_execute);
    DriverException::check_and_throw();
}

}  // namespace TypeDB
