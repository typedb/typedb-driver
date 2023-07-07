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

package com.vaticle.typedb.client.user;

import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.client.api.user.UserManager;
import com.vaticle.typedb.client.common.NativeObject;

import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.jni.typedb_client_jni.user_manager_new;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_all;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_contains;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_create;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_current_user;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_delete;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_get;
import static com.vaticle.typedb.client.jni.typedb_client_jni.users_set_password;

public class UserManagerImpl extends NativeObject implements UserManager {
    private final com.vaticle.typedb.client.jni.UserManager userManager;
    private final com.vaticle.typedb.client.jni.Connection connection;

    public UserManagerImpl(com.vaticle.typedb.client.jni.Connection connection) {
        this.userManager = user_manager_new(connection);
        this.connection = connection;
    }

    @Override
    public boolean contains(String username) {
        return users_contains(userManager, username);
    }

    @Override
    public void create(String username, String password) {
        users_create(userManager, username, password);
    }

    @Override
    public void delete(String username) {
        users_delete(userManager, username);
    }

    @Override
    public Set<User> all() {
        return users_all(userManager).stream().map(user -> new UserImpl(user, connection)).collect(Collectors.toSet());
    }

    @Override
    public User get(String username) {
        var user = users_get(userManager, username);
        if (user != null) return new UserImpl(user, connection);
        else return null;
    }

    @Override
    public User getCurrentUser() {
        return new UserImpl(users_current_user(userManager), connection);
    }

    @Override
    public void passwordSet(String username, String password) {
        users_set_password(userManager, username, password);
    }
}