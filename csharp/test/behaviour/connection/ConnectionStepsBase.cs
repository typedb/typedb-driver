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
using System.Collections.Generic;
using Xunit;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Common;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection
{
    public abstract class ConnectionStepsBase : IDisposable
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
        }

        public abstract ITypeDBDriver CreateTypeDBDriver(string address);

        private TypeDBOptions CreateOptions()
        {
            return new TypeDBOptions();
        }

        public abstract void TypeDBStarts();

        public abstract void ConnectionOpensWithDefaultAuthentication();

        public virtual void ConnectionHasBeenOpened()
        {
            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        public virtual void ConnectionCloses()
        {
            Driver.Close();
            Driver = null;
        }

        public static ITypeDBDriver Driver;
        public static List<ITypeDBSession?> Sessions = new List<ITypeDBSession?>();
        public static TypeDBOptions SessionOptions; // TODO: For the future tests.
        public static TypeDBOptions TransactionOptions;
    }
}
