/*
 * Copyright (C) 2022 Vaticle
 *
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
#pragma once

#include "typedb/common/native.hpp"

namespace TypeDB {

// Forward declarations for friendship
class Session;
class Transaction;
class QueryManager;

/**
 * \brief TypeDB Session and Transaction options.
 *
 * <code>Options</code> can be used to override the default server behaviour.
 */
class Options {
public:
    /**
     * Produces a new <code>Options</code> object.
     *
     * <h3>Examples</h3>
     * <pre>
     * TypeDBOptions options = TypeDBOptions();
     * </pre>
     */
    Options();
    Options(const Options&) = delete;
    Options(Options&&) = default;
    Options& operator=(const Options&) = delete;
    Options& operator=(Options&&) = default;
    ~Options() = default;

    /**
     * Returns the value set for the inference in this <code>TypeDBOptions</code> object.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.infer();
     * </pre>
     */
    std::optional<bool> infer();

    /**
     * Returns the value set for reasoning tracing in this <code>TypeDBOptions</code> object.
     * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.traceInference();
     * </pre>
     */
    std::optional<bool> traceInference();

    /**
     * Returns the value set for the explanation in this <code>TypeDBOptions</code> object.
     * If set to <code>true</code>, explanations for queries are enabled.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.explain();
     * </pre>
     */
    std::optional<bool> explain();

    /**
     * Returns the value set for the parallel execution in this <code>TypeDBOptions</code> object.
     * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.parallel();
     * </pre>
     */
    std::optional<bool> parallel();

    /**
     * Returns the value set for the prefetching in this <code>TypeDBOptions</code> object.
     * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
     * an explicit request for it.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.prefetch();
     * </pre>
     */
    std::optional<bool> prefetch();

    /**
     * Returns the value set for the prefetch size in this <code>TypeDBOptions</code> object.
     * If set, specifies a guideline number of answers that the server should send before the driver
     * issues a fresh request.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.prefetchSize();
     * </pre>
     */
    std::optional<int32_t> prefetchSize();

    /**
     * Returns the value set for the session idle timeout in this <code>TypeDBOptions</code> object.
     * If set, specifies a timeout that allows the server to close sessions if the driver terminates
     * or becomes unresponsive.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.sessionIdleTimeoutMillis();
     * </pre>
     */
    std::optional<int64_t> sessionIdleTimeoutMillis();

    /**
     * Returns the value set for the transaction timeout in this <code>TypeDBOptions</code> object.
     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
     * in unclosed transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.transactionTimeoutMillis();
     * </pre>
     */
    std::optional<int64_t> transactionTimeoutMillis();

    /**
     * Returns the value set for the schema lock acquire timeout in this <code>TypeDBOptions</code> object.
     * If set, specifies how long the driver should wait if opening a session or transaction is blocked
     * by a schema write lock.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.schemaLockAcquireTimeoutMillis();
     * </pre>
     */
    std::optional<int64_t> schemaLockAcquireTimeoutMillis();

    /**
     * Returns the value set for reading data from any replica in this <code>TypeDBOptions</code> object.
     * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.readAnyReplica();
     * </pre>
     */
    std::optional<bool> readAnyReplica();

    /**
     * Explicitly enables or disables inference.
     * Only settable at transaction level and above. Only affects read transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.infer(infer);
     * </pre>
     *
     * @param infer Explicitly enable or disable inference
     */
    Options& infer(bool infer);

    /**
     * Explicitly enables or disables reasoning tracing.
     * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
     * Should be used with <code>parallel = False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.traceInference(traceInference);
     * </pre>
     *
     * @param traceInference Explicitly enable or disable reasoning tracing
     */
    Options& traceInference(bool traceInference);

    /**
     * Explicitly enables or disables explanations.
     * If set to <code>true</code>, enables explanations for queries. Only affects read transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.explain(explain);
     * </pre>
     *
     * @param explain Explicitly enable or disable explanations
     */
    Options& explain(bool explain);

    /**
     * Explicitly enables or disables parallel execution.
     * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.parallel(parallel);
     * </pre>
     *
     * @param parallel Explicitly enable or disable parallel execution
     */
    Options& parallel(bool parallel);

    /**
     * Explicitly enables or disables prefetching.
     * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
     * an explicit request for it.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.prefetch(prefetch);
     * </pre>
     *
     * @param prefetch Explicitly enable or disable prefetching
     */
    Options& prefetch(bool prefetch);

    /**
     * Explicitly sets a prefetch size.
     * If set, specifies a guideline number of answers that the server should send before the driver
     * issues a fresh request.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.prefetchSize(prefetchSize);
     * </pre>
     *
     * @param prefetchSize Number of answers that the server should send before the driver issues a fresh request
     */
    Options& prefetchSize(int32_t prefetchSize);

    /**
     * Explicitly sets a session idle timeout.
     * If set, specifies a timeout that allows the server to close sessions if the driver terminates
     * or becomes unresponsive.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.sessionIdleTimeoutMillis(sessionIdleTimeoutMillis);
     * </pre>
     *
     * @param sessionIdleTimeoutMillis timeout that allows the server to close sessions if the driver terminates
     *                                 or becomes unresponsive
     */
    Options& sessionIdleTimeoutMillis(int64_t timeoutMillis);

    /**
     * Explicitly set a transaction timeout.
     * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
     * in unclosed transactions.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.transactionTimeoutMillis(transactionTimeoutMillis);
     * </pre>
     *
     * @param transactionTimeoutMillis Timeout for killing transactions automatically
     */
    Options& transactionTimeoutMillis(int64_t timeoutMillis);

    /**
     * Explicitly sets schema lock acquire timeout.
     * If set, specifies how long the driver should wait if opening a session or transaction is blocked
     * by a schema write lock.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.schemaLockAcquireTimeoutMillis(schemaLockAcquireTimeoutMillis);
     * </pre>
     *
     * @param schemaLockAcquireTimeoutMillis How long the driver should wait if opening a session
     *                                       or transaction is blocked by a schema write lock
     */
    Options& schemaLockAcquireTimeoutMillis(int64_t timeoutMillis);

    /**
     * Explicitly enables or disables reading data from any replica.
     * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
     * Only settable in TypeDB Cloud.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.readAnyReplica(readAnyReplica);
     * </pre>
     *
     * @param readAnyReplica Explicitly enable or disable reading data from any replica
     */
    Options& readAnyReplica(bool readAnyReplica);

private:
    NativePointer<_native::Options> optionsNative;
    _native::Options* getNative() const;


    friend class DatabaseManager;
    friend class Session;
    friend class QueryManager;
};

}  // namespace TypeDB
