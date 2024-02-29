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
            Driver = CreateTypeDBDriver(TypeDB.DEFAULT_ADDRESS);
        }

        [When(@"connection opens with authentication: {word}, {word}")]
        public void ConnectionOpensWithAuthentication(string username, string password)
        {
            throw new NotImplementedException("Do not use this method for Core tests");
        }
    }
}
