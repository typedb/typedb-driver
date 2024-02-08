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

#nullable enable

using System.Collections.Generic;
using System.Linq;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api.Database;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Common.Exception;
using DriverError = com.vaticle.typedb.driver.Common.Exception.Error.Driver;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBDatabase : NativeObjectWrapper<pinvoke.Database>, IDatabase
    {
        public TypeDBDatabase(pinvoke.Database database)
            : base(database)
        {}

        public string Name()
        {
            if (NativeObject == null || !NativeObject.IsOwned()) // TODO: Wrap in a method (and other similar places), consider providing an exception to it.
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            return pinvoke.typedb_driver.database_get_name(NativeObject);
        }

        public string Schema()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            try
            {
                return pinvoke.typedb_driver.database_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public string TypeSchema()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            try
            {
                return pinvoke.typedb_driver.database_type_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public string RuleSchema()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            try
            {
                return pinvoke.typedb_driver.database_rule_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Delete()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            try
            {
                pinvoke.typedb_driver.database_delete(NativeObject?.Released());
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string ToString()
        {
            return Name();
        }

        public ICollection<IDatabase.IReplica> Replicas()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            return new NativeEnumerable<pinvoke.ReplicaInfo>(
                pinvoke.typedb_driver.database_get_replicas_info(NativeObject))
                .Select(obj => new Replica(obj))
                .ToHashSet<IDatabase.IReplica>();
        }

        public IDatabase.IReplica? PrimaryReplica()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }

            pinvoke.ReplicaInfo replicaInfo = pinvoke.typedb_driver.database_get_primary_replica_info(NativeObject);
            if (replicaInfo == null)
            {
                return null;
            }

            return new Replica(replicaInfo);
        }

        public IDatabase.IReplica? PreferredReplica()
        {
            if (NativeObject == null || !NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
            }
            
            pinvoke.ReplicaInfo replicaInfo = pinvoke.typedb_driver.database_get_preferred_replica_info(NativeObject);
            if (replicaInfo == null)
            {
                return null;
            }

            return new Replica(replicaInfo);
        }

        public class Replica : NativeObjectWrapper<pinvoke.ReplicaInfo>, IDatabase.IReplica
        {
            public Replica(pinvoke.ReplicaInfo replicaInfo)
                : base(replicaInfo)
            {}

            public string Address()
            {
                return pinvoke.typedb_driver.replica_info_get_address(NativeObject);
            }

            public bool IsPrimary()
            {
                return pinvoke.typedb_driver.replica_info_is_primary(NativeObject);
            }

            public bool IsPreferred()
            {
                return pinvoke.typedb_driver.replica_info_is_preferred(NativeObject);
            }

            public long Term()
            {
                return pinvoke.typedb_driver.replica_info_get_term(NativeObject);
            }
        }
    }
}
