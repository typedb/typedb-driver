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

using NUnit.Framework;

namespace Lib.IntegrationTests
{
    [TestFixture]
    public class Helloer_Says
    {
//        [SetUp]
//        public void SetUp()
//        {
//            _helloer = new Helloer(); // not needed as Helloer is static
//        }

        [Test]
        public void HelloToWorld()
        {
            var result = Helloer.SayHello("world");

            Assert.AreEqual(result, "Hello, world!");
        }

        [Test]
        public void HelloToNumber()
        {
            var result = Helloer.SayHello(34);

            Assert.AreEqual(result, "Hello, 34!");
        }

        [Test]
        public void HelloToJohn()
        {
            var result = Helloer.SayHello("John");

            Assert.AreEqual(result, "What's up, John!");
        }
    }
}