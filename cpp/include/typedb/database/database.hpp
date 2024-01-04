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

#include <optional>

#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class DatabaseManager;
class Database;

/**
 * \brief The metadata and state of an individual raft replica of a database.
 */
class ReplicaInfo {
public:
    ReplicaInfo(const ReplicaInfo&) = delete;
    ReplicaInfo(ReplicaInfo&&) = default;
    ReplicaInfo& operator=(const ReplicaInfo&) = delete;
    ReplicaInfo& operator=(ReplicaInfo&&) = default;

    /**
     * Retrieves the address of the server hosting this replica
     */
    std::string address();

    /**
     * Checks whether this is the primary replica of the raft cluster.
     */
    bool isPrimary();

    /**
     * Checks whether this is the preferred replica of the raft cluster.
     * If true, Operations which can be run on any replica will prefer to use this replica.
     */
    bool isPreferred();

    /**
     * The raft protocol ‘term’ of this replica.
     */
    int64_t term();

private:
    NativePointer<_native::ReplicaInfo> replicaInfoNative;
    ReplicaInfo(_native::ReplicaInfo*);

    friend class Database;
    friend class IteratorHelper<_native::ReplicaInfoIterator, _native::ReplicaInfo, ReplicaInfo>;
};

using ReplicaInfoIterable = Iterable<_native::ReplicaInfoIterator, _native::ReplicaInfo, ReplicaInfo>;
using ReplicaInfoIterator = Iterator<_native::ReplicaInfoIterator, _native::ReplicaInfo, ReplicaInfo>;

/**
 * \brief A TypeDB database
 */
class Database {
public:
    Database(const Database&) = delete;
    Database(Database&&) = default;
    ~Database() = default;

    Database& operator=(const Database&) = delete;
    Database& operator=(Database&&) = default;

    bool operator==(const Database& other);

    /**
     * The database name as a string.
     */
    std::string name() const;

    /**
     * Deletes this database.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.deleteDatabase()
     * </pre>
     */
    void deleteDatabase();

    /**
     * A full schema text as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.schema()
     * </pre>
     */
    std::string schema();

    /**
     * The types in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema()
     * </pre>
     */
    std::string typeSchema();

    /**
     * The rules in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.ruleSchema()
     * </pre>
     */
    std::string ruleSchema();

    /**
     * Set of <code>Replica</code> instances for this database.
     * <b>Only works in TypeDB Cloud</b>
     *
     * <h3>Examples</h3>
     * <pre>
     * database.replicas()
     * </pre>
     */
    ReplicaInfoIterable replicas();

    /**
     * Returns the primary replica for this database.
     * _Only works in TypeDB Cloud_
     *
     * <h3>Examples</h3>
     * <pre>
     * database.primaryReplica()
     * </pre>
     */
    std::optional<ReplicaInfo> primaryReplica();

    /**
     * Returns the preferred replica for this database. Operations which can be run on any replica will prefer to use this replica.
     * _Only works in TypeDB Cloud_
     *
     * <h3>Examples</h3>
     * <pre>
     * database.preferredReplica()
     * </pre>
     */
    std::optional<ReplicaInfo> preferredReplica();

private:
    NativePointer<_native::Database> databaseNative;
    Database(_native::Database*) noexcept;

    friend class DatabaseManager;
    friend class IteratorHelper<_native::DatabaseIterator, _native::Database, Database>;
};

}  // namespace TypeDB
