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

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

/**
 * This enum is used to specify the replication role of a server.
 *
 * <h3>Examples</h3>
 * <pre>
 * server.getRole();
 * </pre>
 */
public enum ReplicationRole {
    PRIMARY(0, com.typedb.driver.jni.ReplicationRole.Primary),
    CANDIDATE(1, com.typedb.driver.jni.ReplicationRole.Candidate),
    SECONDARY(2, com.typedb.driver.jni.ReplicationRole.Secondary);

    public final com.typedb.driver.jni.ReplicationRole nativeObject;
    private final int id;

    ReplicationRole(int id, com.typedb.driver.jni.ReplicationRole nativeObject) {
        this.id = id;
        this.nativeObject = nativeObject;
    }

    public static ReplicationRole of(com.typedb.driver.jni.ReplicationRole nativeType) {
        if (nativeType == com.typedb.driver.jni.ReplicationRole.Primary) return PRIMARY;
        else if (nativeType == com.typedb.driver.jni.ReplicationRole.Candidate) return CANDIDATE;
        else if (nativeType == com.typedb.driver.jni.ReplicationRole.Secondary) return SECONDARY;
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    public int id() {
        return id;
    }

    /**
     * Checks whether this is the primary server of the cluster.
     */
    public boolean isPrimary() {
        return nativeObject == com.typedb.driver.jni.ReplicationRole.Primary;
    }

    /**
     * Checks whether this is a candidate server of the cluster.
     */
    public boolean isCandidate() {
        return nativeObject == com.typedb.driver.jni.ReplicationRole.Candidate;
    }

    /**
     * Checks whether this is a secondary server of the cluster.
     */
    public boolean isSecondary() {
        return nativeObject == com.typedb.driver.jni.ReplicationRole.Secondary;
    }
}
