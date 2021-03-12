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
import grakn.client.common.proto.ProtoBuilder;
import grakn.common.collection.ConcurrentSet;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import static grakn.client.common.exception.ErrorMessage.Client.SESSION_CLOSED;
import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_ARGUMENT;

public class SessionRPC implements GraknClient.Session {

    private static final int PULSE_INTERVAL_MILLIS = 5_000;

    private final ClientRPC client;
    private final DatabaseRPC database;
    private final ByteString sessionId;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;
    private final ConcurrentSet<TransactionRPC> transactions;
    private final Type type;
    private final GraknOptions options;
    private final Timer pulse;
    private final ReadWriteLock accessLock;
    private final AtomicBoolean isOpen;
    private final int networkLatencyMillis;

    public SessionRPC(ClientRPC client, String database, Type type, GraknOptions options) {
        try {
            client.reconnect();
            this.client = client;
            this.type = type;
            this.options = options;
            this.database = new DatabaseRPC(client.databases(), database);
            blockingGrpcStub = GraknGrpc.newBlockingStub(client.channel());
            Instant startTime = Instant.now();
            SessionProto.Session.Open.Res res = blockingGrpcStub.sessionOpen(openRequest(database, type, options));
            Instant endTime = Instant.now();
            networkLatencyMillis = (int) (Duration.between(startTime, endTime).toMillis() - res.getServerDurationMillis());
            sessionId = res.getSessionId();
            transactions = new ConcurrentSet<>();
            accessLock = new StampedLock().asReadWriteLock();
            isOpen = new AtomicBoolean(true);
            pulse = new Timer();
            pulse.scheduleAtFixedRate(this.new PulseTask(), 0, PULSE_INTERVAL_MILLIS);
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }

    private static SessionProto.Session.Open.Req openRequest(String database, Type type, GraknOptions options) {
        return SessionProto.Session.Open.Req.newBuilder().setDatabase(database)
                .setType(sessionType(type)).setOptions(ProtoBuilder.options(options)).build();
    }

    private static SessionProto.Session.Type sessionType(Type type) {
        switch (type) {
            case DATA:
                return SessionProto.Session.Type.DATA;
            case SCHEMA:
                return SessionProto.Session.Type.SCHEMA;
            default:
                throw new GraknClientException(ILLEGAL_ARGUMENT);
        }
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type) {
        return transaction(type, GraknOptions.core());
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type, GraknOptions options) {
        try {
            accessLock.readLock().lock();
            if (!isOpen.get()) throw new GraknClientException(SESSION_CLOSED);
            TransactionRPC transactionRPC = new TransactionRPC(this, sessionId, type, options, client.batcher().nextExecutor());
            transactions.add(transactionRPC);
            return transactionRPC;
        } finally {
            accessLock.readLock().unlock();
        }
    }

    @Override
    public DatabaseRPC database() { return database; }

    @Override
    public GraknOptions options() { return options; }

    @Override
    public Type type() { return type; }

    @Override
    public boolean isOpen() { return isOpen.get(); }

    void reconnect() { client.reconnect(); }

    ByteString id() { return sessionId; }

    Channel channel() { return client.channel(); }

    int networkLatencyMillis() { return networkLatencyMillis; }

    @Override
    public void close() {
        try {
            accessLock.writeLock().lock();
            if (isOpen.compareAndSet(true, false)) {
                transactions.forEach(TransactionRPC::close);
                client.removeSession(this);
                pulse.cancel();
                client.reconnect();
                try {
                    SessionProto.Session.Close.Res ignored = blockingGrpcStub.sessionClose(
                            SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build()
                    );
                } catch (StatusRuntimeException e) {
                    // Most likely the session is already closed or the server is no longer running.
                }
            }
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        } finally {
            accessLock.writeLock().unlock();
        }
    }

    private class PulseTask extends TimerTask {

        @Override
        public void run() {
            if (!isOpen()) return;
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
    }
}
