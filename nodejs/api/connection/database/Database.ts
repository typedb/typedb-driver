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

import Replica = Database.Replica;

export interface Database {
    /** The database name as a string. */
    readonly name: string;

    /** The <code>Replica</code> instances for this database. _Only works in TypeDB Enterprise_ */
    readonly replicas: Replica[];

    /** The primary replica for this database. _Only works in TypeDB Enterprise_*/
    readonly primaryReplica: Replica;

     /** The preferred replica for this database. Operations which can be run on any replica will prefer to use this replica. _Only works in TypeDB Enterprise_ */
    readonly preferredReplica: Replica;

    /**
     * Deletes this database.
     *
     * ### Examples
     *
     * ```ts
     * database.delete()
     * ```
     */
    delete(): Promise<void>;

    /**
     * Returns a full schema text as a valid TypeQL define query string.
     *
     * ### Examples
     *
     * ```ts
     * database.schema()
     * ```
     */
    schema(): Promise<string>;
}

export namespace Database {
    /** The metadata and state of an individual raft replica of a database.*/
    export interface Replica {
        /** The database for which this is a replica. */
        readonly databaseName: string;
        /** The raft protocol ‘term’ of this replica. */
        readonly term: number;
        /** Checks whether this is the primary replica of the raft cluster.*/
        readonly primary: boolean;
        /**
         * Checks whether this is the preferred replica of the raft cluster.
         * If true, Operations which can be run on any replica will prefer to use this replica.
         */
        readonly preferred: boolean;
        /** The address of the server hosting this replica */
        readonly address: string;
    }
}
