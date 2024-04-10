/*
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
using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IThingType;
using static TypeDB.Driver.Api.IThingType.Annotation;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class AddressTranslationTestFixture
    {
        [Test]
        public void TestCloudConnectionWithTranslation()
        {
            try
            {
                IDictionary<string, string> addressTranslation = new Dictionary<string, string>() {
                    {"localhost:11729", "localhost:11729"},
                    {"localhost:21729", "localhost:21729"},
                    {"localhost:31729", "localhost:31729"},
                };

                TypeDBCredential connectCredential = new TypeDBCredential(
                    "admin",
                    "password",
                    Environment.GetEnvironmentVariable("ROOT_CA")!);

                using (ITypeDBDriver driver = Drivers.CloudDriver(addressTranslation, connectCredential))
                {
                    driver.Databases.Create("typedb");
                    using (ITypeDBSession session = driver.Session("typedb", SessionType.Data))
                    {
                        using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                        {
                            IEntityType root = transaction.Concepts.RootEntityType;
                            Assert.IsNotNull(root);
                            Assert.AreEqual(1, root.GetSubtypes(transaction).Count());
                        }
                    }
                    driver.Databases.Get("typedb").Delete();
                }
            }
            catch (TypeDBDriverException e)
            {
                Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
                // ...
                Assert.Fail("You can handle exceptions here. However, we do not expect exceptions in CI, so we fail.");
            }
        }
    }
}

