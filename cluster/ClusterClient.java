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
import grakn.client.core.CoreClient;
import grakn.common.collection.Pair;
import grakn.protocol.ClusterServerProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Map<String, CoreClient> coreClients;
    private final Map<String, GraknStub.Cluster> stubs;
    private final ClusterDatabaseManager databaseMgrs;
    private final ConcurrentMap<String, ClusterDatabase> clusterDatabases;
    private boolean isOpen;

    public ClusterClient(Set<String> addresses) {
        this(addresses, CoreClient.calculateParallelisation());
    }

    public ClusterClient(Set<String> addresses, int parallelisation) {
        coreClients = fetchServerAddresses(addresses).stream()
                .map(address -> pair(address, new CoreClient(address, parallelisation)))
                .collect(toMap(Pair::first, Pair::second));
        stubs = coreClients.entrySet().stream()
                .map(client -> pair(client.getKey(), GraknStub.cluster(client.getValue().channel())))
                .collect(toMap(Pair::first, Pair::second));
        databaseMgrs = new ClusterDatabaseManager(this);
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    private Set<String> fetchServerAddresses(Set<String> addresses) {
        for (String address : addresses) {
            try (CoreClient client = new CoreClient(address)) {
                LOG.debug("Fetching list of cluster servers from {}...", address);
                GraknStub.Cluster stub = GraknStub.cluster(client.channel());
                ClusterServerProto.ServerManager.All.Res res = stub.serversAll(allReq());
                Set<String> members = res.getServersList().stream().map(ClusterServerProto.Server::getAddress).collect(toSet());
                LOG.debug("The cluster servers are {}", members);
                return members;
            } catch (GraknClientException e) {
                if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
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

    Map<String, CoreClient> coreClients() {
        return coreClients;
    }

    Set<String> clusterMembers() {
        return coreClients.keySet();
    }

    CoreClient coreClient(String address) {
        return coreClients.get(address);
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
        coreClients.values().forEach(CoreClient::close);
        isOpen = false;
    }
}
