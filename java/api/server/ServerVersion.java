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

import com.typedb.driver.common.exception.ErrorMessage;
import com.typedb.driver.common.exception.TypeDBDriverException;

/**
 * Type of replica.
 *
 * <h3>Examples</h3>
 * <pre>
 * replica.type();
 * </pre>
 */
public class ServerVersion {
    private final String distribution;
    private final String version;

    /**
     * @hidden
     */
    ServerVersion(com.typedb.driver.jni.ServerVersion nativeObject) {
        if (nativeObject == null) throw new TypeDBDriverException(ErrorMessage.Internal.NULL_NATIVE_VALUE);
        this.distribution = nativeObject.getDistribution();
        this.version = nativeObject.getVersion();
    }

    /**
     * Returns the server's distribution.
     *
     * <h3>Examples</h3>
     * <pre>
     * serverVersion.getDistribution();
     * </pre>
     */
    public String getDistribution() {
        return distribution;
    }

    /**
     * Returns the server's version.
     *
     * <h3>Examples</h3>
     * <pre>
     * serverVersion.getVersion();
     * </pre>
     */
    public String getVersion() {
        return version;
    }
}
