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
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";
import ENTERPRISE_INVALID_ROOT_CA_PATH = ErrorMessage.Driver.ENTERPRISE_INVALID_ROOT_CA_PATH;

/**
 * User credentials and TLS encryption settings for connecting to TypeDB enterprise.
 *
 * ### Examples
 *
 * ```ts
 * credential = new TypeDBCredential(username, password)
 * credential = new TypeDBCredential(username, password, "path/to/ca-certificate.pem")
 * ```
 */
export class TypeDBCredential {
    private readonly _username: string;
    private readonly _password: string;
    private readonly _tlsRootCAPath: string;
    /**
     * @param username The name of the user to connect as
     * @param password The password for the user
     * @param tlsRootCAPath Path to the CA certificate to use for authenticating server certificates.
    */
    constructor(username: string, password: string, tlsRootCAPath?: string) {
        this._username = username;
        this._password = password;

        if (tlsRootCAPath != null && !fs.existsSync(tlsRootCAPath)) {
            throw new TypeDBDriverError(ENTERPRISE_INVALID_ROOT_CA_PATH.message(tlsRootCAPath));
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
