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
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.query.Query;
import grakn.client.rpc.response.BatchResponseCollector;
import grakn.client.rpc.response.ResponseCollector;
import grakn.client.rpc.response.ResponseListener;
import grakn.client.rpc.response.SingleResponseCollector;
import grakn.protocol.GraknGrpc;
import grakn.protocol.TransactionProto;
import io.grpc.stub.StreamObserver;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;
import static grakn.client.common.exception.ErrorMessage.Client.CONNECTION_CLOSED;

public class RPCTransaction implements Transaction {

    private final Session session;
    private final Transaction.Type type;
    private final Concepts concepts;
    private final Query query;
    private final StreamObserver<TransactionProto.Transaction.Req> requestSender;
    private final ResponseListener responseListener;

    RPCTransaction(final RPCSession session, final ByteString sessionId, final Type type, final GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.session = session;
            this.type = type;
            this.concepts = new Concepts(this);
            this.query = new Query(this);

            final GraknGrpc.GraknStub stub = GraknGrpc.newStub(session.getChannel());
            this.responseListener = new ResponseListener();
            this.requestSender = stub.transaction(responseListener);

            final TransactionProto.Transaction.Req openTxReq = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options))).build();

            this.execute(openTxReq);
        }
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Session session() {
        return session;
    }

    @Override
    public Database database() {
        return session.database();
    }

    @Override
    public synchronized boolean isOpen() {
        return !responseListener.isTerminated();
    }

    @Override
    public Concepts concepts() {
        return concepts;
    }

    @Override
    public Query query() {
        return query;
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
    public void close() {
        try {
            requestSender.onCompleted();
            responseListener.onCompleted();
        } catch (IllegalStateException e) {
            //IGNORED
            //This is needed to handle the fact that:
            //1. Commits can lead to transaction closures and
            //2. Error can lead to connection closures but the transaction may stay open
            //When this occurs a "half-closed" state is thrown which we can safely ignore
        }
    }

    public TransactionProto.Transaction.Res execute(final TransactionProto.Transaction.Req request) {
        try {
            try (ThreadTrace trace = traceOnThread("execute")) {
                SingleResponseCollector collector = new SingleResponseCollector();
                dispatch(request, collector);
                return collector.receive();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }
    }

    public Stream<TransactionProto.Transaction.Iter.Res> iterate(final TransactionProto.Transaction.Iter.Req request) {
        return StreamSupport.stream(((Iterable<TransactionProto.Transaction.Iter.Res>) () -> new RPCIterator(this, request)).spliterator(), false);
    }

    void executeAsync(final TransactionProto.Transaction.Req request, final SingleResponseCollector collector) {
        try (ThreadTrace trace = traceOnThread("executeAsync")) {
            dispatch(request, collector);
        }
    }

    void iterateAsync(final TransactionProto.Transaction.Iter.Req iterRequest, final BatchResponseCollector collector) {
        try (ThreadTrace trace = traceOnThread("iterateAsync")) {
            final TransactionProto.Transaction.Req request = TransactionProto.Transaction.Req.newBuilder()
                    .setIterReq(iterRequest).build();
            dispatch(request, collector);
        }
    }

    private void dispatch(final TransactionProto.Transaction.Req request, final ResponseCollector collector) {

        // We must add the response collectors in exact the same order we send the requests
        synchronized (this) {
            synchronized (responseListener) {
                if (responseListener.isTerminated()) {
                    throw new GraknClientException(CONNECTION_CLOSED);
                }
                responseListener.addCollector(collector); // Must add collector first to be watertight
            }
            requestSender.onNext(request);
        }
    }
}
