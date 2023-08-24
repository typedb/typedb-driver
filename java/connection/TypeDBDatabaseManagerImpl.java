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

import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.api.database.DatabaseManager;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import java.util.List;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.MISSING_DB_NAME;
import static com.vaticle.typedb.client.jni.typedb_client.database_manager_new;
import static com.vaticle.typedb.client.jni.typedb_client.databases_all;
import static com.vaticle.typedb.client.jni.typedb_client.databases_contains;
import static com.vaticle.typedb.client.jni.typedb_client.databases_create;
import static com.vaticle.typedb.client.jni.typedb_client.databases_get;
import static java.util.stream.Collectors.toList;

public class TypeDBDatabaseManagerImpl extends NativeObject<com.vaticle.typedb.client.jni.DatabaseManager> implements DatabaseManager {
    public TypeDBDatabaseManagerImpl(com.vaticle.typedb.client.jni.Connection connection) {
        super(database_manager_new(connection));
    }

    @Override
    public Database get(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBClientException(MISSING_DB_NAME);
        try {
            return new TypeDBDatabaseImpl(databases_get(nativeObject, name));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public boolean contains(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBClientException(MISSING_DB_NAME);
        try {
            return databases_contains(nativeObject, name);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void create(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBClientException(MISSING_DB_NAME);
        try {
            databases_create(nativeObject, name);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public List<Database> all() {
        try {
            return databases_all(nativeObject).stream().map(TypeDBDatabaseImpl::new).collect(toList());
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
