/*
 * Copyright (C) 2021 Vaticle
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
import com.vaticle.typedb.protocol.OptionsProto;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.NEGATIVE_VALUE_NOT_ALLOWED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static com.vaticle.typedb.common.util.Objects.className;

public class TypeDBOptions {

    private Boolean infer = null;
    private Boolean traceInference = null;
    private Boolean explain = null;
    private Boolean parallel = null;
    private Boolean prefetch = null;
    private Integer prefetchSize = null;
    private Integer sessionIdleTimeoutMillis = null;
    private Integer transactionTimeoutMillis = null;
    private Integer schemaLockAcquireTimeoutMillis = null;

    private TypeDBOptions() {
    }

    @CheckReturnValue
    public static TypeDBOptions core() {
        return new TypeDBOptions();
    }

    @CheckReturnValue
    public static TypeDBOptions.Cluster cluster() {
        return new Cluster();
    }

    @CheckReturnValue
    public boolean isCluster() {
        return false;
    }

    @CheckReturnValue
    public Optional<Boolean> infer() {
        return Optional.ofNullable(infer);
    }

    public TypeDBOptions infer(boolean infer) {
        this.infer = infer;
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> traceInference() {
        return Optional.ofNullable(traceInference);
    }

    public TypeDBOptions traceInference(boolean traceInference) {
        this.traceInference = traceInference;
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> explain() {
        return Optional.ofNullable(explain);
    }

    public TypeDBOptions explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> parallel() {
        return Optional.ofNullable(parallel);
    }

    public TypeDBOptions parallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    @CheckReturnValue
    public Optional<Boolean> prefetch() {
        return Optional.ofNullable(prefetch);
    }

    public TypeDBOptions prefetch(boolean prefetch) {
        this.prefetch = prefetch;
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> prefetchSize() {
        return Optional.ofNullable(prefetchSize);
    }

    public TypeDBOptions prefetchSize(int prefetchSize) {
        if (prefetchSize < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, prefetchSize);
        }
        this.prefetchSize = prefetchSize;
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> sessionIdleTimeoutMillis() {
        return Optional.ofNullable(sessionIdleTimeoutMillis);
    }

    public TypeDBOptions sessionIdleTimeoutMillis(int sessionIdleTimeoutMillis) {
        if (sessionIdleTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, sessionIdleTimeoutMillis);
        }
        this.sessionIdleTimeoutMillis = sessionIdleTimeoutMillis;
        return this;
    }

    @CheckReturnValue
    public Optional<Integer> schemaLockAcquireTimeoutMillis() {
        return Optional.ofNullable(schemaLockAcquireTimeoutMillis);
    }

    public TypeDBOptions transactionTimeoutMillis(int transactionTimeoutMillis) {
        if (transactionTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, transactionTimeoutMillis);
        }
        this.transactionTimeoutMillis = transactionTimeoutMillis;
        return this;
    }

    public Optional<Integer> transactionTimeoutMillis() {
        return Optional.ofNullable(transactionTimeoutMillis);
    }

    public TypeDBOptions schemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis) {
        if (schemaLockAcquireTimeoutMillis < 1) {
            throw new TypeDBClientException(NEGATIVE_VALUE_NOT_ALLOWED, schemaLockAcquireTimeoutMillis);
        }
        this.schemaLockAcquireTimeoutMillis = schemaLockAcquireTimeoutMillis;
        return this;
    }

    @CheckReturnValue
    public Cluster asCluster() {
        throw new TypeDBClientException(ILLEGAL_CAST, className(Cluster.class));
    }

    @CheckReturnValue
    public OptionsProto.Options proto() {
        OptionsProto.Options.Builder builder = OptionsProto.Options.newBuilder();
        infer().ifPresent(builder::setInfer);
        traceInference().ifPresent(builder::setTraceInference);
        explain().ifPresent(builder::setExplain);
        parallel().ifPresent(builder::setParallel);
        prefetchSize().ifPresent(builder::setPrefetchSize);
        prefetch().ifPresent(builder::setPrefetch);
        sessionIdleTimeoutMillis().ifPresent(builder::setSessionIdleTimeoutMillis);
        transactionTimeoutMillis().ifPresent(builder::setTransactionTimeoutMillis);
        schemaLockAcquireTimeoutMillis().ifPresent(builder::setSchemaLockAcquireTimeoutMillis);
        if (isCluster()) asCluster().readAnyReplica().ifPresent(builder::setReadAnyReplica);

        return builder.build();
    }

    public static class Cluster extends TypeDBOptions {

        private Boolean readAnyReplica = null;

        @CheckReturnValue
        public Optional<Boolean> readAnyReplica() {
            return Optional.ofNullable(readAnyReplica);
        }

        public Cluster readAnyReplica(boolean readAnyReplica) {
            this.readAnyReplica = readAnyReplica;
            return this;
        }

        @Override
        @CheckReturnValue
        public boolean isCluster() {
            return true;
        }

        @Override
        @CheckReturnValue
        public Cluster asCluster() {
            return this;
        }
    }
}
