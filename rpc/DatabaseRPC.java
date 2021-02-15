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

import grakn.client.GraknClient;
import grakn.protocol.DatabaseProto;
import grakn.protocol.GraknGrpc;

import static grakn.client.rpc.DatabaseManagerRPC.nonNull;
import static grakn.client.rpc.util.RPCUtils.rpcCall;

public class DatabaseRPC implements GraknClient.Database {

    private final String name;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public DatabaseRPC(DatabaseManagerRPC databaseManager, String name) {
        this.name = name;
        this.blockingGrpcStub = databaseManager.blockingGrpcStub();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void delete() {
        rpcCall(() -> blockingGrpcStub.databaseDelete(DatabaseProto.Database.Delete.Req.newBuilder().setName(nonNull(name)).build()));
    }

    @Override
    public String toString() {
        return name;
    }
}
