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

using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

using NUnit.Framework;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class VectorTest
    {
        private const string DB = "vector-demo-csharp";

        private static string Vec(params double[] v)
        {
            return "vector([" + string.Join(", ", v) + "], \"float32\")";
        }

        [Test]
        public void VectorRoundTripAndSimilaritySearch()
        {
            using var driver = TypeDB.Driver(TypeDB.DefaultAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            if (driver.Databases.Contains(DB)) driver.Databases.Get(DB).Delete();
            driver.Databases.Create(DB);

            using (var tx = driver.Transaction(DB, TransactionType.Schema))
            {
                tx.Query(@"define
                    attribute content value string;
                    attribute embedding value vector(3, ""float32"");
                    entity document owns content @key, owns embedding;").Resolve();
                tx.Commit();
            }

            using (var tx = driver.Transaction(DB, TransactionType.Write))
            {
                tx.Query($"insert $d isa document, has content \"cats are small felines\", has embedding {Vec(1.0, 0.1, 0.0)};").Resolve();
                tx.Query($"insert $d isa document, has content \"dogs are loyal canines\", has embedding {Vec(0.9, 0.3, 0.0)};").Resolve();
                tx.Query($"insert $d isa document, has content \"rust is a systems language\", has embedding {Vec(0.0, 0.1, 1.0)};").Resolve();
                tx.Commit();
            }

            using (var tx = driver.Transaction(DB, TransactionType.Read))
            {
                var rows = tx.Query($@"match
                        let $e in cosine_similarity_search(embedding, {Vec(1.0, 0.0, 0.0)}, 0.5);
                        $d isa document, has content $c, has embedding $e;
                    select $c, $e; limit 2;").Resolve()!
                    .AsConceptRows().ToList();

                var contents = rows.Select(row => row.Get("c")!.TryGetString()!).ToList();
                CollectionAssert.AreEqual(
                    new List<string> { "cats are small felines", "dogs are loyal canines" }, contents);

                var top = rows[0].Get("e")!;
                Assert.IsTrue(top.IsVector());
                CollectionAssert.AreEqual(new List<float> { 1.0f, 0.1f, 0.0f }, top.TryGetVector());
                CollectionAssert.AreEqual(new List<float> { 1.0f, 0.1f, 0.0f }, top.AsAttribute().TryGetValue()!.GetVector());
            }
        }
    }
}
