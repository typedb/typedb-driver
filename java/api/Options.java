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

// TODO: Currently disabled in 3.0

/**
 * TypeDB session and transaction options. <code>Options</code> object can be used to override
 * the default server behaviour.
 */
//public class Options extends NativeObject<com.typedb.driver.jni.Options> {
//    /**
//     * Produces a new <code>Options</code> object.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * Options options = Options();
//     * </pre>
//     */
//    public Options() {
//        super(options_new());
//    }
//
//    /**
//     * Returns the value set for the inference in this <code>Options</code> object.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.infer();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> infer() {
//        if (options_has_infer(nativeObject)) return Optional.of(options_get_infer(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables inference.
//     * Only settable at transaction level and above. Only affects read transactions.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.infer(infer);
//     * </pre>
//     *
//     * @param infer Explicitly enable or disable inference
//     */
//    public Options infer(boolean infer) {
//        options_set_infer(nativeObject, infer);
//        return this;
//    }
//
//    /**
//     * Returns the value set for reasoning tracing in this <code>Options</code> object.
//     * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.traceInference();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> traceInference() {
//        if (options_has_trace_inference(nativeObject)) return Optional.of(options_get_trace_inference(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables reasoning tracing.
//     * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
//     * Should be used with <code>parallel = False</code>.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.traceInference(traceInference);
//     * </pre>
//     *
//     * @param traceInference Explicitly enable or disable reasoning tracing
//     */
//    public Options traceInference(boolean traceInference) {
//        options_set_trace_inference(nativeObject, traceInference);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the explanation in this <code>Options</code> object.
//     * If set to <code>true</code>, explanations for queries are enabled.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.explain();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> explain() {
//        if (options_has_explain(nativeObject)) return Optional.of(options_get_explain(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables explanations.
//     * If set to <code>true</code>, enables explanations for queries. Only affects read transactions.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.explain(explain);
//     * </pre>
//     *
//     * @param explain Explicitly enable or disable explanations
//     */
//    public Options explain(boolean explain) {
//        options_set_explain(nativeObject, explain);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the parallel execution in this <code>Options</code> object.
//     * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.parallel();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> parallel() {
//        if (options_has_parallel(nativeObject)) return Optional.of(options_get_parallel(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables parallel execution.
//     * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.parallel(parallel);
//     * </pre>
//     *
//     * @param parallel Explicitly enable or disable parallel execution
//     */
//    public Options parallel(boolean parallel) {
//        options_set_parallel(nativeObject, parallel);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the prefetching in this <code>Options</code> object.
//     * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
//     * an explicit request for it.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.prefetch();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> prefetch() {
//        if (options_has_prefetch(nativeObject)) return Optional.of(options_get_prefetch(nativeObject));
//        else return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables prefetching.
//     * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
//     * an explicit request for it.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.prefetch(prefetch);
//     * </pre>
//     *
//     * @param prefetch Explicitly enable or disable prefetching
//     */
//    public Options prefetch(boolean prefetch) {
//        options_set_prefetch(nativeObject, prefetch);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the prefetch size in this <code>Options</code> object.
//     * If set, specifies a guideline number of answers that the server should send before the driver
//     * issues a fresh request.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.prefetchSize();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Integer> prefetchSize() {
//        if (options_has_prefetch_size(nativeObject)) return Optional.of(options_get_prefetch_size(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly sets a prefetch size.
//     * If set, specifies a guideline number of answers that the server should send before the driver
//     * issues a fresh request.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.prefetchSize(prefetchSize);
//     * </pre>
//     *
//     * @param prefetchSize Number of answers that the server should send before the driver issues a fresh request
//     */
//    public Options prefetchSize(int prefetchSize) {
//        if (prefetchSize < 1) {
//            throw new TypeDBDriverException(POSITIVE_VALUE_REQUIRED, prefetchSize);
//        }
//        options_set_prefetch_size(nativeObject, prefetchSize);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the session idle timeout in this <code>Options</code> object.
//     * If set, specifies a timeout that allows the server to close sessions if the driver terminates
//     * or becomes unresponsive.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.sessionIdleTimeoutMillis();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Integer> sessionIdleTimeoutMillis() {
//        if (options_has_session_idle_timeout_millis(nativeObject))
//            return Optional.of((int) options_get_session_idle_timeout_millis(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly sets a session idle timeout.
//     * If set, specifies a timeout that allows the server to close sessions if the driver terminates
//     * or becomes unresponsive.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.sessionIdleTimeoutMillis(sessionIdleTimeoutMillis);
//     * </pre>
//     *
//     * @param sessionIdleTimeoutMillis timeout that allows the server to close sessions if the driver terminates
//     *                                 or becomes unresponsive
//     */
//    public Options sessionIdleTimeoutMillis(int sessionIdleTimeoutMillis) {
//        if (sessionIdleTimeoutMillis < 1) {
//            throw new TypeDBDriverException(POSITIVE_VALUE_REQUIRED, sessionIdleTimeoutMillis);
//        }
//        options_set_session_idle_timeout_millis(nativeObject, sessionIdleTimeoutMillis);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the transaction timeout in this <code>Options</code> object.
//     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
//     * in unclosed transactions.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.transactionTimeoutMillis();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Integer> transactionTimeoutMillis() {
//        if (options_has_transaction_timeout_millis(nativeObject))
//            return Optional.of((int) options_get_transaction_timeout_millis(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly set a transaction timeout.
//     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
//     * in unclosed transactions.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.transactionTimeoutMillis(transactionTimeoutMillis);
//     * </pre>
//     *
//     * @param transactionTimeoutMillis Timeout for killing transactions automatically
//     */
//    public Options transactionTimeoutMillis(int transactionTimeoutMillis) {
//        if (transactionTimeoutMillis < 1) {
//            throw new TypeDBDriverException(POSITIVE_VALUE_REQUIRED, transactionTimeoutMillis);
//        }
//        options_set_transaction_timeout_millis(nativeObject, transactionTimeoutMillis);
//        return this;
//    }
//
//    /**
//     * Returns the value set for the schema lock acquire timeout in this <code>Options</code> object.
//     * If set, specifies how long the driver should wait if opening a session or transaction is blocked
//     * by a schema write lock.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.schemaLockAcquireTimeoutMillis();
//     * </pre>
//     */
//    public Optional<Integer> schemaLockAcquireTimeoutMillis() {
//        if (options_has_schema_lock_acquire_timeout_millis(nativeObject))
//            return Optional.of((int) options_get_schema_lock_acquire_timeout_millis(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly sets schema lock acquire timeout.
//     * If set, specifies how long the driver should wait if opening a session or transaction is blocked
//     * by a schema write lock.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.schemaLockAcquireTimeoutMillis(schemaLockAcquireTimeoutMillis);
//     * </pre>
//     *
//     * @param schemaLockAcquireTimeoutMillis How long the driver should wait if opening a session
//     *                                       or transaction is blocked by a schema write lock
//     */
//    public Options schemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis) {
//        if (schemaLockAcquireTimeoutMillis < 1) {
//            throw new TypeDBDriverException(POSITIVE_VALUE_REQUIRED, schemaLockAcquireTimeoutMillis);
//        }
//        options_set_schema_lock_acquire_timeout_millis(nativeObject, schemaLockAcquireTimeoutMillis);
//        return this;
//    }
//
//    /**
//     * Returns the value set for reading data from any replica in this <code>Options</code> object.
//     * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.readAnyReplica();
//     * </pre>
//     */
//    @CheckReturnValue
//    public Optional<Boolean> readAnyReplica() {
//        if (options_has_read_any_replica(nativeObject)) return Optional.of(options_get_read_any_replica(nativeObject));
//        return Optional.empty();
//    }
//
//    /**
//     * Explicitly enables or disables reading data from any replica.
//     * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
//     * Only settable in TypeDB Cloud.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * options.readAnyReplica(readAnyReplica);
//     * </pre>
//     *
//     * @param readAnyReplica Explicitly enable or disable reading data from any replica
//     */
//    public Options readAnyReplica(boolean readAnyReplica) {
//        options_set_read_any_replica(nativeObject, readAnyReplica);
//        return this;
//    }
//}
