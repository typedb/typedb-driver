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
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static grakn.client.GraknProtoBuilder.options;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_LEADER_NOT_YET_ELECTED;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_SERVER_NOT_A_LEADER;
import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Client.ILLEGAL_ARGUMENT;
import static grakn.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;

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
            throw GraknClientException.of(e);
        }
    }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            if (type == Grakn.Transaction.Type.READ_SECONDARY) throw new GraknClientException(ILLEGAL_ARGUMENT, type);

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
                    throw GraknClientException.of(e);
                }
            }
        }

        @Override
        public String database() {
            return database;
        }

        Channel channel() { return channel; }

        public void pulse() {
            boolean alive;
            try {
                alive = blockingGrpcStub.sessionPulse(
                        SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionId).build()).getAlive();
            } catch (StatusRuntimeException exception) {
                alive = false;
            }
            if (!alive) {
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
        public static final int MAX_RETRY_PER_SERVER = 3;
        private final GraknClient.Cluster clusterClient;
        private Database database;
        private final String dbName;
        private final Type type;
        private final GraknOptions options;
        private final ConcurrentMap<Replica.Id, RPCSession.Core> coreSessions;
        private boolean isOpen;

        public Cluster(GraknClient.Cluster clusterClient, String database, Grakn.Session.Type type, GraknOptions options) {
            this.clusterClient = clusterClient;
            this.dbName = database;
            this.type = type;
            this.options = options;
            this.database = databaseDiscover();
            coreSessions = new ConcurrentHashMap<>();
            isOpen = true;
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type) {
            return transaction(type, new GraknOptions());
        }

        @Override
        public Grakn.Transaction transaction(Grakn.Transaction.Type type, GraknOptions options) {
            switch (type) {
                case READ:
                case WRITE:
                    return transactionPrimaryReplica(type, options);
                case READ_SECONDARY:
                    return transactionSecondaryReplica(options);

                default:
                    throw new GraknClientException(ILLEGAL_ARGUMENT, type);
            }
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

        private Grakn.Transaction transactionPrimaryReplica(Grakn.Transaction.Type type, GraknOptions options) {
            for (Address.Cluster.Server server: clusterClient.servers()) {
                int retry = 0;
                while (retry < MAX_RETRY_PER_SERVER) {
                    try {
                        Core leaderSession = coreSessions.computeIfAbsent(
                                database.leader().id(),
                                key -> {
                                    LOG.debug("Opening a session to primary replica '{}'", key);
                                    GraknClient.Core leaderClient = clusterClient.coreClient(server);
                                    return leaderClient.session(key.database(), this.type, this.options);
                                }
                        );
                        return leaderSession.transaction(type, options);
                    } catch (GraknClientException e) {
                        retry++;
                        if (e.getErrorMessage().equals(CLUSTER_SERVER_NOT_A_LEADER)) {
                            database = databaseDiscover(server);
                        } else if (e.getErrorMessage().equals(CLUSTER_LEADER_NOT_YET_ELECTED)) {
                            sleepWait();
                            database = databaseDiscover(server);
                        } else if (e.getErrorMessage().equals(UNABLE_TO_CONNECT)) {
                            break;
                        } else {
                            throw e;
                        }
                    }
                }
            }

            throw clusterNotAvailableException();
        }

        private Grakn.Transaction transactionSecondaryReplica(GraknOptions options) {
            for (Replica replica: database.replicas()) {
                try {
                    Core selectedSession = coreSessions.computeIfAbsent(
                            replica.id(),
                            key -> {
                                LOG.debug("Opening a session to '{}'", key);
                                return clusterClient.coreClient(key.address()).session(key.database(), this.type, this.options);
                            }
                    );
                    LOG.debug("Opening read secondary transaction to secondary replica '{}'", replica);
                    return selectedSession.transaction(Grakn.Transaction.Type.READ, options);
                } catch (GraknClientException e) {
                    if (e.getErrorMessage().equals(UNABLE_TO_CONNECT)) {
                        // retry to next replica
                    } else {
                        throw e;
                    }
                }
            }
            throw clusterNotAvailableException();
        }

        private Database databaseDiscover() {
            for (Address.Cluster.Server address: clusterClient.servers()) {
                try {
                    return databaseDiscover(address);
                } catch (StatusRuntimeException e) {
                    break;
                }
            }
            throw clusterNotAvailableException();
        }

        private Database databaseDiscover(Address.Cluster.Server server) {
            return Database.ofProto(
                    clusterClient.graknClusterRPC(server).databaseDiscover(
                        DatabaseProto.Database.Discover.Req.newBuilder()
                                .setDatabase(dbName)
                                .build()
                    )
            );
        }

        private GraknClientException clusterNotAvailableException() {
            String[] addresses = clusterClient.servers().stream().map(server -> server.toString()).collect(Collectors.toSet()).toArray(new String[]{});
            return new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, (Object) addresses); // remove ambiguity by casting to Object
        }

        private void sleepWait() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e2) {
                throw new GraknClientException(UNEXPECTED_INTERRUPTION);
            }
        }

        public static class Database {
            private final Map<Replica.Id, Replica> replicas;

            private Database(Map<Replica.Id, Replica> replicas) {
                this.replicas = replicas;
            }

            public static Database ofProto(DatabaseProto.Database.Discover.Res res) {
                Map<Replica.Id, Replica> replicaMap = new HashMap<>();

                for (DatabaseProto.Database.Discover.Res.Replica replica: res.getReplicasList()) {
                    Replica.Id id = new Replica.Id(Address.Cluster.Server.parse(replica.getAddress()), replica.getDatabase());
                    replicaMap.put(id, Replica.ofProto(replica));
                }

                return new Database(replicaMap);
            }

            private Replica leader() {
                Map.Entry<Replica.Id, Replica> initial = replicas.entrySet().iterator().next();
                Map.Entry<Replica.Id, Replica> reduce = replicas.entrySet().stream()
                        .filter(entry -> entry.getValue().isLeader())
                        .reduce(initial, (acc, e) -> e.getValue().term > acc.getValue().term ? e : acc);
                if (reduce.getValue().isLeader()) return reduce.getValue();
                else throw new GraknClientException(CLUSTER_LEADER_NOT_YET_ELECTED, (reduce.getValue().term()));
            }

            public Collection<Replica> replicas() {
                return replicas.values();
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

            public boolean isLeader() {
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
