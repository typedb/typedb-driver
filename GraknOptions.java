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
    private Boolean explain = null;
    private Integer batchSize = null;
    private Boolean prefetch = null;
    private Integer sessionIdleTimeout = null;
    private Integer schemaLockAcquireTimeout = null;

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

    public Optional<Boolean> explain() {
        return Optional.ofNullable(explain);
    }

    public GraknOptions explain(boolean explain) {
        this.explain = explain;
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

    public Optional<Integer> sessionIdleTimeout() {
        return Optional.ofNullable(sessionIdleTimeout);
    }

    public GraknOptions sessionIdleTimeout(int sessionIdleTimeout) {
        if (sessionIdleTimeout < 1) {
            throw new GraknClientException(NEGATIVE_VALUE_NOT_ALLOWED.message(sessionIdleTimeout));
        }
        this.sessionIdleTimeout = sessionIdleTimeout;
        return this;
    }

    public Optional<Integer> schemaLockAcquireTimeout() {
        return Optional.ofNullable(schemaLockAcquireTimeout);
    }

    public GraknOptions schemaLockAcquireTimeout(int schemaLockAcquireTimeout) {
        if (schemaLockAcquireTimeout < 1) {
            throw new GraknClientException(NEGATIVE_VALUE_NOT_ALLOWED.message(schemaLockAcquireTimeout));
        }
        this.schemaLockAcquireTimeout = schemaLockAcquireTimeout;
        return this;
    }

    public Cluster asCluster() {
        throw new GraknClientException(ILLEGAL_CAST, Cluster.class);
    }

    public static class Cluster extends GraknOptions {
        private Boolean allowSecondaryReplica = null;

        Cluster() {}

        public Optional<Boolean> allowSecondaryReplica() {
            return Optional.ofNullable(allowSecondaryReplica);
        }

        public Cluster allowSecondaryReplica(boolean primaryReplica) {
            this.allowSecondaryReplica = primaryReplica;
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
