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

package com.vaticle.typedb.driver.api;

import com.vaticle.typedb.driver.api.concept.ConceptManager;
import com.vaticle.typedb.driver.api.logic.LogicManager;
import com.vaticle.typedb.driver.api.query.QueryManager;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.util.function.Consumer;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface TypeDBTransaction extends AutoCloseable {
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
     * The transaction’s type (READ or WRITE)
     */
    @CheckReturnValue
    Type type();

    /**
     * The options for the transaction
     */
    @CheckReturnValue
    TypeDBOptions options();

    /**
     * The <code>ConceptManager</code> for this transaction, providing access to all Concept API methods.
     */
    @CheckReturnValue
    ConceptManager concepts();

    /**
     * The <code>LogicManager</code> for this Transaction, providing access to all Concept API - Logic methods.
     */
    @CheckReturnValue
    LogicManager logic();

    /**
     * The<code></code>QueryManager<code></code> for this Transaction, from which any TypeQL query can be executed.
     */
    @CheckReturnValue
    QueryManager query();

    /**
     * Registers a callback function which will be executed when this session is closed.
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
    void commit();

    /**
     * Rolls back the uncommitted changes made via this transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.rollback()
     * </pre>
     */
    void rollback();

    /**
     * Closes the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.close()
     * </pre>
     */
    void close();

    /**
     * Used to specify the type of transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.transaction(TransactionType.READ);
     * </pre>
     */
    enum Type {
        READ(0, com.vaticle.typedb.driver.jni.TransactionType.Read),
        WRITE(1, com.vaticle.typedb.driver.jni.TransactionType.Write);

        private final int id;
        private final boolean isWrite;
        public final com.vaticle.typedb.driver.jni.TransactionType nativeObject;

        Type(int id, com.vaticle.typedb.driver.jni.TransactionType nativeObject) {
            this.id = id;
            this.nativeObject = nativeObject;

            this.isWrite = nativeObject == com.vaticle.typedb.driver.jni.TransactionType.Write;
        }

        public static Type of(com.vaticle.typedb.driver.jni.TransactionType transactionType) {
            for (Type type : Type.values()) {
                if (type.nativeObject == transactionType) {
                    return type;
                }
            }
            throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
        }

        public int id() {
            return id;
        }

        public boolean isRead() {
            return !isWrite;
        }

        public boolean isWrite() {
            return isWrite;
        }
    }
}
