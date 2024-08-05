/*
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

package connection

import (
    "typedb_driver/go/api/database"
    "typedb_driver/go_wrapper"
)

type TypeDBDatabaseImpl struct {
    nativeObject typedb_driver.Database
}

func NewTypeDBDatabaseImpl(database typedb_driver.Database) *TypeDBDatabaseImpl {
    return &TypeDBDatabaseImpl{
        nativeObject: database,
    }
}

// todo add error checks, for all functions. check if nativeObject.isOwned() before

func (db *TypeDBDatabaseImpl) Name() string {
    return typedb_driver.Database_get_name(db.nativeObject)
}

func (db *TypeDBDatabaseImpl) Schema() string {
    return typedb_driver.Database_schema(db.nativeObject)
}

func (db *TypeDBDatabaseImpl) TypeSchema() string {
    return typedb_driver.Database_type_schema(db.nativeObject)
}

func (db *TypeDBDatabaseImpl) RuleSchema() string {
    return typedb_driver.Database_rule_schema(db.nativeObject)
}

func (db *TypeDBDatabaseImpl) Delete() {
    typedb_driver.Database_delete(db.nativeObject) // TODO should be object.released()
}

// String representation
func (db *TypeDBDatabaseImpl) String() string {
    return db.Name()
}

func (db *TypeDBDatabaseImpl) Replicas() []database.Replica {
    return nil
    // TODO iterate through 'Database_get_replicas_info' and return a set of all replicas using the iterator.
    // replicaIterator:= typedb_driver.Database_get_replicas_info(db.nativeObject)
}

func (db *TypeDBDatabaseImpl) PrimaryReplica() database.Replica {
    res := typedb_driver.Database_get_primary_replica_info(db.nativeObject)
    return NewReplica(res)
}

func (db *TypeDBDatabaseImpl) PreferredReplica() database.Replica {
    res := typedb_driver.Database_get_preferred_replica_info(db.nativeObject)
    return NewReplica(res)
}

type Replica struct {
    nativeObject typedb_driver.ReplicaInfo
}

func NewReplica(replicaInfo typedb_driver.ReplicaInfo) *Replica {
    return &Replica{
        nativeObject: replicaInfo,
    }
}

func (r *Replica) Server() string {
    return typedb_driver.Replica_info_get_server(r.nativeObject)
}

func (r *Replica) IsPrimary() bool {
    return typedb_driver.Replica_info_is_primary(r.nativeObject)
}

func (r *Replica) IsPreferred() bool {
    return typedb_driver.Replica_info_is_preferred(r.nativeObject)
}

func (r *Replica) Term() int64 {
    return typedb_driver.Replica_info_get_term(r.nativeObject)
}
