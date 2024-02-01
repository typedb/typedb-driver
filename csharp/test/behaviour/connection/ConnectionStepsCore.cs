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
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection
{
    public class ConnectionSteps : ConnectionStepsBase
    {
        protected override void BeforeAllOnce()
        {
            base.BeforeAllOnce();
            Console.WriteLine("CORE: This method could be used to set some global things up once!");
        }

        public ConnectionSteps()
            : base()
        {}

        public override void Dispose()
        {
            base.Dispose();
        }

        public override ITypeDBDriver CreateTypeDBDriver(string address)
        {
            return TypeDB.CoreDriver(address);
        }

        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public override void TypeDBStarts()
        {
            Console.WriteLine("CORE: TypeDB Starts, nothing here for now..."); // TODO
        }

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public override void ConnectionOpensWithDefaultAuthentication()
        {
            Driver = CreateTypeDBDriver(TypeDB.s_DefaultAddress);
        }

        [Given(@"connection has been opened")]
        public override void ConnectionHasBeenOpened()
        {
            base.ConnectionHasBeenOpened();
        }

        [When(@"connection closes")]
        public override void ConnectionCloses()
        {
            base.ConnectionCloses();
        }

        [Given(@"connection does not have any database")]
        [Then(@"connection does not have any database")]
        public override void ConnectionDoesNotHaveAnyDatabase()
        {
            base.ConnectionDoesNotHaveAnyDatabase();
        }
    }
}
