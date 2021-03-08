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

package grakn.client;

import grakn.client.common.exception.GraknClientException;

import java.util.Optional;

import static grakn.client.common.exception.ErrorMessage.Client.NEGATIVE_VALUE_NOT_ALLOWED;
import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;

public class GraknOptions {
    private Boolean infer = null;
    private Boolean traceInference = null;
    private Boolean explain = null;
    private Boolean parallel = null;
    private Integer batchSize = null;
    private Boolean prefetch = null;
    private Integer sessionIdleTimeoutMillis = null;
    private Integer schemaLockAcquireTimeoutMillis = null;

    public static GraknOptions core() {
        return new GraknOptions();
    }

    public static GraknOptions.Cluster cluster() {
        return new Cluster();
    }

    public boolean isCluster() {
        return false;
    }

    GraknOptions() {}

    public Optional<Boolean> infer() {
        return Optional.ofNullable(infer);
    }

    public GraknOptions infer(boolean infer) {
        this.infer = infer;
        return this;
    }

    public Optional<Boolean> traceInference() {
        return Optional.ofNullable(traceInference);
    }

    public GraknOptions traceInference(boolean traceInference) {
        this.traceInference = traceInference;
        return this;
    }

    public Optional<Boolean> explain() {
        return Optional.ofNullable(explain);
    }

    public GraknOptions explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    public Optional<Boolean> parallel() {
        return Optional.ofNullable(parallel);
    }

    public GraknOptions parallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    public Optional<Integer> batchSize() {
        return Optional.ofNullable(batchSize);
    }

    public GraknOptions batchSize(int batchSize) {
        if (batchSize < 1) {
            throw new GraknClientException(NEGATIVE_VALUE_NOT_ALLOWED.message(batchSize));
        }
        this.batchSize = batchSize;
        return this;
    }

    public Optional<Boolean> prefetch() {
        return Optional.ofNullable(prefetch);
    }

    public GraknOptions prefetch(boolean prefetch) {
        this.prefetch = prefetch;
        return this;
    }

    public Optional<Integer> sessionIdleTimeoutMillis() {
        return Optional.ofNullable(sessionIdleTimeoutMillis);
    }

    public GraknOptions sessionIdleTimeoutMillis(int sessionIdleTimeoutMillis) {
        if (sessionIdleTimeoutMillis < 1) {
            throw new GraknClientException(NEGATIVE_VALUE_NOT_ALLOWED.message(sessionIdleTimeoutMillis));
        }
        this.sessionIdleTimeoutMillis = sessionIdleTimeoutMillis;
        return this;
    }

    public Optional<Integer> schemaLockAcquireTimeoutMillis() {
        return Optional.ofNullable(schemaLockAcquireTimeoutMillis);
    }

    public GraknOptions schemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis) {
        if (schemaLockAcquireTimeoutMillis < 1) {
            throw new GraknClientException(NEGATIVE_VALUE_NOT_ALLOWED.message(schemaLockAcquireTimeoutMillis));
        }
        this.schemaLockAcquireTimeoutMillis = schemaLockAcquireTimeoutMillis;
        return this;
    }

    public Cluster asCluster() {
        throw new GraknClientException(ILLEGAL_CAST, Cluster.class);
    }

    public static class Cluster extends GraknOptions {
        private Boolean readAnyReplica = null;

        Cluster() {}

        public Optional<Boolean> readAnyReplica() {
            return Optional.ofNullable(readAnyReplica);
        }

        public Cluster readAnyReplica(boolean readAnyReplica) {
            this.readAnyReplica = readAnyReplica;
            return this;
        }

        @Override
        public boolean isCluster() {
            return true;
        }

        @Override
        public Cluster asCluster() {
            return this;
        }
    }
}
