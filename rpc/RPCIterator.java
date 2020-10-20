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

import com.google.common.collect.AbstractIterator;
import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.response.ResponseCollector;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.common.util.Objects.className;

public class RPCIterator extends AbstractIterator<TransactionProto.Transaction.Iter.Res> {

    private ResponseCollector currentBatchCollector;
    private volatile boolean started;
    private TransactionProto.Transaction.Iter.Res first;

    private final RPCTransaction rpcTransaction;

    public RPCIterator(final RPCTransaction rpcTransaction, final TransactionProto.Transaction.Iter.Req req) {
        this.rpcTransaction = rpcTransaction;
        sendAsync(req);
    }

    private void sendAsync(final TransactionProto.Transaction.Iter.Req req) {
        currentBatchCollector = new ResponseCollector();
        rpcTransaction.iterateAsync(req, currentBatchCollector);
    }

    private void nextBatch(final int iteratorID) {
        final TransactionProto.Transaction.Iter.Req iterReq = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setIteratorID(iteratorID).build();
        sendAsync(iterReq);
    }

    public boolean isStarted() {
        return started;
    }

    public void waitForStart() {
        if (first != null) {
            throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        }

        first = computeNext();
    }

    public void waitForStart(final long timeout, final TimeUnit unit) {
        if (first != null) {
            throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        }

        first = computeNext(timeout, unit);
    }

    public <T> QueryFuture<Stream<T>> getFuture(final Function<QueryProto.Query.Iter.Res, T> responseReader) {
        return new QueryStreamFuture<>(this, responseReader);
    }

    @Override
    protected TransactionProto.Transaction.Iter.Res computeNext() {
        if (first != null) {
            final TransactionProto.Transaction.Iter.Res iterRes = first;
            first = null;
            return iterRes;
        }

        final TransactionProto.Transaction.Iter.Res res;
        try {
            res = currentBatchCollector.take().getIterRes();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }
        started = true;
        switch (res.getResCase()) {
            case ITERATORID:
                nextBatch(res.getIteratorID());
                return computeNext();
            case DONE:
                return endOfData();
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(className(TransactionProto.Transaction.Iter.Res.class)));
            default:
                return res;
        }
    }

    protected TransactionProto.Transaction.Iter.Res computeNext(final long timeout, final TimeUnit unit) {
        if (first != null) {
            final TransactionProto.Transaction.Iter.Res iterRes = first;
            first = null;
            return iterRes;
        }

        final TransactionProto.Transaction.Iter.Res res;
        try {
            res = currentBatchCollector.take(timeout, unit).getIterRes();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }
        started = true;
        switch (res.getResCase()) {
            case ITERATORID:
                nextBatch(res.getIteratorID());
                return computeNext();
            case DONE:
                return endOfData();
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(className(TransactionProto.Transaction.Iter.Res.class)));
            default:
                return res;
        }
    }

    public static class QueryStreamFuture<T> implements QueryFuture<Stream<T>> {
        private final RPCIterator iterator;
        private final Function<QueryProto.Query.Iter.Res, T> responseReader;

        public QueryStreamFuture(final RPCIterator iterator, final Function<QueryProto.Query.Iter.Res, T> responseReader) {
            this.iterator = iterator;
            this.responseReader = responseReader;
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
        public Stream<T> get() {
            iterator.waitForStart();
            return StreamSupport.stream(((Iterable<TransactionProto.Transaction.Iter.Res>) () -> iterator).spliterator(), false)
                    .map(res -> responseReader.apply(res.getQueryIterRes()));
        }

        @Override
        public Stream<T> get(long timeout, TimeUnit unit) {
            iterator.waitForStart(timeout, unit);
            return StreamSupport.stream(((Iterable<TransactionProto.Transaction.Iter.Res>) () -> iterator).spliterator(), false)
                    .map(res -> responseReader.apply(res.getQueryIterRes()));
        }
    }
}
