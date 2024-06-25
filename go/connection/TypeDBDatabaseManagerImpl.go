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

import "C"
import (
    "typedb_driver/go/api/database"
    "typedb_driver/go_wrapper"
)

type TypeDBDatabaseManagerImpl struct {
    nativeConnection typedb_driver.Connection
    nativeObject     typedb_driver.DatabaseManager
}

func NewTypeDBDatabaseManagerImpl(nativeConnection typedb_driver.Connection) *TypeDBDatabaseManagerImpl {
    return &TypeDBDatabaseManagerImpl{
        nativeConnection: nativeConnection,
        nativeObject:     typedb_driver.Database_manager_new(nativeConnection),
    }
}

func (dbManager *TypeDBDatabaseManagerImpl) Get(name string) database.Database {
    // TODO deal with errors
    return NewTypeDBDatabaseImpl(typedb_driver.Databases_get(dbManager.nativeObject, name))
}

func (dbManager *TypeDBDatabaseManagerImpl) Contains(name string) bool {
    return typedb_driver.Databases_contains(dbManager.nativeObject, name)
}

func (dbManager *TypeDBDatabaseManagerImpl) Create(name string) {
    typedb_driver.Databases_create(dbManager.nativeObject, name)
}

func (dbManager *TypeDBDatabaseManagerImpl) All() []database.Database {
    //return new NativeIterator<>(databases_all(nativeObject)).stream().map(TypeDBDatabaseImpl::new).collect(toList());
    //TODO implement - dealing with iterator
    return nil
}
