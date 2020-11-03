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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.common.util.Objects.className;

class QueryIterator<T> extends AbstractIterator<T> {

    private volatile boolean started;
    private T first;

    private final UUID requestId;
    private final TransactionProto.Transaction.Req initialRequest;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final RPCTransaction.ResponseCollector.Multiple responseCollector;
    private final Function<TransactionProto.Transaction.Res, T> transformResponse;

    QueryIterator(TransactionProto.Transaction.Req request, StreamObserver<TransactionProto.Transaction.Req> requestObserver,
                  RPCTransaction.ResponseCollector.Multiple responseCollector, Function<TransactionProto.Transaction.Res, T> transformResponse) {
        this.initialRequest = request;
        this.requestId = UUID.fromString(request.getId());
        this.transformResponse = transformResponse;
        this.requestObserver = requestObserver;
        this.responseCollector = responseCollector;
    }

    boolean isStarted() {
        return started;
    }

    synchronized void fetchFirstBatch() {
        if (first != null) throw new GraknClientException(new IllegalStateException("Should not poll RPCIterator multiple times"));
        requestObserver.onNext(initialRequest);
        first = computeNext();
    }

    private void fetchNextBatchAsync() {
        final TransactionProto.Transaction.Req fetchReq = TransactionProto.Transaction.Req.newBuilder()
                .setId(requestId.toString()).setContinue(true).build();
        requestObserver.onNext(fetchReq);
    }

    @Override
    protected T computeNext() {
        if (first != null) {
            final T value = first;
            first = null;
            return value;
        }

        final TransactionProto.Transaction.Res res;
        try {
            res = responseCollector.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }

        started = true;
        switch (res.getResCase()) {
            case DONE:
                return endOfData();
            case CONTINUE:
                fetchNextBatchAsync();
                return computeNext();
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(className(TransactionProto.Transaction.Res.class)));
            default:
                return transformResponse.apply(res);
        }
    }
}
