/*
 * Copyright (C) 2021 Vaticle
 *
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

package com.vaticle.typedb.client.connection.cluster;

import com.vaticle.typedb.client.api.connection.TypeDBClient;
import com.vaticle.typedb.client.api.connection.TypeDBCredential;
import com.vaticle.typedb.client.api.connection.TypeDBOptions;
import com.vaticle.typedb.client.api.connection.TypeDBSession;
import com.vaticle.typedb.client.api.connection.user.UserManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.common.collection.Pair;
import com.vaticle.typedb.protocol.ClusterServerProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.ServerManager.allReq;
import static com.vaticle.typedb.common.collection.Collections.pair;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ClusterClient implements TypeDBClient.Cluster {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterClient.class);

    private final TypeDBCredential credential;
    private final int parallelisation;
    private final Map<String, ClusterServerClient> clusterServerClients;
    private final Map<String, ClusterServerStub> stubs;
    private final ClusterUserManager userMgr;
    private final ClusterDatabaseManager databaseMgr;
    private final ConcurrentMap<String, ClusterDatabase> clusterDatabases;
    private boolean isOpen;

    private ClusterClient(Set<String> addresses, TypeDBCredential credential, int parallelisation) {
        this.credential = credential;
        this.parallelisation = parallelisation;
        clusterServerClients = fetchServerAddresses(addresses).stream()
                .map(address -> pair(address, ClusterServerClient.create(address, credential, parallelisation)))
                .collect(toMap(Pair::first, Pair::second));
        stubs = clusterServerClients.entrySet().stream()
                .map(client -> pair(client.getKey(), ClusterServerStub.create(credential.username(), credential.password(), client.getValue().channel())))
                .collect(toMap(Pair::first, Pair::second));
        userMgr = new ClusterUserManager(this);
        databaseMgr = new ClusterDatabaseManager(this);
        clusterDatabases = new ConcurrentHashMap<>();
        isOpen = true;
    }

    public static Cluster create(Set<String> addresses, TypeDBCredential credential) {
        return new ClusterClient(addresses, credential, ClusterServerClient.calculateParallelisation());
    }

    public static Cluster create(Set<String> addresses, TypeDBCredential credential, int parallelisation) {
        return new ClusterClient(addresses, credential, parallelisation);
    }

    private Set<String> fetchServerAddresses(Set<String> addresses) {
        for (String address : addresses) {
            try (ClusterServerClient client = ClusterServerClient.create(address, credential, parallelisation)) {
                LOG.debug("Fetching list of cluster servers from {}...", address);
                ClusterServerStub stub = ClusterServerStub.create(credential.username(), credential.password(), client.channel());
                ClusterServerProto.ServerManager.All.Res res = stub.serversAll(allReq());
                Set<String> members = res.getServersList().stream().map(ClusterServerProto.Server::getAddress).collect(toSet());
                LOG.debug("The cluster servers are {}", members);
                return members;
            } catch (TypeDBClientException e) {
                if (UNABLE_TO_CONNECT.equals(e.getErrorMessage())) {
                    LOG.error("Fetching cluster servers from {} failed.", address);
                } else {
                    throw e;
                }
            }
        }
        throw new TypeDBClientException(CLUSTER_UNABLE_TO_CONNECT, String.join(",", addresses));
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public UserManager users() {
        return userMgr;
    }

    @Override
    public ClusterDatabaseManager databases() {
        return databaseMgr;
    }

    @Override
    public ClusterSession session(String database, TypeDBSession.Type type) {
        return session(database, type, TypeDBOptions.cluster());
    }

    @Override
    public ClusterSession session(String database, TypeDBSession.Type type, TypeDBOptions options) {
        TypeDBOptions.Cluster clusterOptions = options.asCluster();
        if (clusterOptions.readAnyReplica().isPresent() && clusterOptions.readAnyReplica().get()) {
            return sessionAnyReplica(database, type, clusterOptions);
        } else {
            return sessionPrimaryReplica(database, type, clusterOptions);
        }
    }

    private ClusterSession sessionPrimaryReplica(String database, TypeDBSession.Type type, TypeDBOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runPrimaryReplica();
    }

    private ClusterSession sessionAnyReplica(String database, TypeDBSession.Type type, TypeDBOptions.Cluster options) {
        return openSessionFailsafeTask(database, type, options, this).runAnyReplica();
    }

    private FailsafeTask<ClusterSession> openSessionFailsafeTask(
            String database, TypeDBSession.Type type, TypeDBOptions.Cluster options, ClusterClient client) {
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

    Map<String, ClusterServerClient> clusterServerClients() {
        return clusterServerClients;
    }

    Set<String> clusterServers() {
        return clusterServerClients.keySet();
    }

    ClusterServerClient clusterServerClient(String address) {
        return clusterServerClients.get(address);
    }

    ClusterServerStub stub(String address) {
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
        clusterServerClients.values().forEach(ClusterServerClient::close);
        isOpen = false;
    }
}
