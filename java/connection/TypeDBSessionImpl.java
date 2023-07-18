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

import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import static com.vaticle.typedb.client.jni.typedb_client.session_force_close;
import static com.vaticle.typedb.client.jni.typedb_client.session_get_database_name;
import static com.vaticle.typedb.client.jni.typedb_client.session_is_open;
import static com.vaticle.typedb.client.jni.typedb_client.session_new;
import static com.vaticle.typedb.client.jni.typedb_client.session_on_close;

public class TypeDBSessionImpl extends NativeObject<com.vaticle.typedb.client.jni.Session> implements TypeDBSession {
    private final Type type;
    private final TypeDBOptions options;

    TypeDBSessionImpl(Database database, Type type, TypeDBOptions options) {
        super(newNative(database, type, options));
        this.type = type;
        this.options = options;
    }

    private static com.vaticle.typedb.client.jni.Session newNative(Database database, Type type, TypeDBOptions options) {
        try {
            return session_new(((TypeDBDatabaseImpl) database).nativeObject.released(), type.nativeObject, options.nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
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
        session_on_close(nativeObject, new Callback(function).released());
    }

    @Override
    public void close() {
        session_force_close(nativeObject);
    }

    static class Callback extends com.vaticle.typedb.client.jni.SessionCallbackDirector {
        private final Runnable function;

        Callback(Runnable function) {
            this.function = function;
        }

        @Override
        public void callback() {
            function.run();
        }
    }
}
