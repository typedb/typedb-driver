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
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_commit;
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_is_open;
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_new;
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_on_close;
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_force_close;
import static com.vaticle.typedb.client.jni.typedb_client_jni.transaction_rollback;

public class TypeDBTransactionImpl extends NativeObject implements TypeDBTransaction {
    public final com.vaticle.typedb.client.jni.Transaction transaction;
    private final TypeDBTransaction.Type type;
    private final TypeDBOptions options;

    TypeDBTransactionImpl(TypeDBSessionImpl session, Type type, TypeDBOptions options) {
        this.type = type;
        this.options = options;
        transaction = transaction_new(session.session, com.vaticle.typedb.client.jni.TransactionType.swigToEnum(type.id()), options.options);
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
        if (!transaction.isOwned()) return false;
        else return transaction_is_open(transaction);
    }

    @Override
    public ConceptManager concepts() {
        return new ConceptManagerImpl(transaction);
    }

    @Override
    public LogicManager logic() {
        return new LogicManagerImpl(transaction);
    }

    @Override
    public QueryManager query() {
        return new QueryManagerImpl(transaction);
    }

    @Override
    public void onClose(Consumer<Throwable> function) {
        if (!transaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        transaction_on_close(transaction, new TransactionOnClose(function).released());
    }

    @Override
    public void commit() {
        if (!transaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        transaction_commit(transaction.released());
    }

    @Override
    public void rollback() {
        if (!transaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        transaction_rollback(transaction);
    }

    @Override
    public void close() {
        if (transaction.isOwned()) {
            transaction_force_close(transaction);
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
