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

package grakn.client.cluster;

import grakn.client.api.GraknClient;
import grakn.client.api.GraknOptions;
import grakn.client.api.GraknSession;
import grakn.client.common.exception.GraknClientException;
import grakn.client.common.rpc.GraknStub;
import grakn.common.collection.Pair;
import grakn.protocol.ClusterServerProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static grakn.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static grakn.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static grakn.client.common.rpc.RequestBuilder.Cluster.ServerManager.allReq;
import static grakn.common.collection.Collections.pair;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ClusterClient implements GraknClient.Cluster {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterClient.class);

    private final boolean tlsEnabled;
    @Nullable
    private final Path tlsRootCA;
    private final int parallelisation;
    private final Map<String, ClusterNodeClient> clusterNodeClients;
    private final Map<String, GraknStub.Cluster> stubs;
    private final ClusterDatabaseManager databaseMgrs;
    private final ConcurrentMap<String, ClusterDatabase> clusterDatabases;
    private boolean isOpen;

    ClusterClient(Set<String> addresses, boolean tlsEnabled, @Nullable String tlsRootCA) {
        this(addresses, tlsEnabled, tlsRootCA, ClusterNodeClient.calculateParallelisation());
    }

    ClusterClient(Set<String> addresses, boolean tlsEnabled, @Nullable String tlsRootCA, int parallelisation) {
        this.tlsEnabled = tlsEnabled;
        this.tlsRootCA = tlsRootCA != null ? Paths.get(tlsRootCA) : null;
        this.parallelisation = parallelisation;
        clusterNodeClients = fetchServerAddresses(addresses).stream()
                .map(address -> pair(address, ClusterNodeClient.create(address, tlsEnabled, this.tlsRootCA, parallelisation)))
                .collect(toMap(Pair::first, Pair::second));
        stubs = clusterNodeClients.entrySet().stream()
                .map(client -> pair(client.getKey(), GraknStub.cluster(client.getValue().channel())))
                .collect(toMap(Pair::first, Pair::second));
        databaseMgrs = new ClusterDatabaseManager(this);
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    public static ClusterClient create(Set<String> addresses, boolean tlsEnabled, @Nullable String tlsRootCA) {
        return new ClusterClient(addresses, tlsEnabled, tlsRootCA);
    }

    public static ClusterClient create(Set<String> addresses, boolean tlsEnabled, @Nullable String tlsRootCA, int parallelisation) {
        return new ClusterClient(addresses, tlsEnabled, tlsRootCA, parallelisation);
    }

    private Set<String> fetchServerAddresses(Set<String> addresses) {
        for (String address : addresses) {
            try (ClusterNodeClient client = ClusterNodeClient.create(address, tlsEnabled, tlsRootCA, parallelisation)) {
                LOG.debug("Fetching list of cluster servers from {}...", address);
                GraknStub.Cluster stub = GraknStub.cluster(client.channel());
                ClusterServerProto.ServerManager.All.Res res = stub.serversAll(allReq());
                Set<String> members = res.getServersList().stream().map(ClusterServerProto.Server::getAddress).collect(toSet());
                LOG.debug("The cluster servers are {}", members);
                return members;
            } catch (GraknClientException e) {
                if (e.getErrorMessage().equals(UNABLE_TO_CONNECT)) {
                    LOG.error("Fetching cluster servers from {} failed.", address);
                } else {
                    throw e;
                }
            }
        }
        throw new GraknClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses));
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public ClusterDatabaseManager databases() {
        return databaseMgrs;
    }

    @Override
    public ClusterSession session(String database, GraknSession.Type type) {
        return session(database, type, GraknOptions.cluster());
    }

    @Override
    public ClusterSession session(String database, GraknSession.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOptions = options.asCluster();
        if (clusterOptions.readAnyReplica().isPresent() && clusterOptions.readAnyReplica().get()) {
            return sessionAnyReplica(database, type, clusterOptions);
        } else {
            return sessionPrimaryReplica(database, type, clusterOptions);
        }
    }

    private ClusterSession sessionPrimaryReplica(String database, GraknSession.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runPrimaryReplica();
    }

    private ClusterSession sessionAnyReplica(String database, GraknSession.Type type, GraknOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runAnyReplica();
    }

    private FailsafeTask<ClusterSession> openSessionFailsafeTask(
            String database, GraknSession.Type type, GraknOptions.Cluster options, ClusterClient client) {
        return new FailsafeTask<ClusterSession>(this, database) {
            @Override
            ClusterSession run(ClusterDatabase.Replica replica) {
                return new ClusterSession(client, replica.address(), database, type, options);
            }
        };
    }

    // TODO: this is not good - we should not pass an internal object to be modified outside of this class
    ConcurrentMap<String, ClusterDatabase> databaseByName() {
        return clusterDatabases;
    }

    Map<String, ClusterNodeClient> clusterNodeClients() {
        return clusterNodeClients;
    }

    Set<String> clusterNodes() {
        return clusterNodeClients.keySet();
    }

    ClusterNodeClient clusterNodeClient(String address) {
        return clusterNodeClients.get(address);
    }

    GraknStub.Cluster stub(String address) {
        return stubs.get(address);
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public Cluster asCluster() {
        return this;
    }

    @Override
    public void close() {
        clusterNodeClients.values().forEach(ClusterNodeClient::close);
        isOpen = false;
    }
}
