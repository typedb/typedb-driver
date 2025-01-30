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

package com.typedb.driver.api;

import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.common.Promise;

import javax.annotation.CheckReturnValue;
import java.util.function.Consumer;

public interface Transaction extends AutoCloseable {
    /**
     * Checks whether this transaction is open.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.isOpen();
     * </pre>
     */
    @CheckReturnValue
    boolean isOpen();

    /**
     * The transaction’s type (READ, WRITE, or SCHEMA)
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.getType();
     * </pre>
     */
    @CheckReturnValue
    Type getType();

//    /**
//     * The options for the transaction
//     */
//    @CheckReturnValue
//    Options options();

    /**
     * Execute a TypeQL query in this transaction.
     *
     * <h3>Examples</h3>
     * <pre> // TODO: Add more usage examples, how to unpack answers!
     * transaction.query("define entity person;");
     * </pre>
     *
     * @param query The query to execute.
     */
    @CheckReturnValue
    Promise<? extends QueryAnswer> query(String query) throws com.typedb.driver.common.exception.TypeDBDriverException;

    /**
     * Registers a callback function which will be executed when this transaction is closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.onClose(function);
     * </pre>
     *
     * @param function The callback function.
     */
    void onClose(Consumer<Throwable> function);

    /**
     * Commits the changes made via this transaction to the TypeDB database. Whether or not the transaction is commited successfully, it gets closed after the commit call.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.commit()
     * </pre>
     */
    void commit() throws com.typedb.driver.common.exception.TypeDBDriverException;

    /**
     * Rolls back the uncommitted changes made via this transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.rollback()
     * </pre>
     */
    void rollback() throws com.typedb.driver.common.exception.TypeDBDriverException;

    /**
     * Closes the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.close()
     * </pre>
     */
    void close() throws com.typedb.driver.common.exception.TypeDBDriverException;

    /**
     * Used to specify the type of transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.transaction(dbName, TransactionType.READ);
     * </pre>
     */
    enum Type {
        READ(0, com.typedb.driver.jni.TransactionType.Read),
        WRITE(1, com.typedb.driver.jni.TransactionType.Write),
        SCHEMA(2, com.typedb.driver.jni.TransactionType.Schema);

        public final com.typedb.driver.jni.TransactionType nativeObject;
        private final int id;

        Type(int id, com.typedb.driver.jni.TransactionType nativeObject) {
            this.id = id;
            this.nativeObject = nativeObject;
        }

        public int id() {
            return id;
        }

        public boolean isRead() {
            return nativeObject == com.typedb.driver.jni.TransactionType.Read;
        }

        public boolean isWrite() {
            return nativeObject == com.typedb.driver.jni.TransactionType.Write;
        }

        public boolean isSchema() {
            return nativeObject == com.typedb.driver.jni.TransactionType.Schema;
        }
    }
}
