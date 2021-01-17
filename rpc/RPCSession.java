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

import com.google.protobuf.ByteString;
import grakn.client.Grakn;
import grakn.client.GraknClient;
import grakn.client.GraknOptions;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import grakn.protocol.cluster.ClusterGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static grakn.client.GraknProtoBuilder.options;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_LEADER_NOT_FOUND;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_NOT_AVAILABLE;

public class RPCSession {
    public static class Core implements Grakn.Session {

        private final Channel channel;
        private final String database;
        private final Type type;
        private final ByteString sessionId;
        private final AtomicBoolean isOpen;
        private final Timer pulse;
        private final GraknGrpc.GraknBlockingStub blockingGrpcStub;

        public Core(GraknClient.Core client, String database, Type type, GraknOptions options) {
            this.channel = client.channel();
            this.database = database;
            this.type = type;
            blockingGrpcStub = GraknGrpc.newBlockingStub(channel);
            final SessionProto.Session.Open.Req openReq = SessionProto.Session.Open.Req.newBuilder()
                    .setDatabase(database).setType(sessionType(type)).setOptions(options(options)).build();

            sessionId = blockingGrpcStub.sessionOpen(openReq).getSessionId();
            pulse = new Timer();
            isOpen = new AtomicBoolean(true);
            pulse.scheduleAtFixedRate(this.new PulseTask(), 0, 5000);
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            return new RPCTransaction(this, sessionId, type, options);
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public boolean isOpen() {
            return isOpen.get();
        }

        @Override
        public void close() {
            if (isOpen.compareAndSet(true, false)) {
                pulse.cancel();
                blockingGrpcStub.sessionClose(SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build());
            }
        }

        @Override
        public String database() {
            return database;
        }

        Channel channel() { return channel; }

        public void pulse() {
            final SessionProto.Session.Pulse.Res res = blockingGrpcStub.sessionPulse(
                    SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionId).build());

            if (!res.getAlive()) {
                isOpen.set(false);
                pulse.cancel();
            }
        }

        private static SessionProto.Session.Type sessionType(Type type) {
            switch (type) {
                case DATA:
                    return SessionProto.Session.Type.DATA;
                case SCHEMA:
                    return SessionProto.Session.Type.SCHEMA;
                default:
                    return SessionProto.Session.Type.UNRECOGNIZED;
            }
        }

        private class PulseTask extends TimerTask {
            @Override
            public void run() {
                if (!isOpen()) return;
                pulse();
            }
        }
    }

    public static class Cluster implements Grakn.Session {
        private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);
        private final GraknClient.Cluster client;
        private final String database;
        private final Type type;
        private ClusterGrpc.ClusterBlockingStub clusterDiscoveryRPC;
        private final DatabaseReplicas databaseReplicas;
        private final ConcurrentMap<DatabaseReplica.Id, RPCSession.Core> sessions;
        private final AtomicBoolean isOpen;

