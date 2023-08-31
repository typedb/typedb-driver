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


import {ConnectionOpenReq} from "typedb-protocol/proto/connection";
import {
    DatabaseDeleteReq,
    DatabaseManagerAllReq,
    DatabaseManagerAllRes,
    DatabaseManagerContainsReq,
    DatabaseManagerCreateReq,
    DatabaseManagerGetReq,
    DatabaseManagerGetRes,
    DatabaseRuleSchemaReq,
    DatabaseSchemaReq,
    DatabaseTypeSchemaReq
} from "typedb-protocol/proto/database";
import {TypeDBClient as GRPCStub} from "typedb-protocol/proto/service";
import {TypeDBClientError} from "../errors/TypeDBClientError";
import {ServerManagerAllReq, ServerManagerAllRes} from "typedb-protocol/proto/server";
import {RequestBuilder} from "./RequestBuilder";
import {SessionCloseReq, SessionOpenReq, SessionOpenRes, SessionPulseReq} from "typedb-protocol/proto/session";
import {ClientDuplexStream} from "@grpc/grpc-js";
import {TransactionClient, TransactionServer} from "typedb-protocol/proto/transaction";
import {
    UserManagerAllReq,
    UserManagerAllRes,
    UserManagerContainsReq,
    UserManagerCreateReq,
    UserManagerDeleteReq,
    UserManagerGetReq,
    UserManagerGetRes,
    UserManagerPasswordSetReq,
    UserPasswordUpdateReq,
    UserTokenReq
} from "typedb-protocol/proto/user";
import {ErrorMessage} from "../errors/ErrorMessage";

/*
TODO implement ResilientCall
 */
export abstract class TypeDBStub {
    async open(): Promise<void> {
        await this.connectionOpen(RequestBuilder.Connection.openReq());
    }

    connectionOpen(req: ConnectionOpenReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().connection_open(req, (err) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    serversAll(req: ServerManagerAllReq): Promise<ServerManagerAllRes> {
        return this.mayRenewToken(() =>
            new Promise<ServerManagerAllRes>((resolve, reject) => {
                this.stub().servers_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    databasesCreate(req: DatabaseManagerCreateReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().databases_create(req, (err) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    databasesContains(req: DatabaseManagerContainsReq): Promise<boolean> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().databases_contains(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res.contains);
                });
            })
        );
    }

    databasesGet(req: DatabaseManagerGetReq): Promise<DatabaseManagerGetRes> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().databases_get(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                })
            })
        );
    }

    databasesAll(req: DatabaseManagerAllReq): Promise<DatabaseManagerAllRes> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().databases_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                })
            })
        );
    }

    databaseDelete(req: DatabaseDeleteReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                this.stub().database_delete(req, (err) => {
                    if (err) reject(err);
                    else resolve();
                });
            })
        );
    }

    databaseSchema(req: DatabaseSchemaReq): Promise<string> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                return this.stub().database_schema(req, (err, res) => {
                    if (err) reject(err);
                    else resolve(res.schema);
                });
            })
        );
    }

    databaseTypeSchema(req: DatabaseTypeSchemaReq): Promise<string> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                return this.stub().database_type_schema(req, (err, res) => {
                    if (err) reject(err);
                    else resolve(res.schema);
                });
            })
        );
    }

    databaseRuleSchema(req: DatabaseRuleSchemaReq): Promise<string> {
        return this.mayRenewToken(() =>
            new Promise((resolve, reject) => {
                return this.stub().database_rule_schema(req, (err, res) => {
                    if (err) reject(err);
                    else resolve(res.schema);
                });
            })
        );
    }

    sessionOpen(openReq: SessionOpenReq): Promise<SessionOpenRes> {
        return new Promise<SessionOpenRes>((resolve, reject) => {
            this.stub().session_open(openReq, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            });
        });
    }

    sessionClose(req: SessionCloseReq): Promise<void> {
        return new Promise<void>((resolve, _reject) => {
            this.stub().session_close(req, (err, _res) => {
                if (err) {
                    console.warn("An error has occurred when issuing session close request: %o", err)
                }
                resolve();
            });
        });
    }

    sessionPulse(pulse: SessionPulseReq): Promise<boolean> {
        return new Promise<boolean>((resolve, reject) => {
            this.stub().session_pulse(pulse, (err, res) => {
                if (err) reject(err);
                else {
                    resolve(res.alive);
                }
            });
        });
    }

    transaction(): Promise<ClientDuplexStream<TransactionClient, TransactionServer>> {
        return new Promise<ClientDuplexStream<TransactionClient, TransactionServer>>(
            (resolve, reject) => {
                try {
                    resolve(this.stub().transaction());
                } catch (e) {
                    reject(e);
                }
            });
    }

    usersAll(req: UserManagerAllReq): Promise<UserManagerAllRes> {
        return this.mayRenewToken(() =>
            new Promise<UserManagerAllRes>((resolve, reject) => {
                this.stub().users_all(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    usersContains(req: UserManagerContainsReq): Promise<boolean> {
        return this.mayRenewToken(() =>
            new Promise<boolean>((resolve, reject) => {
                this.stub().users_contains(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res.contains);
                })
            })
        );
    }

    usersCreate(req: UserManagerCreateReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this.stub().users_create(req, (err, _res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    usersDelete(req: UserManagerDeleteReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this.stub().users_delete(req, (err, _res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                });
            })
        );
    }

    usersPasswordSet(req: UserManagerPasswordSetReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this.stub().users_password_set(req, (err, _res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    usersGet(req: UserManagerGetReq): Promise<UserManagerGetRes> {
        return this.mayRenewToken(() =>
            new Promise<UserManagerGetRes>((resolve, reject) => {
                this.stub().users_get(req, (err, res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve(res);
                });
            })
        );
    }

    userPasswordUpdate(req: UserPasswordUpdateReq): Promise<void> {
        return this.mayRenewToken(() =>
            new Promise<void>((resolve, reject) => {
                this.stub().user_password_update(req, (err, _res) => {
                    if (err) reject(new TypeDBClientError(err));
                    else resolve();
                })
            })
        );
    }

    userToken(req: UserTokenReq): Promise<string> {
        return new Promise<string>((resolve, reject) => {
            return this.stub().user_token(req, (err, res) => {
                if (err) reject(err);
                else resolve(res.token);
            });
        });
    }

    abstract stub(): GRPCStub;

    close(): void {
        this.stub().close();
    }

    abstract mayRenewToken<RES>(fn: () => Promise<RES>): Promise<RES>;
}
