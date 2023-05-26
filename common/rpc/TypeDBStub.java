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

package com.vaticle.typedb.client.common.rpc;

import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.protocol.CoreDatabaseProto;
import com.vaticle.typedb.protocol.SessionProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typedb.protocol.TypeDBGrpc;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.function.Supplier;

public abstract class TypeDBStub {

    ConnectionProto

    public CoreDatabaseProto.CoreDatabaseManager.Contains.Res databasesContains(CoreDatabaseProto.CoreDatabaseManager.Contains.Req request) {
        return resilientCall(() -> blockingStub().databasesContains(request));
    }

    public CoreDatabaseProto.CoreDatabaseManager.Create.Res databasesCreate(CoreDatabaseProto.CoreDatabaseManager.Create.Req request) {
        return resilientCall(() -> blockingStub().databasesCreate(request));
    }

    public CoreDatabaseProto.CoreDatabaseManager.All.Res databasesAll(CoreDatabaseProto.CoreDatabaseManager.All.Req request) {
        return resilientCall(() -> blockingStub().databasesAll(request));
    }

    public CoreDatabaseProto.CoreDatabase.Schema.Res databaseSchema(CoreDatabaseProto.CoreDatabase.Schema.Req request) {
        return resilientCall(() -> blockingStub().databaseSchema(request));
    }

    public CoreDatabaseProto.CoreDatabase.TypeSchema.Res databaseTypeSchema(CoreDatabaseProto.CoreDatabase.TypeSchema.Req request) {
        return resilientCall(() -> blockingStub().databaseTypeSchema(request));
    }

    public CoreDatabaseProto.CoreDatabase.RuleSchema.Res databaseRuleSchema(CoreDatabaseProto.CoreDatabase.RuleSchema.Req request) {
        return resilientCall(() -> blockingStub().databaseRuleSchema(request));
    }

    public CoreDatabaseProto.CoreDatabase.Delete.Res databaseDelete(CoreDatabaseProto.CoreDatabase.Delete.Req request) {
        return resilientCall(() -> blockingStub().databaseDelete(request));
    }

    public SessionProto.Session.Open.Res sessionOpen(SessionProto.Session.Open.Req request) {
        return resilientCall(() -> blockingStub().sessionOpen(request));
    }

    public SessionProto.Session.Close.Res sessionClose(SessionProto.Session.Close.Req request) {
        return resilientCall(() -> blockingStub().sessionClose(request));
    }

    public SessionProto.Session.Pulse.Res sessionPulse(SessionProto.Session.Pulse.Req request) {
        return resilientCall(() -> blockingStub().sessionPulse(request));
    }

    public StreamObserver<TransactionProto.Transaction.Client> transaction(StreamObserver<TransactionProto.Transaction.Server> responseObserver) {
        return resilientCall(() -> asyncStub().transaction(responseObserver));
    }

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
