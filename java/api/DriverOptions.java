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

import com.typedb.driver.api.DriverTlsConfig;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.driver_options_get_tls_config;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_primary_failover_retries;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_replica_discovery_attempts;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_request_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.driver_options_get_use_replication;
import static com.typedb.driver.jni.typedb_driver.driver_options_has_replica_discovery_attempts;
import static com.typedb.driver.jni.typedb_driver.driver_options_new;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_tls_config;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_primary_failover_retries;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_replica_discovery_attempts;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_request_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.driver_options_set_use_replication;

/**
 * TypeDB driver options. <code>DriverOptions</code> are used to specify the driver's connection behavior.
 */
public class DriverOptions extends NativeObject<com.typedb.driver.jni.DriverOptions> {
    /**
     * Produces a new <code>DriverOptions</code> object for connecting to TypeDB Server using custom TLS settings.
     * WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
     *
     * <h3>Examples</h3>
     * <pre>
     * DriverOptions options = new DriverOptions(DriverTlsConfig.enabledWithNativeRootCA());
     * </pre>
     */
    public DriverOptions(DriverTlsConfig tlsConfig) {
        super(driver_options_new(tlsConfig.nativeObject));
    }

    /**
     * Returns the TLS configuration associated with this <code>DriverOptions</code>.
     * Specifies the TLS configuration of the connection to TypeDB.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.tlsConfig();
     * </pre>
     */
    @CheckReturnValue
    public DriverTlsConfig tlsConfig() {
        return new DriverTlsConfig(driver_options_get_tls_config(nativeObject));
    }

    /**
     * Overrides the TLS configuration associated with this {@code DriverOptions}.
     * WARNING: Disabled TLS settings will make the driver sending passwords as plaintext.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.tlsConfig(DriverTlsConfig.enabledWithNativeRootCA());
     * </pre>
     */
    public DriverOptions tlsConfig(DriverTlsConfig tlsConfig) {
        Validator.requireNonNull(tlsConfig, "tlsConfig");
        driver_options_set_tls_config(nativeObject, tlsConfig.nativeObject);
        return this;
    }

    /**
     * Returns the request timeout in milliseconds set for this ``DriverOptions`` object.
     * Specifies the maximum time to wait for a response to a unary RPC request.
     * This applies to operations like database creation, user management, and initial
     * transaction opening. It does NOT apply to operations within transactions (queries, commits).
     *
     * <h3>Examples</h3>
     * <pre>
     * options.requestTimeoutMillis();
     * </pre>
     */
    @CheckReturnValue
    public Long requestTimeoutMillis() {
        return driver_options_get_request_timeout_millis(nativeObject);
    }

    /**
     *
     * Sets the maximum time (in milliseconds) to wait for a response to a unary RPC request.
     * This applies to operations like database creation, user management, and initial
     * transaction opening. It does NOT apply to operations within transactions (queries, commits).
     * Defaults to 2 hours (7200000 milliseconds).
     *
     * <h3>Examples</h3>
     * <pre>
     * options.requestTimeoutMillis(30000); // 30 seconds
     * </pre>
     *
     * @param requestTimeoutMillis The request timeout in milliseconds. Must be non-negative.
     */
    public DriverOptions requestTimeoutMillis(long requestTimeoutMillis) {
        Validator.requireNonNegative(requestTimeoutMillis, "requestTimeoutMillis");
        driver_options_set_request_timeout_millis(nativeObject, requestTimeoutMillis);
        return this;
    }

    /**
     * Returns the value set for the replication usage flag in this <code>DriverOptions</code> object.
     * Specifies whether the connection to TypeDB can use cluster replicas provided by the server
     * or it should be limited to a single configured address.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.useReplication();
     * </pre>
     */
    @CheckReturnValue
    public Boolean useReplication() {
        return driver_options_get_use_replication(nativeObject);
    }

    /**
     * Explicitly sets whether the connection to TypeDB can use cluster replicas provided by the server
     * or it should be limited to a single configured address. Defaults to true.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.useReplication(true);
     * </pre>
     *
     * @param useReplication Whether the connection to TypeDB can use replication.
     */
    public DriverOptions useReplication(boolean useReplication) {
        driver_options_set_use_replication(nativeObject, useReplication);
        return this;
    }

    /**
     * Returns the value set for the primary failover retries limit in this <code>DriverOptions</code> object.
     * Limits the number of attempts to redirect a strongly consistent request to another
     * primary replica in case of a failure due to the change of replica roles.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.primaryFailoverRetries();
     * </pre>
     */
    @CheckReturnValue
    public Integer primaryFailoverRetries() {
        return (int) driver_options_get_primary_failover_retries(nativeObject);
    }

    /**
     * Explicitly sets the limit on the number of attempts to redirect a strongly consistent request to another
     * primary replica in case of a failure due to the change of replica roles. Defaults to 1.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.primaryFailoverRetries(1);
     * </pre>
     *
     * @param primaryFailoverRetries The limit of primary failover retries.
     */
    public DriverOptions primaryFailoverRetries(int primaryFailoverRetries) {
        Validator.requireNonNegative(primaryFailoverRetries, "primaryFailoverRetries");
        driver_options_set_primary_failover_retries(nativeObject, primaryFailoverRetries);
        return this;
    }

    /**
     * Returns the value set for the replica discovery attempts limit in this <code>DriverOptions</code> object.
     * Limits the number of driver attempts to discover a single working replica to perform an
     * operation in case of a replica unavailability. Every replica is tested once, which means
     * that at most:
     * - {limit} operations are performed if the limit <= the number of replicas.
     * - {number of replicas} operations are performed if the limit > the number of replicas.
     * - {number of replicas} operations are performed if the limit is None.
     * Affects every eventually consistent operation, including redirect failover, when the new
     * primary replica is unknown.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.replicaDiscoveryAttempts();
     * </pre>
     */
    @CheckReturnValue
    public Optional<Integer> replicaDiscoveryAttempts() {
        if (driver_options_has_replica_discovery_attempts(nativeObject))
            return Optional.of((int) driver_options_get_replica_discovery_attempts(nativeObject));
        return Optional.empty();
    }

    /**
     * Limits the number of driver attempts to discover a single working replica to perform an
     * operation in case of a replica unavailability. Every replica is tested once, which means
     * that at most:
     * - {limit} operations are performed if the limit <= the number of replicas.
     * - {number of replicas} operations are performed if the limit > the number of replicas.
     * - {number of replicas} operations are performed if the limit is None.
     * Affects every eventually consistent operation, including redirect failover, when the new
     * primary replica is unknown. If not set, the maximum (practically unlimited) value is used.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.primaryFailoverRetries(1);
     * </pre>
     *
     * @param replicaDiscoveryAttempts The limit of replica discovery attempts.
     */
    public DriverOptions replicaDiscoveryAttempts(int replicaDiscoveryAttempts) {
        Validator.requireNonNegative(replicaDiscoveryAttempts, "replicaDiscoveryAttempts");
        driver_options_set_replica_discovery_attempts(nativeObject, replicaDiscoveryAttempts);
        return this;
    }
}
