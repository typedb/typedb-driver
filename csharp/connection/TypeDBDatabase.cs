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

using System.Collections.Generic;
using System.Linq;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

using DriverError = Vaticle.Typedb.Driver.Common.Error.Driver;

namespace Vaticle.Typedb.Driver.Connection
{
    public class TypeDBDatabase : NativeObjectWrapper<Pinvoke.Database>, IDatabase
    {
        public TypeDBDatabase(Pinvoke.Database database)
            : base(database)
        {}

        public string Name
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned()) // TODO: Wrap in a method (and other similar places), consider providing an exception to it.
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                return Pinvoke.typedb_driver.database_get_name(NativeObject);
            }
        }

        public string Schema
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                try
                {
                    return Pinvoke.typedb_driver.database_schema(NativeObject);
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        public string TypeSchema
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                try
                {
                    return Pinvoke.typedb_driver.database_type_schema(NativeObject);
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        public string RuleSchema
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                try
                {
                    return Pinvoke.typedb_driver.database_rule_schema(NativeObject);
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        public ICollection<IDatabase.IReplica> Replicas
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                return new NativeEnumerable<Pinvoke.ReplicaInfo>(
                    Pinvoke.typedb_driver.database_get_replicas_info(NativeObject))
                    .Select(obj => new Replica(obj))
                    .ToHashSet<IDatabase.IReplica>();
            }
        }

        public IDatabase.IReplica? PrimaryReplica
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                Pinvoke.ReplicaInfo replicaInfo = Pinvoke.typedb_driver.database_get_primary_replica_info(NativeObject);
                if (replicaInfo == null)
                {
                    return null;
                }

                return new Replica(replicaInfo);
            }
        }

        public IDatabase.IReplica? PreferredReplica
        {
            get
            {
                if (NativeObject == null || !NativeObject.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.DATABASE_DELETED);
                }

                Pinvoke.ReplicaInfo replicaInfo = Pinvoke.typedb_driver.database_get_preferred_replica_info(NativeObject);
                if (replicaInfo == null)
                {
                    return null;
                }

                return new Replica(replicaInfo);
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

        public class Replica : NativeObjectWrapper<Pinvoke.ReplicaInfo>, IDatabase.IReplica
        {
            public Replica(Pinvoke.ReplicaInfo replicaInfo)
                : base(replicaInfo)
            {}

            public bool IsPrimary()
            {
                return Pinvoke.typedb_driver.replica_info_is_primary(NativeObject);
            }

            public bool IsPreferred()
            {
                return Pinvoke.typedb_driver.replica_info_is_preferred(NativeObject);
            }

            public string Address
            {
                get { return Pinvoke.typedb_driver.replica_info_get_address(NativeObject); }
            }

            public long Term
            {
                get { return Pinvoke.typedb_driver.replica_info_get_term(NativeObject); }
            }
        }
    }
}
