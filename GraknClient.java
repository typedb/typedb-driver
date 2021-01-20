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
import grakn.protocol.cluster.GraknClusterGrpc;
import grakn.protocol.cluster.ClusterProto;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_NOT_AVAILABLE;
import static grakn.common.collection.Collections.pair;

public class GraknClient {
    public static final String DEFAULT_ADDRESS = "localhost:1729";

    public static GraknClient.Core core() {
        return core(DEFAULT_ADDRESS);
    }

    public static GraknClient.Core core(String address) {
        return new GraknClient.Core(address);
    }

    public static GraknClient.Cluster cluster() {
        return new GraknClient.Cluster(DEFAULT_ADDRESS);
    }

    public static GraknClient.Cluster cluster(String address) {
        return new GraknClient.Cluster(address);
    }

    public static class Core implements Grakn.Client {
        private final ManagedChannel channel;
        private final RPCDatabaseManager.Core databases;

        private Core(String address) {
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
        private final ConcurrentMap<Address.Cluster.Server, Core> coreClientMap;
        private final Core[] coreClientArray;
        private final AtomicInteger selectedCoreClient;
        private GraknClusterGrpc.GraknClusterBlockingStub clusterDiscoveryRPC;
        private final RPCDatabaseManager.Cluster databases;
        private boolean isOpen;

        private Cluster(String address) {
            Pair<GraknClusterGrpc.GraknClusterBlockingStub, Set<Address.Cluster.Server>> discovery = discoverCluster(address);
            clusterDiscoveryRPC = discovery.first();
            coreClientMap = discovery.second().stream()
                    .map(addr -> pair(addr, new Core(addr.client())))
                    .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
            coreClientArray = coreClientMap.values().toArray(new Core[] {});
            selectedCoreClient = new AtomicInteger();
            databases = new RPCDatabaseManager.Cluster(
                    coreClientMap.entrySet().stream()
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

        public GraknClusterGrpc.GraknClusterBlockingStub selectNextClusterDiscoveryRPC() {
            Core selected = coreClientArray[selectedCoreClient.getAndIncrement() % coreClientMap.size()];
            clusterDiscoveryRPC = GraknClusterGrpc.newBlockingStub(selected.channel());
            return clusterDiscoveryRPC;
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
            coreClientMap.values().forEach(GraknClient.Core::close);
            isOpen = false;
        }

        public ConcurrentMap<Address.Cluster.Server, Core> coreClients() {
            return coreClientMap;
        }

        private Pair<GraknClusterGrpc.GraknClusterBlockingStub, Set<Address.Cluster.Server>> discoverCluster(String... addresses) {
            for (String address: addresses) {
                try (GraknClient.Core client = new Core(address)) {
                    LOG.info("Performing server discovery to {}...", address);
                    GraknClusterGrpc.GraknClusterBlockingStub clusterDiscoveryRPC = GraknClusterGrpc.newBlockingStub(client.channel());
                    ClusterProto.Cluster.Discover.Res res =
                            clusterDiscoveryRPC.clusterDiscover(ClusterProto.Cluster.Discover.Req.newBuilder().build());
                    Set<Address.Cluster.Server> servers = res.getServersList().stream().map(Address.Cluster.Server::parse).collect(Collectors.toSet());
                    LOG.info("Discovered {}", servers);
                    return pair(clusterDiscoveryRPC, servers);
                } catch (StatusRuntimeException e) {
                    LOG.error("Server discovery to {} failed.", address);
                }
            }
            throw new GraknClientException(CLUSTER_NOT_AVAILABLE.message((Object) addresses)); // remove ambiguity by casting to Object
        }
    }
}
