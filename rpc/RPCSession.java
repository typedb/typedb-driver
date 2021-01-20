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
import grakn.protocol.cluster.DatabaseProto;
import grakn.protocol.cluster.GraknClusterGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static grakn.client.GraknProtoBuilder.options;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_LEADER_NOT_YET_ELECTED;
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
            try {this.channel = client.channel();
            this.database = database;
            this.type = type;
            blockingGrpcStub = GraknGrpc.newBlockingStub(channel);
            final SessionProto.Session.Open.Req openReq = SessionProto.Session.Open.Req.newBuilder()
                    .setDatabase(database).setType(sessionType(type)).setOptions(options(options)).build();

            sessionId = blockingGrpcStub.sessionOpen(openReq).getSessionId();
            pulse = new Timer();
            isOpen = new AtomicBoolean(true);
            pulse.scheduleAtFixedRate(this.new PulseTask(), 0, 5000);
        } catch (StatusRuntimeException e) {
            throw new GraknClientException(e);
        }
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
                try {
                    blockingGrpcStub.sessionClose(SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build());
                } catch (StatusRuntimeException e) {
                    throw new GraknClientException(e);
                }
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
        private final GraknClient.Cluster clusterClient;
        private final Database database;
        private final String dbName;
        private final Type type;
        private final GraknOptions options;
        private final ConcurrentMap<Replica.Id, RPCSession.Core> coreSessions;
        private GraknClusterGrpc.GraknClusterBlockingStub clusterDiscoveryRPC;
        private boolean isOpen;

        public Cluster(GraknClient.Cluster clusterClient, String database, Grakn.Session.Type type, GraknOptions options, GraknClusterGrpc.GraknClusterBlockingStub clusterDiscoveryRPC) {
            this.clusterClient = clusterClient;
            this.dbName = database;
            this.type = type;
            this.options = options;
            this.clusterDiscoveryRPC = clusterDiscoveryRPC;
            this.database = discoverDatabaseReplicas();
            coreSessions = new ConcurrentHashMap<>();
            isOpen = true;
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            Replica selected = type != Grakn.Transaction.Type.READ_REPLICA ?
                    database.selectLeader() : database.selectedReplica();
            RPCSession.Core selection = coreSessions.computeIfAbsent(
                    selected.id(),
                    key -> {
                        LOG.info("Opening a session to leader '{}'", selected);
                        return clusterClient.coreClients().get(key.address()).session(key.database(), this.type, this.options);
                    }
            );
            LOG.info("Opening a transaction of of type '{}' to leader '{}'", type, selected);
            return selection.transaction(type, options);
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() {
            coreSessions.values().forEach(Core::close);
            isOpen = false;
        }

        @Override
        public String database() {
            return dbName;
        }

        private Database discoverDatabaseReplicas() {
            int attempt = 0;
            while (attempt < clusterClient.coreClients().size()) {
                try {
                    return Database.ofProto(
                            clusterDiscoveryRPC.databaseDiscover(
                                    DatabaseProto.Database.Discover.Req.newBuilder()
                                            .setDatabase(dbName)
                                            .build()
                            )
                    );
                } catch (StatusRuntimeException e) {
                    GraknClusterGrpc.GraknClusterBlockingStub old = clusterDiscoveryRPC;
                    clusterDiscoveryRPC = this.clusterClient.selectNextClusterDiscoveryRPC();
                    LOG.info("Cluster Discovery RPC failed - server '{}' not available. Attempting to perform the operation onto another server '{}'",
                            old.getChannel().authority(), clusterDiscoveryRPC.getChannel().authority());
                }
            }

            throw new GraknClientException(CLUSTER_NOT_AVAILABLE.message(clusterClient.coreClients()));
        }

        public static class Database {
            private final ConcurrentMap<Replica.Id, Replica> replicaMap;
            private final Replica[] replicaArray;
            private final AtomicInteger selectedReplica;

            private Database(ConcurrentMap<Replica.Id, Replica> replicaMap) {
                this.replicaMap = replicaMap;
                replicaArray = replicaMap.values().toArray(new Replica[]{});
                selectedReplica = new AtomicInteger(0);
            }

            public static Database ofProto(DatabaseProto.Database.Discover.Res res) {
                ConcurrentMap<Replica.Id, Replica> replicaMap = new ConcurrentHashMap<>();

                for (DatabaseProto.Database.Discover.Res.Replica replica: res.getReplicasList()) {
                    Replica.Id id = new Replica.Id(Address.Cluster.Server.parse(replica.getAddress()), replica.getDatabase());
                    replicaMap.put(id, Replica.ofProto(replica));
                }

                return new Database(replicaMap);
            }

            private Replica selectLeader() {
                Map.Entry<Replica.Id, Replica> initial = replicaMap.entrySet().iterator().next();
                Map.Entry<Replica.Id, Replica> reduce = replicaMap.entrySet().stream()
                        .filter(entry -> entry.getValue().role())
                        .reduce(initial, (acc, e) -> e.getValue().term > acc.getValue().term ? e : acc);
                if (reduce.getValue().role()) return reduce.getValue();
                else throw new GraknClientException(CLUSTER_LEADER_NOT_YET_ELECTED.message(reduce.getValue().term()));
            }

            private Replica selectedReplica() {
                return replicaArray[selectedReplica.get()];
            }
        }

        public static class Replica {
            private final Replica.Id id;
            private final boolean isLeader;
            private final long term;

            private Replica(Replica.Id id, long term, boolean isLeader) {
                this.id = id;
                this.term = term;
                this.isLeader = isLeader;
            }

            public static Replica ofProto(DatabaseProto.Database.Discover.Res.Replica replica) {
                return new Replica(
                        new Id(Address.Cluster.Server.parse(replica.getAddress()), replica.getDatabase()),
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
                Replica replica = (Replica) o;
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
                private final Address.Cluster.Server address;
                private final String database;

                Id(Address.Cluster.Server address, String database) {
                    this.address = address;
                    this.database = database;
                }

                public Address.Cluster.Server address() {
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
