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

import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

public interface TypeDBSession extends AutoCloseable {
    @CheckReturnValue
    boolean isOpen();

    @CheckReturnValue
    Type type();

    @CheckReturnValue
    String database_name();

    @CheckReturnValue
    TypeDBOptions options();

    @CheckReturnValue
    TypeDBTransaction transaction(TypeDBTransaction.Type type);

    @CheckReturnValue
    TypeDBTransaction transaction(TypeDBTransaction.Type type, TypeDBOptions options);

    void onClose(Runnable function);

    void close();

    enum Type {
        DATA(0, com.vaticle.typedb.client.jni.SessionType.Data),
        SCHEMA(1, com.vaticle.typedb.client.jni.SessionType.Schema);

        private final int id;
        private final boolean isSchema;
        public final com.vaticle.typedb.client.jni.SessionType nativeObject;

        Type(int id, com.vaticle.typedb.client.jni.SessionType nativeObject) {
            this.id = id;
            this.nativeObject = nativeObject;

            this.isSchema = nativeObject == com.vaticle.typedb.client.jni.SessionType.Schema;
        }

        public static Type of(com.vaticle.typedb.client.jni.SessionType sessionType) {
            for (Type type : Type.values()) {
                if (type.nativeObject == sessionType) {
                    return type;
                }
            }
            throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
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
