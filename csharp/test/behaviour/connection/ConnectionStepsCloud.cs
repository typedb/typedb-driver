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
using Xunit.Gherkin.Quick;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Test.Behaviour.Connection;

namespace com.vaticle.typedb.driver.Test.Behaviour.Connection
{
    public class ConnectionStepsCloud: ConnectionStepsBase
    {
        protected override void BeforeAllOnce()
        {
            base.BeforeAllOnce();
//            TypeDBCloudRunner cloudRunner = TypeDBCloudRunner.create(Paths.get("."), 1, serverOptions);
//            TypeDBSingleton.setTypeDBRunner(cloudRunner);
//            cloudRunner.start();
            Console.WriteLine("CLOUD Before All!");
        }

        public ConnectionStepsCloud()
            : base()
        {Console.WriteLine("CLOUD Constr!");}

        public override void Dispose()
        {
            base.Dispose();

//            driver = createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address());
//            driver.users().all().forEach(user -> {
//                if (!user.username().equals("admin")) {
//                    driver.users().delete(user.username());
//                }
//            });
//
//            driver.close();
//            try
//            {
//                // sleep for eventual consistency to catch up with database deletion on all servers
//                Thread.sleep(100);
//            }
//            catch (InterruptedException e)
//            {
//                throw new RuntimeException(e);
//            }

            Console.WriteLine("CLOUD Dispose!");
        }

        // TODO: Void instead of ITypeDBDriver for now
        public override void CreateTypeDBDriver(string address)
        {
//            return CreateTypeDBDriver(address, "admin", "password", false);
        }

        private void CreateTypeDBDriver(string address, string username, string password, bool tlsEnabled)
        {
//            return TypeDB.CloudDriver(address, new TypeDBCredential(username, password, tlsEnabled));
        }

        [When(@"typedb starts")]
        public override void TypeDBStarts()
        {
            base.TypeDBStarts();
        }

        [When(@"connection opens with default authentication")]
        public override void ConnectionOpensWithDefaultAuthentication()
        {
//            driver = CreateTypeDBDriver(TypeDBSingleton.GetTypeDBRunner().Address());
            Console.WriteLine("Cloud: ConnectionOpensWithDefaultAuthentication");
        }

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
//            if (driver != null)
//            {
//                driver.close();
//                driver = null;
//            }
//
//            driver = CreateTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address(), username, password, false);
            Console.WriteLine("Cloud: ConnectionOpensWithAuthentication");
        }

        [When(@"connection opens with authentication: {word}, {word}; throws exception")]
        public void ConnectionOpensWithAuthenticationThrowsException(string username, string password)
        {
//            assertThrows(() -> createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address(), username, password, false));
            Console.WriteLine("Cloud: ConnectionOpensWithAuthenticationThrowsException");
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
        public override void ConnectionDoesNotHaveAnyDatabase()
        {
            base.ConnectionDoesNotHaveAnyDatabase();
        }

        [Given(@"typedb has configuration")]
        public void TypeDBHasConfiguration(Dictionary<string, string> map)
        {   // TODO: Do we need it?
            // no-op: configuration tests are only run on the backend themselves
        }
    }
}
