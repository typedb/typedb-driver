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
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.connection.TypeDBConnectionFactory;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;

public class ClusterServerConnectionFactory extends TypeDBConnectionFactory {

    private TypeDBCredential credential;

    public ClusterServerConnectionFactory(TypeDBCredential credential) {
        this.credential = credential;
    }

    @Override
    public ManagedChannel newManagedChannel(String address) {
        if (!credential.tlsEnabled()) {
            return plainTextChannel(address);
        } else {
            return TLSChannel(address);
        }
    }

    @Override
    public ClusterServerStub newTypeDBStub(ManagedChannel channel) {
        return ClusterServerStub.create(credential.username(), credential.password(), channel);
    }

    private ManagedChannel TLSChannel(String address) {
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
