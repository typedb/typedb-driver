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
import grakn.protocol.TransactionProto;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.common.util.Objects.className;

class RPCIterator<T> extends AbstractIterator<T> {

    private volatile boolean started;
    private T first;

    private final TransactionProto.Transaction.Req request;
    private final Function<TransactionProto.Transaction.Iter.Res, T> responseReader;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final RPCResponseAccumulator responseCollector;

    RPCIterator(final TransactionProto.Transaction.Req req, final Function<TransactionProto.Transaction.Iter.Res, T> responseReader,
                       final StreamObserver<TransactionProto.Transaction.Req> requestObserver, final RPCResponseAccumulator responseCollector) {
        this.request = req;
        this.responseReader = responseReader;
        this.requestObserver = requestObserver;
        this.responseCollector = responseCollector;
    }

    private void nextBatch(final int iteratorID) {
        final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                .setIterReq(TransactionProto.Transaction.Iter.Req.newBuilder()
                        .setIteratorID(iteratorID)).build();
        requestObserver.onNext(request);
    }

    boolean isStarted() {
        return started;
    }

    synchronized void startIterating() {
        if (first != null) throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        requestObserver.onNext(request);
        first = computeNext();
    }

    synchronized void startIterating(final long timeout, final TimeUnit unit) {
        if (first != null) throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        requestObserver.onNext(request);
        first = computeNext(timeout, unit);
    }

    @Override
    protected T computeNext() {
        return computeNextInternal(collector -> {
            try {
                return collector.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        });
    }

    protected T computeNext(final long timeout, final TimeUnit unit) {
        return computeNextInternal(collector -> {
            try {
                return collector.take(timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GraknClientException(e);
            }
        });
    }

    private T computeNextInternal(Function<RPCResponseAccumulator, TransactionProto.Transaction.Res> awaiter) {
        if (first != null) {
            final T value = first;
            first = null;
            return value;
        }

        final TransactionProto.Transaction.Iter.Res res = awaiter.apply(responseCollector).getIterRes();
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
                return responseReader.apply(res);
        }
    }
}
