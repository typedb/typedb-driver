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
import grakn.client.rpc.DatabaseManagerRPC;
import grakn.client.rpc.DatabaseRPC;
import grakn.protocol.cluster.DatabaseProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class DatabaseClusterRPC implements GraknClient.Database.Cluster {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseClusterRPC.class);
    private final String name;
    private final Map<ServerAddress, DatabaseRPC> databases;
    private final DatabaseManagerClusterRPC databaseManagerCluster;
    private final Set<Replica> replicas;

    private DatabaseClusterRPC(DatabaseManagerClusterRPC databaseManagerCluster, String database) {
        databases = new HashMap<>();
        for (ServerAddress address : databaseManagerCluster.databaseManagers().keySet()) {
            DatabaseManagerRPC databaseManager = databaseManagerCluster.databaseManagers().get(address);
            databases.put(address, new DatabaseRPC(databaseManager, database));
        }
        this.name = database;
        this.databaseManagerCluster = databaseManagerCluster;
        this.replicas = new HashSet<>();
    }

    static DatabaseClusterRPC of(DatabaseProto.Database protoDB, DatabaseManagerClusterRPC databaseManagerCluster) {
        assert protoDB.getReplicasCount() > 0;
        String database = protoDB.getName();
        DatabaseClusterRPC databaseClusterRPC = new DatabaseClusterRPC(databaseManagerCluster, database);
        databaseClusterRPC.replicas().addAll(protoDB.getReplicasList().stream().map(rep -> Replica.of(rep, databaseClusterRPC)).collect(Collectors.toSet()));
        LOG.debug("Discovered database cluster: {}", databaseClusterRPC);
        return databaseClusterRPC;
    }

    public Optional<Replica> primaryReplica() {
        return replicas.stream().filter(Replica::isPrimary).max(Comparator.comparing(Replica::term));
    }

    public Replica preferredSecondaryReplica() {
        return replicas.stream().filter(Replica::isPreferredSecondary).findAny()
                .orElse(replicas.iterator().next());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void delete() {
        for (ServerAddress address : databases.keySet()) {
            if (databaseManagerCluster.databaseManagers().get(address).contains(name)) {
                databases.get(address).delete();
            }
        }
    }

    public Set<Replica> replicas() {
        return replicas;
    }

    @Override
    public String toString() {
        return replicas.toString();
    }

    static class Replica implements GraknClient.Database.Replica {
        private final Replica.Id id;
        private final DatabaseClusterRPC database;
        private final boolean isPrimary;
        private final boolean isPreferredSecondary;
        private final long term;

        private Replica(DatabaseClusterRPC database, ServerAddress address, long term, boolean isPrimary, boolean isPreferredSecondary) {
            this.database = database;
            this.id = new Id(address, database.name());
            this.term = term;
            this.isPrimary = isPrimary;
            this.isPreferredSecondary = isPreferredSecondary;
        }

        public static Replica of(DatabaseProto.Database.Replica replica, DatabaseClusterRPC database) {
            return new Replica(database, ServerAddress.parse(replica.getAddress()),
                    replica.getTerm(), replica.getPrimary(), replica.getPreferredSecondary());
        }

        public Id id() {
            return id;
        }

        @Override
        public GraknClient.Database.Cluster database() {
            return database;
        }

        public long term() {
            return term;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public boolean isPreferredSecondary() {
            return isPreferredSecondary;
        }

        public ServerAddress address() {
            return id.address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Replica replica = (Replica) o;
            return id == replica.id &&
                    term == replica.term &&
                    isPrimary == replica.isPrimary &&
                    isPreferredSecondary == replica.isPreferredSecondary;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, isPrimary, isPreferredSecondary, term);
        }

        @Override
        public String toString() {
            return id + ":" + (isPrimary ? "P" : "S") + ":" + term;
        }

        static class Id {
            private final ServerAddress address;
            private final String databaseName;

            Id(ServerAddress address, String databaseName) {
                this.address = address;
                this.databaseName = databaseName;
            }

            public ServerAddress address() {
                return address;
            }

            public String databaseName() {
                return databaseName;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Id id = (Id) o;
                return Objects.equals(address, id.address) &&
                        Objects.equals(databaseName, id.databaseName());
            }

            @Override
            public int hashCode() {
                return Objects.hash(address, databaseName);
            }

            @Override
            public String toString() {
                return address + "/" + databaseName;
            }
        }
    }
}
