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
import grakn.client.common.exception.ErrorMessage;
import grakn.client.common.exception.GraknClientException;
import grakn.common.collection.ConcurrentSet;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static grakn.client.common.exception.ErrorMessage.Client.SESSION_CLOSED;
import static grakn.client.common.proto.ProtoBuilder.options;

public class SessionRPC implements GraknClient.Session {
    private final ClientRPC client;
    private final DatabaseRPC database;
    private final Type type;
    private final ByteString sessionId;
    private final AtomicBoolean isOpen;
    private final Timer pulse;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;
    private final int networkLatencyMillis;
    private final ConcurrentSet<TransactionRPC> transactions;

    public SessionRPC(ClientRPC client, String database, Type type, GraknOptions options) {
        try {
            client.reconnect();
            this.client = client;
            this.type = type;
            blockingGrpcStub = GraknGrpc.newBlockingStub(client.channel());
            this.database = new DatabaseRPC(client.databases(), database);
            Instant startTime = Instant.now();
            SessionProto.Session.Open.Res res = blockingGrpcStub.sessionOpen(openRequest(database, type, options));
            Instant endTime = Instant.now();
            sessionId = res.getSessionId();
            pulse = new Timer();
            isOpen = new AtomicBoolean(true);
            transactions = new ConcurrentSet<>();
            pulse.scheduleAtFixedRate(this.new PulseTask(), 0, 5000);
            networkLatencyMillis = (int) (ChronoUnit.MILLIS.between(startTime, endTime) - res.getProcessingTimeMillis());
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }

    private SessionProto.Session.Open.Req openRequest(String database, Type type, GraknOptions options) {
        return SessionProto.Session.Open.Req.newBuilder().setDatabase(database)
                .setType(sessionType(type)).setOptions(options(options)).build();
    }

    @Override
    public synchronized GraknClient.Transaction transaction(GraknClient.Transaction.Type type) {
        return transaction(type, GraknOptions.core());
    }

    @Override
    public synchronized GraknClient.Transaction transaction(GraknClient.Transaction.Type type, GraknOptions options) {
        if (isOpen.get()) {
            TransactionRPC transactionRPC = new TransactionRPC(this, sessionId, type, options, client.batcher().nextExecutor());
            transactions.add(transactionRPC);
            return transactionRPC;
        } else {
            throw new GraknClientException(SESSION_CLOSED);
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
    public synchronized void close() {
        if (isOpen.compareAndSet(true, false)) {
            transactions.forEach(TransactionRPC::close);
            client.removeSession(this);
            pulse.cancel();
            try {
                client.reconnect();
                SessionProto.Session.Close.Res ignored = blockingGrpcStub.sessionClose(
                        SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build()
                );
            } catch (StatusRuntimeException e) {
                throw GraknClientException.of(e);
            }
        }
    }

    @Override
    public DatabaseRPC database() {
        return database;
    }

    int networkLatencyMillis() {
        return networkLatencyMillis;
    }

    ClientRPC client() { return client; }

    ByteString id() { return sessionId; }

    Channel channel() {
        return client.channel();
    }

    void reconnect() {
        client.reconnect();
    }

    private void pulse() {
        boolean alive;
        try {
            alive = blockingGrpcStub.sessionPulse(
                    SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionId).build()
            ).getAlive();
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
