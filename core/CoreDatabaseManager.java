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

package grakn.client.core;

import grakn.client.api.database.Database;
import grakn.client.api.database.DatabaseManager;
import grakn.client.common.GraknClientException;
import grakn.client.common.Proto;
import grakn.protocol.GraknGrpc;

import java.util.List;
import java.util.stream.Collectors;

import static grakn.client.common.ErrorMessage.Client.DB_DOES_NOT_EXIST;
import static grakn.client.common.ErrorMessage.Client.MISSING_DB_NAME;

public class CoreDatabaseManager implements DatabaseManager {
    private final CoreClient client;
    private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

    public CoreDatabaseManager(CoreClient client) {
        this.client = client;
        blockingGrpcStub = GraknGrpc.newBlockingStub(client.channel());
    }

    @Override
    public boolean contains(String name) {
        return client.call(() -> blockingGrpcStub.databaseContains(Proto.Database.contains(nonNull(name))).getContains());
    }

    @Override
    public void create(String name) {
        client.call(() -> blockingGrpcStub.databaseCreate(Proto.Database.create(nonNull(name))));
    }

    @Override
    public Database get(String name) {
        if (contains(name)) return new CoreDatabase(this, name);
        else throw new GraknClientException(DB_DOES_NOT_EXIST.message(name));
    }

    @Override
    public List<CoreDatabase> all() {
        List<String> databases = client.call(() -> blockingGrpcStub.databaseAll(Proto.Database.all()).getNamesList());
        return databases.stream().map(name -> new CoreDatabase(this, name)).collect(Collectors.toList());
    }

    public CoreClient client() {
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
