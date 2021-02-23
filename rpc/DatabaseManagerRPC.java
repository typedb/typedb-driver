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
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.DatabaseProto;
import grakn.protocol.GraknGrpc;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.DB_DOES_NOT_EXIST;
import static grakn.client.common.exception.ErrorMessage.Client.MISSING_DB_NAME;
import static grakn.client.rpc.util.RPCUtils.rpcCall;

public class DatabaseManagerRPC implements GraknClient.DatabaseManager {
    private final ClientRPC client;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public DatabaseManagerRPC(ClientRPC client) {
        this.client = client;
        blockingGrpcStub = GraknGrpc.newBlockingStub(client.channel());
    }

    @Override
    public boolean contains(String name) {
        return rpcCall(client, () -> blockingGrpcStub.databaseContains(DatabaseProto.Database.Contains.Req.newBuilder().setName(nonNull(name)).build()).getContains());
    }

    @Override
    public void create(String name) {
        rpcCall(client, () -> blockingGrpcStub.databaseCreate(DatabaseProto.Database.Create.Req.newBuilder().setName(nonNull(name)).build()));
    }

    @Override
    public GraknClient.Database get(String name) {
        if (contains(name)) return new DatabaseRPC(this, name);
        else throw new GraknClientException(DB_DOES_NOT_EXIST.message(name));
    }

    @Override
    public List<DatabaseRPC> all() {
        List<String> databases = rpcCall(client, () -> blockingGrpcStub.databaseAll(DatabaseProto.Database.All.Req.getDefaultInstance()).getNamesList());
        return databases.stream().map(name -> new DatabaseRPC(this, name)).collect(Collectors.toList());
    }

    public ClientRPC client() {
        return client;
    }

    public GraknGrpc.GraknBlockingStub blockingGrpcStub() {
        return blockingGrpcStub;
    }

    static String nonNull(String name) {
        if (name == null) throw new GraknClientException(MISSING_DB_NAME);
        return name;
    }
}
