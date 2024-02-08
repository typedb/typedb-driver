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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Test.Behaviour;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps : ConnectionStepsBase
    {
        public BehaviourSteps()
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
                addresses ?? DEFAULT_ADDRESSES,
                new TypeDBCredential(
                    username ?? DEFAULT_USERNAME,
                    password ?? DEFAULT_PASSWORD,
                    certificatesPath ?? DEFAULT_CERTIFICATES_PATH));
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
            Driver = CreateTypeDBDriver();
        }

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            if (Driver != null)
            {
                Console.WriteLine("Driver was created!!!!!!"); // TODO (needed for debug RN)
                Driver.Close();
                Driver = null;
            }

            Driver = CreateTypeDBDriver(TypeDB.DEFAULT_ADDRESS, username, password);
        }

        public void ConnectionOpensWithAuthenticationThrowsException(string username, string password)
        {
            Console.WriteLine("CLOUD: ConnectionOpensWithAuthenticationThrowsException");
            throw new Exception("This test method is not ready");
            // TODO:
//            assertThrows(() -> createTypeDBDriver(TypeDBSingleton.getTypeDBRunner().address(), username, password, false));
        }

        private static readonly string[] DEFAULT_ADDRESSES =
        {
            "localhost:11729",
//            "localhost:21729", // Should run only with one address!
//            "localhost:31729"
        };

        private static readonly string DEFAULT_CERTIFICATES_PATH = Environment.GetEnvironmentVariable("ROOT_CA");
        private static readonly string DEFAULT_USERNAME = "admin";
        private static readonly string DEFAULT_PASSWORD = "password";
    }
}
