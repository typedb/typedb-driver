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
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.ConceptManager;
import grakn.client.query.QueryManager;
import grakn.protocol.TransactionProto;
import grakn.protocol.TransactionServiceGrpc;
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

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.common.exception.ErrorMessage.Client.CONNECTION_CLOSED;
import static grakn.client.common.exception.ErrorMessage.Client.UNKNOWN_REQUEST_ID;
import static grakn.common.util.Objects.className;

public class RPCTransaction implements Transaction {

    private final Transaction.Type type;
    private final ConceptManager conceptManager;
    private final QueryManager queryManager;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final ConcurrentMap<UUID, ResponseCollector> collectors;
    private final int networkLatencyMillis;
    private final AtomicBoolean isOpen;

    RPCTransaction(RPCSession session, ByteString sessionId, Type type, GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.type = type;
            conceptManager = new ConceptManager(this);
            queryManager = new QueryManager(this);
            collectors = new ConcurrentHashMap<>();
            requestObserver = TransactionServiceGrpc.newStub(session.client().channel()).stream(responseObserver());

            final TransactionProto.Transaction.Req.Builder openRequest = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionId(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options)));
            final Instant startTime = Instant.now();
            final TransactionProto.Transaction.Res res = execute(openRequest);
            final Instant endTime = Instant.now();
            networkLatencyMillis = (int) ChronoUnit.MILLIS.between(startTime, endTime) - res.getOpenRes().getProcessingTimeMillis();
            System.out.println("Network latency is " + networkLatencyMillis + "ms");
            isOpen = new AtomicBoolean(true);
        }
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
    public QueryManager query() {
        return queryManager;
    }

    @Override
    public void commit() {
        final TransactionProto.Transaction.Req.Builder commitReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance());
        execute(commitReq);
        close();
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
        if (isOpen.compareAndSet(true, false)) {
            requestObserver.onCompleted();
        }
    }

    public TransactionProto.Transaction.Res execute(TransactionProto.Transaction.Req.Builder request) {
        return executeAsync(request, res -> res).get();
    }

    public <T> QueryFuture<T> executeAsync(TransactionProto.Transaction.Req.Builder request, Function<TransactionProto.Transaction.Res, T> transformResponse) {
        try (ThreadTrace ignored = traceOnThread("executeAsync")) {
            final ResponseCollector.Single responseCollector = new ResponseCollector.Single();
            final UUID requestId = UUID.randomUUID();
            request.setId(requestId.toString());
            collectors.put(requestId, responseCollector);
            return new QueryFuture.Execute<>(request.build(), requestObserver, responseCollector, transformResponse);
        }
    }

    public <T> Stream<T> iterate(TransactionProto.Transaction.Req.Builder request, Function<TransactionProto.Transaction.Res, T> transformResponse) {
        return iterateAsync(request, transformResponse).get();
    }

    public <T> QueryFuture<Stream<T>> iterateAsync(TransactionProto.Transaction.Req.Builder request, Function<TransactionProto.Transaction.Res, T> transformResponse) {
        try (ThreadTrace ignored = traceOnThread("iterateAsync")) {
            final ResponseCollector.Multiple responseCollector = new ResponseCollector.Multiple();
            final UUID requestId = UUID.randomUUID();
            request.setId(requestId.toString());
            request.setLatencyMillis(networkLatencyMillis);
            collectors.put(requestId, responseCollector);
            return new QueryFuture.Stream<>(request.build(), requestObserver, responseCollector, transformResponse);
        }
    }

    private StreamObserver<TransactionProto.Transaction.Res> responseObserver() {
        return new StreamObserver<TransactionProto.Transaction.Res>() {
            @Override
            public void onNext(TransactionProto.Transaction.Res res) {
                final UUID requestId = UUID.fromString(res.getId());
                final ResponseCollector collector = collectors.get(requestId);
                if (collector == null) throw new GraknClientException(UNKNOWN_REQUEST_ID.message(requestId));
                collector.onResponse(new Response.Ok(res));
                if (collector.isDone()) collectors.remove(requestId);
            }

            @Override
            public void onError(Throwable error) {
                assert error instanceof StatusRuntimeException : "The server only yields these exceptions";
                final Response res = new Response.Error((StatusRuntimeException) error);
                // TODO: is it desirable behaviour to kill all the pending requests?
                // TODO: this isn't nice in any case - the error for other pending requests is misleading
                collectors.values().parallelStream().forEach(x -> x.onResponse(res));
                collectors.clear();
                close();
            }

            @Override
            public void onCompleted() {
                final Response res = new Response.ConnectionClosed();
                collectors.values().parallelStream().forEach(x -> x.onResponse(res));
                collectors.clear();
                // TODO: maybe this should just be close()? But if the server terminates abruptly...
                isOpen.set(false);
            }
        };
    }

    abstract static class Response {

        abstract TransactionProto.Transaction.Res read();

        static class Ok extends Response {
            private final TransactionProto.Transaction.Res response;

            Ok(TransactionProto.Transaction.Res response) {
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

        static class Error extends Response {
            private final StatusRuntimeException error;

            Error(StatusRuntimeException error) {
                this.error = error;
            }

            @Override
            TransactionProto.Transaction.Res read() {
                // TODO: parse different GRPC errors into specific GraknClientException
                throw new GraknClientException(error);
            }

            @Override
            public String toString() {
                return className(getClass()) + "{" + error + "}";
            }
        }

        static class ConnectionClosed extends Response {

            @Override
            TransactionProto.Transaction.Res read() {
                throw new GraknClientException(CONNECTION_CLOSED);
            }
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

        void onResponse(Response response) {
            responseBuffer.add(response);
            if (!(response instanceof Response.Ok) || isLastResponse(response.read())) isDone.set(true);
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
}
