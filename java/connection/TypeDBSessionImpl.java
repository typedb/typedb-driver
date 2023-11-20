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

import com.vaticle.typedb.driver.api.TypeDBOptions;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.database.DatabaseManager;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.util.ArrayList;
import java.util.List;

import static com.vaticle.typedb.driver.jni.typedb_driver.session_force_close;
import static com.vaticle.typedb.driver.jni.typedb_driver.session_get_database_name;
import static com.vaticle.typedb.driver.jni.typedb_driver.session_is_open;
import static com.vaticle.typedb.driver.jni.typedb_driver.session_new;
import static com.vaticle.typedb.driver.jni.typedb_driver.session_on_close;
import static com.vaticle.typedb.driver.jni.typedb_driver.session_on_reopen;

public class TypeDBSessionImpl extends NativeObject<com.vaticle.typedb.driver.jni.Session> implements TypeDBSession {
    private final Type type;
    private final TypeDBOptions options;

    private final List<SessionCallback> callbacks;

    TypeDBSessionImpl(DatabaseManager databaseManager, String database, Type type, TypeDBOptions options) {
        super(newNative(databaseManager, database, type, options));
        this.type = type;
        this.options = options;

        callbacks = new ArrayList<>();
    }

    private static com.vaticle.typedb.driver.jni.Session newNative(DatabaseManager databaseManager, String database, Type type, TypeDBOptions options) {
        try {
            return session_new(((TypeDBDatabaseManagerImpl)databaseManager).nativeObject, database, type.nativeObject, options.nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return session_is_open(nativeObject);
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String database_name() {
        return session_get_database_name(nativeObject);
    }

    @Override
    public TypeDBOptions options() {
        return options;
    }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type) {
        return transaction(type, new TypeDBOptions());
    }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type, TypeDBOptions options) {
        return new TypeDBTransactionImpl(this, type, options);
    }

    @Override
    public void onClose(Runnable function) {
        try {
            SessionCallback callback = new SessionCallback(function);
            callbacks.add(callback);
            session_on_close(nativeObject, callback.released());
        } catch (com.vaticle.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public void onReopen(Runnable function) {
        try {
            SessionCallback callback = new SessionCallback(function);
            callbacks.add(callback);
            session_on_reopen(nativeObject, callback.released());
        } catch (com.vaticle.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public void close() {
        try {
            session_force_close(nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        } finally {
            callbacks.clear();
        }
    }

    static class SessionCallback extends com.vaticle.typedb.driver.jni.SessionCallbackDirector {
        private final Runnable function;

        SessionCallback(Runnable function) throws com.vaticle.typedb.driver.jni.Error {
            this.function = function;
        }

        @Override
        public void callback() {
            function.run();
        }
    }
}
