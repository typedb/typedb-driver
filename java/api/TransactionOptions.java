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
import com.typedb.driver.common.Validator;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.transaction_options_get_schema_lock_acquire_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.transaction_options_get_transaction_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.transaction_options_has_schema_lock_acquire_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.transaction_options_has_transaction_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.transaction_options_new;
import static com.typedb.driver.jni.typedb_driver.transaction_options_set_schema_lock_acquire_timeout_millis;
import static com.typedb.driver.jni.typedb_driver.transaction_options_set_transaction_timeout_millis;


/**
 * TypeDB transaction options. <code>TransactionOptions</code> object can be used to override
 * the default server behaviour for opened transactions.
 */
public class TransactionOptions extends NativeObject<com.typedb.driver.jni.TransactionOptions> {
    /**
     * Produces a new <code>TransactionOptions</code> object.
     *
     * <h3>Examples</h3>
     * <pre>
     * TransactionOptions options = TransactionOptions();
     * </pre>
     */
    public TransactionOptions() {
        super(transaction_options_new());
    }

    /**
     * Returns the value set for the transaction timeout in this <code>TransactionOptions</code> object.
     * If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.transactionTimeoutMillis();
     * </pre>
     */
    @CheckReturnValue
    public Optional<Integer> transactionTimeoutMillis() {
        if (transaction_options_has_transaction_timeout_millis(nativeObject))
            return Optional.of((int) transaction_options_get_transaction_timeout_millis(nativeObject));
        return Optional.empty();
    }

    /**
     * Explicitly set a transaction timeout.
     * If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.transactionTimeoutMillis(transactionTimeoutMillis);
     * </pre>
     *
     * @param transactionTimeoutMillis Timeout for killing transactions automatically
     */
    public TransactionOptions transactionTimeoutMillis(int transactionTimeoutMillis) {
        Validator.requirePositive(transactionTimeoutMillis, "transactionTimeoutMillis");
        transaction_options_set_transaction_timeout_millis(nativeObject, transactionTimeoutMillis);
        return this;
    }

    /**
     * Returns the value set for the schema lock acquire timeout in this <code>TransactionOptions</code> object.
     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.schemaLockAcquireTimeoutMillis();
     * </pre>
     */
    public Optional<Integer> schemaLockAcquireTimeoutMillis() {
        if (transaction_options_has_schema_lock_acquire_timeout_millis(nativeObject))
            return Optional.of((int) transaction_options_get_schema_lock_acquire_timeout_millis(nativeObject));
        return Optional.empty();
    }

    /**
     * Explicitly sets schema lock acquire timeout.
     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.schemaLockAcquireTimeoutMillis(schemaLockAcquireTimeoutMillis);
     * </pre>
     *
     * @param schemaLockAcquireTimeoutMillis How long the driver should wait if opening a transaction
     *                                       is blocked by a schema write lock
     */
    public TransactionOptions schemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis) {
        Validator.requirePositive(schemaLockAcquireTimeoutMillis, "schemaLockAcquireTimeoutMillis");
        transaction_options_set_schema_lock_acquire_timeout_millis(nativeObject, schemaLockAcquireTimeoutMillis);
        return this;
    }
}
