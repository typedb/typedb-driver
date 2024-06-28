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

package user

import "C"
import (
    "typedb_driver/go/api/user"
    "typedb_driver/go_wrapper"
)

type UserManagerImpl struct {
    nativeConnection typedb_driver.Connection
    nativeObject     typedb_driver.UserManager
}

func NewUserManagerImpl(nativeConnection typedb_driver.Connection) *UserManagerImpl {
    return &UserManagerImpl{
        nativeConnection: nativeConnection,
        nativeObject:     typedb_driver.User_manager_new(nativeConnection),
    }
}

func (userMngr *UserManagerImpl) Contains(username string) bool {
    return typedb_driver.Users_contains(userMngr.nativeObject, username)
}

func (userMngr *UserManagerImpl) Create(username, password string) {
    typedb_driver.Users_create(userMngr.nativeObject, username, password)
}

func (userMngr *UserManagerImpl) Delete(username string) {
    typedb_driver.Users_delete(userMngr.nativeObject, username)
}

func (userMngr *UserManagerImpl) Get(username string) typedb_driver.User {
    user_fetched := typedb_driver.Users_get(userMngr.nativeObject, username)
    if user_fetched != nil {
        return NewUserImpl(user_fetched, *userMngr).nativeObject
    } else {
        return nil
    }
}

func (userMngr *UserManagerImpl) PasswordSet(username, password string) {
    typedb_driver.Users_set_password(userMngr.nativeObject, username, password)
}

func (userMngr *UserManagerImpl) GetCurrentUser() user.User {
    return nil
    //    TODO implement
}

func (userMngr *UserManagerImpl) All() map[string]user.User {
    userSet := make(map[string]user.User)
    // TODO implement - iterator
    return userSet
}
