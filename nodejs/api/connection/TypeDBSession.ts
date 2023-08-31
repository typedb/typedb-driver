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

import {SessionType as SessionTypeProto} from "typedb-protocol/proto/session";
import {Database} from "./database/Database";
import {TypeDBOptions} from "./TypeDBOptions";
import {TransactionType, TypeDBTransaction} from "./TypeDBTransaction";

export interface TypeDBSession {
    isOpen(): boolean;

    readonly type: SessionType;

    readonly database: Database;

    readonly options: TypeDBOptions;

    transaction(type: TransactionType, options?: TypeDBOptions): Promise<TypeDBTransaction>;

    close(): Promise<void>;
}

export interface SessionType {
    proto(): SessionTypeProto;

    isData(): boolean;

    isSchema(): boolean;
}

export namespace SessionType {
    class SessionTypeImpl implements SessionType {
        private readonly _type: SessionTypeProto;

        constructor(type: SessionTypeProto) {
            this._type = type;
        }

        proto(): SessionTypeProto {
            return this._type;
        }

        isData(): boolean {
            return this == DATA;
        }

        isSchema(): boolean {
            return this == SCHEMA;
        }
    }

    export const DATA = new SessionTypeImpl(SessionTypeProto.DATA);
    export const SCHEMA = new SessionTypeImpl(SessionTypeProto.SCHEMA);
}
