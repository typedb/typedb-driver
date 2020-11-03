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
import grakn.protocol.TransactionProto;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public abstract class QueryFuture<T> implements Future<T> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false; // Can't cancel
    }

    @Override
    public boolean isCancelled() {
        return false; // Can't cancel
    }

    @Override
    public abstract T get();

    @Override
    public abstract T get(long timeout, TimeUnit unit);

    static class Execute<T> extends QueryFuture<T> {
        private final RPCTransaction.ResponseCollector.Single collector;
        private final Function<TransactionProto.Transaction.Res, T> transformResponse;

        public Execute(TransactionProto.Transaction.Req request, StreamObserver<TransactionProto.Transaction.Req> requestObserver,
                       RPCTransaction.ResponseCollector.Single collector, Function<TransactionProto.Transaction.Res, T> transformResponse) {
            this.collector = collector;
            this.transformResponse = transformResponse;
            requestObserver.onNext(request);
        }

        @Override
        public boolean isDone() {
            return collector.isDone();
        }

        @Override
        public T get() {
            try {
                final TransactionProto.Transaction.Res res = collector.take();
                return transformResponse.apply(res);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            try {
                final TransactionProto.Transaction.Res res = collector.take(timeout, unit);
                return transformResponse.apply(res);
            } catch (InterruptedException | TimeoutException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }
    }

    static class Stream<T> extends QueryFuture<java.util.stream.Stream<T>> {
        private final QueryIterator<T> iterator;

        public Stream(TransactionProto.Transaction.Req request, StreamObserver<TransactionProto.Transaction.Req> requestObserver,
                      RPCTransaction.ResponseCollector.Multiple responseCollector, Function<TransactionProto.Transaction.Res, T> transformResponse) {
            this.iterator = new QueryIterator<>(request, requestObserver, responseCollector, transformResponse);
            iterator.fetchFirstBatch();
        }

        @Override
        public boolean isDone() {
            return iterator.isStarted();
        }

        @Override
        public java.util.stream.Stream<T> get() {
            return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
        }

        @Override
        public java.util.stream.Stream<T> get(long timeout, TimeUnit unit) {
            return get();
        }
    }
}
