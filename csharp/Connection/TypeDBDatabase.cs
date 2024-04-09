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

        private IDatabase.IReplica? _primaryReplica;
        private bool _primaryReplicaFetched = false;

        private IDatabase.IReplica? _preferredReplica;
        private bool _preferredReplicaFetched = false;

        public TypeDBDatabase(Pinvoke.Database database)
            : base(database)
        {}

        public string Name
        {
            get
            {
                Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);
                return _name ?? (_name = Pinvoke.typedb_driver.database_get_name(NativeObject));
            }
        }

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

        public string GetRuleSchema()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            try
            {
                return Pinvoke.typedb_driver.database_rule_schema(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ISet<IDatabase.IReplica> GetReplicas()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);

            return new NativeEnumerable<Pinvoke.ReplicaInfo>(
                Pinvoke.typedb_driver.database_get_replicas_info(NativeObject))
                .Select(obj => new Replica(obj))
                .ToHashSet<IDatabase.IReplica>();
        }

        public IDatabase.IReplica? PrimaryReplica
        {
            get
            {
                Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);
                if (_primaryReplicaFetched)
                {
                    return _primaryReplica;
                }

                Pinvoke.ReplicaInfo replicaInfo = Pinvoke.typedb_driver.database_get_primary_replica_info(NativeObject);
                _primaryReplicaFetched = true;

                if (replicaInfo == null)
                {
                    return null;
                }

                return (_primaryReplica = new Replica(replicaInfo));
            }
        }

        public IDatabase.IReplica? PreferredReplica
        {
            get
            {
                Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.DATABASE_DELETED);
                if (_preferredReplicaFetched)
                {
                    return _preferredReplica;
                }

                Pinvoke.ReplicaInfo replicaInfo = Pinvoke.typedb_driver.database_get_preferred_replica_info(NativeObject);
                _preferredReplicaFetched = true;

                if (replicaInfo == null)
                {
                    return null;
                }

                return (_preferredReplica = new Replica(replicaInfo));
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

        public class Replica : NativeObjectWrapper<Pinvoke.ReplicaInfo>, IDatabase.IReplica
        {
            private string? _serverID;
            private long? _term;

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

            public string ServerID
            {
                get { return _serverID ?? (_serverID = Pinvoke.typedb_driver.replica_info_get_server_id(NativeObject)); }
            }

            public long Term
            {
                get
                {
                    if (!_term.HasValue)
                    {
                        _term = Pinvoke.typedb_driver.replica_info_get_term(NativeObject);
                    }

                    return _term.Value;
                }
            }
        }
    }
}
