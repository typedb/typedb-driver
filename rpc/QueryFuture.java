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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public abstract class QueryFuture<T> implements Future<T> {
    @Override
    public abstract T get();

    @Override
    public abstract T get(long timeout, TimeUnit unit);

    static class Execute<T> extends QueryFuture<T> {
        private final RPCResponseCollector collector;
        private final Function<TransactionProto.Transaction.Res, T> responseReader;

        public Execute(final RPCResponseCollector collector, final Function<TransactionProto.Transaction.Res, T> responseReader) {
            this.collector = collector;
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
            return collector.isDone();
        }

        @Override
        public T get() {
            try {
                final TransactionProto.Transaction.Res res = collector.take();
                return responseReader.apply(res);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }

        @Override
        public T get(final long timeout, final TimeUnit unit) {
            try {
                final TransactionProto.Transaction.Res res = collector.take(timeout, unit);
                return responseReader.apply(res);
            } catch (InterruptedException | TimeoutException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        }
    }

    static class Stream<T> extends QueryFuture<java.util.stream.Stream<T>> {
        private final RPCIterator<T> iterator;

        public Stream(final RPCIterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false; // Can't cancel
        }

        @Override
        public boolean isCancelled() {
            return false; // Can't cancel
        }

        @Override
        public boolean isDone() {
            return iterator.isStarted();
        }

        @Override
        public java.util.stream.Stream<T> get() {
            iterator.startIterating();
            return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
        }

        @Override
        public java.util.stream.Stream<T> get(long timeout, TimeUnit unit) {
            iterator.startIterating(timeout, unit);
            return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
        }
    }
}
