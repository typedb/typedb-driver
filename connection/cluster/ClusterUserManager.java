/*
 * Copyright (C) 2022 Vaticle
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

import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.client.api.user.UserManager;

import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.containsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.createReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.deleteReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.allReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.passwordSetReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.UserManager.getReq;


public class ClusterUserManager implements UserManager {

    static final String SYSTEM_DB = "_system";

    private final ClusterClient client;

    public ClusterUserManager(ClusterClient client) {
        this.client = client;
    }

    @Override
    public boolean contains(String username) {
        ClusterClient.FailsafeTask<Boolean> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                (parameter) ->
                        parameter.client().stub().usersContains(containsReq(username)).getContains()
        );

        return failsafeTask.runPrimaryReplica();
    }

    @Override
    public void create(String username, String password) {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                (parameter) -> {
                    parameter.client().stub().usersCreate(createReq(username, password));
                    return null;
                }
        );

        failsafeTask.runPrimaryReplica();
    }

    @Override
    public void delete(String username) {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                parameter -> {
                    parameter.client().stub().usersDelete(deleteReq(username));
                    return null;
                }
        );
        failsafeTask.runPrimaryReplica();
    }

    @Override
    public Set<User> all() {
        ClusterClient.FailsafeTask<Set<User>> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                (parameter) ->
                        parameter.client().stub()
                                .usersAll(allReq()).getUsersList().stream()
                                .map(user -> new ClusterUser(client, user.getUsername(), user.getPasswordExpiryDays()))
                                .collect(Collectors.toSet())
        );
        return failsafeTask.runPrimaryReplica();
    }

    @Override
    public User get(String username) {
        ClusterClient.FailsafeTask<User> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                (parameter) -> {
                    com.vaticle.typedb.protocol.ClusterUserProto.User user = parameter.client().stub().usersGet(getReq(username)).getUser();
                    return new ClusterUser(client, user.getUsername(), user.getPasswordExpiryDays());
                }
        );
        return failsafeTask.runPrimaryReplica();
    }

    @Override
    public void passwordSet(String username, String password) {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                (parameter) -> {
                    parameter.client().stub().usersPasswordSet(passwordSetReq(username, password));
                    return null;
                }
        );
        failsafeTask.runPrimaryReplica();
    }
}
