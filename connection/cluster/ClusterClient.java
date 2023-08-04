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

package com.vaticle.typedb.client.connection.cluster;

import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.client.api.user.UserManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.protocol.ClusterDatabaseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.DB_DOES_NOT_EXIST;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.DatabaseManager.getReq;

public class ClusterClient implements TypeDBClient.Cluster {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterClient.class);

    private final TypeDBCredential credential;
    private final int parallelisation;
    private final Map<String, ClusterServerClient> clusterServerClients;
    private final ClusterUserManager userMgr;
    private final ClusterDatabaseManager databaseMgr;
    private final ConcurrentMap<String, ClusterDatabase> clusterDatabases;
    private boolean isOpen;

    public ClusterClient(Set<String> initAddresses, TypeDBCredential credential) {
        this(initAddresses, credential, ClusterServerClient.calculateParallelisation());
    }

    public ClusterClient(Set<String> initAddresses, TypeDBCredential credential, int parallelisation) {
        this.credential = credential;
        this.parallelisation = parallelisation;
        Set<String> currAddresses = fetchCurrentAddresses(initAddresses);
        clusterServerClients = createClients(credential, parallelisation, currAddresses);
        userMgr = new ClusterUserManager(this);
        databaseMgr = new ClusterDatabaseManager(this);
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    private Set<String> fetchCurrentAddresses(Set<String> servers) {
        Map<String, Throwable> perServerExceptions = new HashMap<>();
        for (String server : servers) {
            try (ClusterServerClient client = new ClusterServerClient(server, credential, parallelisation)) {
                client.validateConnection();
                return client.servers();
            } catch (TypeDBClientException e) {
                if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                    LOG.warn("Unable to fetch list of all servers from server {}.", server);
                    if (e.getCause() != null) perServerExceptions.put(server, e);
                } else {
                    throw e;
                }
            }
        }

        String description;
        if (!perServerExceptions.isEmpty()) {
            StringBuilder reasons = new StringBuilder("Reasons:[\n");
            perServerExceptions.entrySet().stream()
                    .map(entry -> String.format("\t- %s: %s\n", entry.getKey(), entry.getValue().getCause().getMessage()))
                    .forEach(reasons::append);
            description = reasons.append("]").toString();
        } else description = String.join(",", servers);
        throw new TypeDBClientException(CLUSTER_UNABLE_TO_CONNECT, description);
    }

    private Map<String, ClusterServerClient> createClients(TypeDBCredential credential, int parallelisation, Set<String> addresses) {
        Map<String, ClusterServerClient> clients = new HashMap<>();
        boolean available = false;
        for (String address : addresses) {
            ClusterServerClient client = new ClusterServerClient(address, credential, parallelisation);
            try {
                client.validateConnection();
                available = true;
            } catch (TypeDBClientException e) {
                // do nothing
            }
            clients.put(address, client);
        }
        if (!available) throw new TypeDBClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses));
        return clients;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public User user() {
        return users().get(credential.username());
    }

    @Override
    public UserManager users() {
        return userMgr;
    }

    @Override
    public ClusterDatabaseManager databases() {
        return databaseMgr;
    }

    @Override
    public ClusterSession session(String database, TypeDBSession.Type type) {
        return session(database, type, TypeDBOptions.cluster());
    }

    @Override
    public ClusterSession session(String database, TypeDBSession.Type type, TypeDBOptions options) {
        TypeDBOptions.Cluster clusterOptions = options.asCluster();
        if (clusterOptions.readAnyReplica().isPresent() && clusterOptions.readAnyReplica().get()) {
            return sessionAnyReplica(database, type, clusterOptions);
        } else {
            return sessionPrimaryReplica(database, type, clusterOptions);
        }
    }

    private ClusterSession sessionPrimaryReplica(String database, TypeDBSession.Type type, TypeDBOptions.Cluster options) {
        return createFailsafeTask(
                database,
                parameter -> new ClusterSession(this, parameter.replica().address(), database, type, options)
        ).runPrimaryReplica();
    }

    private ClusterSession sessionAnyReplica(String database, TypeDBSession.Type type, TypeDBOptions.Cluster options) {
        return createFailsafeTask(
                database,
                parameter -> new ClusterSession(this, parameter.replica().address(), database, type, options)
        ).runAnyReplica();
    }

    Map<String, ClusterServerClient> clusterServerClients() {
        return clusterServerClients;
    }

    ClusterServerClient clusterServerClient(String address) {
        return clusterServerClients.get(address);
    }

    <RESULT> FailsafeTask<RESULT> createFailsafeTask(
            String database,
            Function<FailsafeTaskParams, RESULT> run) {
        return createFailsafeTask(database, run, run);
    }

    <RESULT> FailsafeTask<RESULT> createFailsafeTask(
            String database,
            Function<FailsafeTaskParams, RESULT> run,
            Function<FailsafeTaskParams, RESULT> rerun
    ) {
        return new FailsafeTask<>(database) {
            @Override
            RESULT run(FailsafeTaskParams parameter) {
                return run.apply(parameter);
            }

            @Override
            RESULT rerun(FailsafeTaskParams parameter) {
                return rerun.apply(parameter);
            }
        };
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public Cluster asCluster() {
        return this;
    }

    @Override
    public void close() {
        clusterServerClients.values().forEach(ClusterServerClient::close);
        isOpen = false;
    }

    abstract class FailsafeTask<RESULT> {

        private final Logger LOG = LoggerFactory.getLogger(FailsafeTask.class);
        private final int PRIMARY_REPLICA_TASK_MAX_RETRIES = 10;
        private final int FETCH_REPLICAS_MAX_RETRIES = 10;
        private final int WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;
        private final String database;

        private FailsafeTask(String database) {
            this.database = database;
        }

        abstract RESULT run(FailsafeTaskParams replica);

        RESULT rerun(FailsafeTaskParams replica) {
            return run(replica);
        }

        RESULT runPrimaryReplica() {
            ClusterDatabase database = clusterDatabases.get(this.database);
            ClusterDatabase.Replica replica;
            if (database == null || !database.primaryReplica().isPresent()) {
                replica = seekPrimaryReplica();
            } else {
                replica = database.primaryReplica().get();
            }
            int retries = 0;
            while (true) {
                try {
                    FailsafeTaskParams parameter = new FailsafeTaskParams(
                            fetchValidatedServerClient(replica.address()), replica
                    );
                    return retries == 0 ? run(parameter) : rerun(parameter);
                } catch (TypeDBClientException e) {
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

        RESULT runAnyReplica() {
            ClusterDatabase clusterDatabase = clusterDatabases.get(database);
            if (clusterDatabase == null) clusterDatabase = fetchDatabaseReplicas();

            // Try the preferred secondary replica first, then go through the others
            List<ClusterDatabase.Replica> replicas = new ArrayList<>();
            replicas.add(clusterDatabase.preferredReplica());
            for (ClusterDatabase.Replica replica : clusterDatabase.replicas()) {
                if (!replica.isPreferred()) replicas.add(replica);
            }

            int retries = 0;
            for (ClusterDatabase.Replica replica : replicas) {
                try {
                    FailsafeTaskParams parameter = new FailsafeTaskParams(fetchValidatedServerClient(replica.address()), replica);
                    return retries == 0 ? run(parameter) : rerun(parameter);
                } catch (TypeDBClientException e) {
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
                ClusterDatabase clusterDatabase = fetchDatabaseReplicas();
                if (clusterDatabase.primaryReplica().isPresent()) {
                    return clusterDatabase.primaryReplica().get();
                } else if (isNonExistentRaftClusterInfo(clusterDatabase)) {
                    throw new TypeDBClientException(DB_DOES_NOT_EXIST, this.database);
                } else {
                    waitForPrimaryReplicaSelection();
                    retries++;
                }
            }
            throw clusterNotAvailableException();
        }

        private ClusterDatabase fetchDatabaseReplicas() {
            for (String serverAddress : clusterServerClients.keySet()) {
                try {
                    LOG.debug("Fetching replica info from {}", serverAddress);
                    ClusterDatabaseProto.ClusterDatabaseManager.Get.Res res = fetchValidatedServerClient(serverAddress)
                            .stub().databasesGet(getReq(database));
                    ClusterDatabase clusterDatabase = ClusterDatabase.of(res.getDatabase(), ClusterClient.this);
                    if (!isNonExistentRaftClusterInfo(clusterDatabase)) {
                        clusterDatabases.put(database, clusterDatabase);
                    }
                    return clusterDatabase;
                } catch (TypeDBClientException e) {
                    if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                        LOG.debug("Failed to fetch replica info for database '" + database + "' from " +
                                serverAddress + ". Attempting next server.", e);
                    } else {
                        throw e;
                    }
                }
            }
            throw clusterNotAvailableException();
        }

        private void waitForPrimaryReplicaSelection() {
            try {
                Thread.sleep(WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS);
            } catch (InterruptedException e) {
                throw new TypeDBClientException(UNEXPECTED_INTERRUPTION);
            }
        }

        private boolean isNonExistentRaftClusterInfo(ClusterDatabase clusterDatabase) {
            return clusterDatabase.primaryReplica().isEmpty() &&
                    clusterDatabase.replicas().stream().allMatch(replica -> replica.term() == -1);
        }

        private ClusterServerClient fetchValidatedServerClient(String address) {
            ClusterServerClient serverClient = clusterServerClient(address);
            if (!serverClient.isConnectionValidated()) serverClient.validateConnection(); // may throw exception
            return serverClient;
        }

        private TypeDBClientException clusterNotAvailableException() {
            return new TypeDBClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", clusterServerClients.keySet()));
        }
    }

    static class FailsafeTaskParams {

        private final ClusterServerClient client;
        private final ClusterDatabase.Replica replica;

        public FailsafeTaskParams(ClusterServerClient client, ClusterDatabase.Replica replica) {
            this.client = client;
            this.replica = replica;
        }

        public ClusterServerClient client() {
            return client;
        }

        public ClusterDatabase.Replica replica() {
            return replica;
        }
    }
}
