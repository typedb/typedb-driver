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

import {CallCredentials, ChannelCredentials, credentials, Metadata} from "@grpc/grpc-js";
import * as fs from "fs";
import {ClusterDatabaseManager} from "typedb-protocol/cluster/cluster_database_pb";
import {ServerManager} from "typedb-protocol/cluster/cluster_server_pb";
import {TypeDBClusterClient} from "typedb-protocol/cluster/cluster_service_grpc_pb";
import {ClusterUser, ClusterUserManager} from "typedb-protocol/cluster/cluster_user_pb";
import {TypeDBClient} from "typedb-protocol/core/core_service_grpc_pb";
import {TypeDBCredential} from "../../api/connection/TypeDBCredential";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {TypeDBStub} from "../../common/rpc/TypeDBStub";

export class ClusterServerStub extends TypeDBStub {

    private readonly _stub: TypeDBClient;
    private readonly _clusterStub: TypeDBClusterClient;

    constructor(address: string, credential: TypeDBCredential) {
        super();
        const stubCredentials = this.createChannelCredentials(credential);
        this._stub = new TypeDBClient(address, stubCredentials);
        this._clusterStub = new TypeDBClusterClient(address, stubCredentials);
    }

    private createChannelCredentials(credential: TypeDBCredential): ChannelCredentials {
        const callCreds = this.createCallCredentials(credential);
        if (credential.tlsRootCAPath != null) {
            const rootCert = fs.readFileSync(credential.tlsRootCAPath);
            return credentials.combineChannelCredentials(ChannelCredentials.createSsl(rootCert), callCreds);
        } else {
            return credentials.combineChannelCredentials(ChannelCredentials.createSsl(), callCreds);
        }
    }

    private createCallCredentials(credential: TypeDBCredential): CallCredentials {
        const metaCallback = (_params: any, callback: any) => {
            const meta = new Metadata();
            meta.add('username', credential.username);
            meta.add('password', credential.password);
            callback(null, meta);
        }
        return CallCredentials.createFromMetadataGenerator(metaCallback);
    }

    serversAll(req: ServerManager.All.Req): Promise<ServerManager.All.Res> {
        return new Promise<ServerManager.All.Res>((resolve, reject) => {
            this._clusterStub.servers_all(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            });
        });
    }

    usersAll(req: ClusterUserManager.All.Req): Promise<ClusterUserManager.All.Res> {
        return new Promise<ClusterUserManager.All.Res>((resolve, reject) => {
            this._clusterStub.users_all(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            });
        });
    }

    usersContains(req: ClusterUserManager.Contains.Req): Promise<boolean> {
        return new Promise<boolean>((resolve, reject) => {
            this._clusterStub.users_contains(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res.getContains());
            })
        });
    }

    userCreate(req: ClusterUserManager.Create.Req): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            this._clusterStub.users_create(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve();
            })
        });
    }

    userPassword(req: ClusterUser.Password.Req): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            this._clusterStub.user_password(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve();
            })
        });
    }

    userDelete(req: ClusterUser.Delete.Req): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            this._clusterStub.user_delete(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve();
            })
        });
    }

    databasesClusterGet(req: ClusterDatabaseManager.Get.Req): Promise<ClusterDatabaseManager.Get.Res> {
        return new Promise<ClusterDatabaseManager.Get.Res>((resolve, reject) => {
            this._clusterStub.databases_get(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            })
        });
    }

    databasesClusterAll(req: ClusterDatabaseManager.All.Req): Promise<ClusterDatabaseManager.All.Res> {
        return new Promise<ClusterDatabaseManager.All.Res>((resolve, reject) => {
            this._clusterStub.databases_all(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            })
        });
    }

    stub(): TypeDBClient {
        return this._stub;
    }

    closeClient(): void {
        this._stub.close();
        this._clusterStub.close();
    }
}
