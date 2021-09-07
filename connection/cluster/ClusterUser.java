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

import com.vaticle.typedb.client.api.connection.user.User;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.User.deleteReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.User.passwordReq;
import static com.vaticle.typedb.client.connection.cluster.ClusterUserManager.SYSTEM_DB;

public class ClusterUser implements User {

    private final ClusterClient client;
    private final String username;

    public ClusterUser(ClusterClient client, String username) {
        this.client = client;
        this.username = username;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public void password(String password) {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                parameter -> {
                    parameter.client().stub().userPassword(passwordReq(username, password));
                    return null;
                }
        );
        failsafeTask.runPrimaryReplica();
    }

    @Override
    public void delete() {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                parameter -> {
                    parameter.client().stub().userDelete(deleteReq(username));
                    return null;
                }
        );

        failsafeTask.runPrimaryReplica();
    }
}
