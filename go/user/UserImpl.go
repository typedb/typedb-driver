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

import (
    "typedb_driver/go_wrapper"
)

type UserImpl struct {
    users        UserManagerImpl
    nativeObject typedb_driver.User
}

func NewUserImpl(user typedb_driver.User, users UserManagerImpl, ) *UserImpl {
    return &UserImpl{
        users:        users,
        nativeObject: user,
    }
}

func (u *UserImpl) Username() string {
    return typedb_driver.User_get_username(u.nativeObject)
}

func (u *UserImpl) PasswordExpirySeconds() int64 {
    return typedb_driver.User_get_password_expiry_seconds(u.nativeObject)
}

func (u *UserImpl) PasswordUpdate(passwordOld string, passwordNew string) {
    typedb_driver.User_password_update(u.nativeObject, u.users.nativeObject, passwordOld, passwordNew)
}
