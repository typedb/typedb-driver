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

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.common.util.Objects.className;

class ResponseIterator<T> extends AbstractIterator<T> {

    private final UUID requestId;
    private final SynchronizedStreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final TransactionRPC.ResponseCollector.Multiple responseCollector;
    private final Function<TransactionProto.Transaction.Res, Stream<T>> transformResponse;
    private Iterator<T> currentIterator;

    ResponseIterator(UUID requestId, SynchronizedStreamObserver<TransactionProto.Transaction.Req> requestObserver,
                     TransactionRPC.ResponseCollector.Multiple responseCollector, Function<TransactionProto.Transaction.Res, Stream<T>> transformResponse) {
        this.requestId = requestId;
        this.transformResponse = transformResponse;
        this.requestObserver = requestObserver;
        this.responseCollector = responseCollector;
    }

    @Override
    protected T computeNext() {
        if (currentIterator != null && currentIterator.hasNext()) {
            return currentIterator.next();
        }

        TransactionProto.Transaction.Res res;
        try {
            res = responseCollector.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }

        switch (res.getResCase()) {
            case CONTINUE:
                TransactionProto.Transaction.Req continueReq = TransactionProto.Transaction.Req.newBuilder()
                        .setId(requestId.toString()).setContinue(true).build();
                requestObserver.onNext(continueReq);
                return computeNext();
            case DONE:
                return endOfData();
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(className(TransactionProto.Transaction.Res.class)));
            default:
                currentIterator = transformResponse.apply(res).iterator();
                return computeNext();
        }
    }
}
