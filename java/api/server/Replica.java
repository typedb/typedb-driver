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

import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

/**
 * The metadata and state of an individual raft replica of a driver connection.
 */
public interface Replica {
    /**
     * The address this replica is hosted at.
     */
    @CheckReturnValue
    String address();

    /**
     * Gets the type of this replica: whether it's a primary or a secondary replica.
     */
    @CheckReturnValue
    ReplicaType type();

    /**
     * The raft protocol ‘term’ of this replica.
     */
    @CheckReturnValue
    long term();
}
