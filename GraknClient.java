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

package grakn.client;

import grakn.client.common.exception.GraknClientException;
import grakn.client.rpc.Address;
import grakn.client.rpc.RPCDatabaseManager;
import grakn.client.rpc.RPCSession;
import grakn.common.collection.Pair;
import grakn.protocol.cluster.ClusterGrpc;
import grakn.protocol.cluster.ClusterProto;
import grakn.protocol.cluster.DiscoveryProto;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_NOT_AVAILABLE;
import static grakn.common.collection.Collections.pair;

public class GraknClient {
    public static final String DEFAULT_URI = "localhost:1729";

    public static class Core implements Grakn.Client {
        private final ManagedChannel channel;
        private final RPCDatabaseManager.Core databases;

        // TODO:
        //  it is inevitable that the code will have to change when switching from Core to Cluster.
        //  therefore we just have to minimise it but not aim to forcefully reduce it to 0
        // with this argument, adding a static create method GraknClient.core(addr) and GraknClient.cluster(user, pass, addr...) makes sense
        public Core() {
            this(DEFAULT_URI);
        }

        public Core(String address) {
            channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
            databases = new RPCDatabaseManager.Core(channel);
        }

        @Override
        public RPCSession.Core session(String database, Grakn.Session.Type type) {
            return session(database, type, new GraknOptions());
        }

        @Override
        public RPCSession.Core session(String database, Grakn.Session.Type type, GraknOptions options) {
            return new RPCSession.Core(this, database, type, options);
        }

        @Override
        public RPCDatabaseManager.Core databases() {
            return databases;
        }

        @Override
        public boolean isOpen() {
            return !channel.isShutdown();
        }

        @Override
        public void close() {
            try {
                channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public Channel channel() {
            return channel;
        }
    }

    public static class Cluster implements Grakn.Client {
        private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);
        private final AtomicInteger selector;
        private ClusterGrpc.ClusterBlockingStub clusterDiscoveryRPC;
        private final ConcurrentMap<Address.Cluster, Core> clients;
        private final RPCDatabaseManager.Cluster databases;
        private boolean isOpen;

        public Cluster() {
            this(DEFAULT_URI);
        }

        public Cluster(String address) {
            selector = new AtomicInteger();
            Pair<ClusterGrpc.ClusterBlockingStub, Set<Address.Cluster>> cluster = discoverCluster(address);
            clusterDiscoveryRPC = cluster.first();
            clients = cluster.second().stream()
                    .map(addr -> pair(addr, new Core(addr.client())))
                    .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
            databases = new RPCDatabaseManager.Cluster(
                    clients.entrySet().stream()
                            .map(client -> pair(client.getKey(), client.getValue().databases()))
                            .collect(Collectors.toConcurrentMap(Pair::first, Pair::second))
            );
            isOpen = true;
        }

        @Override
        public RPCSession.Cluster session(String database, Grakn.Session.Type type) {
            return session(database, type, new GraknOptions());
        }

        @Override
        public RPCSession.Cluster session(String database, Grakn.Session.Type type, GraknOptions options) {
            return new RPCSession.Cluster(this, database, type, options, clusterDiscoveryRPC);
        }

        public ClusterGrpc.ClusterBlockingStub selectNextClusterDiscoveryRPC() {
            Core selected = selectNextClient();
            clusterDiscoveryRPC = ClusterGrpc.newBlockingStub(selected.channel());
            return clusterDiscoveryRPC;
        }

        private Core selectNextClient() {
            int index = selector.getAndIncrement() % clients.size();
            return new ArrayList<>(clients.values()).get(index);
        }

        @Override
        public RPCDatabaseManager.Cluster databases() {
            return databases;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() {
            clients.values().forEach(GraknClient.Core::close);
            isOpen = false;
        }

        public ConcurrentMap<Address.Cluster, Core> clients() {
            return clients;
        }

        private Pair<ClusterGrpc.ClusterBlockingStub, Set<Address.Cluster>> discoverCluster(String... addresses) {
            for (String address: addresses) {
                try {
                    LOG.info("Performing server discovery to {}...", address);
                    ClusterGrpc.ClusterBlockingStub clusterDiscoveryRPC = bootstrapClusterDiscoveryRPC(address);
                    DiscoveryProto.Discovery.Cluster.Res res =
                            clusterDiscoveryRPC.clusterDiscovery(DiscoveryProto.Discovery.Cluster.Req.newBuilder().build());
                    Set<Address.Cluster> servers = res.getServersList().stream().map(Address.Cluster::parse).collect(Collectors.toSet());
                    LOG.info("Discovered {}", servers);
                    return pair(clusterDiscoveryRPC, servers);
                } catch (StatusRuntimeException e) {
                    LOG.error("Server discovery to {} failed.", address);
                }
            }
            throw new GraknClientException(CLUSTER_NOT_AVAILABLE.message((Object) addresses)); // remove ambiguity by casting to Object
        }

        private ClusterGrpc.ClusterBlockingStub bootstrapClusterDiscoveryRPC(String bootstrapAddress) {
            GraknClient.Core client = new Core(bootstrapAddress);
            return ClusterGrpc.newBlockingStub(client.channel());
        }
    }
}
