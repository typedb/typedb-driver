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
import static com.typedb.driver.jni.typedb_driver.server_routing_auto;
import static com.typedb.driver.jni.typedb_driver.server_routing_direct;

/**
 * Server routing directive for operations against a distributed server. All driver methods have
 * default recommended values, however, some operations can be configured in order to
 * target a specific server in the cluster. This setting does not affect clusters with a single node.
 */
public abstract class ServerRouting {
    public abstract com.typedb.driver.jni.ServerRouting nativeValue();

    public static ServerRouting of(com.typedb.driver.jni.ServerRouting nativeValue) {
        if (nativeValue.getTag() == com.typedb.driver.jni.ServerRoutingType.Auto) return new Auto();
        else if (nativeValue.getTag() == com.typedb.driver.jni.ServerRoutingType.Direct) {
            return new Direct(nativeValue.getAddress());
        }
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    public static com.typedb.driver.jni.ServerRouting nativeValue(ServerRouting serverRouting) {
        if (serverRouting == null) {
            return null;
        } else {
            return serverRouting.nativeValue();
        }
    }

    /**
     * Automatic server routing. Driver automatically selects the server (primary in clusters).
     */
    public static final class Auto extends ServerRouting {
        public Auto() {
        }

        @Override
        public com.typedb.driver.jni.ServerRouting nativeValue() {
            return newNative();
        }

        private static com.typedb.driver.jni.ServerRouting newNative() {
            return server_routing_auto();
        }

        @Override
        public String toString() {
            return "Auto";
        }
    }

    /**
     * Route to a specific known server at the given address. Mostly used for debugging purposes.
     */
    public static final class Direct extends ServerRouting {
        private final String address;

        public Direct(String address) {
            this.address = address;
        }

        @Override
        public com.typedb.driver.jni.ServerRouting nativeValue() {
            return newNative(this.address);
        }

        private static com.typedb.driver.jni.ServerRouting newNative(String address) {
            return server_routing_direct(address);
        }

        /**
         * Retrieves the address of the server this routing targets.
         */
        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "Direct(" + address + ")";
        }
    }
}
