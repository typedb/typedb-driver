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
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.ConceptManager;
import com.vaticle.typedb.client.api.logic.LogicManager;
import com.vaticle.typedb.client.api.query.QueryManager;
import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.logic.LogicManagerImpl;
import com.vaticle.typedb.client.query.QueryManagerImpl;

import java.util.function.Consumer;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_commit;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_force_close;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_is_open;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_new;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_on_close;
import static com.vaticle.typedb.client.jni.typedb_client.transaction_rollback;

public class TypeDBTransactionImpl extends NativeObject<com.vaticle.typedb.client.jni.Transaction> implements TypeDBTransaction {
    private final TypeDBTransaction.Type type;
    private final TypeDBOptions options;

    private final ConceptManager conceptManager;
    private final LogicManager logicManager;
    private final QueryManager queryManager;

    TypeDBTransactionImpl(TypeDBSessionImpl session, Type type, TypeDBOptions options) {
        super(newNative(session, type, options));
        this.type = type;
        this.options = options;

        conceptManager = new ConceptManagerImpl(nativeObject);
        logicManager = new LogicManagerImpl(nativeObject);
        queryManager = new QueryManagerImpl(nativeObject);
    }

    private static com.vaticle.typedb.client.jni.Transaction newNative(TypeDBSessionImpl session, Type type, TypeDBOptions options) {
        try {
            return transaction_new(session.nativeObject, type.nativeObject, options.nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public TypeDBOptions options() {
        return options;
    }

    @Override
    public boolean isOpen() {
        if (!nativeObject.isOwned()) return false;
        else return transaction_is_open(nativeObject);
    }

    @Override
    public ConceptManager concepts() {
        return conceptManager;
    }

    @Override
    public LogicManager logic() {
        return logicManager;
    }

    @Override
    public QueryManager query() {
        return queryManager;
    }

    @Override
    public void onClose(Consumer<Throwable> function) {
        if (!nativeObject.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        transaction_on_close(nativeObject, new TransactionOnClose(function).released());
    }

    @Override
    public void commit() {
        if (!nativeObject.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            // NOTE: .released() relinquishes ownership of the native object to the Rust side
            transaction_commit(nativeObject.released());
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void rollback() {
        if (!nativeObject.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            transaction_rollback(nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void close() {
        if (nativeObject.isOwned()) {
            transaction_force_close(nativeObject);
        }
    }

    static class TransactionOnClose extends com.vaticle.typedb.client.jni.TransactionCallbackDirector {
        private final Consumer<Throwable> function;

        public TransactionOnClose(Consumer<Throwable> function) {
            this.function = function;
        }

        @Override
        public void callback(com.vaticle.typedb.client.jni.Error e) {
            function.accept(e);
        }
    }
}
