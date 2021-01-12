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

import com.google.protobuf.ByteString;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.ConceptManager;
import grakn.client.logic.LogicManager;
import grakn.client.query.QueryManager;
import grakn.protocol.GraknGrpc;
import grakn.protocol.TransactionProto;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grakn.client.GraknProtoBuilder.options;
import static grakn.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static grakn.client.common.exception.ErrorMessage.Client.UNKNOWN_REQUEST_ID;
import static grakn.client.common.tracing.TracingProtoBuilder.tracingData;
import static grakn.common.util.Objects.className;

public class RPCTransaction implements Transaction {

    private final Transaction.Type type;
    private final ConceptManager conceptManager;
    private final LogicManager logicManager;
    private final QueryManager queryManager;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final ResponseCollectors collectors;
    private final int networkLatencyMillis;
    private final AtomicBoolean isOpen;

    RPCTransaction(RPCSession.Core session, ByteString sessionId, Type type, GraknOptions options) {
        this.type = type;
        conceptManager = new ConceptManager(this);
        logicManager = new LogicManager(this);
        queryManager = new QueryManager(this);
        collectors = new ResponseCollectors();

        // Opening the StreamObserver exposes these atomics to another thread, so we must initialize them first.
        isOpen = new AtomicBoolean(true);
        requestObserver = GraknGrpc.newStub(session.channel()).transaction(responseObserver());
        final TransactionProto.Transaction.Req.Builder openRequest = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                        .setSessionId(sessionId)
                        .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                        .setOptions(options(options)));
        final Instant startTime = Instant.now();
        final TransactionProto.Transaction.Open.Res res = execute(openRequest).getOpenRes();
        final Instant endTime = Instant.now();
        networkLatencyMillis = (int) ChronoUnit.MILLIS.between(startTime, endTime) - res.getProcessingTimeMillis();
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    public ConceptManager concepts() {
        return conceptManager;
    }

    @Override
    public LogicManager logic() {
        return logicManager;
    }

    @Override
    public QueryManager query() {
        return queryManager;
    }

