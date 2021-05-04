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
import grakn.client.common.exception.GraknClientException;
import grakn.client.common.rpc.GraknStub;
import grakn.client.core.CoreDatabaseManager;
import grakn.common.collection.Pair;
import grakn.protocol.ClusterDatabaseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import static grakn.client.common.exception.ErrorMessage.Client.DB_DOES_NOT_EXIST;
import static grakn.client.common.rpc.RequestBuilder.Cluster.DatabaseManager.allReq;
import static grakn.client.common.rpc.RequestBuilder.Cluster.DatabaseManager.getReq;
import static grakn.common.collection.Collections.pair;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ClusterDatabaseManager implements DatabaseManager.Cluster {

    private static final Logger LOG = LoggerFactory.getLogger(FailsafeTask.class);

    private final Map<String, CoreDatabaseManager> databaseMgrs;
    private final ClusterClient client;

    public ClusterDatabaseManager(ClusterClient client) {
        this.client = client;
        this.databaseMgrs = client.coreClients().entrySet().stream()
                .map(c -> pair(c.getKey(), c.getValue().databases()))
                .collect(toMap(Pair::first, Pair::second));
    }

    @Override
    public boolean contains(String name) {
        return failsafeTask(name, (stub, coreDbMgr) -> coreDbMgr.contains(name));
    }

    @Override
    public void create(String name) {
        failsafeTask(name, (stub, coreDbMgr) -> {
            coreDbMgr.create(name);
            return null;
        });
    }

    @Override
    public Database.Cluster get(String name) {
        return failsafeTask(name, (stub, coreDbMgr) -> {
            if (contains(name)) {
                ClusterDatabaseProto.ClusterDatabaseManager.Get.Res res = stub.databasesGet(getReq(name));
                return ClusterDatabase.of(res.getDatabase(), client, this);
            }
            else throw new GraknClientException(DB_DOES_NOT_EXIST, name);
        });
    }

    @Override
    public List<Database.Cluster> all() {
        StringBuilder errors = new StringBuilder();
        for (String address : databaseMgrs.keySet()) {
            try {
                ClusterDatabaseProto.ClusterDatabaseManager.All.Res res = client.stub(address).databasesAll(allReq());
                return res.getDatabasesList().stream().map(db -> ClusterDatabase.of(db, client, this)).collect(toList());
            } catch (GraknClientException e) {
                errors.append("- ").append(address).append(": ").append(e).append("\n");
            }
        }
        throw new GraknClientException(CLUSTER_ALL_NODES_FAILED, errors.toString());
    }

    Map<String, CoreDatabaseManager> databaseMgrs() {
        return databaseMgrs;
    }

    private <RESULT> RESULT failsafeTask(String name, BiFunction<GraknStub.Cluster, CoreDatabaseManager, RESULT> task) {
        FailsafeTask<RESULT> failsafeTask = new FailsafeTask<RESULT>(client, name) {

            @Override
            RESULT run(ClusterDatabase.Replica replica) {
                return task.apply(client.stub(replica.address()), client.coreClient(replica.address()).databases());
            }

            @Override
            RESULT rerun(ClusterDatabase.Replica replica) {
                return run(replica);
            }
        };
        try {
            return failsafeTask.runAnyReplica();
        } catch (GraknClientException e) {
            if (CLUSTER_REPLICA_NOT_PRIMARY.equals(e.getErrorMessage())) {
                return failsafeTask.runPrimaryReplica();
            } else throw e;
        }
    }
}
