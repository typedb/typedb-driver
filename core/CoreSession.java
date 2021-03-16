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

package grakn.client.core;

import com.google.protobuf.ByteString;
import grakn.client.api.GraknOptions;
import grakn.client.api.Session;
import grakn.client.api.Transaction;
import grakn.client.common.GraknClientException;
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

import static grakn.client.common.ErrorMessage.Client.SESSION_CLOSED;
import static grakn.client.common.RequestBuilder.Session.closeReq;
import static grakn.client.common.RequestBuilder.Session.openReq;
import static grakn.client.common.RequestBuilder.Session.pulseReq;

public class CoreSession implements Session {

    private static final int PULSE_INTERVAL_MILLIS = 5_000;

    private final CoreClient client;
    private final CoreDatabase database;
    private final ByteString sessionID;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;
    private final ConcurrentSet<Transaction.Extended> transactions;
    private final Type type;
    private final GraknOptions options;
    private final Timer pulse;
    private final ReadWriteLock accessLock;
    private final AtomicBoolean isOpen;
    private final int networkLatencyMillis;

    public CoreSession(CoreClient client, String database, Type type, GraknOptions options) {
        try {
            client.reconnect();
            this.client = client;
            this.type = type;
            this.options = options;
            this.database = new CoreDatabase(client.databases(), database);
            blockingGrpcStub = GraknGrpc.newBlockingStub(client.channel());
            Instant startTime = Instant.now();
            SessionProto.Session.Open.Res res = blockingGrpcStub.sessionOpen(
                    openReq(database, type.proto(), options.proto())
            );
            Instant endTime = Instant.now();
            networkLatencyMillis = (int) (Duration.between(startTime, endTime).toMillis() - res.getServerDurationMillis());
            sessionID = res.getSessionId();
            transactions = new ConcurrentSet<>();
            accessLock = new StampedLock().asReadWriteLock();
            isOpen = new AtomicBoolean(true);
            pulse = new Timer();
            pulse.scheduleAtFixedRate(this.new PulseTask(), 0, PULSE_INTERVAL_MILLIS);
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }

    @Override
    public boolean isOpen() { return isOpen.get(); }

    @Override
    public Type type() { return type; }

    @Override
    public CoreDatabase database() { return database; }

    @Override
    public GraknOptions options() { return options; }

    @Override
    public Transaction transaction(Transaction.Type type) {
        return transaction(type, GraknOptions.core());
    }

    @Override
    public Transaction transaction(Transaction.Type type, GraknOptions options) {
        try {
            accessLock.readLock().lock();
            if (!isOpen.get()) throw new GraknClientException(SESSION_CLOSED);
            Transaction.Extended transactionRPC = new CoreTransaction(this, sessionID, type, options, client.transmitter());
            transactions.add(transactionRPC);
            return transactionRPC;
        } finally {
            accessLock.readLock().unlock();
        }
    }

    ByteString id() { return sessionID; }

    Channel channel() { return client.channel(); }

    int networkLatencyMillis() { return networkLatencyMillis; }

    void reconnect() { client.reconnect(); }

    @Override
    public void close() {
        try {
            accessLock.writeLock().lock();
            if (isOpen.compareAndSet(true, false)) {
                transactions.forEach(Transaction.Extended::close);
                client.removeSession(this);
                pulse.cancel();
                client.reconnect();
                try {
                    SessionProto.Session.Close.Res ignore = blockingGrpcStub.sessionClose(closeReq(sessionID));
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
                alive = blockingGrpcStub.sessionPulse(pulseReq(sessionID)).getAlive();
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
