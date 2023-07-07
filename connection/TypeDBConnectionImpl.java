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

package com.vaticle.typedb.client.connection;

import com.vaticle.typedb.client.api.TypeDBConnection;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.database.DatabaseManager;
import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.client.api.user.UserManager;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.jni.Error;
import com.vaticle.typedb.client.user.UserManagerImpl;

import java.util.Set;

import static com.vaticle.typedb.client.jni.typedb_client.connection_open_encrypted;
import static com.vaticle.typedb.client.jni.typedb_client.connection_open_plaintext;

public class TypeDBConnectionImpl extends NativeObject implements TypeDBConnection {
    private final com.vaticle.typedb.client.jni.Connection connection;

    private final UserManager userMgr;
    private final DatabaseManager databaseMgr;
    private boolean isOpen;

    public TypeDBConnectionImpl(String address) throws Error {
        this(connection_open_plaintext(address));
    }

    public TypeDBConnectionImpl(Set<String> initAddresses, TypeDBCredential credential) throws Error {
        this(connection_open_encrypted(initAddresses.toArray(new String[0]), credential.credential));
    }

    private TypeDBConnectionImpl(com.vaticle.typedb.client.jni.Connection connection) {
        this.connection = connection;
        databaseMgr = new TypeDBDatabaseManagerImpl(this.connection);
        userMgr = new UserManagerImpl(this.connection);
        isOpen = true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public User user() {
        return users().getCurrentUser();
    }

    @Override
    public UserManager users() {
        return userMgr;
    }

    @Override
    public DatabaseManager databases() {
        return databaseMgr;
    }

    @Override
    public TypeDBSession session(String database, TypeDBSession.Type type) {
        return session(database, type, new TypeDBOptions());
    }

    @Override
    public TypeDBSession session(String database, TypeDBSession.Type type, TypeDBOptions options) {
        return new TypeDBSessionImpl(databases().get(database), type, options);
    }

    @Override
    public void close() {
        isOpen = false;
        connection.delete();
    }
}
