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

package grakn.client.query;

import com.google.common.collect.AbstractIterator;
import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.RPCTransceiver;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.common.util.Objects.className;

public class QueryIterator extends AbstractIterator<TransactionProto.Transaction.Iter.Res> {

    private Batch currentBatch;
    private volatile boolean started;
    private TransactionProto.Transaction.Iter.Res first;

    private final RPCTransceiver transceiver;

    public QueryIterator(final RPCTransceiver transceiver, final TransactionProto.Transaction.Iter.Req req) {
        this.transceiver = transceiver;
        sendRequest(req);
    }

    private void sendRequest(final TransactionProto.Transaction.Iter.Req req) {
        currentBatch = new Batch();

        final TransactionProto.Transaction.Req transactionReq = TransactionProto.Transaction.Req.newBuilder()
                .setIterReq(req).build();

        transceiver.sendMultipleAsync(transactionReq, currentBatch);
    }

    private void nextBatch(int iteratorID) {
        final TransactionProto.Transaction.Iter.Req iterReq = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setIteratorID(iteratorID).build();

        sendRequest(iterReq);
    }

    private static class Batch extends RPCTransceiver.MultiResponseCollector {
        @Override
        protected boolean isLastResponse(final TransactionProto.Transaction.Res response) {
            final TransactionProto.Transaction.Iter.Res iterRes = response.getIterRes();
            return iterRes.getIteratorID() != 0 || iterRes.getDone();
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void waitForStart(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
        if (first != null) {
            throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        }

        first = currentBatch.poll(timeout, unit).getIterRes();
    }

    public void waitForStart() throws TimeoutException {
        if (first != null) {
            throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        }

        first = currentBatch.poll().getIterRes();
    }

    public <T> QueryFuture<Stream<T>> await(final Function<QueryProto.Query.Iter.Res, T> responseReader) {
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
            res = currentBatch.take().getIterRes();
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

    private static class QueryStreamFuture<T> implements QueryFuture<Stream<T>> {
        private final QueryIterator iterator;
        private final Function<QueryProto.Query.Iter.Res, T> responseReader;

        public QueryStreamFuture(final QueryIterator iterator, final Function<QueryProto.Query.Iter.Res, T> responseReader) {
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
            try {
                iterator.waitForStart();
            } catch (TimeoutException e) {
                throw new GraknClientException(e);
            }
            return getInternal();
        }

        @Override
        public Stream<T> get(long timeout, TimeUnit unit) {
            try {
                iterator.waitForStart(timeout, unit);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(ex);
            } catch (TimeoutException ex) {
                throw new GraknClientException(ex);
            }
            return getInternal();
        }

        protected Stream<T> getInternal() {
            return StreamSupport.stream(((Iterable<TransactionProto.Transaction.Iter.Res>) () -> iterator).spliterator(), false)
                    .map(res -> responseReader.apply(res.getQueryIterRes()));
        }
    }
}
