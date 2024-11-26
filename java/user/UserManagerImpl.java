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

package com.typedb.driver.user;

import com.typedb.driver.api.user.User;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.Set;
import java.util.stream.Collectors;

import static com.typedb.driver.jni.typedb_driver.users_all;
import static com.typedb.driver.jni.typedb_driver.users_contains;
import static com.typedb.driver.jni.typedb_driver.users_create;
import static com.typedb.driver.jni.typedb_driver.users_current_username;
import static com.typedb.driver.jni.typedb_driver.users_delete;
import static com.typedb.driver.jni.typedb_driver.users_get;
import static com.typedb.driver.jni.typedb_driver.users_set_password;

public class UserManagerImpl implements UserManager {
    com.typedb.driver.jni.TypeDBDriver nativeDriver;

    public UserManagerImpl(com.typedb.driver.jni.TypeDBDriver driver) {
        nativeDriver = driver;
    }

    @Override
    public boolean contains(String username) {
        try {
            return users_contains(nativeDriver, username);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void create(String username, String password) {
        try {
            users_create(nativeDriver, username, password);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete(String username) {
        try {
            users_delete(nativeDriver, username);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public User get(String username) {
        try {
            com.typedb.driver.jni.User user = users_get(nativeDriver, username);
            if (user != null) return new UserImpl(user, this);
            else return null;
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public String getCurrentUsername() {
        try { // TODO: Make noexcept if we leave it returning just a String
            return users_current_username(nativeDriver);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Set<User> all() {
        try {
            return new NativeIterator<>(users_all(nativeDriver)).stream().map(user -> new UserImpl(user, this)).collect(Collectors.toSet());
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void setPassword(String username, String password) {
        try {
            users_set_password(nativeDriver, username, password);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
