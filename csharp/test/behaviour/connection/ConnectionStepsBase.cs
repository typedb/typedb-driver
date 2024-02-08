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

using System;
using System.Collections.Generic;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public abstract class ConnectionStepsBase : Feature, IDisposable
    {
        protected ConnectionStepsBase() // "Before"
        {
            SessionOptions = CreateOptions().Infer(true);
            TransactionOptions = CreateOptions().Infer(true);
        }

        public virtual void Dispose() // "After"
        {
            if (Driver == null)
            {
                return;
            }

            foreach (var db in Driver.Databases().GetAll())
            {
                db.Delete();
            }

            foreach (var session in Sessions)
            {
                session.Close();
            }
            Sessions.Clear();

            if (Driver.IsOpen())
            {
                Driver.Close();
            }
        }

        public abstract ITypeDBDriver CreateTypeDBDriver(string address);

        private TypeDBOptions CreateOptions()
        {
            return new TypeDBOptions();
        }

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        [Given(@"connection has been opened")]
        public virtual void ConnectionHasBeenOpened()
        {
            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        [When(@"connection closes")]
        public virtual void ConnectionCloses()
        {
            Driver.Close();
            Driver = null;
        }

        public static void SaveTransaction(ITypeDBTransaction? transaction, ITypeDBSession? session)
        {
            if (!SessionsToTransactions.ContainsKey(session))
            {
                SessionsToTransactions[session] = new List<ITypeDBTransaction?>();
            }

            SessionsToTransactions[session].Add(transaction);
        }

        public static ITypeDBDriver Driver;
        public static List<ITypeDBSession?> Sessions = new List<ITypeDBSession?>();
        public static TypeDBOptions SessionOptions;
        public static TypeDBOptions TransactionOptions;
        public static Dictionary<ITypeDBSession, List<ITypeDBTransaction?>> SessionsToTransactions =
            new Dictionary<ITypeDBSession, List<ITypeDBTransaction?>>();
    }
}
