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
import com.vaticle.typedb.protocol.CoreDatabaseProto.CoreDatabase;
import com.vaticle.typedb.protocol.CoreDatabaseProto.CoreDatabaseManager;
import com.vaticle.typedb.protocol.SessionProto.Session;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typedb.protocol.TypeDBGrpc;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.function.Supplier;

public abstract class TypeDBStub {

    abstract public CoreDatabaseManager.Contains.Res databasesContains(CoreDatabaseManager.Contains.Req request);

    abstract public CoreDatabaseManager.Create.Res databasesCreate(CoreDatabaseManager.Create.Req request);

    abstract public CoreDatabaseManager.All.Res databasesAll(CoreDatabaseManager.All.Req request);

    abstract public CoreDatabase.Schema.Res databaseSchema(CoreDatabase.Schema.Req request);

    abstract public CoreDatabase.Delete.Res databaseDelete(CoreDatabase.Delete.Req request);

    abstract public Session.Open.Res sessionOpen(Session.Open.Req request);

    abstract public Session.Close.Res sessionClose(Session.Close.Req request);

    abstract public Session.Pulse.Res sessionPulse(Session.Pulse.Req request);

    abstract public StreamObserver<TransactionProto.Transaction.Client> transaction(StreamObserver<TransactionProto.Transaction.Server> responseObserver);

    protected abstract ManagedChannel channel();

    protected abstract TypeDBGrpc.TypeDBBlockingStub blockingStub();

    protected abstract TypeDBGrpc.TypeDBStub asyncStub();

    protected <RES> RES resilientCall(Supplier<RES> function) {
        try {
            ensureConnected();
            return function.get();
        } catch (StatusRuntimeException e) {
            throw TypeDBClientException.of(e);
        }
    }

    private void ensureConnected() {
        // The Channel is a persistent HTTP connection. If it gets interrupted (say, by the server going down) then
        // gRPC's recovery logic will kick in, marking the Channel as being in a transient failure state and rejecting
        // all RPC calls while in this state. It will attempt to reconnect periodically in the background, using an
        // exponential backoff algorithm. Here, we ensure that when the user needs that connection urgently (e.g: to
        // open a TypeDB session), it tries to reconnect immediately instead of just failing without trying.
        if (channel().getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)) {
            channel().resetConnectBackoff();
        }
    }
}
