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

package com.typedb.driver.api.database;

import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

public interface Database {

    /**
     * The database name as a string.
     */
    @CheckReturnValue
    String name();

    /**
     * A full schema text as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.schema()
     * </pre>
     */
    @CheckReturnValue
    String schema() throws TypeDBDriverException;

    /**
     * The types in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema()
     * </pre>
     */
    @CheckReturnValue
    String typeSchema() throws TypeDBDriverException;

    /**
     * Deletes this database.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.delete()
     * </pre>
     */
    void delete() throws TypeDBDriverException;

//    /**
//     * Set of <code>Replica</code> instances for this database.
//     * <b>Only works in TypeDB Cloud / Enterprise</b>
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * database.replicas()
//     * </pre>
//     */
//    @CheckReturnValue
//    Set<? extends Replica> replicas();
//
//    /**
//     * Returns the primary replica for this database.
//     * <b>Only works in TypeDB Cloud / Enterprise</b>
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * database.primaryReplica()
//     * </pre>
//     */
//    @CheckReturnValue
//    Optional<? extends Replica> primaryReplica();
//
//    /**
//     * Returns the preferred replica for this database. Operations which can be run on any replica will prefer to use this replica.
//     * <b>Only works in TypeDB Cloud / Enterprise</b>
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * database.preferredReplica()
//     * </pre>
//     */
//    @CheckReturnValue
//    Optional<? extends Replica> preferredReplica();
//
//    /**
//     * The metadata and state of an individual raft replica of a database.
//     */
//    interface Replica {
//
//        /**
//         * The server hosting this replica
//         */
//        @CheckReturnValue
//        String server();
//
//        /**
//         * Checks whether this is the primary replica of the raft cluster.
//         */
//        @CheckReturnValue
//        boolean isPrimary();
//
//        /**
//         * Checks whether this is the preferred replica of the raft cluster.
//         * If true, Operations which can be run on any replica will prefer to use this replica.
//         */
//        @CheckReturnValue
//        boolean isPreferred();
//
//        /**
//         * The raft protocol ‘term’ of this replica.
//         */
//        @CheckReturnValue
//        long term();
//    }
}
