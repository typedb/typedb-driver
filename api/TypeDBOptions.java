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

import com.vaticle.typedb.client.common.NativeObject;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.NEGATIVE_VALUE_NOT_ALLOWED;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_explain;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_infer;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_parallel;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_prefetch;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_prefetch_size;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_read_any_replica;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_schema_lock_acquire_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_session_idle_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_trace_inference;
import static com.vaticle.typedb.client.jni.typedb_client.options_get_transaction_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_explain;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_infer;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_parallel;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_prefetch;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_prefetch_size;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_read_any_replica;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_schema_lock_acquire_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_session_idle_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_trace_inference;
import static com.vaticle.typedb.client.jni.typedb_client.options_has_transaction_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_new;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_explain;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_infer;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_parallel;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_prefetch;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_prefetch_size;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_read_any_replica;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_schema_lock_acquire_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_session_idle_timeout_millis;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_trace_inference;
import static com.vaticle.typedb.client.jni.typedb_client.options_set_transaction_timeout_millis;

public class TypeDBOptions extends NativeObject {
    public final com.vaticle.typedb.client.jni.Options options;

    public TypeDBOptions() {
        options = options_new();
    }

    @CheckReturnValue
    public Optional<Boolean> infer() {
        if (options_has_infer(options)) return Optional.of(options_get_infer(options));
        return Optional.empty();
    }

    public TypeDBOptions infer(boolean infer) {
        options_set_infer(options, infer);
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> traceInference() {
        if (options_has_trace_inference(options)) return Optional.of(options_get_trace_inference(options));
        return Optional.empty();
    }

    public TypeDBOptions traceInference(boolean traceInference) {
        options_set_trace_inference(options, traceInference);
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> explain() {
        if (options_has_explain(options)) return Optional.of(options_get_explain(options));
        return Optional.empty();
    }

    public TypeDBOptions explain(boolean explain) {
        options_set_explain(options, explain);
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> parallel() {
        if (options_has_parallel(options)) return Optional.of(options_get_parallel(options));
        return Optional.empty();
    }

    public TypeDBOptions parallel(boolean parallel) {
        options_set_parallel(options, parallel);
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> prefetch() {
        if (options_has_prefetch(options)) return Optional.of(options_get_prefetch(options));
        else return Optional.empty();
    }

    public TypeDBOptions prefetch(boolean prefetch) {
        options_set_prefetch(options, prefetch);
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> prefetchSize() {
        if (options_has_prefetch_size(options)) return Optional.of(options_get_prefetch_size(options));
        return Optional.empty();
    }

    public TypeDBOptions prefetchSize(int prefetchSize) {
        if (prefetchSize < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, prefetchSize);
        }
        options_set_prefetch_size(options, prefetchSize);
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> sessionIdleTimeoutMillis() {
        if (options_has_session_idle_timeout_millis(options))
            return Optional.of((int) options_get_session_idle_timeout_millis(options));
        return Optional.empty();
    }

    public TypeDBOptions sessionIdleTimeoutMillis(int sessionIdleTimeoutMillis) {
        if (sessionIdleTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, sessionIdleTimeoutMillis);
        }
        options_set_session_idle_timeout_millis(options, sessionIdleTimeoutMillis);
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> schemaLockAcquireTimeoutMillis() {
        if (options_has_transaction_timeout_millis(options))
            return Optional.of((int) options_get_transaction_timeout_millis(options));
        return Optional.empty();
    }

    public TypeDBOptions transactionTimeoutMillis(int transactionTimeoutMillis) {
        if (transactionTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, transactionTimeoutMillis);
        }
        options_set_transaction_timeout_millis(options, transactionTimeoutMillis);
        return this;
    }

    public Optional<Integer> transactionTimeoutMillis() {
        if (options_has_schema_lock_acquire_timeout_millis(options))
            return Optional.of((int) options_get_schema_lock_acquire_timeout_millis(options));
        return Optional.empty();
    }

    public TypeDBOptions schemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis) {
        if (schemaLockAcquireTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, schemaLockAcquireTimeoutMillis);
        }
        options_set_schema_lock_acquire_timeout_millis(options, schemaLockAcquireTimeoutMillis);
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> readAnyReplica() {
        if (options_has_read_any_replica(options)) return Optional.of(options_get_read_any_replica(options));
        return Optional.empty();
    }

    public TypeDBOptions readAnyReplica(boolean readAnyReplica) {
        options_set_read_any_replica(options, readAnyReplica);
        return this;
    }
}
