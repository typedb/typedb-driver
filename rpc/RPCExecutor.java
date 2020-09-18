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

package grakn.client.rpc;

import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.response.SingleResponseCollector;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RPCExecutor {

    private final SingleResponseCollector collector;

    public RPCExecutor(final RPCTransaction rpcTransaction, final TransactionProto.Transaction.Req req) {
        collector = new SingleResponseCollector();
        rpcTransaction.executeAsync(req, collector);
    }

    public boolean isDone() {
        return collector.isDone();
    }

    public TransactionProto.Transaction.Res receive() throws InterruptedException {
        return collector.receive();
    }

    public TransactionProto.Transaction.Res receive(final long timeout, final TimeUnit unit) throws InterruptedException {
        return collector.receive(timeout, unit);
    }

    public <T> QueryFuture<T> await() {
        return new QueryExecuteFuture<>(this, null);
    }

    public <T> QueryFuture<T> await(final Function<QueryProto.Query.Res, T> responseReader) {
        return new QueryExecuteFuture<>(this, responseReader);
    }

    private static class QueryExecuteFuture<T> implements QueryFuture<T> {
        private final RPCExecutor executor;
        private final Function<QueryProto.Query.Res, T> responseReader;

        public QueryExecuteFuture(final RPCExecutor executor, @Nullable final Function<QueryProto.Query.Res, T> responseReader) {
            this.executor = executor;
            this.responseReader = responseReader;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false; // Can't cancel
        }

        @Override
        public boolean isCancelled() {
            return false; // Can't cancel
        }

        @Override
        public boolean isDone() {
            return executor.isDone();
        }

        @Override
        public T get() {
            try {
                final TransactionProto.Transaction.Res res = executor.receive();
                return responseReader == null ? null : responseReader.apply(res.getQueryRes());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }

        @Override
        public T get(final long timeout, final TimeUnit unit) {
            try {
                final TransactionProto.Transaction.Res res = executor.receive(timeout, unit);
                return responseReader == null ? null : responseReader.apply(res.getQueryRes());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }
    }
}
