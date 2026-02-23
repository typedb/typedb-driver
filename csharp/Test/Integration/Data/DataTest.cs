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

using System;
using System.Linq;

using NUnit.Framework;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class DataTest
    {
        private const string ServerAddress = "127.0.0.1:1729";

        [Test]
        public void TransactionTypeEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.TransactionType native, TransactionType wrapper)
            {
                Assert.AreEqual(wrapper, (TransactionType)native);
                Assert.AreEqual(native, (Pinvoke.TransactionType)wrapper);
            }

            CheckConversions(Pinvoke.TransactionType.Read, TransactionType.Read);
            CheckConversions(Pinvoke.TransactionType.Write, TransactionType.Write);
            CheckConversions(Pinvoke.TransactionType.Schema, TransactionType.Schema);
        }

        [Test]
        public void QueryTypeEnumFromNativeIsCorrect()
        {
            Assert.AreEqual(QueryType.Read, QueryTypeExtensions.FromNative(Pinvoke.QueryType.ReadQuery));
            Assert.AreEqual(QueryType.Write, QueryTypeExtensions.FromNative(Pinvoke.QueryType.WriteQuery));
            Assert.AreEqual(QueryType.Schema, QueryTypeExtensions.FromNative(Pinvoke.QueryType.SchemaQuery));
        }

        [Test]
        public void QueryTypeExtensionMethodsWork()
        {
            Assert.IsTrue(QueryType.Read.IsRead());
            Assert.IsFalse(QueryType.Read.IsWrite());
            Assert.IsFalse(QueryType.Read.IsSchema());

            Assert.IsFalse(QueryType.Write.IsRead());
            Assert.IsTrue(QueryType.Write.IsWrite());
            Assert.IsFalse(QueryType.Write.IsSchema());

            Assert.IsFalse(QueryType.Schema.IsRead());
            Assert.IsFalse(QueryType.Schema.IsWrite());
            Assert.IsTrue(QueryType.Schema.IsSchema());
        }

        [Test]
        public void TransactionTypeMatchesQueryType()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));

            string dbName = "test-data-enums";
            if (driver.Databases.Contains(dbName))
            {
                driver.Databases.Get(dbName).Delete();
            }
            driver.Databases.Create(dbName);

            try
            {
                // Read transaction should return Read query type
                using (var tx = driver.Transaction(dbName, TransactionType.Read))
                {
                    Assert.AreEqual(TransactionType.Read, tx.Type);
                    var answer = tx.Query("match entity $x;").Resolve();
                    Assert.AreEqual(QueryType.Read, answer.QueryType);
                }

                // Schema transaction should return Schema query type for define
                using (var tx = driver.Transaction(dbName, TransactionType.Schema))
                {
                    Assert.AreEqual(TransactionType.Schema, tx.Type);
                    var answer = tx.Query("define entity test-entity;").Resolve();
                    Assert.AreEqual(QueryType.Schema, answer.QueryType);
                    tx.Commit();
                }

                // Write transaction should return Write query type for insert
                using (var tx = driver.Transaction(dbName, TransactionType.Write))
                {
                    Assert.AreEqual(TransactionType.Write, tx.Type);
                    var answer = tx.Query("insert $x isa test-entity;").Resolve();
                    Assert.AreEqual(QueryType.Write, answer.QueryType);
                    tx.Commit();
                }
            }
            finally
            {
                driver.Databases.Get(dbName).Delete();
            }
        }
    }
}
