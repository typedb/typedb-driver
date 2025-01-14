/*
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

package com.typedb.driver.connection;

import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Promise;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.answer.QueryAnswerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.typedb.driver.common.exception.ErrorMessage.Query.MISSING_QUERY;
import static com.typedb.driver.jni.typedb_driver.transaction_commit;
import static com.typedb.driver.jni.typedb_driver.transaction_force_close;
import static com.typedb.driver.jni.typedb_driver.transaction_is_open;
import static com.typedb.driver.jni.typedb_driver.transaction_new;
import static com.typedb.driver.jni.typedb_driver.transaction_on_close;
import static com.typedb.driver.jni.typedb_driver.transaction_query;
import static com.typedb.driver.jni.typedb_driver.transaction_rollback;

public class TransactionImpl extends NativeObject<com.typedb.driver.jni.Transaction> implements Transaction {
    private final Transaction.Type type;
//    private final Options options;

    private final List<TransactionOnClose> callbacks;

    TransactionImpl(Driver driver, String database, Type type/*, Options options*/) throws TypeDBDriverException {
        super(newNative(driver, database, type/*, options*/));
        this.type = type;
//        this.options = options;

        callbacks = new ArrayList<>();
    }

    private static com.typedb.driver.jni.Transaction newNative(Driver driver, String database, Type type/*, Options options*/) {
        try {
            return transaction_new(((DriverImpl) driver).nativeObject, database, type.nativeObject/*, options.nativeObject*/);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Type getType() {
        return type;
    }

//    @Override
//    public Options options() {
//        return options;
//    }

    @Override
    public boolean isOpen() {
        if (!nativeObject.isOwned()) return false;
        else return transaction_is_open(nativeObject);
    }

    @Override
    public Promise<? extends QueryAnswer> query(String query) throws TypeDBDriverException {
        if (query == null || query.isBlank()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return Promise.map(transaction_query(nativeObject, query/*, options.nativeObject*/), QueryAnswerImpl::of);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void onClose(Consumer<Throwable> function) throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            TransactionOnClose callback = new TransactionOnClose(function);
            callbacks.add(callback);
            transaction_on_close(nativeObject, callback.released());
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public void commit() throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            // NOTE: .released() relinquishes ownership of the native object to the Rust side
            transaction_commit(nativeObject.released()).get();
        } catch (com.typedb.driver.jni.Error.Unchecked e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void rollback() throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            transaction_rollback(nativeObject).get();
        } catch (com.typedb.driver.jni.Error.Unchecked e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void close() throws TypeDBDriverException {
        if (nativeObject.isOwned()) {
            try {
                transaction_force_close(nativeObject);
            } catch (com.typedb.driver.jni.Error error) {
                throw new TypeDBDriverException(error);
            } finally {
                callbacks.clear();
            }
        }
    }

    static class TransactionOnClose extends com.typedb.driver.jni.TransactionCallbackDirector {
        private final Consumer<Throwable> function;

        public TransactionOnClose(Consumer<Throwable> function) {
            this.function = function;
        }

        @Override
        public void callback(com.typedb.driver.jni.Error e) {
            function.accept(e);
        }
    }
}
