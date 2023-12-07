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

#include "typedb/database/database.hpp"

#include <iostream>
#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../common/utils.hpp"

using namespace TypeDB;

namespace TypeDB {

std::string ReplicaInfo::address() {
    CHECK_NATIVE(replicaInfoNative);
    return Utils::stringFromNative(_native::replica_info_get_address(replicaInfoNative.get()));
}

bool ReplicaInfo::replica_info_is_primary() {
    CHECK_NATIVE(replicaInfoNative);
    return _native::replica_info_is_primary(replicaInfoNative.get());
}

bool ReplicaInfo::replica_info_is_preferred() {
    CHECK_NATIVE(replicaInfoNative);
    return _native::replica_info_is_preferred(replicaInfoNative.get());
}

int64_t ReplicaInfo::replica_info_get_term() {
    CHECK_NATIVE(replicaInfoNative);
    return _native::replica_info_get_term(replicaInfoNative.get());
}

ReplicaInfo::ReplicaInfo(_native::ReplicaInfo* replicaInfoNative)
    : replicaInfoNative(replicaInfoNative, _native::replica_info_drop) {}

Database::Database(_native::Database* db) noexcept
    : databaseNative(db, _native::database_close) {}

bool Database::operator==(const Database& other) {
    return databaseNative == other.databaseNative;
}

std::string Database::name() const {
    CHECK_NATIVE(databaseNative);
    return Utils::stringFromNative(_native::database_get_name(databaseNative.get()));
}

ReplicaInfoIterable Database::replicas() {
    CHECK_NATIVE(databaseNative);
    WRAPPED_NATIVE_CALL(ReplicaInfoIterable, _native::database_get_replicas_info(databaseNative.get()));
}

void Database::drop() {
    CHECK_NATIVE(databaseNative);
    _native::database_delete(databaseNative.get());
    TypeDBDriverException::check_and_throw();
    databaseNative.release();  //  Release avoids the dangling pointer invoking the deleter
}

TYPEDB_ITERATOR_HELPER(
    _native::ReplicaInfoIterator,
    _native::ReplicaInfo,
    ReplicaInfo,
    _native::replica_info_iterator_drop,
    _native::replica_info_iterator_next,
    _native::replica_info_drop)

}  // namespace TypeDB
