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

import grakn.client.Grakn;
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
import java.util.stream.Collectors;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.common.collection.Collections.pair;

public class RPCClientCluster implements Grakn.Client.Cluster {
    private static final Logger LOG = LoggerFactory.getLogger(RPCClientCluster.class);
    private final Map<Address.Server, RPCClient> coreClients;
    private final Map<Address.Server, GraknClusterGrpc.GraknClusterBlockingStub> graknClusterRPCs;
    private final RPCDatabaseManagerCluster databases;
    private boolean isOpen;

    public RPCClientCluster(String address) {
        coreClients = discoverCluster(address).stream()
                .map(addr -> pair(addr, new RPCClient(addr.client())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        graknClusterRPCs = coreClients.entrySet().stream()
                .map(client -> pair(client.getKey(), GraknClusterGrpc.newBlockingStub(client.getValue().channel())))
                .collect(Collectors.toMap(Pair::first, Pair::second));
        databases = new RPCDatabaseManagerCluster(
                coreClients.entrySet().stream()
                        .map(client -> pair(client.getKey(), client.getValue().databases()))
                        .collect(Collectors.toMap(Pair::first, Pair::second))
        );
        isOpen = true;
    }

    @Override
    public RPCSessionCluster session(String database, Grakn.Session.Type type) {
        return session(database, type, GraknOptions.cluster());
    }

    @Override
    public RPCSessionCluster session(String database, Grakn.Session.Type type, GraknOptions.Cluster options) {
        return new RPCSessionCluster(this, database, type, options);
    }

    @Override
    public RPCDatabaseManagerCluster databases() {
        return databases;
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

    public Set<Address.Server> clusterMembers() {
        return coreClients.keySet();
    }

    public RPCClient coreClient(Address.Server address) {
        return coreClients.get(address);
    }

    public GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC(Address.Server address) {
        return graknClusterRPCs.get(address);
    }

    private Set<Address.Server> discoverCluster(String... addresses) {
        for (String address: addresses) {
            try (RPCClient client = new RPCClient(address)) {
                LOG.debug("Performing cluster discovery to {}...", address);
                GraknClusterGrpc.GraknClusterBlockingStub graknClusterRPC = GraknClusterGrpc.newBlockingStub(client.channel());
                ClusterProto.Cluster.Discover.Res res =
                        graknClusterRPC.clusterDiscover(ClusterProto.Cluster.Discover.Req.newBuilder().build());
                Set<Address.Server> members = res.getServersList().stream().map(Address.Server::parse).collect(Collectors.toSet());
                LOG.debug("Discovered {}", members);
                return members;
            } catch (StatusRuntimeException e) {
                LOG.error("Cluster discovery to {} failed.", address);
            }
        }
        throw clusterNotAvailableException(addresses);
    }

    private GraknClientException clusterNotAvailableException(String... addresses) {
        return new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses)); // remove ambiguity by casting to Object
    }
}
