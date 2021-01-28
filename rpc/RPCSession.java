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
import grakn.client.GraknClient;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static grakn.client.GraknProtoBuilder.options;

public class RPCSession implements GraknClient.Session {
    private final Channel channel;
    private final String database;
    private final Type type;
    private final ByteString sessionId;
    private final AtomicBoolean isOpen;
    private final Timer pulse;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public RPCSession(RPCClient client, String database, Type type, GraknOptions options) {
        try {
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
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type) {
        return transaction(type, GraknOptions.core());
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type, GraknOptions options) {
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
            try {
                blockingGrpcStub.sessionClose(SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build());
            } catch (StatusRuntimeException e) {
                throw GraknClientException.of(e);
            }
        }
    }

    @Override
    public String database() {
        return database;
    }

    Channel channel() { return channel; }

    public void pulse() {
        boolean alive;
        try {
            alive = blockingGrpcStub.sessionPulse(
                    SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionId).build()).getAlive();
        } catch (StatusRuntimeException exception) {
            alive = false;
        }
        if (!alive) {
            isOpen.set(false);
            pulse.cancel();
        }
    }

    private static SessionProto.Session.Type sessionType(Type type) {
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
            pulse();
        }
    }
}
