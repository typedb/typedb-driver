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
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.ManagedChannel;

import static grakn.client.common.ProtoBuilder.options;

public class RPCSession implements Session {

    private final ManagedChannel channel;
    private final String databaseName;
    private final GraknGrpc.GraknBlockingStub sessionStub;
    private final ByteString sessionId;
    private boolean isOpen;

    RPCSession(final ManagedChannel channel, final String databaseName, final Session.Type type, final GraknOptions options) {
        this.databaseName = databaseName;
        this.channel = channel;
        this.sessionStub = GraknGrpc.newBlockingStub(channel);

        final SessionProto.Session.Open.Req openReq = SessionProto.Session.Open.Req.newBuilder()
                .setDatabase(databaseName)
                .setType(sessionType(type))
                .setOptions(options(options)).build();

        final SessionProto.Session.Open.Res response = sessionStub.sessionOpen(openReq);
        sessionId = response.getSessionID();
        isOpen = true;
    }

    @Override
    public Transaction transaction(final Transaction.Type type, final GraknOptions options) {
        return new RPCTransaction(this, sessionId, type, options);
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        if (!isOpen) return;

        final SessionProto.Session.Close.Req closeReq = SessionProto.Session.Close.Req.newBuilder()
                .setSessionID(sessionId).build();

        sessionStub.sessionClose(closeReq);
        isOpen = false;
    }

    @Override
    public Database database() {
        return new RPCDatabase(databaseName);
    }

    ManagedChannel getChannel() {
        return channel;
    }

    private static SessionProto.Session.Type sessionType(final Session.Type type) {
        switch (type) {
            case DATA:
                return SessionProto.Session.Type.DATA;
            case SCHEMA:
                return SessionProto.Session.Type.SCHEMA;
            default:
                return SessionProto.Session.Type.UNRECOGNIZED;
        }
    }
}
