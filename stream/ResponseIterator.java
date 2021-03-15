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

package grakn.client.stream;

import grakn.client.common.GraknClientException;
import grakn.client.common.Proto;
import grakn.protocol.TransactionProto;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import static grakn.client.common.ErrorMessage.Client.MISSING_RESPONSE;
import static grakn.client.common.ErrorMessage.Internal.ILLEGAL_STATE;

public class ResponseIterator implements Iterator<TransactionProto.Transaction.ResPart> {

    private final UUID requestID;
    private final RequestTransmitter.Dispatcher dispatcher;
    private final ResponseCollector.Queue<TransactionProto.Transaction.ResPart> responseCollector;
    private TransactionProto.Transaction.ResPart next;
    private State state;

    enum State {EMTPY, FETCHED, DONE}

    public ResponseIterator(UUID requestID, ResponseCollector.Queue<TransactionProto.Transaction.ResPart> responseQueue,
                            RequestTransmitter.Dispatcher requestDispatcher) {
        this.requestID = requestID;
        this.dispatcher = requestDispatcher;
        this.responseCollector = responseQueue;
        state = State.EMTPY;
        next = null;
    }

    private boolean fetchAndCheck() {
        TransactionProto.Transaction.ResPart resPart = responseCollector.take();
        switch (resPart.getResCase()) {
            case RES_NOT_SET:
                throw new GraknClientException(MISSING_RESPONSE.message(requestID));
            case STREAM_RES_PART:
                if (resPart.getStreamResPart().getIsDone()) {
                    state = State.DONE;
                    return false;
                } else {
                    dispatcher.dispatch(Proto.Transaction.streamReq(requestID));
                    return fetchAndCheck();
                }
            default:
                next = resPart;
                state = State.FETCHED;
                return true;
        }
    }

    @Override
    public boolean hasNext() {
        switch (state) {
            case DONE:
                return false;
            case FETCHED:
                return true;
            case EMTPY:
                return fetchAndCheck();
            default:
                throw new GraknClientException(ILLEGAL_STATE);
        }
    }

    @Override
    public TransactionProto.Transaction.ResPart next() {
        if (!hasNext()) throw new NoSuchElementException();
        state = State.EMTPY;
        return next;
    }
}
