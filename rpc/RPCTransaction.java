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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.common.exception.ErrorMessage.Client.UNKNOWN_REQUEST_ID;

public class RPCTransaction implements Transaction {

    private final Transaction.Type type;
    private final ConceptManager conceptManager;
    private final QueryManager queryManager;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final ConcurrentMap<UUID, RPCResponseCollector> collectors;
    private final AtomicBoolean isOpen;

    RPCTransaction(final RPCSession session, final ByteString sessionId, final Type type, final GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.type = type;
            conceptManager = new ConceptManager(this);
            queryManager = new QueryManager(this);
            collectors = new ConcurrentHashMap<>();

            final StreamObserver<TransactionProto.Transaction.Res> responseObserver = createResponseObserver();
            requestObserver = TransactionServiceGrpc.newStub(session.getChannel()).stream(responseObserver);

            final TransactionProto.Transaction.Req.Builder openRequest = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options)));
            execute(openRequest);
            isOpen = new AtomicBoolean(true);
        }
    }

    private StreamObserver<TransactionProto.Transaction.Res> createResponseObserver() {
        return new StreamObserver<TransactionProto.Transaction.Res>() {
            @Override
            public void onNext(final TransactionProto.Transaction.Res value) {
                processResponse(new RPCResponse.Ok(value));
            }

            @Override
            public void onError(final Throwable throwable) {
                assert throwable instanceof StatusRuntimeException : "The server only yields these exceptions";
                processResponse(new RPCResponse.Error((StatusRuntimeException) throwable));
            }

            @Override
            public void onCompleted() {
                processResponse(new RPCResponse.Completed());
            }
        };
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

    public TransactionProto.Transaction.Res execute(final TransactionProto.Transaction.Req.Builder request) {
        return executeAsync(request, res -> res).get();
    }

    public <T> QueryFuture<T> executeAsync(final TransactionProto.Transaction.Req.Builder request, final Function<TransactionProto.Transaction.Res, T> responseReader) {
        try (ThreadTrace trace = traceOnThread("executeAsync")) {
            final RPCResponseCollector collector = new RPCResponseCollector.Single();
            dispatchRequest(request, collector);
            return new QueryFuture.Execute<>(collector, responseReader);
        }
    }

    public <T> Stream<T> iterate(final TransactionProto.Transaction.Req.Builder request, final Function<TransactionProto.Transaction.Res, T> responseReader) {
        return iterateAsync(request, responseReader).get();
    }

    public <T> QueryFuture<Stream<T>> iterateAsync(final TransactionProto.Transaction.Req.Builder request, final Function<TransactionProto.Transaction.Res, T> responseReader) {
        try (ThreadTrace trace = traceOnThread("iterateAsync")) {
            final RPCResponseCollector.Multiple collector = new RPCResponseCollector.Multiple();
            final UUID requestId = UUID.randomUUID();
            request.setId(requestId.toString());
            collectors.put(requestId, collector);
            return new QueryFuture.Stream<>(new RPCIterator<>(request.build(), responseReader, requestObserver, collector));
        }
    }

    private void dispatchRequest(final TransactionProto.Transaction.Req.Builder request, final RPCResponseCollector collector) {
        final UUID requestId = UUID.randomUUID();
        request.setId(requestId.toString());
        collectors.put(requestId, collector);
        requestObserver.onNext(request.build());
    }

    private void processResponse(final RPCResponse res) {
        if (res instanceof RPCResponse.Ok) {
            final UUID requestId = UUID.fromString(res.read().getId());
            final RPCResponseCollector collector = collectors.get(requestId);
            if (collector == null) throw new GraknClientException(UNKNOWN_REQUEST_ID.message(requestId));
            collector.onResponse(res);
            if (collector.isDone()) collectors.remove(requestId);
        } else if (res instanceof RPCResponse.Error) {
            // TODO: is it desirable behaviour to kill all the pending requests?
            // TODO: this isn't nice in any case - the error for other pending requests is misleading
            collectors.values().parallelStream().forEach(x -> x.onResponse(res));
            collectors.clear();
            close();
        }
    }
}
