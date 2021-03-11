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
import grakn.client.rpc.common.TransactionRequestBatcher;
import grakn.common.concurrent.NamedThreadFactory;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static grakn.common.util.Objects.className;

public class ClientRPC implements GraknClient {

    private static final String GRAKN_CLIENT_RPC_THREAD_NAME = "grakn-client-rpc";

    private final ManagedChannel channel;
    private final TransactionRequestBatcher batcher;
    private final DatabaseManagerRPC databases;
    private final ConcurrentMap<ByteString, SessionRPC> sessions;

    public ClientRPC(String address) {
        this(address, calculateParallelisation());
    }

    public ClientRPC(String address, int parallelisation) {
        NamedThreadFactory tf = NamedThreadFactory.create(GRAKN_CLIENT_RPC_THREAD_NAME);
        channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        batcher = new TransactionRequestBatcher(parallelisation, tf);
        databases = new DatabaseManagerRPC(this);
        sessions = new ConcurrentHashMap<>();
    }

    public static int calculateParallelisation() {
        int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 4) return 2;
        else if (cores <= 9) return 3;
        else if (cores <= 16) return 4;
        else return (int) Math.ceil(cores / 4.0);
    }

    @Override
    public SessionRPC session(String database, GraknClient.Session.Type type) {
        return session(database, type, GraknOptions.core());
    }

    @Override
    public SessionRPC session(String database, GraknClient.Session.Type type, GraknOptions options) {
        SessionRPC session = new SessionRPC(this, database, type, options);
        assert !sessions.containsKey(session.id());
        sessions.put(session.id(), session);
        return session;
    }

    @Override
    public DatabaseManagerRPC databases() {
        return databases;
    }

    @Override
    public boolean isOpen() {
        return !channel.isShutdown();
    }

    @Override
    public void close() {
        try {
            sessions.values().forEach(SessionRPC::close);
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            batcher.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    @Override
    public Cluster asCluster() {
        throw new GraknClientException(ILLEGAL_CAST.message(className(GraknClient.class), className(GraknClient.Cluster.class)));
    }

    public <RES> RES call(Supplier<RES> req) {
        try {
            reconnect();
            return req.get();
        } catch (StatusRuntimeException e) {
            throw GraknClientException.of(e);
        }
    }

    public ManagedChannel channel() {
        return channel;
    }

    void reconnect() {
        // The Channel is a persistent HTTP connection. If it gets interrupted (say, by the server going down) then
        // gRPC's recovery logic will kick in, marking the Channel as being in a transient failure state and rejecting
        // all RPC calls while in this state. It will attempt to reconnect periodically in the background, using an
        // exponential backoff algorithm. Here, we ensure that when the user needs that connection urgently (e.g: to
        // open a Grakn session), it tries to reconnect immediately instead of just failing without trying.
        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
            channel.resetConnectBackoff();
        }
    }

    TransactionRequestBatcher batcher() {
        return batcher;
    }

    void removeSession(SessionRPC session) {
        sessions.remove(session.id());
    }
}