    @Override
    public void commit() {
        final TransactionProto.Transaction.Req.Builder commitReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance());
        try {
            execute(commitReq);
        } finally {
            close();
        }
    }

    @Override
    public void rollback() {
        final TransactionProto.Transaction.Req.Builder rollbackReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setRollbackReq(TransactionProto.Transaction.Rollback.Req.getDefaultInstance());
        execute(rollbackReq);
    }

    @Override
    public void close() {
        close(new Response.Done.Completed());
    }

    private void close(Response.Done doneResponse) {
        if (isOpen.compareAndSet(true, false)) {
            collectors.clear(doneResponse);
            requestObserver.onCompleted();
        }
    }

    public TransactionProto.Transaction.Res execute(TransactionProto.Transaction.Req.Builder request) {
        return executeAsync(request, res -> res).get();
    }

    public <T> QueryFuture<T> executeAsync(TransactionProto.Transaction.Req.Builder request, Function<TransactionProto.Transaction.Res, T> transformResponse) {
        if (!isOpen.get()) throw new GraknClientException(TRANSACTION_CLOSED);

        final ResponseCollector.Single responseCollector = new ResponseCollector.Single();
        final UUID requestId = UUID.randomUUID();
        request.setId(requestId.toString());
        collectors.put(requestId, responseCollector);
        requestObserver.onNext(request.build());
        return new QueryFuture<>(responseCollector, transformResponse);
    }

    public <T> Stream<T> stream(TransactionProto.Transaction.Req.Builder request, Function<TransactionProto.Transaction.Res, Stream<T>> transformResponse) {
        if (!isOpen.get()) throw new GraknClientException(TRANSACTION_CLOSED);

        final ResponseCollector.Multiple responseCollector = new ResponseCollector.Multiple();
        final UUID requestId = UUID.randomUUID();
        request.setId(requestId.toString());
        request.setLatencyMillis(networkLatencyMillis);
        collectors.put(requestId, responseCollector);
        requestObserver.onNext(request.build());
        final ResponseIterator<T> responseIterator = new ResponseIterator<>(requestId, requestObserver, responseCollector, transformResponse);
        return StreamSupport.stream(((Iterable<T>) () -> responseIterator).spliterator(), false);
    }

    private StreamObserver<TransactionProto.Transaction.Res> responseObserver() {
        return new StreamObserver<TransactionProto.Transaction.Res>() {
            @Override
            public void onNext(TransactionProto.Transaction.Res res) {
                if (!isOpen.get()) return;

                final UUID requestId = UUID.fromString(res.getId());
                final ResponseCollector collector = collectors.get(requestId);
                assert collector != null : UNKNOWN_REQUEST_ID.message(requestId);
                collector.add(new Response.Result(res));
            }

            @Override
            public void onError(Throwable ex) {
                assert ex instanceof StatusRuntimeException : "The server sent an exception of unexpected type " + ex;
                // TODO: this isn't nice - an error from one request isn't really appropriate for all of them (see #180)
                close(new Response.Done.Error((StatusRuntimeException) ex));
            }

            @Override
            public void onCompleted() {
                close(new Response.Done.Completed());
            }
        };
    }

    private static class ResponseCollectors {
        private final ConcurrentMap<UUID, ResponseCollector> collectors;

        private ResponseCollectors() {
            collectors = new ConcurrentHashMap<>();
        }

        private ResponseCollector get(UUID requestId) {
            return collectors.get(requestId);
        }

        private synchronized void put(UUID requestId, ResponseCollector collector) {
            collectors.put(requestId, collector);
        }

        private synchronized void clear(Response.Done doneResponse) {
            collectors.values().parallelStream().forEach(collector -> collector.add(doneResponse));
            collectors.clear();
        }
    }

    abstract static class ResponseCollector {
        private final AtomicBoolean isDone;
        /** gRPC response messages, errors, and notifications of abrupt termination are all sent to this blocking queue. */
        private final BlockingQueue<Response> responseBuffer;

        ResponseCollector() {
            isDone = new AtomicBoolean(false);
            responseBuffer = new LinkedBlockingQueue<>();
        }

        void add(Response response) {
            responseBuffer.add(response);
            if (!(response instanceof Response.Result) || isLastResponse(response.read())) isDone.set(true);
        }

        boolean isDone() {
            return isDone.get();
        }

        TransactionProto.Transaction.Res take() throws InterruptedException {
            return responseBuffer.take().read();
        }

        TransactionProto.Transaction.Res take(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            final Response response = responseBuffer.poll(timeout, unit);
            if (response != null) return response.read();
            else throw new TimeoutException();
        }

        abstract boolean isLastResponse(TransactionProto.Transaction.Res response);

        static class Single extends ResponseCollector {

            @Override
            boolean isLastResponse(TransactionProto.Transaction.Res response) {
                return true;
            }
        }

        static class Multiple extends ResponseCollector {

            @Override
            boolean isLastResponse(TransactionProto.Transaction.Res response) {
                return response.getDone();
            }
        }
    }

    abstract static class Response {

        abstract TransactionProto.Transaction.Res read();

        static class Result extends Response {
            private final TransactionProto.Transaction.Res response;

            Result(TransactionProto.Transaction.Res response) {
                this.response = response;
            }

            @Override
            TransactionProto.Transaction.Res read() {
                return response;
            }

            @Override
            public String toString() {
                return className(getClass()) + "{" + response + "}";
            }
        }

        static abstract class Done extends Response {

            static class Completed extends Done {

                @Override
                TransactionProto.Transaction.Res read() {
                    throw new GraknClientException(TRANSACTION_CLOSED);
                }

                @Override
                public String toString() {
                    return className(getClass()) + "{}";
                }
            }

            static class Error extends Done {

                private final StatusRuntimeException  error;

                Error(StatusRuntimeException error) {
                    // TODO: parse different gRPC errors into specific GraknClientException
                    this.error = error;
                }

                @Override
                TransactionProto.Transaction.Res read() {
                    throw new GraknClientException(error);
                }

                @Override
                public String toString() {
                    return className(getClass()) + "{" + error + "}";
                }
            }
        }
    }
}
