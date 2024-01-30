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

using System;

using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Api.Databases;

namespace com.vaticle.typedb.driver.Api
{
    public interface ITypeDBDriver: IDisposable // TODO: IDisposable instead of Java's AutoCloseable, check and implement later.
    {
        /**
         * Checks whether this connection is presently open.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.IsOpen();
         * </pre>
         */
        bool IsOpen();

        /**
         * The <code>IDatabaseManager</code> for this connection, providing access to database management methods.
         */
        IDatabaseManager Databases();

        /**
         * Opens a session to the given database with default options.
         *
         * @see TypeDBDriver#Session(string, ITypeDBSession.Type, TypeDBOptions)
         */
        ITypeDBSession Session(string database, ITypeDBSession.Type type);

        /**
         * Opens a communication tunnel (session) to the given database on the running TypeDB server.
         * For more information on the methods, available with sessions, see the <code>ITypeDBSession</code> section.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Session(database, sessionType, options);
         * </pre>
         *
         * @param database The name of the database with which the session connects
         * @param type The type of session to be created (DATA or SCHEMA)
         * @param options <code>TypeDBOptions</code> for the session
         */
        ITypeDBSession Session(string database, ITypeDBSession.Type type, TypeDBOptions options);

        /**
         * Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Close()
         * </pre>
         */
        void Close();

        /**
         * The <code>UserManager</code> instance for this connection, providing access to user management methods.
         * Only for TypeDB Cloud.
         */
//        IUser User(); // TODO

        /**
         * Returns the logged-in user for the connection. Only for TypeDB Cloud.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users();
         * </pre>
         */
//        IUserManager Users(); // TODO
    }
}
