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

using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// TypeDB transaction options. <c>TransactionOptions</c> can be used to override
    /// the default server behaviour for opened transactions.
    /// </summary>
    public class TransactionOptions : NativeObjectWrapper<Pinvoke.TransactionOptions>
    {
        /// <summary>
        /// Produces a new <c>TransactionOptions</c> object.
        /// </summary>
        /// <example>
        /// <code>
        /// TransactionOptions options = new TransactionOptions
        /// {
        ///     TransactionTimeoutMillis = 10_000,
        ///     SchemaLockAcquireTimeoutMillis = 3000
        /// };
        /// </code>
        /// </example>
        public TransactionOptions()
            : base(Pinvoke.typedb_driver.transaction_options_new())
        {
        }

        /// <summary>
        /// Gets or sets the transaction timeout in milliseconds.
        /// If set, specifies a timeout for killing transactions automatically,
        /// preventing memory leaks in unclosed transactions.
        /// </summary>
        /// <example>
        /// <code>
        /// options.TransactionTimeoutMillis = 5000;
        /// </code>
        /// </example>
        public long? TransactionTimeoutMillis
        {
            get
            {
                if (Pinvoke.typedb_driver.transaction_options_has_transaction_timeout_millis(NativeObject))
                {
                    return Pinvoke.typedb_driver.transaction_options_get_transaction_timeout_millis(NativeObject);
                }
                return null;
            }
            set
            {
                if (value.HasValue)
                {
                    Validator.RequirePositive(value.Value, nameof(TransactionTimeoutMillis));
                    Pinvoke.typedb_driver.transaction_options_set_transaction_timeout_millis(NativeObject, value.Value);
                }
            }
        }

        /// <summary>
        /// Gets or sets the schema lock acquire timeout in milliseconds.
        /// If set, specifies how long the driver should wait if opening a transaction
        /// is blocked by an exclusive schema write lock.
        /// </summary>
        /// <example>
        /// <code>
        /// options.SchemaLockAcquireTimeoutMillis = 3000;
        /// </code>
        /// </example>
        public long? SchemaLockAcquireTimeoutMillis
        {
            get
            {
                if (Pinvoke.typedb_driver.transaction_options_has_schema_lock_acquire_timeout_millis(NativeObject))
                {
                    return Pinvoke.typedb_driver.transaction_options_get_schema_lock_acquire_timeout_millis(NativeObject);
                }
                return null;
            }
            set
            {
                if (value.HasValue)
                {
                    Validator.RequirePositive(value.Value, nameof(SchemaLockAcquireTimeoutMillis));
                    Pinvoke.typedb_driver.transaction_options_set_schema_lock_acquire_timeout_millis(NativeObject, value.Value);
                }
            }
        }
    }
}
