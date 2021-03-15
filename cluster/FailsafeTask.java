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

import grakn.client.common.GraknClientException;
import grakn.protocol.cluster.DatabaseProto;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static grakn.client.common.ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import static grakn.client.common.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.client.common.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static grakn.client.common.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;

abstract class FailsafeTask<E> {

    private static final Logger LOG = LoggerFactory.getLogger(FailsafeTask.class);
    private static final int PRIMARY_REPLICA_TASK_MAX_RETRIES = 10;
    private static final int FETCH_REPLICAS_MAX_RETRIES = 10;
    private static final int WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;

    private final ClusterClient client;
    private final String database;

    FailsafeTask(ClusterClient client, String database) {
        this.client = client;
        this.database = database;
    }

    abstract E run(ClusterDatabase.Replica replica);

    E rerun(ClusterDatabase.Replica replica) {
        return run(replica);
    }

    E runPrimaryReplica() {
        if (!client.clusterDatabases().containsKey(database)
                || !client.clusterDatabases().get(database).primaryReplica().isPresent()) {
            seekPrimaryReplica();
        }
        ClusterDatabase.Replica replica = client.clusterDatabases().get(database).primaryReplica().get();
        int retries = 0;
        while (true) {
            try {
                return retries == 0 ? run(replica) : rerun(replica);
            } catch (GraknClientException e) {
                if (CLUSTER_REPLICA_NOT_PRIMARY.equals(e.getErrorMessage())
                        || UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                    LOG.debug("Unable to open a session or transaction, retrying in 2s...", e);
                    waitForPrimaryReplicaSelection();
                    replica = seekPrimaryReplica();
                } else throw e;
            }
            if (++retries > PRIMARY_REPLICA_TASK_MAX_RETRIES) throw clusterNotAvailableException();
        }
    }

    E runAnyReplica() {
        ClusterDatabase databaseClusterRPC = client.clusterDatabases().get(database);
        if (databaseClusterRPC == null) databaseClusterRPC = fetchDatabaseReplicas();

        // Try the preferred secondary replica first, then go through the others
        List<ClusterDatabase.Replica> replicas = new ArrayList<>();
        replicas.add(databaseClusterRPC.preferredReplica());
        for (ClusterDatabase.Replica replica : databaseClusterRPC.replicas()) {
            if (!replica.isPreferred()) replicas.add(replica);
        }

        int retries = 0;
        for (ClusterDatabase.Replica replica : replicas) {
            try {
                return retries == 0 ? run(replica) : rerun(replica);
            } catch (GraknClientException e) {
                if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                    LOG.debug("Unable to open a session or transaction to " + replica.id() +
                                      ". Attempting next replica.", e);
                } else {
                    throw e;
                }
            }
            retries++;
        }
        throw clusterNotAvailableException();
    }

    private ClusterDatabase.Replica seekPrimaryReplica() {
        int retries = 0;
        while (retries < FETCH_REPLICAS_MAX_RETRIES) {
            ClusterDatabase databaseClusterRPC = fetchDatabaseReplicas();
            if (databaseClusterRPC.primaryReplica().isPresent()) {
                return databaseClusterRPC.primaryReplica().get();
            } else {
                waitForPrimaryReplicaSelection();
                retries++;
            }
        }
        throw clusterNotAvailableException();
    }

    private ClusterDatabase fetchDatabaseReplicas() {
        for (String serverAddress : client.clusterMembers()) {
            try {
                LOG.debug("Fetching replica info from {}", serverAddress);
                DatabaseProto.Database.Get.Res res = client.graknClusterRPC(serverAddress).databaseGet(
                        DatabaseProto.Database.Get.Req.newBuilder().setName(database).build()
                );
                ClusterDatabase databaseClusterRPC = ClusterDatabase.of(res.getDatabase(), client.databases());
                client.clusterDatabases().put(database, databaseClusterRPC);
                return databaseClusterRPC;
            } catch (StatusRuntimeException e) {
                LOG.debug("Failed to fetch replica info for database '" + database + "' from " +
                                  serverAddress + ". Attempting next server.", e);
            }
        }
        throw clusterNotAvailableException();
    }

    private void waitForPrimaryReplicaSelection() {
        try {
            Thread.sleep(WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS);
        } catch (InterruptedException e) {
            throw new GraknClientException(UNEXPECTED_INTERRUPTION);
        }
    }

    private GraknClientException clusterNotAvailableException() {
        return new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", client.clusterMembers()));
    }
}
