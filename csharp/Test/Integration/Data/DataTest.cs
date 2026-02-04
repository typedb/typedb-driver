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

using Xunit;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    public class DataTest
    {
        private const string ServerAddress = "127.0.0.1:1729";

        [Fact]
        public void TransactionTypeEnumIsCorrectlyConvertible()
        {
            void CheckConversions(Pinvoke.TransactionType native, TransactionType wrapper)
            {
                Assert.Equal(wrapper, (TransactionType)native);
                Assert.Equal(native, (Pinvoke.TransactionType)wrapper);
            }

            CheckConversions(Pinvoke.TransactionType.Read, TransactionType.Read);
            CheckConversions(Pinvoke.TransactionType.Write, TransactionType.Write);
            CheckConversions(Pinvoke.TransactionType.Schema, TransactionType.Schema);
        }

        [Fact]
        public void QueryTypeEnumFromNativeIsCorrect()
        {
            Assert.Equal(QueryType.Read, QueryTypeExtensions.FromNative(Pinvoke.QueryType.ReadQuery));
            Assert.Equal(QueryType.Write, QueryTypeExtensions.FromNative(Pinvoke.QueryType.WriteQuery));
            Assert.Equal(QueryType.Schema, QueryTypeExtensions.FromNative(Pinvoke.QueryType.SchemaQuery));
        }

        [Fact]
        public void QueryTypeExtensionMethodsWork()
        {
            Assert.True(QueryType.Read.IsRead());
            Assert.False(QueryType.Read.IsWrite());
            Assert.False(QueryType.Read.IsSchema());

            Assert.False(QueryType.Write.IsRead());
            Assert.True(QueryType.Write.IsWrite());
            Assert.False(QueryType.Write.IsSchema());

            Assert.False(QueryType.Schema.IsRead());
            Assert.False(QueryType.Schema.IsWrite());
            Assert.True(QueryType.Schema.IsSchema());
        }

        [Fact]
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
                    Assert.Equal(TransactionType.Read, tx.Type);
                    var answer = tx.Query("match entity $x;");
                    Assert.Equal(QueryType.Read, answer.QueryType);
                }

                // Schema transaction should return Schema query type for define
                using (var tx = driver.Transaction(dbName, TransactionType.Schema))
                {
                    Assert.Equal(TransactionType.Schema, tx.Type);
                    var answer = tx.Query("define entity test-entity;");
                    Assert.Equal(QueryType.Schema, answer.QueryType);
                    tx.Commit();
                }

                // Write transaction should return Write query type for insert
                using (var tx = driver.Transaction(dbName, TransactionType.Write))
                {
                    Assert.Equal(TransactionType.Write, tx.Type);
                    var answer = tx.Query("insert $x isa test-entity;");
                    Assert.Equal(QueryType.Write, answer.QueryType);
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
