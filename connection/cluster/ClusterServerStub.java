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

package com.vaticle.typedb.client.connection.cluster;

import com.vaticle.typedb.client.api.connection.TypeDBCredential;
import com.vaticle.typedb.client.common.exception.ErrorMessage;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.protocol.ClusterDatabaseProto;
import com.vaticle.typedb.protocol.ClusterServerProto;
import com.vaticle.typedb.protocol.ClusterUserProto;
import com.vaticle.typedb.protocol.ClusterUserTokenProto;
import com.vaticle.typedb.protocol.CoreDatabaseProto;
import com.vaticle.typedb.protocol.SessionProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typedb.protocol.TypeDBClusterGrpc;
import com.vaticle.typedb.protocol.TypeDBGrpc;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserToken.renewReq;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class ClusterServerStub extends TypeDBStub {

    private final TypeDBCredential credential;
    private String token;
    private final ManagedChannel channel;
    private final TypeDBGrpc.TypeDBBlockingStub blockingStub;
    private final TypeDBGrpc.TypeDBStub asyncStub;
    private final TypeDBClusterGrpc.TypeDBClusterBlockingStub clusterBlockingStub;

    ClusterServerStub(ManagedChannel channel, TypeDBCredential credential) {
        super();
        this.credential = credential;
        this.channel = channel;
        this.blockingStub = TypeDBGrpc.newBlockingStub(channel).withCallCredentials(createCallCredentials());
        this.asyncStub = TypeDBGrpc.newStub(channel).withCallCredentials(createCallCredentials());
        this.clusterBlockingStub = TypeDBClusterGrpc.newBlockingStub(channel).withCallCredentials(createCallCredentials());
        try {
            ClusterUserTokenProto.ClusterUserToken.Renew.Res res = clusterBlockingStub.userTokenRenew(renewReq(this.credential.username()));
            token = res.getToken();
        } catch (StatusRuntimeException e) {
            // ignore UNAVAILABLE exception
            if (e.getStatus().getCode() != Status.Code.UNAVAILABLE) {
                throw e;
            }
        }
    }

    private CallCredentials createCallCredentials() {
        return new CallCredentials() {
            private final Metadata.Key<String> TOKEN_FIELD = Metadata.Key.of("token", ASCII_STRING_MARSHALLER);
            private final Metadata.Key<String> USERNAME_FIELD = Metadata.Key.of("username", ASCII_STRING_MARSHALLER);
            private final Metadata.Key<String> PASSWORD_FIELD = Metadata.Key.of("password", ASCII_STRING_MARSHALLER);

            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                appExecutor.execute(() -> {
                    Metadata headers = new Metadata();
                    headers.put(USERNAME_FIELD, credential.username());
                    if (token != null) {
                        headers.put(TOKEN_FIELD, token);
                    } else {
                        headers.put(PASSWORD_FIELD, credential.password());
                    }
                    applier.apply(headers);
                });
            }

            @Override
            public void thisUsesUnstableApi() { }
        };
    }

    public ClusterServerProto.ServerManager.All.Res serversAll(ClusterServerProto.ServerManager.All.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.serversAll(request));
    }

    public ClusterUserProto.ClusterUserManager.Contains.Res usersContains(ClusterUserProto.ClusterUserManager.Contains.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.usersContains(request));
    }

    public ClusterUserProto.ClusterUserManager.Create.Res usersCreate(ClusterUserProto.ClusterUserManager.Create.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.usersCreate(request));
    }

    public ClusterUserProto.ClusterUserManager.All.Res usersAll(ClusterUserProto.ClusterUserManager.All.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.usersAll(request));
    }

    public ClusterUserProto.ClusterUser.Password.Res userPassword(ClusterUserProto.ClusterUser.Password.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.userPassword(request));
    }

    public ClusterUserProto.ClusterUser.Delete.Res userDelete(ClusterUserProto.ClusterUser.Delete.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.userDelete(request));
    }

    public ClusterDatabaseProto.ClusterDatabaseManager.Get.Res databasesGet(ClusterDatabaseProto.ClusterDatabaseManager.Get.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.databasesGet(request));
    }

    public ClusterDatabaseProto.ClusterDatabaseManager.All.Res databasesAll(ClusterDatabaseProto.ClusterDatabaseManager.All.Req request) {
        return mayRenewToken(() -> clusterBlockingStub.databasesAll(request));
    }

    @Override
    public CoreDatabaseProto.CoreDatabaseManager.Contains.Res databasesContains(CoreDatabaseProto.CoreDatabaseManager.Contains.Req request) {
        return mayRenewToken(() -> blockingStub().databasesContains(request));
    }

    @Override
    public CoreDatabaseProto.CoreDatabaseManager.Create.Res databasesCreate(CoreDatabaseProto.CoreDatabaseManager.Create.Req request) {
        return mayRenewToken(() -> blockingStub().databasesCreate(request));
    }

    @Override
    public CoreDatabaseProto.CoreDatabaseManager.All.Res databasesAll(CoreDatabaseProto.CoreDatabaseManager.All.Req request) {
        return mayRenewToken(() -> blockingStub().databasesAll(request));
    }

    @Override
    public CoreDatabaseProto.CoreDatabase.Schema.Res databaseSchema(CoreDatabaseProto.CoreDatabase.Schema.Req request) {
        return mayRenewToken(() -> blockingStub().databaseSchema(request));
    }

    @Override
    public CoreDatabaseProto.CoreDatabase.Delete.Res databaseDelete(CoreDatabaseProto.CoreDatabase.Delete.Req request) {
        return mayRenewToken(() -> blockingStub().databaseDelete(request));
    }

    @Override
    public SessionProto.Session.Open.Res sessionOpen(SessionProto.Session.Open.Req request) {
        return mayRenewToken(() -> blockingStub().sessionOpen(request));
    }

    @Override
    public SessionProto.Session.Close.Res sessionClose(SessionProto.Session.Close.Req request) {
        return mayRenewToken(() -> blockingStub().sessionClose(request));
    }

    @Override
    public SessionProto.Session.Pulse.Res sessionPulse(SessionProto.Session.Pulse.Req request) {
        return mayRenewToken(() -> blockingStub().sessionPulse(request));
    }

    @Override
    public StreamObserver<TransactionProto.Transaction.Client> transaction(StreamObserver<TransactionProto.Transaction.Server> responseObserver) {
        return mayRenewToken(() -> asyncStub().transaction(responseObserver));
    }

    @Override
    protected ManagedChannel channel() {
        return channel;
    }

    @Override
    protected TypeDBGrpc.TypeDBBlockingStub blockingStub() {
        return blockingStub;
    }

    @Override
    protected TypeDBGrpc.TypeDBStub asyncStub() {
        return asyncStub;
    }

    private <RES> RES mayRenewToken(Supplier<RES> function) {
        try {
            return resilientCall(function);
        } catch (TypeDBClientException e) {
            if (e.getErrorMessage() != null && e.getErrorMessage().equals(ErrorMessage.Client.CLUSTER_TOKEN_CREDENTIAL_INVALID)) {
                token = null;
                ClusterUserTokenProto.ClusterUserToken.Renew.Res res = clusterBlockingStub.userTokenRenew(renewReq(credential.username()));
                token = res.getToken();
                try {
                    return resilientCall(function);
                } catch (StatusRuntimeException e2) {
                    throw TypeDBClientException.of(e2);
                }
            } else throw e;
        }
    }
}
