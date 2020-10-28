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
import grakn.client.concept.ConceptManager;
import grakn.client.query.QueryManager;
import grakn.protocol.TransactionProto;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;

public class RPCTransaction implements Transaction {

    private final Transaction.Type type;
    private final ConceptManager conceptManager;
    private final QueryManager queryManager;
    private final StreamObserver<TransactionProto.Transaction.Req> requestObserver;
    private final BlockingQueue<RPCResponseAccumulator> collectorQueue;
    private final AtomicBoolean isOpen;

    RPCTransaction(final RPCSession session, final ByteString sessionId, final Type type, final GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.type = type;
            conceptManager = new ConceptManager(this);
            queryManager = new QueryManager(this);
            collectorQueue = new LinkedBlockingQueue<>();

            final StreamObserver<TransactionProto.Transaction.Res> responseObserver = createResponseObserver();
            requestObserver = session.getAsyncGrpcStub().transaction(responseObserver);

            final TransactionProto.Transaction.Req openRequest = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options))).build();
            execute(openRequest);
            isOpen = new AtomicBoolean(true);
        }
    }

    private StreamObserver<TransactionProto.Transaction.Res> createResponseObserver() {
        return new StreamObserver<TransactionProto.Transaction.Res>() {
            @Override
            public synchronized void onNext(final TransactionProto.Transaction.Res value) {
                processResponse(new RPCResponse.Ok(value));
            }

            @Override
            public void onError(final Throwable throwable) {
                assert throwable instanceof StatusRuntimeException : "The server only yields these exceptions";
                while (collectorQueue.peek() != null)
                    processResponse(new RPCResponse.Error((StatusRuntimeException) throwable));
            }

            @Override
            public void onCompleted() {
                while (collectorQueue.peek() != null) processResponse(new RPCResponse.Completed());
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
        final TransactionProto.Transaction.Req commitReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance()).build();
        execute(commitReq);
        close();
    }

    @Override
    public void rollback() {
        final TransactionProto.Transaction.Req rollbackReq = TransactionProto.Transaction.Req.newBuilder()
                .putAllMetadata(tracingData())
                .setRollbackReq(TransactionProto.Transaction.Rollback.Req.getDefaultInstance()).build();
        execute(rollbackReq);
    }

    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            requestObserver.onCompleted();
        }
    }

    public TransactionProto.Transaction.Res execute(final TransactionProto.Transaction.Req request) {
        return executeAsync(request, res -> res).get();
    }

    public <T> QueryFuture<T> executeAsync(final TransactionProto.Transaction.Req request, final Function<TransactionProto.Transaction.Res, T> responseReader) {
        try (ThreadTrace trace = traceOnThread("executeAsync")) {
            final RPCResponseAccumulator collector = new RPCResponseAccumulator();
            dispatchRequest(request, collector);
            return new QueryFuture.Execute<>(collector, responseReader);
        }
    }

    public <T> Stream<T> iterate(final TransactionProto.Transaction.Iter.Req iterRequest, final Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
        return iterateAsync(iterRequest, responseReader).get();
    }

    public <T> QueryFuture<Stream<T>> iterateAsync(final TransactionProto.Transaction.Iter.Req iterRequest, final Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
        try (ThreadTrace trace = traceOnThread("iterateAsync")) {
            final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                    .setIterReq(iterRequest).build();
            final RPCResponseAccumulator collector = new RPCResponseAccumulator();
            final RPCIterator<T> iterator = new RPCIterator<>(request, responseReader, requestObserver, collector);
            collectorQueue.add(collector);
            return new QueryFuture.Stream<>(iterator);
        }
    }

    private synchronized void dispatchRequest(final TransactionProto.Transaction.Req request, final RPCResponseAccumulator collector) {
        // We must add the response collectors in the exact same order we send the requests
        // TODO: this doesn't seem like it should be true
        collectorQueue.add(collector);
        requestObserver.onNext(request);
    }

    private void processResponse(final RPCResponse res) {
        // TODO: This probably doesn't work if we send a slow query followed by a fast query down the same transaction.
        final RPCResponseAccumulator currentCollector = collectorQueue.peek();
        assert currentCollector != null;
        currentCollector.onResponse(res);
        if (currentCollector.isDone()) collectorQueue.poll();
    }
}
