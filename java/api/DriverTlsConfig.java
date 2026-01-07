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
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.driver_tls_config_new_disabled;
import static com.typedb.driver.jni.typedb_driver.driver_tls_config_new_enabled_with_native_root_ca;
import static com.typedb.driver.jni.typedb_driver.driver_tls_config_new_enabled_with_root_ca_path;
import static com.typedb.driver.jni.typedb_driver.driver_tls_config_is_enabled;
import static com.typedb.driver.jni.typedb_driver.driver_tls_config_has_root_ca_path;
import static com.typedb.driver.jni.typedb_driver.driver_tls_config_get_root_ca_path;

/**
 * TLS configuration for the TypeDB driver.
 *
 * <code>DriverTlsConfig</code> represents a fully constructed and validated TLS configuration.
 * If TLS is enabled, the underlying TLS config is built eagerly at construction time,
 * ensuring that no connection attempt can observe a partially-configured TLS state.
 * <p>
 * The driver defaults to using TLS with <b>native system trust roots</b>.
 * This matches typical system and container deployments while still allowing
 * explicit opt-out or custom PKI configuration.
 */
public class DriverTlsConfig extends NativeObject<com.typedb.driver.jni.DriverTlsConfig> {
    protected DriverTlsConfig(com.typedb.driver.jni.DriverTlsConfig nativeObject) {
        super(nativeObject);
    }

    /**
     * Creates a TLS configuration with TLS disabled.
     * WARNING: Disabling TLS causes credentials and data to be transmitted in plaintext.
     *
     * <h3>Examples</h3>
     * <pre>
     * DriverTlsConfig tlsConfig = DriverTlsConfig.disabled();
     * </pre>
     */
    @CheckReturnValue
    public static DriverTlsConfig disabled() {
        return new DriverTlsConfig(driver_tls_config_new_disabled());
    }

    /**
     * Creates a TLS configuration enabled with system native trust roots.
     *
     * <h3>Examples</h3>
     * <pre>
     * DriverTlsConfig tlsConfig = DriverTlsConfig.enabledWithNativeRootCA();
     * </pre>
     */
    @CheckReturnValue
    public static DriverTlsConfig enabledWithNativeRootCA() {
        return new DriverTlsConfig(driver_tls_config_new_enabled_with_native_root_ca());
    }

    /**
     * Creates a TLS configuration enabled with a custom root CA certificate bundle (PEM).
     *
     * @param tlsRootCAPath Path to PEM-encoded root CA certificate bundle.
     *
     *                      <h3>Examples</h3>
     *                      <pre>
     *                                           DriverTlsConfig tlsConfig = DriverTlsConfig.enabledWithRootCA("path/to/ca-certificate.pem");
     *                                           </pre>
     */
    @CheckReturnValue
    public static DriverTlsConfig enabledWithRootCA(String tlsRootCAPath) {
        Validator.requireNonNull(tlsRootCAPath, "tlsRootCAPath");
        try {
            return new DriverTlsConfig(driver_tls_config_new_enabled_with_root_ca_path(tlsRootCAPath));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    /**
     * Returns whether TLS is enabled.
     *
     * <h3>Examples</h3>
     * <pre>
     * tlsConfig.isEnabled();
     * </pre>
     */
    @CheckReturnValue
    public boolean isEnabled() {
        return driver_tls_config_is_enabled(nativeObject);
    }

    /**
     * Returns the configured custom root CA path, if present.
     * If TLS is enabled with native roots (or disabled), this will be empty.
     *
     * <h3>Examples</h3>
     * <pre>
     * tlsConfig.rootCAPath();
     * </pre>
     */
    @CheckReturnValue
    public Optional<String> rootCAPath() {
        if (!driver_tls_config_has_root_ca_path(nativeObject)) return Optional.empty();
        return Optional.ofNullable(driver_tls_config_get_root_ca_path(nativeObject));
    }
}
