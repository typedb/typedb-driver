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

package com.vaticle.typedb.client.common.rpc;

import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.protocol.ClusterServerProto;
import com.vaticle.typedb.protocol.ClusterUserProto;
import com.vaticle.typedb.protocol.CoreDatabaseProto.CoreDatabase;
import com.vaticle.typedb.protocol.CoreDatabaseProto.CoreDatabaseManager;
import com.vaticle.typedb.protocol.SessionProto.Session;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typedb.protocol.TypeDBClusterGrpc;
import com.vaticle.typedb.protocol.TypeDBGrpc;
import io.grpc.CallCredentials;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.vaticle.typedb.protocol.ClusterDatabaseProto.ClusterDatabaseManager;
import static com.vaticle.typedb.protocol.ClusterUserProto.ClusterUserManager;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public abstract class TypeDBStub {

    private final ManagedChannel channel;

    private TypeDBStub(ManagedChannel channel) {
        this.channel = channel;
    }

    public static Core core(ManagedChannel channel) {
        return new Core(channel, TypeDBGrpc.newBlockingStub(channel), TypeDBGrpc.newStub(channel));
    }

    public static ClusterNode clusterNode(String username, String password, ManagedChannel channel) {
        Metadata.Key<String> USERNAME = Metadata.Key.of("username", ASCII_STRING_MARSHALLER);
        Metadata.Key<String> PASSWORD = Metadata.Key.of("password", ASCII_STRING_MARSHALLER);
        CallCredentials callCredentials = new CallCredentials() {

            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                appExecutor.execute(() -> {
                    Metadata headers = new Metadata();
                    headers.put(USERNAME, username);
                    headers.put(PASSWORD, password);
                    System.out.println("GRAKN CLIENT - USERNAME: " + username + ", PASSWORD: " + password);
                    applier.apply(headers);
                });
            }

            @Override
            public void thisUsesUnstableApi() { }
        };

        return new ClusterNode(
                channel,
                TypeDBGrpc.newBlockingStub(channel).withCallCredentials(callCredentials),
                TypeDBGrpc.newStub(channel).withCallCredentials(callCredentials),
                TypeDBClusterGrpc.newBlockingStub(channel).withCallCredentials(callCredentials)
        );
    }

    private void ensureConnected() {
        // The Channel is a persistent HTTP connection. If it gets interrupted (say, by the server going down) then
        // gRPC's recovery logic will kick in, marking the Channel as being in a transient failure state and rejecting
        // all RPC calls while in this state. It will attempt to reconnect periodically in the background, using an
        // exponential backoff algorithm. Here, we ensure that when the user needs that connection urgently (e.g: to
        // open a TypeDB session), it tries to reconnect immediately instead of just failing without trying.
        if (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
            channel.resetConnectBackoff();
        }
    }

    protected <RES> RES resilientCall(Supplier<RES> function) {
        try {
            ensureConnected();
            return function.get();
        } catch (StatusRuntimeException e) {
            throw TypeDBClientException.of(e);
        }
    }

    public static class Core extends TypeDBStub {

        private final TypeDBGrpc.TypeDBBlockingStub blockingStub;
        private final TypeDBGrpc.TypeDBStub asyncStub;

        private Core(ManagedChannel channel, TypeDBGrpc.TypeDBBlockingStub blockingStub, TypeDBGrpc.TypeDBStub asyncStub) {
            super(channel);
            this.blockingStub = blockingStub;
            this.asyncStub = asyncStub;
        }

        public CoreDatabaseManager.Contains.Res databasesContains(CoreDatabaseManager.Contains.Req request) {
            return resilientCall(() -> blockingStub.databasesContains(request));
        }

        public CoreDatabaseManager.Create.Res databasesCreate(CoreDatabaseManager.Create.Req request) {
            return resilientCall(() -> blockingStub.databasesCreate(request));
        }

        public CoreDatabaseManager.All.Res databasesAll(CoreDatabaseManager.All.Req request) {
            return resilientCall(() -> blockingStub.databasesAll(request));
        }

        public CoreDatabase.Schema.Res databaseSchema(CoreDatabase.Schema.Req request) {
            return resilientCall(() -> blockingStub.databaseSchema(request));
        }

        public CoreDatabase.Delete.Res databaseDelete(CoreDatabase.Delete.Req request) {
            return resilientCall(() -> blockingStub.databaseDelete(request));
        }

        public Session.Open.Res sessionOpen(Session.Open.Req request) {
            return resilientCall(() -> blockingStub.sessionOpen(request));
        }

        public Session.Close.Res sessionClose(Session.Close.Req request) {
            return resilientCall(() -> blockingStub.sessionClose(request));
        }

        public Session.Pulse.Res sessionPulse(Session.Pulse.Req request) {
            return resilientCall(() -> blockingStub.sessionPulse(request));
        }

        public StreamObserver<TransactionProto.Transaction.Client> transaction(StreamObserver<TransactionProto.Transaction.Server> responseObserver) {
            return resilientCall(() -> asyncStub.transaction(responseObserver));
        }
    }

    public static class ClusterNode extends TypeDBStub.Core {

        private final TypeDBClusterGrpc.TypeDBClusterBlockingStub blockingStub;

        private ClusterNode(ManagedChannel channel, TypeDBGrpc.TypeDBBlockingStub blockingStub,
                            TypeDBGrpc.TypeDBStub asyncStub,
                            TypeDBClusterGrpc.TypeDBClusterBlockingStub clusterBlockingStub) {
            super(channel, blockingStub, asyncStub);
            this.blockingStub = clusterBlockingStub;
        }

        public ClusterServerProto.ServerManager.All.Res serversAll(ClusterServerProto.ServerManager.All.Req request) {
            return resilientCall(() -> blockingStub.serversAll(request));
        }

        public ClusterUserManager.Contains.Res usersContains(ClusterUserManager.Contains.Req request) {
            return resilientCall(() -> blockingStub.usersContains(request));
        }

        public ClusterUserProto.ClusterUserManager.Create.Res usersCreate(ClusterUserManager.Create.Req request) {
            return resilientCall(() -> blockingStub.usersCreate(request));
        }

        public ClusterUserManager.All.Res usersAll(ClusterUserManager.All.Req request) {
            return resilientCall(() -> blockingStub.usersAll(request));
        }

        public ClusterUserProto.ClusterUser.Delete.Res userDelete(ClusterUserProto.ClusterUser.Delete.Req request) {
            return resilientCall(() -> blockingStub.usersDelete(request));
        }

        public ClusterDatabaseManager.Get.Res databasesGet(ClusterDatabaseManager.Get.Req request) {
            return resilientCall(() -> blockingStub.databasesGet(request));
        }

        public ClusterDatabaseManager.All.Res databasesAll(ClusterDatabaseManager.All.Req request) {
            return resilientCall(() -> blockingStub.databasesAll(request));
        }
    }
}
