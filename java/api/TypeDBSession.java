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

import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface TypeDBSession extends AutoCloseable {

    /**
     * Checks whether this session is open.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.isOpen();
     * </pre>
     */
    @CheckReturnValue
    boolean isOpen();

    /**
     * The current session’s type (SCHEMA or DATA)
     */
    @CheckReturnValue
    Type type();

    /**
     * Returns the name of the database of the session.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.databaseName();
     * </pre>
     */
    @CheckReturnValue
    String database_name();

    /**
     * Gets the options for the session
     */
    @CheckReturnValue
    TypeDBOptions options();

    /**
     * Opens a transaction on the database connected to the session with default options.
     * 
     * @see TypeDBSession#transaction(TypeDBTransaction.Type, TypeDBOptions)
     */
    @CheckReturnValue
    TypeDBTransaction transaction(TypeDBTransaction.Type type);

    /**
     * Opens a transaction to perform read or write queries on the database connected to the session.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.transaction(transactionType, options);
     * </pre>
     *
     * @param type The type of transaction to be created (READ or WRITE)
     * @param options Options for the session
     */
    @CheckReturnValue
    TypeDBTransaction transaction(TypeDBTransaction.Type type, TypeDBOptions options);

    /**
     * Registers a callback function which will be executed when this session is closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.onClose(function)
     * </pre>
     *
     * @param function The callback function.
     */
    void onClose(Runnable function);

    /**
     * Closes the session. Before opening a new session, the session currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * session.close();
     * </pre>
     */
    void close();

    /**
     * Used to specify the type of the session.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.session(database, TypeDBSession.Type.SCHEMA);
     * </pre>
     */
    enum Type {
        DATA(0, com.vaticle.typedb.driver.jni.SessionType.Data),
        SCHEMA(1, com.vaticle.typedb.driver.jni.SessionType.Schema);

        private final int id;
        private final boolean isSchema;
        public final com.vaticle.typedb.driver.jni.SessionType nativeObject;

        Type(int id, com.vaticle.typedb.driver.jni.SessionType nativeObject) {
            this.id = id;
            this.nativeObject = nativeObject;

            this.isSchema = nativeObject == com.vaticle.typedb.driver.jni.SessionType.Schema;
        }

        public static Type of(com.vaticle.typedb.driver.jni.SessionType sessionType) {
            for (Type type : Type.values()) {
                if (type.nativeObject == sessionType) {
                    return type;
                }
            }
            throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
        }

        public int id() {
            return id;
        }

        public boolean isData() {
            return !isSchema;
        }

        public boolean isSchema() {
            return isSchema;
        }
    }
}
