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

package grakn.client.rpc.cluster;

import grakn.client.GraknClient;
import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.DatabaseManagerRPC;
import grakn.client.rpc.DatabaseRPC;
import grakn.protocol.cluster.DatabaseProto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;

public class DatabaseManagerClusterRPC implements GraknClient.DatabaseManager.Cluster {
    private final Map<ServerAddress, DatabaseManagerRPC> databaseManagers;
    private final ClientClusterRPC client;

    public DatabaseManagerClusterRPC(ClientClusterRPC client, Map<ServerAddress, DatabaseManagerRPC> databaseManagers) {
        this.client = client;
        this.databaseManagers = databaseManagers;
    }

    @Override
    public boolean contains(String name) {
        List<GraknClientException> errors = new ArrayList<>();
        for (DatabaseManagerRPC databaseManager : databaseManagers.values()) {
            try {
                return databaseManager.contains(name);
            } catch (GraknClientException e) {
                errors.add(e);
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED.message(errors.toString()));
    }

    @Override
    public void create(String name) {
        for (DatabaseManagerRPC databaseManager : databaseManagers.values()) {
            if (!databaseManager.contains(name)) {
                databaseManager.create(name);
            }
        }
    }

    @Override
    public GraknClient.Database.Cluster get(String name) {
        List<GraknClientException> errors = new ArrayList<>();
        for (ServerAddress address : databaseManagers.keySet()) {
            try {
                DatabaseProto.Database.Get.Res res = client.graknClusterRPC(address)
                        .databaseGet(DatabaseProto.Database.Get.Req.newBuilder().setName(name).build());
                return DatabaseClusterRPC.of(res.getDatabase(), this);
            } catch (GraknClientException e) {
                errors.add(e);
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED.message(errors.toString()));
    }

    @Override
    public List<GraknClient.Database.Cluster> all() {
        List<GraknClientException> errors = new ArrayList<>();
        for (ServerAddress address : databaseManagers.keySet()) {
            try {
                DatabaseProto.Database.All.Res res = client.graknClusterRPC(address).databaseAll(DatabaseProto.Database.All.Req.getDefaultInstance());
                return res.getDatabasesList().stream().map(db -> DatabaseClusterRPC.of(db, this)).collect(Collectors.toList());
            } catch (GraknClientException e) {
                errors.add(e);
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED.message(errors.toString()));
    }

    Map<ServerAddress, DatabaseManagerRPC> databaseManagers() {
        return databaseManagers;
    }
}
