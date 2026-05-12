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

import com.typedb.driver.analyze.AnalyzedQueryImpl;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.QueryOptions;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.TransactionOptions;
import com.typedb.driver.api.analyze.AnalyzedQuery;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Promise;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.answer.QueryAnswerImpl;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.concept.ConceptImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.typedb.driver.jni.typedb_driver.transaction_analyze;
import static com.typedb.driver.jni.typedb_driver.transaction_commit;
import static com.typedb.driver.jni.typedb_driver.transaction_close;
import static com.typedb.driver.jni.typedb_driver.transaction_is_open;
import static com.typedb.driver.jni.typedb_driver.transaction_new;
import static com.typedb.driver.jni.typedb_driver.transaction_on_close;
import static com.typedb.driver.common.exception.ErrorMessage.Driver.INVALID_TYPE_AS_GIVEN_INPUT;
import static com.typedb.driver.jni.typedb_driver.given_row_new;
import static com.typedb.driver.jni.typedb_driver.given_row_set_index_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_row_set_index_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_push;
import static com.typedb.driver.jni.typedb_driver.transaction_query;
import static com.typedb.driver.jni.typedb_driver.transaction_query_given_rows;
import static com.typedb.driver.jni.typedb_driver.transaction_rollback;

public class TransactionImpl extends NativeObject<com.typedb.driver.jni.Transaction> implements Transaction {
    private final Transaction.Type type;
    private final TransactionOptions options;

    private final List<TransactionOnClose> callbacks;

    TransactionImpl(Driver driver, String database, Type type, TransactionOptions options) throws TypeDBDriverException {
        super(newNative(driver, database, type, options));
        this.type = type;
        this.options = options;

        callbacks = new ArrayList<>();
    }

    private static com.typedb.driver.jni.Transaction newNative(Driver driver, String database, Type type, TransactionOptions options) {
        try {
            return transaction_new(((DriverImpl) driver).nativeObject, database, type.nativeObject, options.nativeObject);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public TransactionOptions options() {
        return options;
    }

    @Override
    public boolean isOpen() {
        if (!nativeObject.isOwned()) return false;
        else return transaction_is_open(nativeObject);
    }

    @Override
    public Promise<? extends QueryAnswer> query(String query) throws TypeDBDriverException {
        return query(query, new QueryOptions());
    }

    @Override
    public Promise<? extends QueryAnswer> query(String query, QueryOptions options) throws TypeDBDriverException {
        Validator.requireNonNull(query, "query");
        try {
            return Promise.map(transaction_query(nativeObject, query, options.nativeObject), QueryAnswerImpl::of);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Promise<? extends QueryAnswer> query(String query, QueryOptions options, Iterable<? extends Iterable<Optional<? extends Concept>>> givenRows) throws TypeDBDriverException {
        Validator.requireNonNull(query, "query");
        try {
            // NOTE: .released() relinquishes ownership of the native rows to the Rust side
            return Promise.map(transaction_query_given_rows(nativeObject, query, options.nativeObject, buildNativeGivenRows(givenRows).released()), QueryAnswerImpl::of);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.QueryGivenRows buildNativeGivenRows(Iterable<? extends Iterable<Optional<? extends Concept>>> rows) {
        List<List<Optional<? extends Concept>>> rowList = new ArrayList<>();
        for (Iterable<Optional<? extends Concept>> row : rows) {
            List<Optional<? extends Concept>> entries = new ArrayList<>();
            for (Optional<? extends Concept> entry : row) entries.add(entry);
            rowList.add(entries);
        }
        com.typedb.driver.jni.QueryGivenRows nativeRows = given_rows_new(rowList.size());
        for (int rowIndex = 0; rowIndex < rowList.size(); rowIndex++) {
            List<Optional<? extends Concept>> entries = rowList.get(rowIndex);
            com.typedb.driver.jni.QueryGivenRow nativeRow = given_row_new(entries.size());
            for (int i = 0; i < entries.size(); i++) {
                Optional<? extends Concept> entry = entries.get(i);
                if (entry.isEmpty()) {
                    given_row_set_index_to_empty(nativeRow, i);
                } else {
                    Concept concept = entry.get();
                    if (concept.isType()) throw new TypeDBDriverException(INVALID_TYPE_AS_GIVEN_INPUT, concept.getLabel(), rowIndex);
                    given_row_set_index_to_concept(nativeRow, i, ((ConceptImpl) concept).nativeObject);
                }
            }
            // NOTE: .released() relinquishes ownership of the native row to the Rust side
            given_rows_push(nativeRows, nativeRow.released());
        }
        return nativeRows;
    }

    @Override
    public Promise<? extends AnalyzedQuery> analyze(String query) throws TypeDBDriverException {
        Validator.requireNonNull(query, "query");
        try {
            return Promise.map(transaction_analyze(nativeObject, query), AnalyzedQueryImpl::new);
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
            transaction_on_close(nativeObject, callback.released()).get();
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
                transaction_close(nativeObject).get();
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
