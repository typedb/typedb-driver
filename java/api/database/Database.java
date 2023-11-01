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

package com.vaticle.typedb.driver.api.database;

import javax.annotation.CheckReturnValue;
import java.util.Optional;
import java.util.Set;

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
    String schema();

    /**
     * The types in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema()
     * </pre>
     */
    @CheckReturnValue
    String typeSchema();

    /**
     * The rules in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.ruleSchema()
     * </pre>
     */
    @CheckReturnValue
    String ruleSchema();

    /**
     * Deletes this database.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.delete()
     * </pre>
     */
    void delete();

    /**
     * Set of <code>Replica</code> instances for this database.
     * <b>Only works in TypeDB Enterprise</b>
     *
     * <h3>Examples</h3>
     * <pre>
     * database.replicas()
     * </pre>
     */
    @CheckReturnValue
    Set<? extends Replica> replicas();

    /**
     * Returns the primary replica for this database.
     * _Only works in TypeDB Enterprise_
     *
     * <h3>Examples</h3>
     * <pre>
     * database.primaryReplica()
     * </pre>
     */
    @CheckReturnValue
    Optional<? extends Replica> primaryReplica();

    /**
     * Returns the preferred replica for this database. Operations which can be run on any replica will prefer to use this replica.
     * _Only works in TypeDB Enterprise_
     *
     * <h3>Examples</h3>
     * <pre>
     * database.preferredReplica()
     * </pre>
     */
    @CheckReturnValue
    Optional<? extends Replica> preferredReplica();

    /**
     * The metadata and state of an individual raft replica of a database.
     */
    interface Replica {

        /**
         * Retrieves the address of the server hosting this replica
         */
        @CheckReturnValue
        String address();

        /**
         * Checks whether this is the primary replica of the raft cluster.
         */
        @CheckReturnValue
        boolean isPrimary();

        /**
         * Checks whether this is the preferred replica of the raft cluster.
         * If true, Operations which can be run on any replica will prefer to use this replica.
         */
        @CheckReturnValue
        boolean isPreferred();

        /**
         * The raft protocol ‘term’ of this replica.
         */
        @CheckReturnValue
        long term();
    }
}
