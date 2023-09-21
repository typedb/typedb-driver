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

    export function coreDriver(address: string = DEFAULT_ADDRESS): Promise<TypeDBDriver> {
        return new TypeDBDriverImpl(address).open();
    }

    export function enterpriseDriver(addresses: string | string[], credential: TypeDBCredential): Promise<TypeDBDriver> {
        if (typeof addresses === 'string') addresses = [addresses];
        return new TypeDBDriverImpl(addresses, credential).open();
    }
}