        public Cluster(GraknClient.Cluster client, String database, Grakn.Session.Type type, GraknOptions options, ClusterGrpc.ClusterBlockingStub clusterDiscoveryRPC) {
            this.client = client;
            this.database = database;
            this.type = type;
            this.clusterDiscoveryRPC = clusterDiscoveryRPC;
            this.databaseReplicas = discoverDatabaseReplicas();
            sessions = new ConcurrentHashMap<>();
            isOpen = new AtomicBoolean(true);
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            DatabaseReplica leader = databaseReplicas.selectLeader();
            RPCSession.Core leaderSession = sessions.computeIfAbsent(
                    leader.id(),
                    key -> {
                        LOG.info("Opening a session to leader '{}'", leader);
                        return client.clients().get(key.address()).session(key.database(), this.type);
                    }
            );
            LOG.info("Opening a transaction to leader '{}'", leader);
            return leaderSession.transaction(type, options);
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public boolean isOpen() {
            return isOpen.get();
        }

        @Override
        public void close() {
            sessions.values().forEach(Core::close);
        }

        @Override
        public String database() {
            return database;
        }

        private DatabaseReplicas discoverDatabaseReplicas() {
            int attempt = 0;
            while (attempt < client.clients().size()) {
                try {
                    return DatabaseReplicas.ofProto(
                            clusterDiscoveryRPC.databaseReplicas(
                                    grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Req.newBuilder()
                                            .setDatabase(database)
                                            .build()
                            )
                    );
                } catch (StatusRuntimeException e) {
                    ClusterGrpc.ClusterBlockingStub old = clusterDiscoveryRPC;
                    clusterDiscoveryRPC = this.client.selectNextClusterDiscoveryRPC();
                    LOG.info("cluster discovery RPC '{}' not available. selecting another: '{}'",
                            old.getChannel().authority(), clusterDiscoveryRPC.getChannel().authority());
                }
            }

            throw new GraknClientException(CLUSTER_NOT_AVAILABLE.message(client.clients()));
        }

        public static class DatabaseReplicas {
            private final String database;
            private final ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> replicaMap;
            private final Set<Address.Cluster> addresses;

            private DatabaseReplicas(String database, ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> replicaMap) {
                this.database = database;
                this.replicaMap = replicaMap;
                this.addresses = replicaMap.keySet().stream().map(DatabaseReplica.Id::address).collect(Collectors.toSet());
            }

            public static DatabaseReplicas ofProto(grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Res res) {
                ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> replicaMap = new ConcurrentHashMap<>();

                for (grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Res.DatabaseReplica replica: res.getReplicasList()) {
                    DatabaseReplica.Id id = new DatabaseReplica.Id(Address.Cluster.parse(replica.getAddress()), replica.getDatabase());
                    replicaMap.put(id, DatabaseReplica.ofProto(replica));
                }

                return new DatabaseReplicas(replicaMap.values().iterator().next().id().database(), replicaMap);
            }

            private DatabaseReplica selectLeader() {
                Map.Entry<DatabaseReplica.Id, DatabaseReplica> initial = replicaMap.entrySet().iterator().next();
                Map.Entry<DatabaseReplica.Id, DatabaseReplica> reduce = replicaMap.entrySet().stream()
                        .filter(entry -> entry.getValue().role())
                        .reduce(initial, (acc, e) -> e.getValue().term > acc.getValue().term ? e : acc);
                if (reduce.getValue().role()) return reduce.getValue();
                else throw new GraknClientException(CLUSTER_LEADER_NOT_FOUND.message(reduce.getValue().term())); // TODO: throw a checked exception (LEADER_NOT_FOUND)
            }

            // TODO: for when the transaction type is read replica
            private DatabaseReplica selectRandomReplica() {
                return replicaMap.get(new DatabaseReplica.Id(selectRandomReplicaAddress(), database));
            }

            private Address.Cluster selectRandomReplicaAddress() {
                return addresses.iterator().next();
            }
        }

        public static class DatabaseReplica {
            private final DatabaseReplica.Id id;
            private final boolean isLeader;
            private final long term;

            private DatabaseReplica(DatabaseReplica.Id id, long term, boolean isLeader) {
                this.id = id;
                this.term = term;
                this.isLeader = isLeader;
            }

            public static DatabaseReplica ofProto(grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Res.DatabaseReplica replica) {
                return new DatabaseReplica(
                        new Id(Address.Cluster.parse(replica.getAddress()), replica.getDatabase()),
                        replica.getTerm(),
                        replica.getIsLeader()
                );
            }

            public Id id() {
                return id;
            }

            public long term() {
                return term;
            }

            public boolean role() {
                return isLeader;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                DatabaseReplica replica = (DatabaseReplica) o;
                return term == replica.term &&
                        isLeader == replica.isLeader;
            }

            @Override
            public int hashCode() {
                return Objects.hash(isLeader, term);
            }

            @Override
            public String toString() {
                return id + ":" + (isLeader ? "L" : "N") + ":" + term;
            }

            public static class Id {
                private final Address.Cluster address;
                private final String database;

                Id(Address.Cluster address, String database) {
                    this.address = address;
                    this.database = database;
                }

                public Address.Cluster address() {
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
}
