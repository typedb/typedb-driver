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

    private final TransactionProto.Transaction.Req initialRequest;
    private final Function<TransactionProto.Transaction.Res, T> responseReader;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final RPCResponseCollector.Multiple responseCollector;

    RPCIterator(final TransactionProto.Transaction.Req initialRequest, final Function<TransactionProto.Transaction.Res, T> responseReader,
                       final StreamObserver<TransactionProto.Transaction.Req> requestObserver, final RPCResponseCollector.Multiple responseCollector) {
        this.initialRequest = initialRequest;
        this.responseReader = responseReader;
        this.requestObserver = requestObserver;
        this.responseCollector = responseCollector;
    }

    private void nextBatch(final String requestId) {
        final TransactionProto.Transaction.Req nextRequest = TransactionProto.Transaction.Req.newBuilder().setId(requestId).build();
        requestObserver.onNext(nextRequest);
    }

    boolean isStarted() {
        return started;
    }

    synchronized void startIterating() {
        if (first != null) throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        requestObserver.onNext(initialRequest);
        first = computeNext();
    }

    synchronized void startIterating(final long timeout, final TimeUnit unit) {
        if (first != null) throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        requestObserver.onNext(initialRequest);
        first = computeNext(timeout, unit);
    }

    @Override
    protected T computeNext() {
        // TODO: This is horrible
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

    private T computeNextInternal(Function<RPCResponseCollector, TransactionProto.Transaction.Res> awaiter) {
        if (first != null) {
            final T value = first;
            first = null;
            return value;
        }

        final TransactionProto.Transaction.Res res = awaiter.apply(responseCollector);
        started = true;
        switch (res.getResCase()) {
            case DONE:
                if (res.getDone()) {
                    return endOfData();
                } else {
                    nextBatch(initialRequest.getId());
                    return computeNext();
                }
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(className(TransactionProto.Transaction.Res.class)));
            default:
                return responseReader.apply(res);
        }
    }
}
