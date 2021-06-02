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

import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.protocol.ClusterDatabaseProto;
import com.vaticle.typedb.protocol.ClusterServerProto;
import com.vaticle.typedb.protocol.ClusterUserProto;
import com.vaticle.typedb.protocol.TypeDBClusterGrpc;
import com.vaticle.typedb.protocol.TypeDBGrpc;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class ClusterServerStub extends TypeDBStub {

    private final TypeDBClusterGrpc.TypeDBClusterBlockingStub clusterBlockingStub;

    private ClusterServerStub(ManagedChannel channel, TypeDBGrpc.TypeDBBlockingStub blockingStub, TypeDBGrpc.TypeDBStub asyncStub,
                              TypeDBClusterGrpc.TypeDBClusterBlockingStub clusterBlockingStub) {
        super(channel, blockingStub, asyncStub);
        this.clusterBlockingStub = clusterBlockingStub;
    }

    public static ClusterServerStub create(String username, String password, ManagedChannel channel) {
        CredentialEmbedder credentialEmbedder = new CredentialEmbedder(username, password);
        return new ClusterServerStub(channel,
                TypeDBGrpc.newBlockingStub(channel).withCallCredentials(credentialEmbedder),
                TypeDBGrpc.newStub(channel).withCallCredentials(credentialEmbedder),
                TypeDBClusterGrpc.newBlockingStub(channel).withCallCredentials(credentialEmbedder));
    }


    public ClusterServerProto.ServerManager.All.Res serversAll(ClusterServerProto.ServerManager.All.Req request) {
        return resilientCall(() -> clusterBlockingStub.serversAll(request));
    }

    public ClusterUserProto.ClusterUserManager.Contains.Res usersContains(ClusterUserProto.ClusterUserManager.Contains.Req request) {
        return resilientCall(() -> clusterBlockingStub.usersContains(request));
    }

    public ClusterUserProto.ClusterUserManager.Create.Res usersCreate(ClusterUserProto.ClusterUserManager.Create.Req request) {
        return resilientCall(() -> clusterBlockingStub.usersCreate(request));
    }

    public ClusterUserProto.ClusterUserManager.All.Res usersAll(ClusterUserProto.ClusterUserManager.All.Req request) {
        return resilientCall(() -> clusterBlockingStub.usersAll(request));
    }

    public ClusterUserProto.ClusterUser.Delete.Res userDelete(ClusterUserProto.ClusterUser.Delete.Req request) {
        return resilientCall(() -> clusterBlockingStub.userDelete(request));
    }

    public ClusterDatabaseProto.ClusterDatabaseManager.Get.Res databasesGet(ClusterDatabaseProto.ClusterDatabaseManager.Get.Req request) {
        return resilientCall(() -> clusterBlockingStub.databasesGet(request));
    }

    public ClusterDatabaseProto.ClusterDatabaseManager.All.Res databasesAll(ClusterDatabaseProto.ClusterDatabaseManager.All.Req request) {
        return resilientCall(() -> clusterBlockingStub.databasesAll(request));
    }

    public static class CredentialEmbedder extends CallCredentials {
        private static final Metadata.Key<String> USERNAME_FIELD = Metadata.Key.of("username", ASCII_STRING_MARSHALLER);
        private static final Metadata.Key<String> PASSWORD_FIELD = Metadata.Key.of("password", ASCII_STRING_MARSHALLER);

        private final String username;
        private final String password;

        public CredentialEmbedder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
            appExecutor.execute(() -> {
                Metadata headers = new Metadata();
                headers.put(USERNAME_FIELD, username);
                headers.put(PASSWORD_FIELD, password);
                applier.apply(headers);
            });
        }

        @Override
        public void thisUsesUnstableApi() { }
    }

}
