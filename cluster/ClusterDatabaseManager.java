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

package grakn.client.cluster;

import grakn.client.api.database.Database;
import grakn.client.api.database.DatabaseManager;
import grakn.client.common.GraknClientException;
import grakn.client.core.CoreDatabaseManager;
import grakn.protocol.ClusterDatabaseProto;

import java.util.List;
import java.util.Map;

import static grakn.client.common.ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;
import static java.util.stream.Collectors.toList;

public class ClusterDatabaseManager implements DatabaseManager.Cluster {
    private final Map<String, CoreDatabaseManager> databaseManagers;
    private final ClusterClient client;

    public ClusterDatabaseManager(ClusterClient client, Map<String, CoreDatabaseManager> databaseManagers) {
        this.client = client;
        this.databaseManagers = databaseManagers;
    }

    @Override
    public boolean contains(String name) {
        StringBuilder errors = new StringBuilder();
        for (String address : databaseManagers.keySet()) {
            try {
                return databaseManagers.get(address).contains(name);
            } catch (GraknClientException e) {
                errors.append("- ").append(address).append(": ").append(e).append("\n");
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED, errors.toString());
    }

    @Override
    public void create(String name) {
        for (CoreDatabaseManager databaseManager : databaseManagers.values()) {
            if (!databaseManager.contains(name)) {
                databaseManager.create(name);
            }
        }
    }

    @Override
    public Database.Cluster get(String name) {
        StringBuilder errors = new StringBuilder();
        for (String address : databaseManagers.keySet()) {
            try {
                ClusterDatabaseProto.ClusterDatabaseManager.Get.Res res = client.coreClient(address).call(
                        () -> client.stub(address).databasesGet(ClusterDatabaseProto.ClusterDatabaseManager.Get.Req.newBuilder().setName(name).build())
                );
                return ClusterDatabase.of(res.getDatabase(), this);
            } catch (GraknClientException e) {
                errors.append("- ").append(address).append(": ").append(e).append("\n");
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED, errors.toString());
    }

    @Override
    public List<Database.Cluster> all() {
        StringBuilder errors = new StringBuilder();
        for (String address : databaseManagers.keySet()) {
            try {
                ClusterDatabaseProto.ClusterDatabaseManager.All.Res res = client.coreClient(address).call(
                        () -> client.stub(address).databasesAll(
                                ClusterDatabaseProto.ClusterDatabaseManager.All.Req.getDefaultInstance()
                        ));
                return res.getDatabasesList().stream().map(db -> ClusterDatabase.of(db, this)).collect(toList());
            } catch (GraknClientException e) {
                errors.append("- ").append(address).append(": ").append(e).append("\n");
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED, errors.toString());
    }

    Map<String, CoreDatabaseManager> databaseManagers() {
        return databaseManagers;
    }
}
