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

import { Database } from "../../api/connection/database/Database";
import { User } from "../../api/connection/user/User";
import { UserManager } from "../../api/connection/user/UserManager";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { ClusterUser, FailsafeTask } from "../../dependencies_internal";
import { ClusterClient } from "./ClusterClient";

export class ClusterUserManager implements UserManager {

    static _SYSTEM_DB = "_system";
    private readonly _client: ClusterClient;

    constructor(client: ClusterClient) {
        this._client = client;
    }

    async all(): Promise<User[]> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersAll(RequestBuilder.Cluster.UserManager.allReq())
                .then((res) => {
                    return res.getUsersList().map(user => ClusterUser.of(user, this._client));
                });
        });
        return await failsafeTask.runPrimaryReplica();
    }

    async contains(username: string): Promise<boolean> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersContains(RequestBuilder.Cluster.UserManager.containsReq(username))
        })
        return await failsafeTask.runPrimaryReplica();
    }

    async create(username: string, password: string): Promise<void> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersCreate(RequestBuilder.Cluster.UserManager.createReq(username, password))
        })
        await failsafeTask.runPrimaryReplica();
    }

    async delete(username: string): Promise<void> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersDelete(RequestBuilder.Cluster.UserManager.deleteReq(username))
        })
        await failsafeTask.runPrimaryReplica();
    }

    async get(username: string): Promise<ClusterUser> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersGet(RequestBuilder.Cluster.UserManager.getReq(username))
        })
        return ClusterUser.of((await failsafeTask.runPrimaryReplica()).getUser(), this._client);
    }

    async passwordSet(username: string, password: string): Promise<void> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address).usersPasswordSet(RequestBuilder.Cluster.UserManager.passwordSetReq(username, password))
        })
        await failsafeTask.runPrimaryReplica();
    }
}

class UserManagerFailsafeTask<T> extends FailsafeTask<T> {

    private readonly _task: (replica: Database.Replica) => Promise<T>;

    constructor(client: ClusterClient, task: (replica: Database.Replica) => Promise<T>) {
        super(client, ClusterUserManager._SYSTEM_DB);
        this._task = task;
    }

    async run(replica: Database.Replica): Promise<T> {
        return await this._task(replica);
    }
}
