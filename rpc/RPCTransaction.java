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
import grakn.client.concept.Concepts;
import grakn.client.query.Query;
import grakn.protocol.GraknGrpc;
import grakn.protocol.TransactionProto;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.Grakn.Transaction.Type.WRITE;
import static grakn.client.common.ProtoBuilder.options;
import static grakn.client.common.ProtoBuilder.tracingData;

public class RPCTransaction implements Transaction {

    private final Session session;
    private final Transaction.Type type;
    private final Concepts concepts;
    private final Query query;
    private final RPCTransceiver transceiver;

    RPCTransaction(final RPCSession session, final ByteString sessionId, final Type type, final GraknOptions options) {
        try (ThreadTrace ignored = traceOnThread(type == WRITE ? "tx.write" : "tx.read")) {
            this.transceiver = RPCTransceiver.create(GraknGrpc.newStub(session.getChannel()));
            this.session = session;
            this.type = type;
            this.concepts = new Concepts(this);
            this.query = new Query(this);

            final TransactionProto.Transaction.Req openTxReq = TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData())
                    .setOpenReq(TransactionProto.Transaction.Open.Req.newBuilder()
                            .setSessionID(sessionId)
                            .setType(TransactionProto.Transaction.Type.forNumber(type.id()))
                            .setOptions(options(options))).build();

            this.transceiver.execute(openTxReq);
        }
    }

    public RPCTransceiver transceiver() {
        return transceiver;
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

    public void close() {
        transceiver.close();
    }

    @Override
    public boolean isOpen() {
        return transceiver.isOpen();
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

        transceiver.execute(commitReq);
        close();
    }
}
