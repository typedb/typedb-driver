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

import {
    CallCredentials,
    ChannelCredentials,
    ClientDuplexStream,
    credentials,
    Metadata,
    ServiceError
} from "@grpc/grpc-js";
import * as fs from "fs";
import {ClusterDatabaseManager} from "typedb-protocol/cluster/cluster_database_pb";
import {ServerManager} from "typedb-protocol/cluster/cluster_server_pb";
import {TypeDBClusterClient} from "typedb-protocol/cluster/cluster_service_grpc_pb";
import {ClusterUser, ClusterUserManager} from "typedb-protocol/cluster/cluster_user_pb";
import {TypeDBClient} from "typedb-protocol/core/core_service_grpc_pb";
import {TypeDBCredential} from "../../api/connection/TypeDBCredential";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {TypeDBStub} from "../../common/rpc/TypeDBStub";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import CLUSTER_TOKEN_CREDENTIAL_INVALID = ErrorMessage.Client.CLUSTER_TOKEN_CREDENTIAL_INVALID;
import {
    CoreDatabase as CoreDatabaseProto,
    CoreDatabaseManager as CoreDatabaseMgrProto
} from "typedb-protocol/core/core_database_pb";
import {TypeDBDatabaseImpl} from "../TypeDBDatabaseImpl";
import {Session} from "typedb-protocol/common/session_pb";
import * as common_transaction_pb from "typedb-protocol/common/transaction_pb";

function isServiceError(e: Error | ServiceError): e is ServiceError {
    return "code" in e;
}

export class ClusterServerStub extends TypeDBStub {

    private readonly _credential: TypeDBCredential;
    private _token: string;
    private readonly _stub: TypeDBClient;
    private readonly _clusterStub: TypeDBClusterClient;

    constructor(address: string, credential: TypeDBCredential) {
        super();
        this._credential = credential;
        this._token = null;
        const stubCredentials = this.createChannelCredentials();
        this._stub = new TypeDBClient(address, stubCredentials);
        this._clusterStub = new TypeDBClusterClient(address, stubCredentials);
    }

    public async open(): Promise<void> {
        try {
            await this.connectionOpen(RequestBuilder.Connection.openReq());
            const req = RequestBuilder.Cluster.User.tokenReq(this._credential.username);
            this._token = await this.userToken(req);
        } catch (e) {
            if (!isServiceError(e)) throw e;
        }
    }

    private createChannelCredentials(): ChannelCredentials {
        const callCreds = this.createCallCredentials();
        if (this._credential.tlsRootCAPath != null) {
            const rootCert = fs.readFileSync(this._credential.tlsRootCAPath);
            return credentials.combineChannelCredentials(ChannelCredentials.createSsl(rootCert), callCreds);
        } else {
            return credentials.combineChannelCredentials(ChannelCredentials.createSsl(), callCreds);
        }
    }

    private createCallCredentials(): CallCredentials {
        const metaCallback = (_params: any, callback: any) => {
            const metadata = new Metadata();
            metadata.add('username', this._credential.username);
            if (this._token == null) {
                metadata.add('password', this._credential.password);
            } else {
                metadata.add('token', this._token);
            }
            callback(null, metadata);
        }
        return CallCredentials.createFromMetadataGenerator(metaCallback);
    }

