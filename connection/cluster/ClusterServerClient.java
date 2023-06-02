/*
 * Copyright (C) 2022 Vaticle
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

import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.connection.TypeDBClientImpl;
import com.vaticle.typedb.protocol.ClusterServerProto;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.Set;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLIENT_NOT_OPEN;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.ServerManager.allReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Connection.openReq;
import static java.util.stream.Collectors.toSet;

class ClusterServerClient extends TypeDBClientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterClient.class);

    private final String address;
    private final ManagedChannel channel;
    private final ClusterServerStub stub;

    ClusterServerClient(String address, TypeDBCredential credential, int parallelisation) {
        super(parallelisation);
        this.address = address;
        channel = createManagedChannel(address, credential);
        stub = new ClusterServerStub(channel, credential);
    }

    protected void validateConnection() {
        stub.connectionOpen(openReq());
        connectionValidated = true;
    }

    private ManagedChannel createManagedChannel(String address, TypeDBCredential credential) {
        if (!credential.tlsEnabled()) {
            return NettyChannelBuilder.forTarget(address)
                    .usePlaintext()
                    .build();
        } else {
            try {
                SslContext sslContext;
                if (credential.tlsRootCA().isPresent()) {
                    sslContext = GrpcSslContexts.forClient().trustManager(credential.tlsRootCA().get().toFile()).build();
                } else {
                    sslContext = GrpcSslContexts.forClient().build();
                }
                return NettyChannelBuilder.forTarget(address).useTransportSecurity().sslContext(sslContext).build();
            } catch (SSLException e) {
                throw new TypeDBClientException(e.getMessage(), e);
            }
        }
    }

    @Override
    public ManagedChannel channel() {
        return channel;
    }

    @Override
    public ClusterServerStub stub() {
        return stub;
    }

    public Set<String> servers() {
        if (!isOpen()) throw new TypeDBClientException(CLIENT_NOT_OPEN);
        LOG.debug("Fetching list of all servers from server {}...", address);
        ClusterServerProto.ServerManager.All.Res res = stub.serversAll(allReq());
        Set<String> addresses = res.getServersList().stream().map(ClusterServerProto.Server::getAddress).collect(toSet());
        LOG.debug("The list of all servers fetched: {}", addresses);
        return addresses;
    }
}
