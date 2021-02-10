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
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.RPCSession;
import grakn.protocol.cluster.DatabaseProto;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;

public class RPCSessionCluster implements GraknClient.Session {
    private static final Logger LOG = LoggerFactory.getLogger(GraknClient.Session.class);
    public static final int MAX_RETRY_PER_REPLICA = 10;
    public static final int WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;
    private final RPCGraknClientCluster clusterClient;
    private Database database;
    private final String dbName;
    private final GraknClient.Session.Type type;
    private final GraknOptions.Cluster options;
    private final ConcurrentMap<Replica.Id, RPCSession> coreSessions;
    private boolean isOpen;

    public RPCSessionCluster(RPCGraknClientCluster clusterClient, String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        this.clusterClient = clusterClient;
        this.dbName = database;
        this.type = type;
        this.options = options;
        fetchLatestReplicaInfo(MAX_RETRY_PER_REPLICA);
        coreSessions = new ConcurrentHashMap<>();
        isOpen = true;
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type) {
        return transaction(type, GraknOptions.cluster());
    }

    @Override
    public GraknClient.Transaction transaction(GraknClient.Transaction.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOpt = options.asCluster();
        if (clusterOpt.readAnyReplica().isPresent() && clusterOpt.readAnyReplica().get()) {
            return transactionSecondaryReplica(type, clusterOpt);
        } else {
            return transactionPrimaryReplica(type, options);
        }
    }

    @Override
    public GraknClient.Session.Type type() {
        return type;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        coreSessions.values().forEach(RPCSession::close);
        isOpen = false;
    }

    @Override
    public String database() {
        return dbName;
    }

    private GraknClient.Transaction transactionPrimaryReplica(GraknClient.Transaction.Type type, GraknOptions options) {
        if (!database.primaryReplica().isPresent()) {
            fetchLatestReplicaInfo(MAX_RETRY_PER_REPLICA);
        }

        while (true) {
            try {
                return transactionReplica(type, options, database.primaryReplica().get());
            } catch (GraknClientException e) {
                if (CLUSTER_REPLICA_NOT_PRIMARY.equals(e.getErrorMessage())) {
                    LOG.debug("Unable to open a session or transaction", e);
                    fetchLatestReplicaInfo(MAX_RETRY_PER_REPLICA);
                } else if ((UNABLE_TO_CONNECT.equals(e.getErrorMessage()))) {
                    waitForPrimaryReplicaSelection();
                    fetchLatestReplicaInfo(MAX_RETRY_PER_REPLICA);
                } else throw e;
            }
        }
    }

    private GraknClient.Transaction transactionSecondaryReplica(GraknClient.Transaction.Type type, GraknOptions.Cluster options) {
        for (Replica replica: database.replicas()) {
            try {
                return transactionReplica(type, options, replica);
            } catch (GraknClientException e) {
                if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                    LOG.debug("Unable to open a session or transaction to " + replica.id() + ". Attempting next replica.", e);
                } else {
                    throw e;
                }
            }
        }
        throw clusterNotAvailableException();
    }

    private GraknClient.Transaction transactionReplica(GraknClient.Transaction.Type type, GraknOptions options, Replica replica) {
        RPCSession selectedSession = coreSessions.computeIfAbsent(
                replica.id(),
                key -> {
                    LOG.debug("Opening a session to '{}'", key);
                    return clusterClient.coreClient(key.address()).session(key.database(), this.type, this.options);
                }
        );
        LOG.debug("Opening read secondary transaction to secondary replica '{}'", replica.id());
        return selectedSession.transaction(type, options);
    }

    private Replica fetchLatestReplicaInfo(int maxRetries) {
        int retries = 0;
        while (retries < maxRetries) {
            database = databaseDiscover();
            if (database.primaryReplica().isPresent()) {
                return database.primaryReplica().get();
            } else {
                waitForPrimaryReplicaSelection();
                retries++;
            }
        }

        throw clusterNotAvailableException();
    }

    private Database databaseDiscover() {
        for (Address.Server server: clusterClient.clusterMembers()) {
            try {
                return Database.ofProto(clusterClient.graknClusterRPC(server).databaseReplicas(
                        DatabaseProto.Database.Replicas.Req.newBuilder().setDatabase(dbName).build()));
            } catch (StatusRuntimeException e) {
                LOG.debug("Failed to fetch replica info from " + server + ". Attempting next server.", e);
            }
        }
        throw clusterNotAvailableException();
    }

    private GraknClientException clusterNotAvailableException() {
        String addresses = clusterClient.clusterMembers().stream().map(Address.Server::toString).collect(Collectors.joining(","));
        return new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, addresses);
    }

    private void waitForPrimaryReplicaSelection() {
        try {
            Thread.sleep(WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS);
        } catch (InterruptedException e) {
            throw new GraknClientException(UNEXPECTED_INTERRUPTION);
        }
    }

    public static class Database {
        private final Map<Replica.Id, Replica> replicas;

        private Database(Map<Replica.Id, Replica> replicas) {
            assert !replicas.isEmpty();
            this.replicas = replicas;
        }

        public static Database ofProto(DatabaseProto.Database.Replicas.Res res) {
            Map<Replica.Id, Replica> replicaMap = new HashMap<>();

            for (DatabaseProto.Database.Replica replica: res.getReplicasList()) {
                Replica.Id id = new Replica.Id(Address.Server.parse(replica.getAddress()), replica.getDatabase());
                replicaMap.put(id, Replica.ofProto(replica));
            }

            return new Database(replicaMap);
        }

        private Optional<Replica> primaryReplica() {
            return replicas.values().stream()
                    .filter(Replica::isPrimary)
                    .max(Comparator.comparing(e -> e.term));
        }

        public Collection<Replica> replicas() {
            return replicas.values();
        }
    }

    public static class Replica {
        private final Replica.Id id;
        private final boolean isPrimary;
        private final long term;

        private Replica(Replica.Id id, long term, boolean isPrimary) {
            this.id = id;
            this.term = term;
            this.isPrimary = isPrimary;
        }

        public static Replica ofProto(DatabaseProto.Database.Replica replica) {
            return new Replica(
                    new Id(Address.Server.parse(replica.getAddress()), replica.getDatabase()),
                    replica.getTerm(),
                    replica.getPrimary()
            );
        }

        public Id id() {
            return id;
        }

        public long term() {
            return term;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Replica replica = (Replica) o;
            return term == replica.term &&
                    isPrimary == replica.isPrimary;
        }

        @Override
        public int hashCode() {
            return Objects.hash(isPrimary, term);
        }

        @Override
        public String toString() {
            return id + ":" + (isPrimary ? "P" : "S") + ":" + term;
        }

        public static class Id {
            private final Address.Server address;
            private final String database;

            Id(Address.Server address, String database) {
                this.address = address;
                this.database = database;
            }

            public Address.Server address() {
                return address;
            }

            public String database() {
                return database;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Id id = (Id) o;
                return Objects.equals(address, id.address) &&
                        Objects.equals(database, id.database);
            }

            @Override
            public int hashCode() {
                return Objects.hash(address, database);
            }

            @Override
            public String toString() {
                return address + "/" + database;
            }
        }
    }
}