    serversAll(req: ServerManager.All.Req): Promise<ServerManager.All.Res> {
        return this.mayRenewToken(() =>
            new Promise<ServerManager.All.Res>((resolve, reject) => {
                this._clusterStub.servers_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    usersAll(req: ClusterUserManager.All.Req): Promise<ClusterUserManager.All.Res> {
        return this.mayRenewToken(() =>
            new Promise<ClusterUserManager.All.Res>((resolve, reject) => {
                this._clusterStub.users_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    usersContains(req: ClusterUserManager.Contains.Req): Promise<boolean> {
        return this.mayRenewToken(() =>
            new Promise<boolean>((resolve, reject) => {
                this._clusterStub.users_contains(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res.getContains());
                })
            })
        );
    }

    usersCreate(req: ClusterUserManager.Create.Req): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this._clusterStub.users_create(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    usersDelete(req: ClusterUserManager.Delete.Req): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this._clusterStub.users_delete(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                });
            })
        );
    }

    usersPasswordSet(req: ClusterUserManager.PasswordSet.Req): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this._clusterStub.users_password_set(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    usersGet(req: ClusterUserManager.Get.Req): Promise<ClusterUserManager.Get.Res> {
        return this.mayRenewToken(() =>
            new Promise<ClusterUserManager.Get.Res>((resolve, reject) => {
                this._clusterStub.users_get(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    userPasswordUpdate(req: ClusterUser.PasswordUpdate.Req): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this._clusterStub.user_password_update(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    databasesClusterGet(req: ClusterDatabaseManager.Get.Req): Promise<ClusterDatabaseManager.Get.Res> {
        return this.mayRenewToken(() =>
            new Promise<ClusterDatabaseManager.Get.Res>((resolve, reject) => {
                this._clusterStub.databases_get(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                })
            })
        );
    }

    databasesClusterAll(req: ClusterDatabaseManager.All.Req): Promise<ClusterDatabaseManager.All.Res> {
        return this.mayRenewToken(() =>
            new Promise<ClusterDatabaseManager.All.Res>((resolve, reject) => {
                this._clusterStub.databases_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                })
            })
        );
    }

    databasesCreate(req: CoreDatabaseMgrProto.Create.Req): Promise<void> {
        return this.mayRenewToken(() => super.databasesCreate(req));
    }

    databasesContains(req: CoreDatabaseMgrProto.Contains.Req): Promise<boolean> {
        return this.mayRenewToken(() => super.databasesContains(req));
    }

    databasesAll(req: CoreDatabaseMgrProto.All.Req): Promise<TypeDBDatabaseImpl[]> {
        return this.mayRenewToken(() => super.databasesAll(req));
    }

    databaseDelete(req: CoreDatabaseProto.Delete.Req): Promise<void> {
        return this.mayRenewToken(() => super.databaseDelete(req));
    }

    databaseSchema(req: CoreDatabaseProto.Schema.Req): Promise<string> {
        return this.mayRenewToken(() => super.databaseSchema(req));
    }

    sessionOpen(req: Session.Open.Req): Promise<Session.Open.Res> {
        return this.mayRenewToken(() => super.sessionOpen(req));
    }

    sessionClose(req: Session.Close.Req): Promise<void> {
        return this.mayRenewToken(() => super.sessionClose(req));
    }

    sessionPulse(req: Session.Pulse.Req): Promise<boolean> {
        return this.mayRenewToken(() => super.sessionPulse(req));
    }

    transaction(): Promise<ClientDuplexStream<common_transaction_pb.Transaction.Client, common_transaction_pb.Transaction.Server>> {
        return this.mayRenewToken(() => super.transaction());
    }

    private async mayRenewToken<RES>(fn: () => Promise<RES>): Promise<RES> {
        try {
            return await fn();
        } catch (e) {
            if (e instanceof TypeDBClientError && CLUSTER_TOKEN_CREDENTIAL_INVALID === e.messageTemplate) {
                console.log(`token '${this._token}' expired. renewing...`);
                this._token = null;
                const req = RequestBuilder.Cluster.User.tokenReq(this._credential.username);
                this._token = await this.userToken(req);
                console.log(`token renewed to '${this._token}'`);
                try {
                    return await fn();
                } catch (e2) {
                    if (isServiceError(e2)) {
                        throw new TypeDBClientError(e2);
                    } else throw e2;
                }
            } else throw e;
        }
    }

    private async userToken(req: ClusterUser.Token.Req): Promise<string> {
        return new Promise<string>((resolve, reject) => {
            return this._clusterStub.user_token(req, (err, res) => {
                if (err) reject(err);
                else resolve(res.getToken());
            });
        });
    }

    stub(): TypeDBClient {
        return this._stub;
    }

    close(): void {
        this._stub.close();
        this._clusterStub.close();
    }
}
