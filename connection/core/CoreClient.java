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

package com.vaticle.typedb.client.connection.core;

import com.google.protobuf.ByteString;
import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.client.connection.TypeDBClientImpl;
import com.vaticle.typedb.protocol.ClientProto;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import javax.annotation.Nullable;

import java.util.Timer;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Client.openReq;

public class CoreClient extends TypeDBClientImpl {

    private final ByteString clientID;
    private final ManagedChannel channel;
    private final TypeDBStub stub;

    public CoreClient(String address) {
        this(address, calculateParallelisation());
    }

    public CoreClient(String address, int parallelisation) {
        this(address, null, parallelisation);
    }

    public CoreClient(String address, @Nullable Integer idleTimeoutMillis, int parallelisation) {
        super(idleTimeoutMillis, parallelisation);
        channel = NettyChannelBuilder.forTarget(address).usePlaintext().build();
        stub = CoreStub.create(channel);
        ClientProto.Client.Open.Res res = stub().clientOpen(openReq(idleTimeoutMillis));
        clientID = res.getClientId();
        pulseActivate();
    }

    @Override
    public ByteString ID() {
        return clientID;
    }

    @Override
    public ManagedChannel channel() {
        return channel;
    }

    @Override
    public TypeDBStub stub() {
        return stub;
    }
}
