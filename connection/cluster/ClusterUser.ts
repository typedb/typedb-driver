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

import { User as UserProto } from "typedb-protocol/cluster/cluster_user_pb";
import { Database } from "../../api/connection/database/Database";
import { User } from "../../api/connection/user/User";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { ClusterUserManager, FailsafeTask } from "../../dependencies_internal";
import { ClusterClient } from "./ClusterClient";

export class ClusterUser implements User {

    private readonly _client: ClusterClient;
    private readonly _username: string;
    private readonly _passwordExpirySeconds: number;

    constructor(client: ClusterClient, username: string, passwordExpirySeconds: number) {
        this._client = client;
        this._username = username;
        this._passwordExpirySeconds = passwordExpirySeconds;
    }

    static of(user: UserProto, client: ClusterClient): ClusterUser {
        switch (user.getPasswordExpiryCase()) {
            case UserProto.PasswordExpiryCase.PASSWORD_EXPIRY_NOT_SET: return new ClusterUser(client, user.getUsername(), null);
            case UserProto.PasswordExpiryCase.PASSWORD_EXPIRY_SECONDS: return new ClusterUser(client, user.getUsername(), user.getPasswordExpirySeconds());
        }
    }

    get passwordExpirySeconds(): number {
        return this._passwordExpirySeconds;
    }

    async passwordUpdate(oldPassword: string, newPassword: string): Promise<void> {
        const failsafeTask = new ClusterUserFailsafeTask(this._client, (replica) => {
            return this._client.stub(replica.address).userPasswordUpdate(RequestBuilder.Cluster.User.passwordUpdateReq(this.username, oldPassword, newPassword));
        });
        await failsafeTask.runPrimaryReplica();
    }

    get username(): string {
        return this._username;
    }
}

class ClusterUserFailsafeTask<T> extends FailsafeTask<T> {

    private readonly _task: (replica: Database.Replica) => Promise<T>;

    constructor(client: ClusterClient, task: (replica: Database.Replica) => Promise<T>) {
        super(client, ClusterUserManager._SYSTEM_DB);
        this._task = task;
    }

    run(replica: Database.Replica): Promise<T> {
        return this._task(replica);
    }
}
