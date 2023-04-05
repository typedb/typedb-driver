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
import com.vaticle.typedb.protocol.ClusterUserProto;

import java.util.Optional;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Cluster.User.passwordUpdateReq;
import static com.vaticle.typedb.client.connection.cluster.ClusterUserManager.SYSTEM_DB;

public class ClusterUser implements User {

    private final ClusterClient client;
    private final String username;
    private final Optional<Long> passwordExpirySeconds;

    public ClusterUser(ClusterClient client, String username, Optional<Long> passwordExpirySeconds) {
        this.client = client;
        this.username = username;
        this.passwordExpirySeconds = passwordExpirySeconds;
    }

    public static ClusterUser of(ClusterUserProto.User user, ClusterClient client) {
        if (user.getPasswordExpiryCase() ==
                ClusterUserProto.User.PasswordExpiryCase.PASSWORDEXPIRY_NOT_SET) {
            return new ClusterUser(client, user.getUsername(), Optional.empty());
        }
        else {
            return new ClusterUser(client, user.getUsername(), Optional.of(user.getPasswordExpirySeconds()));
        }
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public Optional<Long> passwordExpirySeconds() {
        return passwordExpirySeconds;
    }

    @Override
    public void passwordUpdate(String passwordOld, String passwordNew) {
        ClusterClient.FailsafeTask<Void> failsafeTask = client.createFailsafeTask(
                SYSTEM_DB,
                parameter -> {
                    parameter.client().stub().userPasswordUpdate(passwordUpdateReq(username, passwordOld, passwordNew));
                    return null;
                }
        );
        failsafeTask.runPrimaryReplica();
    }
}
