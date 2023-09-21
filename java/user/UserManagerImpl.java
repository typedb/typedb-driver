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

package com.vaticle.typedb.driver.user;

import com.vaticle.typedb.driver.api.user.User;
import com.vaticle.typedb.driver.api.user.UserManager;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.driver.jni.typedb_driver.user_manager_new;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_all;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_contains;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_create;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_current_user;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_delete;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_get;
import static com.vaticle.typedb.driver.jni.typedb_driver.users_set_password;

public class UserManagerImpl extends NativeObject<com.vaticle.typedb.driver.jni.UserManager> implements UserManager {
    public UserManagerImpl(com.vaticle.typedb.driver.jni.Connection nativeConnection) {
        super(user_manager_new(nativeConnection));
    }

    @Override
    public boolean contains(String username) {
        try {
            return users_contains(nativeObject, username);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void create(String username, String password) {
        try {
            users_create(nativeObject, username, password);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete(String username) {
        try {
            users_delete(nativeObject, username);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Set<User> all() {
        try {
            return users_all(nativeObject).stream().map(user -> new UserImpl(user, this)).collect(Collectors.toSet());
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public User get(String username) {
        try {
            com.vaticle.typedb.driver.jni.User user = users_get(nativeObject, username);
            if (user != null) return new UserImpl(user, this);
            else return null;
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void passwordSet(String username, String password) {
        try {
            users_set_password(nativeObject, username, password);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    public User getCurrentUser() {
        try {
            return new UserImpl(users_current_user(nativeObject), this);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
