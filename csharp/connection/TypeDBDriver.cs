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

using com.vaticle.typedb.driver.Api;
// TODO:
//import com.vaticle.typedb.driver.Common;
//import com.vaticle.typedb.driver.Common.Exception;
//import com.vaticle.typedb.driver.User;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBDriver: NativeObject<pinvoke.Connection>, ITypeDBDriver
    {
        private sealed readonly IUserManager userMgr;
        private sealed readonly IDatabaseManager databaseMgr;

        public TypeDBDriverImpl(string address)
            : this(OpenCore(address))
        {}

        public TypeDBDriver(HashSet<string> initAddresses, TypeDBCredential credential)
            : this(OpenCloud(initAddresses, credential))
        {}

        private TypeDBDriver(pinvoke.Connection connection)
            : base(connection)
        {
            databaseMgr = new TypeDBDatabaseManager(this.nativeObject);
            userMgr = new UserManager(this.nativeObject);
        }

        private static pinvoke.Connection openCore(String address)
        {
            try
            {
                return pinvoke.connection_open_core(address);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private static pinvoke.Connection OpenCloud(HashSet<string> initAddresses, TypeDBCredential credential)
        {
            try
            {
                return pinvoke.connection_open_cloud(initAddresses.toArray(new string[0]), credential.nativeObject);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override bool IsOpen()
        {
            return pinvoke.connection_is_open(nativeObject);
        }

        public override User user()
        {
            return userMgr.GetCurrentUser();
        }

        public override IUserManager Users()
        {
            return userMgr;
        }

        public override IDatabaseManager Databases()
        {
            return databaseMgr;
        }

        public override ITypeDBSession Session(string database, TypeDBSession.Type type)
        {
            return Session(database, type, new TypeDBOptions());
        }

        public override ITypeDBSession Session(string database, TypeDBSession.Type type, TypeDBOptions options)
        {
            return new TypeDBSession(databases(), database, type, options);
        }

        public override void Close()
        {
            if (!IsOpen())
            {
                return;
            }

            try
            {
                connection_force_close(nativeObject);
            }
            catch (pinvoke.Error error)
            {
                throw new TypeDBDriverException(error);
            }
        }
    }
}
