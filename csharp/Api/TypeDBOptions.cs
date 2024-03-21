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

using TypeDB.Driver;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Api
{
    /**
     * TypeDB session and transaction options. <code>TypeDBOptions</code> object can be used to override
     * the default server behaviour.
     */
    public class TypeDBOptions : NativeObjectWrapper<Pinvoke.Options>
    {
        /**
         * Produces a new <code>TypeDBOptions</code> object.
         *
         * <h3>Examples</h3>
         * <pre>
         * TypeDBOptions options = TypeDBOptions();
         * </pre>
         */
        public TypeDBOptions()
            : base(Pinvoke.typedb_driver.options_new())
        {}

        /**
         * Returns the value set for the inference in this <code>TypeDBOptions</code> object.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Infer();
         * </pre>
         */
        public bool? Infer() 
        {
            if (Pinvoke.typedb_driver.options_has_infer(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_infer(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables inference.
         * Only settable at transaction level and above. Only affects read transactions.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Infer(infer);
         * </pre>
         *
         * @param infer Explicitly enable or disable inference
         */
        public TypeDBOptions Infer(bool infer)
        {
            Pinvoke.typedb_driver.options_set_infer(NativeObject, infer);
            return this;
        }

        /**
         * Returns the value set for reasoning tracing in this <code>TypeDBOptions</code> object.
         * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.TraceInference();
         * </pre>
         */
        public bool? TraceInference()
        {
            if (Pinvoke.typedb_driver.options_has_trace_inference(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_trace_inference(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables reasoning tracing.
         * If set to <code>true</code>, reasoning tracing graphs are output in the logging directory.
         * Should be used with <code>parallel = False</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.TraceInference(traceInference);
         * </pre>
         *
         * @param traceInference Explicitly enable or disable reasoning tracing
         */
        public TypeDBOptions TraceInference(bool traceInference)
        {
            Pinvoke.typedb_driver.options_set_trace_inference(NativeObject, traceInference);
            return this;
        }

        /**
         * Returns the value set for the explanation in this <code>TypeDBOptions</code> object.
         * If set to <code>true</code>, explanations for queries are enabled.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Explain();
         * </pre>
         */
        public bool? Explain()
        {
            if (Pinvoke.typedb_driver.options_has_explain(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_explain(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables explanations.
         * If set to <code>true</code>, enables explanations for queries. Only affects read transactions.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Explain(explain);
         * </pre>
         *
         * @param explain Explicitly enable or disable explanations
         */
        public TypeDBOptions Explain(bool explain)
        {
            Pinvoke.typedb_driver.options_set_explain(NativeObject, explain);
            return this;
        }

        /**
         * Returns the value set for the parallel execution in this <code>TypeDBOptions</code> object.
         * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Parallel();
         * </pre>
         */
        public bool? Parallel()
        {
            if (Pinvoke.typedb_driver.options_has_parallel(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_parallel(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables parallel execution.
         * If set to <code>true</code>, the server uses parallel instead of single-threaded execution.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Parallel(parallel);
         * </pre>
         *
         * @param parallel Explicitly enable or disable parallel execution
         */
        public TypeDBOptions Parallel(bool parallel)
        {
            Pinvoke.typedb_driver.options_set_parallel(NativeObject, parallel);
            return this;
        }

        /**
         * Returns the value set for the prefetching in this <code>TypeDBOptions</code> object.
         * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
         * an explicit request for it.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Prefetch();
         * </pre>
         */
        public bool? Prefetch()
        {
            if (Pinvoke.typedb_driver.options_has_prefetch(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_prefetch(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables prefetching.
         * If set to <code>true</code>, the first batch of answers is streamed to the driver even without
         * an explicit request for it.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.Prefetch(prefetch);
         * </pre>
         *
         * @param prefetch Explicitly enable or disable prefetching
         */
        public TypeDBOptions Prefetch(bool prefetch)
        {
            Pinvoke.typedb_driver.options_set_prefetch(NativeObject, prefetch);
            return this;
        }

        /**
         * Returns the value set for the prefetch size in this <code>TypeDBOptions</code> object.
         * If set, specifies a guideline number of answers that the server should send before the driver
         * issues a fresh request.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.PrefetchSize();
         * </pre>
         */
        public int? PrefetchSize()
        {
            if (Pinvoke.typedb_driver.options_has_prefetch_size(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_prefetch_size(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly sets a prefetch size.
         * If set, specifies a guideline number of answers that the server should send before the driver
         * issues a fresh request.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.PrefetchSize(prefetchSize);
         * </pre>
         *
         * @param prefetchSize Number of answers that the server should send before the driver issues a fresh request
         */
        public TypeDBOptions PrefetchSize(int prefetchSize)
        {
            Validator.ThrowIfTrue(() => prefetchSize < 1, DriverError.POSITIVE_VALUE_REQUIRED, prefetchSize);

            Pinvoke.typedb_driver.options_set_prefetch_size(NativeObject, prefetchSize);
            return this;
        }

        /**
         * Returns the value set for the session idle timeout in this <code>TypeDBOptions</code> object.
         * If set, specifies a timeout that allows the server to close sessions if the driver terminates
         * or becomes unresponsive.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.SessionIdleTimeoutMillis();
         * </pre>
         */
        public int? SessionIdleTimeoutMillis()
        {
            if (Pinvoke.typedb_driver.options_has_session_idle_timeout_millis(NativeObject))
            {
                return (int)Pinvoke.typedb_driver.options_get_session_idle_timeout_millis(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly sets a session idle timeout.
         * If set, specifies a timeout that allows the server to close sessions if the driver terminates
         * or becomes unresponsive.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.SessionIdleTimeoutMillis(sessionIdleTimeoutMillis);
         * </pre>
         *
         * @param sessionIdleTimeoutMillis timeout that allows the server to close sessions if the driver terminates
         *                                 or becomes unresponsive.
         */
        public TypeDBOptions SessionIdleTimeoutMillis(int sessionIdleTimeoutMillis)
        {
            Validator.ThrowIfTrue(
                () => sessionIdleTimeoutMillis < 1,
                DriverError.POSITIVE_VALUE_REQUIRED,
                sessionIdleTimeoutMillis);

            Pinvoke.typedb_driver.options_set_session_idle_timeout_millis(NativeObject, sessionIdleTimeoutMillis);
            return this;
        }

        /**
         * Returns the value set for the transaction timeout in this <code>TypeDBOptions</code> object.
         * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
         * in unclosed transactions.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.TransactionTimeoutMillis();
         * </pre>
         */
        public int? TransactionTimeoutMillis()
        {
            if (Pinvoke.typedb_driver.options_has_transaction_timeout_millis(NativeObject))
            {
                return (int)Pinvoke.typedb_driver.options_get_transaction_timeout_millis(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly set a transaction timeout.
         * If set, specifies a timeout for killing transactions automatically, preventing memory leaks
         * in unclosed transactions.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.TransactionTimeoutMillis(transactionTimeoutMillis);
         * </pre>
         *
         * @param transactionTimeoutMillis Timeout for killing transactions automatically.
         */
        public TypeDBOptions TransactionTimeoutMillis(int transactionTimeoutMillis)
        {
            Validator.ThrowIfTrue(
                () => transactionTimeoutMillis < 1,
                DriverError.POSITIVE_VALUE_REQUIRED,
                transactionTimeoutMillis);

            Pinvoke.typedb_driver.options_set_transaction_timeout_millis(NativeObject, transactionTimeoutMillis);
            return this;
        }

        /**
         * Returns the value set for the schema lock acquire timeout in this <code>TypeDBOptions</code> object.
         * If set, specifies how long the driver should wait if opening a session or transaction is blocked
         * by a schema write lock.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.SchemaLockAcquireTimeoutMillis();
         * </pre>
         */
        public int? SchemaLockAcquireTimeoutMillis()
        {
            if (Pinvoke.typedb_driver.options_has_schema_lock_acquire_timeout_millis(NativeObject))
            {
                return (int)Pinvoke.typedb_driver.options_get_schema_lock_acquire_timeout_millis(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly sets schema lock acquire timeout.
         * If set, specifies how long the driver should wait if opening a session or transaction is blocked
         * by a schema write lock.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.SchemaLockAcquireTimeoutMillis(schemaLockAcquireTimeoutMillis);
         * </pre>
         *
         * @param schemaLockAcquireTimeoutMillis How long the driver should wait if opening a session
         *                                       or transaction is blocked by a schema write lock.
         */
        public TypeDBOptions SchemaLockAcquireTimeoutMillis(int schemaLockAcquireTimeoutMillis)
        {
            Validator.ThrowIfTrue(
                () => schemaLockAcquireTimeoutMillis < 1,
                DriverError.POSITIVE_VALUE_REQUIRED,
                schemaLockAcquireTimeoutMillis);

            Pinvoke.typedb_driver.options_set_schema_lock_acquire_timeout_millis(NativeObject, schemaLockAcquireTimeoutMillis);
            return this;
        }

        /**
         * Returns the value set for reading data from any replica in this <code>TypeDBOptions</code> object.
         * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.ReadAnyReplica();
         * </pre>
         */
        public bool? ReadAnyReplica()
        {
            if (Pinvoke.typedb_driver.options_has_read_any_replica(NativeObject))
            {
                return Pinvoke.typedb_driver.options_get_read_any_replica(NativeObject);
            }
            return null;
        }

        /**
         * Explicitly enables or disables reading data from any replica.
         * If set to <code>True</code>, enables reading data from any replica, potentially boosting read throughput.
         * Only settable in TypeDB Cloud.
         *
         * <h3>Examples</h3>
         * <pre>
         * options.ReadAnyReplica(readAnyReplica);
         * </pre>
         *
         * @param readAnyReplica Explicitly enable or disable reading data from any replica
         */
        public TypeDBOptions ReadAnyReplica(bool readAnyReplica)
        {
            Pinvoke.typedb_driver.options_set_read_any_replica(NativeObject, readAnyReplica);
            return this;
        }
    }
}
