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
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import grakn.client.common.exception.GraknClientException;
import grakn.client.common.rpc.GraknStub;
import grakn.client.stream.RequestTransmitter;
import grakn.common.collection.ConcurrentSet;
import grakn.protocol.SessionProto;
import io.grpc.StatusRuntimeException;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import static grakn.client.common.exception.ErrorMessage.Client.SESSION_CLOSED;
import static grakn.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static grakn.client.common.rpc.RequestBuilder.Session.closeReq;
import static grakn.client.common.rpc.RequestBuilder.Session.openReq;
import static grakn.client.common.rpc.RequestBuilder.Session.pulseReq;

public class CoreSession implements GraknSession {

    private static final int PULSE_INTERVAL_MILLIS = 5_000;

    private final CoreClient client;
    private final CoreDatabase database;
    private final ByteString sessionID;
    private final ConcurrentSet<GraknTransaction.Extended> transactions;
    private final Type type;
    private final GraknOptions options;
    private final Timer pulse;
    private final ReadWriteLock accessLock;
    private final AtomicBoolean isOpen;
    private final int networkLatencyMillis;

    public CoreSession(CoreClient client, String database, Type type, GraknOptions options) {
        this.client = client;
        this.type = type;
        this.options = options;
        Instant startTime = Instant.now();
        SessionProto.Session.Open.Res res = client.stub().sessionOpen(
                openReq(database, type.proto(), options.proto())
        );
        Instant endTime = Instant.now();
        this.database = new CoreDatabase(client.databases(), database);
        networkLatencyMillis = (int) (Duration.between(startTime, endTime).toMillis() - res.getServerDurationMillis());
        sessionID = res.getSessionId();
        transactions = new ConcurrentSet<>();
        accessLock = new StampedLock().asReadWriteLock();
        isOpen = new AtomicBoolean(true);
        pulse = new Timer();
        pulse.scheduleAtFixedRate(this.new PulseTask(), 0, PULSE_INTERVAL_MILLIS);
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
    public GraknTransaction transaction(GraknTransaction.Type type) {
        return transaction(type, GraknOptions.core());
    }

    @Override
    public GraknTransaction transaction(GraknTransaction.Type type, GraknOptions options) {
        try {
            accessLock.readLock().lock();
            if (!isOpen.get()) throw new GraknClientException(SESSION_CLOSED);
            GraknTransaction.Extended transactionRPC = new CoreTransaction(this, sessionID, type, options);
            transactions.add(transactionRPC);
            return transactionRPC;
        } finally {
            accessLock.readLock().unlock();
        }
    }

    ByteString id() { return sessionID; }

    GraknStub.Core stub() {
        return client.stub();
    }

    RequestTransmitter transmitter() {
        return client.transmitter();
    }

    int networkLatencyMillis() { return networkLatencyMillis; }

    @Override
    public void close() {
        try {
            accessLock.writeLock().lock();
            if (isOpen.compareAndSet(true, false)) {
                transactions.forEach(GraknTransaction.Extended::close);
                client.removeSession(this);
                pulse.cancel();
                try {
                    SessionProto.Session.Close.Res ignore = stub().sessionClose(closeReq(sessionID));
                } catch (GraknClientException e) {
                    if (e.getErrorMessage().equals(UNABLE_TO_CONNECT)) {
                        // Most likely the session is already closed or the server is no longer running.
                    } else {
                        throw e;
                    }
                }
            }
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
                alive = stub().sessionPulse(pulseReq(sessionID)).getAlive();
            }  catch (GraknClientException e) {
                if (e.getErrorMessage().equals(UNABLE_TO_CONNECT)) {
                    alive = false;
                } else {
                    throw e;
                }
            }
            if (!alive) {
                isOpen.set(false);
                pulse.cancel();
            }
        }
    }
}
