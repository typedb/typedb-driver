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

package com.vaticle.typedb.client.api;

import com.vaticle.typedb.client.api.concept.ConceptManager;
import com.vaticle.typedb.client.api.logic.LogicManager;
import com.vaticle.typedb.client.api.query.QueryManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import java.util.function.Consumer;
import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_STATE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface TypeDBTransaction extends AutoCloseable {
    @CheckReturnValue
    boolean isOpen();

    @CheckReturnValue
    Type type();

    @CheckReturnValue
    TypeDBOptions options();

    @CheckReturnValue
    ConceptManager concepts();

    @CheckReturnValue
    LogicManager logic();

    @CheckReturnValue
    QueryManager query();

    void onClose(Consumer<Throwable> function);

    void commit();

    void rollback();

    void close();

    enum Type {
        READ(0),
        WRITE(1);

        private final int id;
        private final boolean isWrite;

        Type(int id) {
            this.id = id;
            this.isWrite = id == 1;
        }

        public static Type of(com.vaticle.typedb.client.jni.TransactionType transactionType) {
            switch (transactionType) {
                case Read: return READ;
                case Write: return WRITE;
                default: throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
            }
        }

        public com.vaticle.typedb.client.jni.TransactionType asJNI() {
            switch (this) {
                case READ: return com.vaticle.typedb.client.jni.TransactionType.Read;
                case WRITE: return com.vaticle.typedb.client.jni.TransactionType.Write;
                default: throw new TypeDBClientException(ILLEGAL_STATE);
            }
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
