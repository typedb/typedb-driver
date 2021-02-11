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
import grakn.client.rpc.RPCClient;
import grakn.common.collection.Pair;
import grakn.protocol.cluster.ClusterProto;
import grakn.protocol.cluster.GraknClusterGrpc;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.common.collection.Collections.pair;

public class RPCGraknClientCluster implements GraknClient {
    private static final Logger LOG = LoggerFactory.getLogger(RPCGraknClientCluster.class);
    private final Map<ServerAddress, RPCClient> coreClients;
    private final Map<ServerAddress, GraknClusterGrpc.GraknClusterBlockingStub> graknClusterRPCs;
    private final RPCClusterDatabaseManager databaseManagers;
    private final ConcurrentMap<String, RPCClusterDatabase> clusterDatabases;
    private boolean isOpen;

    public RPCGraknClientCluster(String... addresses) {
        coreClients = discoverCluster(addresses).stream()
                .map(addr -> pair(addr, new RPCClient(addr.client())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        graknClusterRPCs = coreClients.entrySet().stream()
                .map(client -> pair(client.getKey(), GraknClusterGrpc.newBlockingStub(client.getValue().channel())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        databaseManagers = new RPCClusterDatabaseManager(
                coreClients.entrySet().stream()
                        .map(client -> pair(client.getKey(), client.getValue().databases()))
                        .collect(Collectors.toMap(Pair::first, Pair::second))
        );
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    @Override
    public RPCClusterSession session(String database, GraknClient.Session.Type type) {
        return session(database, type, GraknOptions.cluster());
    }

    @Override
    public RPCClusterSession session(String database, GraknClient.Session.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOptions = options.asCluster();
        if (clusterOptions.readAnyReplica().isPresent() && clusterOptions.readAnyReplica().get()) {
            return sessionSecondaryReplica(database, type, clusterOptions);
        } else {
            return sessionPrimaryReplica(database, type, clusterOptions);
        }
    }

    private RPCClusterSession sessionPrimaryReplica(String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runPrimaryReplica(database);
    }

    private RPCClusterSession sessionSecondaryReplica(String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runSecondaryReplica(database);
    }

    private FailsafeTask<RPCClusterSession> openSessionFailsafeTask(String database, Session.Type type, GraknOptions.Cluster options, RPCGraknClientCluster client) {
        return new FailsafeTask<RPCClusterSession>(this) {

            @Override
            RPCClusterSession run(RPCClusterDatabase.Replica replica) {
                return new RPCClusterSession(client, replica.address(), database, type, options);
            }
        };
    }

    @Override
    public RPCClusterDatabaseManager databases() {
        return databaseManagers;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        coreClients.values().forEach(RPCClient::close);
        isOpen = false;
    }

    ConcurrentMap<String, RPCClusterDatabase> clusterDatabases() {
        return clusterDatabases;
    }

    public Set<ServerAddress> clusterMembers() {
        return coreClients.keySet();
    }

    public RPCClient coreClient(ServerAddress address) {
        return coreClients.get(address);
    }

    public GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC(ServerAddress address) {
        return graknClusterRPCs.get(address);
    }

    private Set<ServerAddress> discoverCluster(String... addresses) {
        for (String address : addresses) {
            try (RPCClient client = new RPCClient(address)) {
                LOG.debug("Performing cluster discovery to {}...", address);
                GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC = GraknClusterGrpc.newBlockingStub(client.channel());
                ClusterProto.Cluster.Servers.Res res =
                        graknClusterRPC.clusterServers(ClusterProto.Cluster.Servers.Req.newBuilder().build());
                Set<ServerAddress> members = res.getServersList().stream().map(ServerAddress::parse).collect(Collectors.toSet());
                LOG.debug("Discovered {}", members);
                return members;
            } catch (StatusRuntimeException e) {
                LOG.error("Cluster discovery to {} failed.", address);
            }
        }
        throw new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses));
    }
}
