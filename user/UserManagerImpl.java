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

import java.util.Set;

public class UserManagerImpl implements UserManager {

    static final String SYSTEM_DB = "_system";

    public UserManagerImpl(com.vaticle.typedb.client.jni.Connection connection) {
        // TODO
    }

    @Override
    public boolean contains(String username) {
        return false; // TODO
    }

    @Override
    public void create(String username, String password) {
    }

    @Override
    public void delete(String username) {
    }

    @Override
    public Set<User> all() {
        return null; // TODO
    }

    @Override
    public User get(String username) {
        return null; // TODO
    }

    @Override
    public void passwordSet(String username, String password) {
    }
}
