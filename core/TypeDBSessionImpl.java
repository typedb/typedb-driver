/*
 * Copyright (C) 2021 Vaticle
 *
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

package com.vaticle.typedb.client.core;

import com.google.protobuf.ByteString;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.client.stream.RequestTransmitter;
import com.vaticle.typedb.common.collection.ConcurrentSet;
import com.vaticle.typedb.protocol.SessionProto;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.SESSION_CLOSED;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Session.closeReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Session.openReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Session.pulseReq;

public class TypeDBSessionImpl implements TypeDBSession {

    private static final int PULSE_INTERVAL_MILLIS = 5_000;

    private final TypeDBClientImpl client;
    private final TypeDBDatabaseImpl database;
    private final ByteString sessionID;
    private final ConcurrentSet<TypeDBTransaction.Extended> transactions;
    private final Type type;
    private final TypeDBOptions options;
    private final Timer pulse;
    private final ReadWriteLock accessLock;
    private final AtomicBoolean isOpen;
    private final int networkLatencyMillis;

    public TypeDBSessionImpl(TypeDBClientImpl client, String database, Type type, TypeDBOptions options) {
        this.client = client;
        this.type = type;
        this.options = options;
        Instant startTime = Instant.now();
        SessionProto.Session.Open.Res res = client.stub().sessionOpen(
                openReq(database, type.proto(), options.proto())
        );
        Instant endTime = Instant.now();
        this.database = new TypeDBDatabaseImpl(client.databases(), database);
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
    public TypeDBDatabaseImpl database() { return database; }

    @Override
    public TypeDBOptions options() { return options; }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type) {
        return transaction(type, TypeDBOptions.core());
    }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type, TypeDBOptions options) {
        try {
            accessLock.readLock().lock();
            if (!isOpen.get()) throw new TypeDBClientException(SESSION_CLOSED);
            TypeDBTransaction.Extended transactionRPC = new TypeDBTransactionImpl(this, sessionID, type, options);
            transactions.add(transactionRPC);
            return transactionRPC;
        } finally {
            accessLock.readLock().unlock();
        }
    }

    ByteString id() { return sessionID; }

    TypeDBStub stub() {
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
                transactions.forEach(TypeDBTransaction.Extended::close);
                client.removeSession(this);
                pulse.cancel();
                try {
                    stub().sessionClose(closeReq(sessionID));
                } catch (TypeDBClientException e) {
                    // Most likely the session is already closed or the server is no longer running.
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
            } catch (TypeDBClientException exception) {
                alive = false;
            }
            if (!alive) {
                isOpen.set(false);
                pulse.cancel();
            }
        }
    }
}
