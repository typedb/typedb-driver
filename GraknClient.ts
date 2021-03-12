/*
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
    GraknOptions, ConceptManager, QueryManager, LogicManager, ClientRPC, ServerAddress, ClientClusterRPC
} from "./dependencies_internal";

export interface GraknClient {
    session(databaseName: string, type: SessionType, options?: GraknOptions): Promise<GraknClient.Session>;
    databases(): GraknClient.DatabaseManager;
    isOpen(): boolean;
    close(): void;
    isCluster(): boolean;
}

export interface GraknClientCluster extends GraknClient {
    databases(): GraknClient.DatabaseManagerCluster;
}

export namespace GraknClient {

    export const DEFAULT_ADDRESS = "localhost:1729";

    export function core(address: string = DEFAULT_ADDRESS): GraknClient {
        return new ClientRPC(address);
    }

    export function cluster(addresses: string[]): Promise<GraknClientCluster> {
        return new ClientClusterRPC().open(addresses);
    }

    export interface DatabaseManager {
        contains(name: string): Promise<boolean>;
        create(name: string): Promise<void>;
        get(name: string): Promise<Database>;
        all(): Promise<Database[]>;
    }

    export interface DatabaseManagerCluster extends DatabaseManager {
        get(name: string): Promise<DatabaseCluster>;
        all(): Promise<DatabaseCluster[]>;
    }

    export interface Database {
        name(): string;
        delete(): Promise<void>;
    }

    export interface DatabaseCluster extends Database {
        replicas(): DatabaseReplica[];
        primaryReplica(): DatabaseReplica;
        preferredSecondaryReplica(): DatabaseReplica;
    }

    export interface DatabaseReplica {
        database(): DatabaseCluster;
        term(): number;
        isPrimary(): boolean;
        isPreferredSecondary(): boolean;
        address(): ServerAddress;
    }

    export interface Session {
        transaction(type: TransactionType, options?: GraknOptions): Promise<Transaction>;
        type(): SessionType;
        options(): GraknOptions;
        isOpen(): boolean;
        close(): Promise<void>;
        database(): Database;
    }

    export interface Transaction {
        type(): TransactionType;
        options(): GraknOptions;
        isOpen(): boolean;
        concepts(): ConceptManager;
        logic(): LogicManager;
        query(): QueryManager;
        commit(): Promise<void>;
        rollback(): Promise<void>;
        close(): Promise<void>;
    }
}

export enum SessionType {
    DATA,
    SCHEMA,
}

export enum TransactionType {
    READ,
    WRITE,
}
