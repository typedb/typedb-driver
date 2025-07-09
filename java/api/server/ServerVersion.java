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

import com.typedb.driver.common.NativeObject;

/**
 * A full TypeDB server's version specification.
 *
 * <h3>Examples</h3>
 * <pre>
 * driver.serverVersion();
 * </pre>
 */
public class ServerVersion extends NativeObject<com.typedb.driver.jni.ServerVersion> {
    /**
     * @hidden
     */
    public ServerVersion(com.typedb.driver.jni.ServerVersion nativeObject) {
        super(nativeObject);
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
        assert (nativeObject.isOwned());
        return nativeObject.getDistribution();
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
        assert (nativeObject.isOwned());
        return nativeObject.getVersion();
    }
}
