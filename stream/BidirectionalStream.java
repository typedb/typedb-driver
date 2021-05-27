/*
 * Copyright (C) 2021 Vaticle
 *
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

package com.vaticle.typedb.client.stream;

import com.google.protobuf.ByteString;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.Req;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.Res;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.ResPart;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.vaticle.typedb.client.common.collection.Bytes.bytesToUUID;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.UNKNOWN_REQUEST_ID;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.ILLEGAL_ARGUMENT;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.UUIDAsByteString;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

public class BidirectionalStream implements AutoCloseable {

    private final ResponseCollector<Res> resCollector;
    private final ResponseCollector<ResPart> resPartCollector;
    private final RequestTransmitter.Dispatcher dispatcher;
    private final AtomicBoolean isOpen;

    public BidirectionalStream(TypeDBStub stub, RequestTransmitter transmitter) {
        resPartCollector = new ResponseCollector<>();
        resCollector = new ResponseCollector<>();
        isOpen = new AtomicBoolean(false);
        dispatcher = transmitter.dispatcher(stub.transaction(new ResponseObserver()));
        isOpen.set(true);
    }

    public Single<Res> single(Req.Builder request, boolean batch) {
        UUID requestID = UUID.randomUUID();
        Req req = request.setReqId(UUIDAsByteString(requestID)).build();
        ResponseCollector.Queue<Res> queue = resCollector.queue(requestID);
        if (batch) dispatcher.dispatch(req);
        else dispatcher.dispatchNow(req);
        return new Single<>(queue);
    }

    public Stream<ResPart> stream(Req.Builder request) {
        UUID requestID = UUID.randomUUID();
        ResponseCollector.Queue<ResPart> collector = resPartCollector.queue(requestID);
        dispatcher.dispatch(request.setReqId(UUIDAsByteString(requestID)).build());
        ResponsePartIterator iterator = new ResponsePartIterator(requestID, collector, dispatcher);
        return StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED | IMMUTABLE), false);
    }

    public boolean isOpen() {
        return isOpen.get();
    }

    private void collect(Res res) {
        UUID requestID = byteStringAsUUID(res.getReqId());
        ResponseCollector.Queue<Res> collector = resCollector.get(requestID);
        if (collector != null) collector.put(res);
        else throw new TypeDBClientException(UNKNOWN_REQUEST_ID, requestID);
    }

    private void collect(ResPart resPart) {
        UUID requestID = byteStringAsUUID(resPart.getReqId());
        ResponseCollector.Queue<ResPart> collector = resPartCollector.get(requestID);
        if (collector != null) collector.put(resPart);
        else throw new TypeDBClientException(UNKNOWN_REQUEST_ID, requestID);
    }

    private static UUID byteStringAsUUID(ByteString byteString) {
        return bytesToUUID(byteString.toByteArray());
    }

    @Override
    public void close() {
        close(null);
    }

    private void close(@Nullable StatusRuntimeException error) {
        if (isOpen.compareAndSet(true, false)) {
            resCollector.close(error);
            resPartCollector.close(error);
            try {
                dispatcher.close();
            } catch (StatusRuntimeException e) {
                throw TypeDBClientException.of(e);
            }
        }
    }

    public static class Single<T> {

        private final ResponseCollector.Queue<T> queue;

        public Single(ResponseCollector.Queue<T> queue) {
            this.queue = queue;
        }

        public T get() {
            return queue.take();
        }
    }

    private class ResponseObserver implements StreamObserver<Server> {

        @Override
        public void onNext(Server serverMsg) {
            if (!isOpen.get()) return;

            switch (serverMsg.getServerCase()) {
                case RES:
                    collect(serverMsg.getRes());
                    break;
                case RES_PART:
                    collect(serverMsg.getResPart());
                    break;
                default:
                case SERVER_NOT_SET:
                    throw new TypeDBClientException(ILLEGAL_ARGUMENT);
            }
        }

        @Override
        public void onError(Throwable t) {
            assert t instanceof StatusRuntimeException : "The server sent an exception of unexpected type " + t.getClass();
            // TODO: this isn't nice - an error from one request isn't really appropriate for all of them (see #180)
            close((StatusRuntimeException) t);
        }

        @Override
        public void onCompleted() {
            close();
        }
    }
}
