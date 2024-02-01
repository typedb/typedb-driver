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
using System.Threading;
using Xunit;
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection
{
    public abstract class ConnectionStepsBase : Feature, IDisposable
    {
        public static ITypeDBDriver Driver;
        public static TypeDBOptions SessionOptions; // TODO: For the future tests.
        public static TypeDBOptions TransactionOptions;
        private static bool s_isBeforeAllRan = false;

        protected virtual void BeforeAllOnce()
        {
            Console.WriteLine("BASE: This method could be used to set some global things up once!");

            if (s_isBeforeAllRan)
            {
                return;
            }

            s_isBeforeAllRan = true;
        }

        protected ConnectionStepsBase()
        {
            Console.WriteLine("BASE: BEFORE!");
            BeforeAllOnce();

            SessionOptions = CreateOptions().Infer(true);
            TransactionOptions = CreateOptions().Infer(true);
        }

        public virtual void Dispose()
        {
            Console.WriteLine("BASE: AFTER!");

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
            Console.WriteLine("BASE: ConnectionHasBeenOpened");
            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
        }

        public virtual void ConnectionCloses()
        {
            Console.WriteLine("BASE: ConnectionCloses");
            Driver.Close();
            Driver = null;
        }

        public virtual void ConnectionDoesNotHaveAnyDatabase()
        {
            Console.WriteLine("BASE: ConnectionDoesNotHaveAnyDatabase");
            Assert.NotNull(Driver);
            Assert.True(Driver.IsOpen());
            Assert.Equal(0, Driver.Databases().GetAll().Count);
        }
    }
}
