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
    public class TypeDBDatabase : NativeObjectWrapper<Pinvoke.Database>, IDatabase
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

        public void Delete()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            try
            {
                Pinvoke.typedb_driver.database_delete(NativeObject?.Released());
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string ToString()
        {
            return Name;
        }
    }
}
