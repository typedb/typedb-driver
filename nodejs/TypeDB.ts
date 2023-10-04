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

import {TypeDBDriver as TypeDBDriver} from "./api/connection/TypeDBDriver";
import {TypeDBCredential} from "./api/connection/TypeDBCredential";
import {TypeDBDriverImpl} from "./connection/TypeDBDriverImpl";

export namespace TypeDB {
    export const DEFAULT_ADDRESS = "127.0.0.1:1729";

    /**
     * Creates a connection to TypeDB.
     * @param address - Address of the TypeDB server.
     *
     * ### Examples
     *
     * ```ts
     * const driver = TypeDB.enterpriseDriver(["127.0.0.1:11729"], new TypeDBCredential(username, password));
     * ```
     */
    export function coreDriver(address: string = DEFAULT_ADDRESS): Promise<TypeDBDriver> {
        return new TypeDBDriverImpl(address).open();
    }

    /**
     * Creates a connection to TypeDB Enterprise, authenticating with the provided credentials.
     * @param addresses - List of addresses of the individual TypeDB enterprise servers.
     * As long one specified address is provided, the driver will discover the other addresses from that server.
     * @param credential - The credentials to log in, and encryption settings. See <code>{@link TypeDBCredential}</code>
     *
     * ### Examples
     *
     * ```ts
     * const driver = TypeDB.enterpriseDriver(["127.0.0.1:11729"], new TypeDBCredential(username, password));
     * ```
     */
    export function enterpriseDriver(addresses: string | string[], credential: TypeDBCredential): Promise<TypeDBDriver> {
        if (typeof addresses === 'string') addresses = [addresses];
        return new TypeDBDriverImpl(addresses, credential).open();
    }
}
