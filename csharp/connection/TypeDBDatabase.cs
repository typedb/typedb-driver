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

using com.vaticle.typedb.driver.pinvoke;
using com.vaticle.typedb.driver.Api.Databases;
// TODO:
//using com.vaticle.typedb.driver.Common;
//using com.vaticle.typedb.driver.Common.Exception;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBDatabase: NativeObjectWrapper<pinvoke.Database>, IDatabase
    {
        public TypeDBDatabase(pinvoke.Database database)
            : base(database)
        {}

        public override string Name()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            return pinvoke.database_get_name(NativeObject);
        }

        public override string Schema()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return pinvoke.database_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string TypeSchema()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return database_type_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string RuleSchema()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return database_rule_schema(NativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override void Delete()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                // NOTE: .released() relinquishes ownership of the native object to the Rust side
                database_delete(NativeObject.released());
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

        public override HashSet<Database.IReplica> Replicas()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            // TODO:
            return HashSet<Database.IReplica>();
//            return new NativeIterator<>(database_get_replicas_info(NativeObject)).stream().map(Replica::new).collect(Collectors.toSet());
        }

        public override IReplica? PrimaryReplica() 
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            pinvoke.ReplicaInfo replicaInfo = database_get_primary_replica_info(NativeObject);
            if (replicaInfo == null)
            {
                return null
            }

            return new Replica(replicaInfo);
        }

        public override IReplica? PreferredReplica() 
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }
            
            pinvoke.ReplicaInfo replicaInfo = database_get_preferred_replica_info(NativeObject);
            if (replicaInfo == null)
            {
                return null;
            }

            return new Replica(replicaInfo);
        }

        public static class Replica: NativeObjectWrapper<pinvoke.ReplicaInfo>, Database.IReplica
        {
            Replica(pinvoke.ReplicaInfo replicaInfo)
                : base(replicaInfo)
            {}

            public override string Address()
            {
                return pinvoke.replica_info_get_address(NativeObject);
            }

            public override bool IsPrimary()
            {
                return pinvoke.replica_info_is_primary(NativeObject);
            }

            public override bool IsPreferred()
            {
                return pinvoke.replica_info_is_preferred(NativeObject);
            }

            public override long Term()
            {
                return pinvoke.replica_info_get_term(NativeObject);
            }
        }
    }
}
