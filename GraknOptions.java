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

public class GraknOptions {

    private Boolean infer = null;
    private Boolean explain = null;
    private BatchSize batchSize = null;

    public Boolean infer() {
        return infer;
    }

    public GraknOptions infer(boolean infer) {
        this.infer = infer;
        return this;
    }

    public Boolean explain() {
        return explain;
    }

    public GraknOptions explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    public BatchSize batchSize() {
        return batchSize;
    }

    public GraknOptions batchSize(final BatchSize batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public static class BatchSize {

        private final Integer size;
        private final boolean all;

        private BatchSize(final Integer size, final boolean all) {
            this.size = size;
            this.all = all;
        }

        public static BatchSize of(final int size) {
            return new BatchSize(size, false);
        }

        public static BatchSize all() {
            return new BatchSize(null, true);
        }

        public int getSize() {
            return size;
        }

        public boolean isAll() {
            return all;
        }
    }
}
