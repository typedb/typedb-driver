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
import grakn.client.rpc.ClientRPC;
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

public class ClientClusterRPC implements GraknClient.Cluster {
    private static final Logger LOG = LoggerFactory.getLogger(ClientClusterRPC.class);
    private final Map<ServerAddress, ClientRPC> coreClients;
    private final Map<ServerAddress, GraknClusterGrpc.GraknClusterBlockingStub> graknClusterRPCs;
    private final DatabaseManagerClusterRPC databaseManagers;
    private final ConcurrentMap<String, DatabaseClusterRPC> clusterDatabases;
    private boolean isOpen;

    public ClientClusterRPC(String... addresses) {
        coreClients = fetchClusterServers(addresses).stream()
                .map(addr -> pair(addr, new ClientRPC(addr.client())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        graknClusterRPCs = coreClients.entrySet().stream()
                .map(client -> pair(client.getKey(), GraknClusterGrpc.newBlockingStub(client.getValue().channel())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        databaseManagers = new DatabaseManagerClusterRPC(this,
                coreClients.entrySet().stream()
                        .map(client -> pair(client.getKey(), client.getValue().databases()))
                        .collect(Collectors.toMap(Pair::first, Pair::second))
        );
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    @Override
    public SessionClusterRPC session(String database, GraknClient.Session.Type type) {
        return session(database, type, GraknOptions.cluster());
    }

    @Override
    public SessionClusterRPC session(String database, GraknClient.Session.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOptions = options.asCluster();
        if (clusterOptions.readAnyReplica().isPresent() && clusterOptions.readAnyReplica().get()) {
            return sessionAnyReplica(database, type, clusterOptions);
        } else {
            return sessionPrimaryReplica(database, type, clusterOptions);
        }
    }

    private SessionClusterRPC sessionPrimaryReplica(String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runPrimaryReplica();
    }

    private SessionClusterRPC sessionAnyReplica(String database, GraknClient.Session.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runAnyReplica();
    }

    private FailsafeTask<SessionClusterRPC> openSessionFailsafeTask(String database, Session.Type type, GraknOptions.Cluster options, ClientClusterRPC client) {
        return new FailsafeTask<SessionClusterRPC>(this, database) {

            @Override
            SessionClusterRPC run(DatabaseClusterRPC.Replica replica) {
                return new SessionClusterRPC(client, replica.address(), database, type, options);
            }
        };
    }

    @Override
    public DatabaseManagerClusterRPC databases() {
        return databaseManagers;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        coreClients.values().forEach(ClientRPC::close);
        isOpen = false;
    }

    ConcurrentMap<String, DatabaseClusterRPC> clusterDatabases() {
        return clusterDatabases;
    }

    public Set<ServerAddress> clusterMembers() {
        return coreClients.keySet();
    }

    public ClientRPC coreClient(ServerAddress address) {
        return coreClients.get(address);
    }

    public GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC(ServerAddress address) {
        return graknClusterRPCs.get(address);
    }

    private Set<ServerAddress> fetchClusterServers(String... addresses) {
        for (String address : addresses) {
            try (ClientRPC client = new ClientRPC(address)) {
                LOG.debug("Fetching list of cluster servers from {}...", address);
                GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC = GraknClusterGrpc.newBlockingStub(client.channel());
                ClusterProto.Cluster.Servers.Res res =
                        graknClusterRPC.clusterServers(ClusterProto.Cluster.Servers.Req.newBuilder().build());
                Set<ServerAddress> members = res.getServersList().stream().map(ServerAddress::parse).collect(Collectors.toSet());
                LOG.debug("The cluster servers are {}", members);
                return members;
            } catch (StatusRuntimeException e) {
                LOG.error("Fetching cluster servers from {} failed.", address);
            }
        }
        throw new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses));
    }
}
