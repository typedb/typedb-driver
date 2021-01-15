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
import grakn.client.common.exception.ErrorMessage;
import grakn.client.common.exception.GraknClientException;
import grakn.common.collection.Pair;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import grakn.protocol.cluster.ClusterGrpc;
import io.grpc.Channel;

import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static grakn.client.GraknProtoBuilder.options;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_SERVER_NOT_FOUND;
import static grakn.common.collection.Collections.pair;

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
        private final String database;
        private final Type type;
        private final DatabaseReplicas databaseReplicas;
        private final ConcurrentMap<DatabaseReplica.Id, RPCSession.Core> sessions;
        private final AtomicBoolean isOpen;

        public Cluster(GraknClient.Cluster client, String database, Grakn.Session.Type type, GraknOptions options) {
            this.database = database;
            this.type = type;
            this.databaseReplicas = new DatabaseReplicas(database, client);
            sessions = client.clients().entrySet().stream()
                    .map(entry -> pair(new DatabaseReplica.Id(entry.getKey(), database), entry.getValue().session(database, type, options)))
                    .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
            isOpen = new AtomicBoolean(true);
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            DatabaseReplica.Id leader = databaseReplicas.leader();
            System.out.println("opening a transaction to leader '" + leader + "'");
            RPCSession.Core leaderSession = sessions.get(leader);
            if (leaderSession == null) throw new GraknClientException(CLUSTER_SERVER_NOT_FOUND.message(leader.address())); // TODO: throw a checked exception (SERVER_NOT_FOUND)
            else return leaderSession.transaction(type, options);
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

        public static class DatabaseReplicas {
            private final String database;
            private final GraknClient.Cluster client;
            private final ClusterGrpc.ClusterBlockingStub clusterBlockingStub;
            private final ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> replicaMap;

            public DatabaseReplicas(String database, GraknClient.Cluster client) {
                this.database = database;
                this.client = client;
                clusterBlockingStub = ClusterGrpc.newBlockingStub(randomReplicaClient().channel());
                replicaMap = createReplicaMap();
            }

            private DatabaseReplica.Id leader() {
                Map.Entry<DatabaseReplica.Id, DatabaseReplica> initial = replicaMap.entrySet().iterator().next();
                Map.Entry<DatabaseReplica.Id, DatabaseReplica> reduce = replicaMap.entrySet().stream()
                        .filter(entry -> entry.getValue().role())
                        .reduce(initial, (acc, e) -> e.getValue().term > acc.getValue().term ? e : acc);
                if (reduce.getValue().role()) return reduce.getKey();
                else throw new GraknClientException(ErrorMessage.Client.CLUSTER_LEADER_NOT_FOUND.message(reduce.getValue().term())); // TODO: throw a checked exception (LEADER_NOT_FOUND)
            }

            private DatabaseReplica.Id randomReplica() {
                String randomAddress = this.client.clients().keySet().iterator().next();
                return new DatabaseReplica.Id(randomAddress, database);
            }

            private GraknClient.Core randomReplicaClient() {
                return client.clients().get(randomReplica().address());
            }

            private ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> createReplicaMap() {
                ConcurrentMap<DatabaseReplica.Id, DatabaseReplica> replicaMap = new ConcurrentHashMap<>();
                DatabaseReplica.Id source = randomReplica();
                grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Res res = clusterBlockingStub
                        .databaseReplicas(grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Req.newBuilder().setDatabase(source.database()).build());
                for (grakn.protocol.cluster.SessionProto.Session.DatabaseReplicas.Res.DatabaseReplica replica: res.getReplicasList()) {
                    replicaMap.put(new DatabaseReplica.Id(replica.getAddress(), replica.getDatabase()), DatabaseReplica.ofProto(replica));
                }
                return replicaMap;
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
                return new DatabaseReplica(new Id(replica.getAddress(), replica.getDatabase()), replica.getTerm(), replica.getIsLeader());
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

            public static class Id {
                private final String address;
                private final String database;

                Id(String address, String database) {
                    this.address = address;
                    this.database = database;
                }

                public String address() {
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
            }
        }
    }
}
