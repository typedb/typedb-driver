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

import javax.annotation.CheckReturnValue;

import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.driver_options_new;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_is_tls_enabled;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_is_tls_enabled;
import static com.typedb.driver.jni.typedb_driver.driver_options_has_tls_root_ca_path;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_tls_root_ca_path;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_tls_root_ca_path;

/**
 * TypeDB driver options. <code>DriverOptions</code> are used to specify the driver's connection behavior.
 */
public class DriverOptions extends NativeObject<com.typedb.driver.jni.DriverOptions> {
    /**
     * Produces a new <code>DriverOptions</code> object.
     *
     * <h3>Examples</h3>
     * <pre>
     * DriverOptions options = DriverOptions();
     * </pre>
     */
    public DriverOptions() {
        super(driver_options_new());
    }

    /**
     * Returns the value set for the TLS flag in this <code>DriverOptions</code> object.
     * Specifies whether the connection to TypeDB must be done over TLS.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.isTlsEnabled();
     * </pre>
     */
    @CheckReturnValue
    public Boolean isTlsEnabled() {
        return driver_options_get_is_tls_enabled(nativeObject);
    }

    /**
     * Explicitly sets whether the connection to TypeDB must be done over TLS.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.isTlsEnabled(true);
     * </pre>
     *
     * @param isTlsEnabled Whether the connection to TypeDB must be done over TLS.
     */
    public DriverOptions isTlsEnabled(boolean isTlsEnabled) {
        driver_options_set_is_tls_enabled(nativeObject, isTlsEnabled);
        return this;
    }

    /**
     * Returns the TLS root CA set in this <code>DriverOptions</code> object.
     * Specifies the root CA used in the TLS config for server certificates authentication.
     * Uses system roots if None is set.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.tlsRootCAPath();
     * </pre>
     */
    @CheckReturnValue
    public Optional<String> tlsRootCAPath() {
        if (driver_options_has_tls_root_ca_path(nativeObject))
            return Optional.of(driver_options_get_tls_root_ca_path(nativeObject));
        return Optional.empty();
    }

    /**
     * Returns the TLS root CA set in this <code>DriverOptions</code> object.
     * Specifies the root CA used in the TLS config for server certificates authentication.
     * Uses system roots if None is set.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.tlsRootCAPath(Optional.of("/path/to/ca-certificate.pem"));
     * </pre>
     *
     * @param tlsRootCAPath The path to the TLS root CA. If None, system roots are used.
     */
    public DriverOptions tlsRootCAPath(Optional<String> tlsRootCAPath) {
        driver_options_set_tls_root_ca_path(nativeObject, tlsRootCAPath.orElse(null));
        return this;
    }

    // TODO: Add other flags when they are finalized!
}
