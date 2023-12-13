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

package com.vaticle.typedb.driver.connection;

import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBCredential;
import com.vaticle.typedb.driver.api.TypeDBOptions;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.database.DatabaseManager;
import com.vaticle.typedb.driver.api.user.User;
import com.vaticle.typedb.driver.api.user.UserManager;
import com.vaticle.typedb.driver.common.Loader;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.user.UserManagerImpl;

import java.util.Set;

import static com.vaticle.typedb.driver.jni.typedb_driver.connection_force_close;
import static com.vaticle.typedb.driver.jni.typedb_driver.connection_is_open;
import static com.vaticle.typedb.driver.jni.typedb_driver.connection_open_cloud_with_id;
import static com.vaticle.typedb.driver.jni.typedb_driver.connection_open_core_with_id;

public class TypeDBDriverImpl extends NativeObject<com.vaticle.typedb.driver.jni.Connection> implements TypeDBDriver {
    private final UserManagerImpl userMgr;
    private final DatabaseManager databaseMgr;

    private static final String driverName = "Java";
    private static final String driverVersion = Loader.getDriverVersion();

    public TypeDBDriverImpl(String address) throws TypeDBDriverException {
        this(openCore(address));
    }

    public TypeDBDriverImpl(Set<String> initAddresses, TypeDBCredential credential) throws TypeDBDriverException {
        this(openCloud(initAddresses, credential));
    }

    private TypeDBDriverImpl(com.vaticle.typedb.driver.jni.Connection connection) {
        super(connection);
        databaseMgr = new TypeDBDatabaseManagerImpl(this.nativeObject);
        userMgr = new UserManagerImpl(this.nativeObject);
    }

    private static com.vaticle.typedb.driver.jni.Connection openCore(String address) {
        try {
            return connection_open_core_with_id(address, driverName, driverVersion);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.vaticle.typedb.driver.jni.Connection openCloud(Set<String> initAddresses, TypeDBCredential credential) {
        try {
            return connection_open_cloud_with_id(initAddresses.toArray(new String[0]), credential.nativeObject, driverName, driverVersion);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return connection_is_open(nativeObject);
    }

    @Override
    public User user() {
        return userMgr.getCurrentUser();
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
        return new TypeDBSessionImpl(databases(), database, type, options);
    }

    @Override
    public void close() {
        if (!isOpen()) return;
        try {
            connection_force_close(nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }
}
