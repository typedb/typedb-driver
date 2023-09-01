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

import * as fs from "fs";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import CLUSTER_INVALID_ROOT_CA_PATH = ErrorMessage.Client.CLUSTER_INVALID_ROOT_CA_PATH;

export class TypeDBCredential {
    private readonly _username: string;
    private readonly _password: string;
    private readonly _tlsRootCAPath: string;

    constructor(username: string, password: string, tlsRootCAPath?: string) {
        this._username = username;
        this._password = password;

        if (tlsRootCAPath != null && !fs.existsSync(tlsRootCAPath)) {
            throw new TypeDBClientError(CLUSTER_INVALID_ROOT_CA_PATH.message(tlsRootCAPath));
        }
        this._tlsRootCAPath = tlsRootCAPath;
    }

    get username(): string {
        return this._username;
    }

    get password(): string {
        return this._password;
    }

    get tlsRootCAPath(): string {
        return this._tlsRootCAPath;
    }
}
