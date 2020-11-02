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
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.GraknOptions;
import grakn.protocol.SessionProto;
import grakn.protocol.SessionServiceGrpc;

import java.util.concurrent.atomic.AtomicBoolean;

import static grakn.client.common.ProtoBuilder.options;

public class RPCSession implements Session {

    private final GraknClient client;
    private final String database;
    private final Type type;
    private final ByteString sessionId;
    private final AtomicBoolean isOpen;
    private final SessionServiceGrpc.SessionServiceBlockingStub sessionService;

    RPCSession(final GraknClient client, final String database, final Type type, final GraknOptions options) {
        this.client = client;
        this.database = database;
        this.type = type;
        sessionService = SessionServiceGrpc.newBlockingStub(client.channel());

        final SessionProto.Session.Open.Req openReq = SessionProto.Session.Open.Req.newBuilder()
                .setDatabase(database).setType(sessionType(type)).setOptions(options(options)).build();

        sessionId = sessionService.open(openReq).getSessionID();
        isOpen = new AtomicBoolean(true);
    }

    @Override
    public Transaction transaction(final Transaction.Type type) {
        return transaction(type, new GraknOptions());
    }

    @Override
    public Transaction transaction(final Transaction.Type type, final GraknOptions options) {
        return new RPCTransaction(this, sessionId, type, options);
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
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            sessionService.close(SessionProto.Session.Close.Req.newBuilder().setSessionID(sessionId).build());
        }
    }

    @Override
    public String database() {
        return database;
    }

    GraknClient client() { return client; }

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
