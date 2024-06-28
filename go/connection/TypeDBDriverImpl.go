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
    "typedb_driver/go/api/user"
    "typedb_driver/go_wrapper"
)

type TypeDBDriver struct {
    nativeObject typedb_driver.Connection
    databaseMgr  database.DatabaseManager
    userMgr      user.UserManager
}

func NewTypeDBDriver(address string) *TypeDBDriver {
    core_conn := openCore(address)
    databaseMgr := NewTypeDBDatabaseManagerImpl(core_conn)
    //userMgr := NewUserManagerImpl(core_conn)

    return &TypeDBDriver{
        nativeObject: openCore(address),
        databaseMgr:  databaseMgr,
        //userMgr: userMgr,
    }
}

func openCore(address string) typedb_driver.Connection {
    return typedb_driver.Connection_open_core(address)
}

func (d *TypeDBDriver) IsOpen() bool {
    return typedb_driver.Connection_is_open(d.nativeObject)
}

func (d *TypeDBDriver) User() user.User {
    // TODO implement users
    //    return d.userMgr.GetCurrentUser()
    return nil
}

func (d *TypeDBDriver) Databases() database.DatabaseManager {
    return d.databaseMgr
}

func (d *TypeDBDriver) Session(database string, typ typedb_driver.SessionType) typedb_driver.Session {
    //    return NewTypeDBSessionImpl
    // TODO implement sessions
    return nil
}

func (d *TypeDBDriver) Close() {
    if !d.IsOpen() {
        return
    }
    typedb_driver.Connection_force_close(d.nativeObject)
}
