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

import grakn.protocol.cluster.DatabaseProto;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class DatabaseCluster {
    private final Map<Replica.Id, Replica> replicas;

    private DatabaseCluster(Map<Replica.Id, Replica> replicas) {
        assert !replicas.isEmpty();
        this.replicas = replicas;
    }

    public static DatabaseCluster ofProto(DatabaseProto.Database.Replicas.Res res) {
        Map<Replica.Id, Replica> replicaMap = new HashMap<>();

        for (DatabaseProto.Database.Replica replica: res.getReplicasList()) {
            Replica.Id id = new Replica.Id(Address.Server.parse(replica.getAddress()), replica.getDatabase());
            replicaMap.put(id, Replica.ofProto(replica));
        }

        return new DatabaseCluster(replicaMap);
    }

    Optional<Replica> primaryReplica() {
        return replicas.values().stream()
                .filter(Replica::isPrimary)
                .max(Comparator.comparing(Replica::term));
    }

    Replica preferredSecondaryReplica() {
        return replicas.values().stream()
                .filter(Replica::isPreferredSecondary)
                .max(Comparator.comparing(Replica::term))
                .orElse(replicas.values().stream().findAny().get());
    }

    public Collection<Replica> replicas() {
        return replicas.values();
    }

    static class Replica {
        private final Replica.Id id;
        private final boolean isPrimary;
        private final boolean isPreferredSecondary;
        private final long term;

        private Replica(Replica.Id id, long term, boolean isPrimary, boolean isPreferredSecondary) {
            this.id = id;
            this.term = term;
            this.isPrimary = isPrimary;
            this.isPreferredSecondary = isPreferredSecondary;
        }

        public static Replica ofProto(DatabaseProto.Database.Replica replica) {
            return new Replica(
                    new Id(Address.Server.parse(replica.getAddress()), replica.getDatabase()),
                    replica.getTerm(),
                    replica.getPrimary(),
                    replica.getPreferredSecondary()
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

        public boolean isPreferredSecondary() {
            return isPreferredSecondary;
        }

        public Address.Server address() {
            return id.address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Replica replica = (Replica) o;
            return term == replica.term &&
                    isPrimary == replica.isPrimary &&
                    isPreferredSecondary == replica.isPreferredSecondary;
        }

        @Override
        public int hashCode() {
            return Objects.hash(isPrimary, isPreferredSecondary, term);
        }

        @Override
        public String toString() {
            return id + ":" + (isPrimary ? "P" : "S") + ":" + term;
        }

        static class Id {
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
