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
            Console.WriteLine("CLOUD: This method could be used to set some global things up once!");
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
            return CreateTypeDBDriver(address);
        }

        private ITypeDBDriver CreateTypeDBDriver(
            ICollection<string> addresses = null,
            string username = null,
            string password = null,
            string certificatesPath = null)
        {
            return TypeDB.CloudDriver(
                addresses ?? s_defaultAddresses,
                new TypeDBCredential(
                    username ?? _DefaultUsername,
                    password ?? _DefaultPassword,
                    certificatesPath ?? s_defaultCertificatesPath));
        }

        private ITypeDBDriver CreateTypeDBDriver(string address, string username = null, string password = null)
        {
            return CreateTypeDBDriver(new string[]{address}, username, password);
        }

        [Given(@"typedb starts")]
        [When(@"typedb starts")]
        public override void TypeDBStarts()
        {
            Console.WriteLine("CLOUD: TypeDB Starts, nothing here for now..."); // TODO
        }

        [Given(@"connection opens with default authentication")]
        [When(@"connection opens with default authentication")]
        public override void ConnectionOpensWithDefaultAuthentication()
        {
            Console.WriteLine("CLOUD: ConnectionOpensWithDefaultAuthentication");
            Driver = CreateTypeDBDriver();
        }

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            Console.WriteLine("CLOUD: ConnectionOpensWithAuthentication");
            if (Driver != null)
            {
                Driver.Close();
                Driver = null;
            }

            Driver = CreateTypeDBDriver(TypeDB.s_DefaultAddress, username, password);
        }

        [When(@"connection opens with authentication: {word}, {word}; throws exception")]
        public void ConnectionOpensWithAuthenticationThrowsException(string username, string password)
        {
            Console.WriteLine("CLOUD: ConnectionOpensWithAuthenticationThrowsException");
            // TODO:
//            assertThrows(() -> createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address(), username, password, false));
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

        private static readonly string[] s_defaultAddresses =
        {
            "localhost:11729",
//            "localhost:21729", // Should run only with one address!
//            "localhost:31729"
        };
        private static readonly string s_defaultCertificatesPath = Environment.GetEnvironmentVariable("ROOT_CA");
        private const string _DefaultUsername = "admin";
        private const string _DefaultPassword = "password";
    }
}
