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
import grakn.client.GraknClient;
import grakn.client.GraknOptions;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.Channel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static grakn.client.GraknProtoBuilder.options;

public class RPCSession implements Session {

    private final Channel channel;
    private final String database;
    private final Type type;
    private final ByteString sessionId;
    private final AtomicBoolean isOpen;
    private final Timer pulse;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public RPCSession(GraknClient.Core client, String database, Type type, GraknOptions options) {
        this.channel = client.channel();
        this.database = database;
        this.type = type;
        blockingGrpcStub = GraknGrpc.newBlockingStub(channel);

        final SessionProto.Session.Open.Req openReq = SessionProto.Session.Open.Req.newBuilder()
                .setDatabase(database).setType(sessionType(type)).setOptions(options(options)).build();

        sessionId = blockingGrpcStub.sessionOpen(openReq).getSessionId();
        pulse = new Timer();
        isOpen = new AtomicBoolean(true);
        pulse.scheduleAtFixedRate(this.new PulseTask(), 0, 5000);
    }

    @Override
    public Transaction transaction(Transaction.Type type) {
        return transaction(type, new GraknOptions());
    }

    @Override
    public Transaction transaction(Transaction.Type type, GraknOptions options) {
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
            pulse.cancel();
            blockingGrpcStub.sessionClose(SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build());
        }
    }

    @Override
    public String database() {
        return database;
    }

    Channel channel() { return channel; }

    private static SessionProto.Session.Type sessionType(Session.Type type) {
        switch (type) {
            case DATA:
                return SessionProto.Session.Type.DATA;
            case SCHEMA:
                return SessionProto.Session.Type.SCHEMA;
            default:
                return SessionProto.Session.Type.UNRECOGNIZED;
        }
    }

    private class PulseTask extends TimerTask {
        @Override
        public void run() {
            if (!isOpen()) return;
            final SessionProto.Session.Pulse.Res res = blockingGrpcStub.sessionPulse(
                    SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionId).build());
            if (!res.getAlive()) {
                isOpen.set(false);
                pulse.cancel();
            }
        }
    }
}
