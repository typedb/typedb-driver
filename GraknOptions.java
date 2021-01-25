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

import static grakn.client.common.exception.ErrorMessage.Client.NEGATIVE_BATCH_SIZE;
import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;

public class GraknOptions {
    private Boolean infer = null;
    private Boolean explain = null;
    private Integer batchSize = null;

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
            throw new GraknClientException(NEGATIVE_BATCH_SIZE.message(batchSize));
        }
        this.batchSize = batchSize;
        return this;
    }

    public static GraknOptions core() {
        return new GraknOptions();
    }

    public static GraknOptions.Cluster cluster() {
        return new Cluster();
    }

    boolean isCluster() {
        return false;
    }

    Cluster asCluster() {
        if (isCluster()) return (Cluster) this;
        else throw new GraknClientException(ILLEGAL_CAST, Cluster.class);
    }

    public static class Cluster extends GraknOptions {
        private Boolean primaryReplica = null;

        Cluster() {}

        public Optional<Boolean> primaryReplica() {
            return Optional.ofNullable(primaryReplica);
        }

        public Cluster primaryReplica(boolean primaryReplica) {
            this.primaryReplica = primaryReplica;
            return this;
        }

        @Override
        public boolean isCluster() {
            return true;
        }
    }
}
