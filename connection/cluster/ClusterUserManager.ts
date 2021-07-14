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

import { Database } from "../../api/connection/database/Database";
import { User } from "../../api/connection/user/User";
import { UserManager } from "../../api/connection/user/UserManager";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { ClusterUser, FailsafeTask } from "../../dependencies_internal";
import { ClusterClient } from "./ClusterClient";
import CLUSTER_USER_DOES_NOT_EXIST = ErrorMessage.Client.CLUSTER_USER_DOES_NOT_EXIST;

export class ClusterUserManager implements UserManager {

    static _SYSTEM_DB = "_system";
    private readonly _client: ClusterClient;

    constructor(client: ClusterClient) {
        this._client = client;
    }

    all(): Promise<User[]> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address()).usersAll(RequestBuilder.Cluster.UserManager.allReq())
                .then((res) => {
                    return res.getNamesList().map(name => new ClusterUser(this._client, name));
                });
        });
        return failsafeTask.runPrimaryReplica();
    }

    contains(name: string): Promise<boolean> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address()).usersContains(RequestBuilder.Cluster.UserManager.containsReq(name))
        })
        return failsafeTask.runPrimaryReplica();
    }

    create(name: string, password: string): Promise<void> {
        const failsafeTask = new UserManagerFailsafeTask(this._client, (replica: Database.Replica) => {
            return this._client.stub(replica.address()).userCreate(RequestBuilder.Cluster.UserManager.createReq(name, password))
        })
        return failsafeTask.runPrimaryReplica();
    }

    async get(name: string): Promise<User> {
        if (await this.contains(name)) return new ClusterUser(this._client, name);
        else throw new TypeDBClientError(CLUSTER_USER_DOES_NOT_EXIST.message(name));
    }

}

class UserManagerFailsafeTask<T> extends FailsafeTask<T> {

    private readonly _task: (replica: Database.Replica) => Promise<T>;

    constructor(client: ClusterClient, task: (replica: Database.Replica) => Promise<T>) {
        super(client, ClusterUserManager._SYSTEM_DB);
        this._task = task;
    }

    run(replica: Database.Replica): Promise<T> {
        return this._task(replica);
    }

}
