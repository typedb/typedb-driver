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

package com.typedb.driver.api;

import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.typedb.driver.jni.typedb_driver.consistency_level_eventual;
import static com.typedb.driver.jni.typedb_driver.consistency_level_replica_dependent;
import static com.typedb.driver.jni.typedb_driver.consistency_level_strong;

/**
 * Consistency levels of operations against a distributed server. All driver methods have default
 * recommended values, however, readonly operations can be configured in order to potentially
 * speed up the execution (introducing risks of stale data) or test a specific replica.
 * This setting does not affect clusters with a single node.
 */
public abstract class ConsistencyLevel {
    public abstract com.typedb.driver.jni.ConsistencyLevel nativeValue();

    public static ConsistencyLevel of(com.typedb.driver.jni.ConsistencyLevel nativeValue) {
        if (nativeValue.getTag() == com.typedb.driver.jni.ConsistencyLevelTag.Strong) return new Strong();
        else if (nativeValue.getTag() == com.typedb.driver.jni.ConsistencyLevelTag.Eventual) return new Eventual();
        else if (nativeValue.getTag() == com.typedb.driver.jni.ConsistencyLevelTag.ReplicaDependent) {
            return new ReplicaDependent(nativeValue.getAddress());
        }
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    public static com.typedb.driver.jni.ConsistencyLevel nativeValue(ConsistencyLevel consistencyLevel) {
        if (consistencyLevel == null) {
            return null;
        } else {
            return consistencyLevel.nativeValue();
        }
    }

    /**
     * Strongest consistency, always up-to-date due to the guarantee of the primary replica usage.
     * May require more time for operation execution.
     */
    public static final class Strong extends ConsistencyLevel {
        Strong() {
        }

        @Override
        public com.typedb.driver.jni.ConsistencyLevel nativeValue() {
            return newNative();
        }

        private static com.typedb.driver.jni.ConsistencyLevel newNative() {
            return consistency_level_strong();
        }

        @Override
        public String toString() {
            return "Strong";
        }
    }

    /**
     * Allow stale reads from any replica. May not reflect latest writes. The execution may be
     * eventually faster compared to other consistency levels.
     */
    public static final class Eventual extends ConsistencyLevel {
        Eventual() {
        }

        @Override
        public com.typedb.driver.jni.ConsistencyLevel nativeValue() {
            return newNative();
        }

        private static com.typedb.driver.jni.ConsistencyLevel newNative() {
            return consistency_level_eventual();
        }

        @Override
        public String toString() {
            return "Eventual";
        }
    }

    /**
     * The operation is executed against the provided replica address only. Its guarantees depend
     * on the replica selected.
     */
    public static final class ReplicaDependent extends ConsistencyLevel {
        private final String address;

        public ReplicaDependent(String address) {
            this.address = address;
        }

        @Override
        public com.typedb.driver.jni.ConsistencyLevel nativeValue() {
            return newNative(this.address);
        }

        private static com.typedb.driver.jni.ConsistencyLevel newNative(String address) {
            return consistency_level_replica_dependent(address);
        }

        /**
         * Retrieves the address of the replica this consistency level depends on.
         */
        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "ReplicaDependent(" + address + ")";
        }
    }
}