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
    public class TypeDBDatabase: NativeObject<pinvoke.Database>, IDatabase
    {
        public TypeDBDatabase(pinvoke.Database database)
            : base(database)
        {}

        public override string Name()
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            return pinvoke.database_get_name(nativeObject);
        }

        public override string Schema()
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return pinvoke.database_schema(nativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string TypeSchema()
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return database_type_schema(nativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override string RuleSchema()
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                return database_rule_schema(nativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override void Delete()
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            try
            {
                // NOTE: .released() relinquishes ownership of the native object to the Rust side
                database_delete(nativeObject.released());
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
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            // TODO:
            return HashSet<Database.IReplica>();
//            return new NativeIterator<>(database_get_replicas_info(nativeObject)).stream().map(Replica::new).collect(Collectors.toSet());
        }

        public override IReplica? PrimaryReplica() 
        {
            if (!nativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }

            pinvoke.ReplicaInfo replicaInfo = database_get_primary_replica_info(nativeObject);
            if (replicaInfo == null)
            {
                return null
            }

            return new Replica(replicaInfo);
        }

        public override IReplica? PreferredReplica() 
        {
            if (!nativeObject.IsOwned()) 
            {
                throw new TypeDBDriverException(DATABASE_DELETED);
            }
            
            pinvoke.ReplicaInfo replicaInfo = database_get_preferred_replica_info(nativeObject);
            if (replicaInfo == null)
            {
                return null;
            }

            return new Replica(replicaInfo);
        }

        public static class Replica: NativeObject<pinvoke.ReplicaInfo>, Database.IReplica
        {
            Replica(pinvoke.ReplicaInfo replicaInfo)
                : base(replicaInfo)
            {}

            public override string Address()
            {
                return pinvoke.replica_info_get_address(nativeObject);
            }

            public override bool IsPrimary()
            {
                return pinvoke.replica_info_is_primary(nativeObject);
            }

            public override bool IsPreferred()
            {
                return pinvoke.replica_info_is_preferred(nativeObject);
            }

            public override long Term()
            {
                return pinvoke.replica_info_get_term(nativeObject);
            }
        }
    }
}
