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

package com.typedb.driver.api.server;

import com.typedb.driver.api.server.ReplicaRole;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

/**
 * The metadata and state of an individual raft replica of a driver connection.
 */
public interface ServerReplica {
    // TODO: This is what u64 is converted to. This one feels weird, although I don't know what to do with it.

    /**
     * Returns the id of this replica.
     */
    @CheckReturnValue
    long getID();

    /**
     * Returns the address this replica is hosted at.
     */
    @CheckReturnValue
    String getAddress();

    /**
     * Returns whether this is the primary replica of the raft cluster or any of the supporting types.
     */
    @CheckReturnValue
    Optional<ReplicaRole> getRole();

    /**
     * Checks whether this is the primary replica of the raft cluster.
     */
    @CheckReturnValue
    Boolean isPrimary();

    /**
     * Returns the raft protocol ‘term’ of this replica.
     */
    @CheckReturnValue
    Optional<Long> getTerm();
}
