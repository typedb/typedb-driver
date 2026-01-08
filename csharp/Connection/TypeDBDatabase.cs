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

using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Connection
{
    /// <summary>
    /// A TypeDB database.
    /// </summary>
    public class TypeDBDatabase : NativeObjectWrapper<Pinvoke.Database>, IDatabase, System.IDisposable
    {
        private string? _name;

        public TypeDBDatabase(Pinvoke.Database database)
            : base(database)
        {}

        /// <inheritdoc/>
        public string Name
        {
            get
            {
                Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);
                return _name ?? (_name = Pinvoke.typedb_driver.database_get_name(NativeObject));
            }
        }

        /// <inheritdoc/>
        public string GetSchema()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            try
            {
                return Pinvoke.typedb_driver.database_schema(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public string GetTypeSchema()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            try
            {
                return Pinvoke.typedb_driver.database_type_schema(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public void Delete()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            try
            {
                // Note: database_delete only consumes the handle if delete succeeds.
                // If delete fails (e.g., database has open transactions), the handle
                // remains valid and we should NOT release ownership.
                // Pass NativeObject directly (not Released()) so we retain ownership on error.
                bool success = Pinvoke.typedb_driver.database_delete(NativeObject);
                if (success)
                {
                    // Delete succeeded, native side consumed the handle.
                    // Release ownership to prevent finalizer from double-freeing.
                    NativeObject.Released();
                }
                // If success is false, SWIG will throw due to check_error()
            }
            catch (Pinvoke.Error e)
            {
                // On error, NativeObject still owns the handle (which is still valid in Rust)
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public override string ToString()
        {
            return Name;
        }

        /// <summary>
        /// Disposes the database wrapper to free native resources immediately.
        /// </summary>
        public void Dispose()
        {
            // Dispose the underlying SWIG object to free native memory immediately
            // instead of waiting for GC finalization (which can cause race conditions)
            if (NativeObject is System.IDisposable disposable)
            {
                disposable.Dispose();
            }
        }
    }
}
