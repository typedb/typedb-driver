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

import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.Nullable;

import static com.typedb.driver.jni.typedb_driver.connection_settings_new;

/**
 * User connection settings (TLS encryption, etc.) for connecting to TypeDB Server.
 *
 * <h3>Examples</h3>
 * <pre>
 * ConnectionSettings connectionSettings = new ConnectionSettings(true, Path.of("path/to/ca-certificate.pem"));
 * </pre>
 */
public class ConnectionSettings extends NativeObject<com.typedb.driver.jni.ConnectionSettings> {
    /**
     * @param isTlsEnabled  Specify whether the connection to TypeDB Server must be done over TLS.
     * @param tlsRootCAPath Path to the CA certificate to use for authenticating server certificates.
     */
    public ConnectionSettings(boolean isTlsEnabled, String tlsRootCAPath) { // TODO: Maybe Optional<String>? Optional.of(Path.of(..))?..
        super(newNative(isTlsEnabled, tlsRootCAPath));
    }

    private static com.typedb.driver.jni.ConnectionSettings newNative(boolean isTlsEnabled, @Nullable String tlsRootCAPath) {
        try {
            return connection_settings_new(isTlsEnabled, tlsRootCAPath);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }
}
