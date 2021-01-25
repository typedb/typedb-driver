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
import grakn.protocol.cluster.ClusterProto;
import grakn.protocol.cluster.GraknClusterGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Client.ILLEGAL_ARGUMENT;
import static grakn.common.collection.Collections.pair;

public class GraknClient {
    public static final String DEFAULT_ADDRESS = "localhost:1729";

    public static Core core() {
        return core(DEFAULT_ADDRESS);
    }

    public static Core core(String address) {
        return new Core(address);
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
            return session(database, type, GraknOptions.core());
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
        private final Map<Address.Cluster.Server, Core> coreClients;
        private final Map<Address.Cluster.Server, GraknClusterGrpc.GraknClusterBlockingStub> graknClusterRPCs;
        private final RPCDatabaseManager.Cluster databases;
        private boolean isOpen;

        private Cluster(String address) {
            coreClients = discoverCluster(address).stream()
                    .map(addr -> pair(addr, new Core(addr.client())))
                    .collect(Collectors.toMap(Pair::first, Pair::second));
            graknClusterRPCs = coreClients.entrySet().stream()
                    .map(client -> pair(client.getKey(), GraknClusterGrpc.newBlockingStub(client.getValue().channel())))
                    .collect(Collectors.toMap(Pair::first, Pair::second));
            databases = new RPCDatabaseManager.Cluster(
                    coreClients.entrySet().stream()
                            .map(client -> pair(client.getKey(), client.getValue().databases()))
                            .collect(Collectors.toMap(Pair::first, Pair::second))
            );
            isOpen = true;
        }

        @Override
        public RPCSession.Cluster session(String database, Grakn.Session.Type type) {
            return session(database, type, GraknOptions.core());
        }

        @Override
        public RPCSession.Cluster session(String database, Grakn.Session.Type type, GraknOptions options) {
            if (!options.isCluster()) throw new GraknClientException(ILLEGAL_ARGUMENT, options);
            return new RPCSession.Cluster(this, database, type, options.asCluster());
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
            coreClients.values().forEach(GraknClient.Core::close);
            isOpen = false;
        }

        public Set<Address.Cluster.Server> servers() {
            return coreClients.keySet();
        }

        public Core coreClient(Address.Cluster.Server address) {
            return coreClients.get(address);
        }

        public GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC(Address.Cluster.Server address) {
            return graknClusterRPCs.get(address);
        }

        private Set<Address.Cluster.Server> discoverCluster(String... addresses) {
            for (String address: addresses) {
                try (Core client = new Core(address)) {
                    LOG.debug("Performing server discovery to {}...", address);
                    GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC = GraknClusterGrpc.newBlockingStub(client.channel());
                    ClusterProto.Cluster.Discover.Res res =
                            graknClusterRPC.clusterDiscover(ClusterProto.Cluster.Discover.Req.newBuilder().build());
                    Set<Address.Cluster.Server> servers = res.getServersList().stream().map(Address.Cluster.Server::parse).collect(Collectors.toSet());
                    LOG.debug("Discovered {}", servers);
                    return servers;
                } catch (StatusRuntimeException e) {
                    LOG.error("Server discovery to {} failed.", address);
                }
            }
            throw clusterNotAvailableException();
        }

        private GraknClientException clusterNotAvailableException() {
            String addresses = servers().stream().map(Address.Cluster.Server::toString).collect(Collectors.joining(","));
            return new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, addresses); // remove ambiguity by casting to Object
        }
    }
}
