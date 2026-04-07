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
using System.Collections.Generic;
using System.Linq;

using NUnit.Framework;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    [TestFixture]
    public class ValuesTest
    {
        private const string ServerAddress = "127.0.0.1:1729";
        private const string DatabaseName = "typedb";

        [SetUp]
        public void SetUp()
        {
            Cleanup();
        }

        [TearDown]
        public void TearDown()
        {
            Cleanup();
        }

        private void Cleanup()
        {
            try
            {
                using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
                if (driver.Databases.Contains(DatabaseName))
                {
                    driver.Databases.Get(DatabaseName).Delete();
                }
            }
            catch (TypeDBDriverException)
            {
                // Server not running - ignore for unit tests that don't need a database
            }
        }

        // ============================================================================
        // Duration unit tests (no database required)
        // ============================================================================

        [Test]
        public void DurationParseYears()
        {
            var duration = Duration.Parse("P1Y");
            Assert.AreEqual(12, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(0, duration.Nanos);
        }

        [Test]
        public void DurationParseMonths()
        {
            var duration = Duration.Parse("P1M");
            Assert.AreEqual(1, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(0, duration.Nanos);
        }

        [Test]
        public void DurationParseDays()
        {
            var duration = Duration.Parse("P1D");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(1, duration.Days);
            Assert.AreEqual(0, duration.Nanos);
        }

        [Test]
        public void DurationParseWeeks()
        {
            var duration = Duration.Parse("P7W");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(49, duration.Days);
            Assert.AreEqual(0, duration.Nanos);
        }

        [Test]
        public void DurationParseHours()
        {
            var duration = Duration.Parse("P0DT1H");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(3_600_000_000_000L, duration.Nanos);
        }

        [Test]
        public void DurationParseMinutes()
        {
            var duration = Duration.Parse("P0DT1M");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(60_000_000_000L, duration.Nanos);
        }

        [Test]
        public void DurationParseSeconds()
        {
            var duration = Duration.Parse("P0DT1S");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(1_000_000_000L, duration.Nanos);
        }

        [Test]
        public void DurationParseFractionalSeconds()
        {
            var duration = Duration.Parse("P0DT0.000000001S");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(1L, duration.Nanos);
        }

        [Test]
        public void DurationParseFractionalSeconds100Nanos()
        {
            var duration = Duration.Parse("P0DT0.0000001S");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(100L, duration.Nanos);
        }

        [Test]
        public void DurationParseZero()
        {
            var duration = Duration.Parse("P0DT0S");
            Assert.AreEqual(0, duration.Months);
            Assert.AreEqual(0, duration.Days);
            Assert.AreEqual(0, duration.Nanos);
        }

        [Test]
        public void DurationParseComplex()
        {
            var duration = Duration.Parse("P1Y10M7DT15H44M5.00394892S");
            Assert.AreEqual(22, duration.Months); // 1*12 + 10
            Assert.AreEqual(7, duration.Days);
            // 15H * 3600 * 10^9 + 44M * 60 * 10^9 + 5S * 10^9 + 3948920 nanos
            long expectedNanos = 15L * 3600 * 1_000_000_000 + 44L * 60 * 1_000_000_000 + 5L * 1_000_000_000 + 3_948_920;
            Assert.AreEqual(expectedNanos, duration.Nanos);
        }

        [Test]
        public void DurationParseMaxComplex()
        {
            var duration = Duration.Parse("P999Y12M31DT24H59M59.999999999S");
            Assert.AreEqual(12000, duration.Months); // 999*12 + 12
            Assert.AreEqual(31, duration.Days);
            long expectedNanos = 24L * 3600 * 1_000_000_000 + 59L * 60 * 1_000_000_000 + 59L * 1_000_000_000 + 999_999_999;
            Assert.AreEqual(expectedNanos, duration.Nanos);
        }

        [Test]
        public void DurationEquality()
        {
            Assert.AreEqual(Duration.Parse("P1Y0M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.AreEqual(Duration.Parse("P0Y12M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.AreNotEqual(Duration.Parse("P0Y1M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.AreEqual(Duration.Parse("P0Y0M49DT0H0M0S"), Duration.Parse("P7W"));
        }

        [Test]
        public void DurationToString()
        {
            var duration = new Duration(12, 0, 0);
            Assert.AreEqual("P1Y", duration.ToString());

            var duration2 = new Duration(14, 0, 0); // 1 year 2 months
            Assert.AreEqual("P1Y2M", duration2.ToString());

            var duration3 = new Duration(0, 7, 0);
            Assert.AreEqual("P7D", duration3.ToString());

            var duration4 = new Duration(0, 0, 3661_000_000_000L); // 1h 1m 1s
            Assert.AreEqual("PT1H1M1S", duration4.ToString());
        }

        // ============================================================================
        // Datetime unit tests (no database required)
        // ============================================================================

        [Test]
        public void DatetimeParse()
        {
            var datetime = Datetime.Parse("2024-10-09T13:07:38.123456789");
            Assert.AreEqual(2024, datetime.DateTime.Year);
            Assert.AreEqual(10, datetime.DateTime.Month);
            Assert.AreEqual(9, datetime.DateTime.Day);
            Assert.AreEqual(13, datetime.DateTime.Hour);
            Assert.AreEqual(7, datetime.DateTime.Minute);
            Assert.AreEqual(38, datetime.DateTime.Second);
            Assert.AreEqual(123456789u, datetime.SubsecNanos);
        }

        [Test]
        public void DatetimeParseNoFractional()
        {
            var datetime = Datetime.Parse("1970-01-01T00:00:00");
            Assert.AreEqual(1970, datetime.DateTime.Year);
            Assert.AreEqual(1, datetime.DateTime.Month);
            Assert.AreEqual(1, datetime.DateTime.Day);
            Assert.AreEqual(0, datetime.DateTime.Hour);
            Assert.AreEqual(0, datetime.DateTime.Minute);
            Assert.AreEqual(0, datetime.DateTime.Second);
            Assert.AreEqual(0u, datetime.SubsecNanos);
            Assert.AreEqual(0, datetime.Seconds);
        }

        [Test]
        public void DatetimeEquality()
        {
            var dt1 = Datetime.Parse("2024-10-09T13:07:38.123456789");
            var dt2 = Datetime.Parse("2024-10-09T13:07:38.123456789");
            var dt3 = Datetime.Parse("2024-10-09T13:07:38.123456780");
            Assert.AreEqual(dt1, dt2);
            Assert.AreNotEqual(dt1, dt3);
        }

        [Test]
        public void DatetimeToString()
        {
            var datetime = new Datetime(1728479258, 123456789);
            Assert.That(datetime.ToString(), Does.Contain("123456789"));
        }

        // ============================================================================
        // DatetimeTZ unit tests (no database required)
        // ============================================================================

        [Test]
        public void DatetimeTZParseIana()
        {
            var datetimeTz = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            Assert.AreEqual(2024, datetimeTz.DateTimeOffset.Year);
            Assert.AreEqual(10, datetimeTz.DateTimeOffset.Month);
            Assert.AreEqual(9, datetimeTz.DateTimeOffset.Day);
            Assert.AreEqual(13, datetimeTz.DateTimeOffset.Hour);
            Assert.AreEqual(7, datetimeTz.DateTimeOffset.Minute);
            Assert.AreEqual(38, datetimeTz.DateTimeOffset.Second);
            Assert.AreEqual(123456789u, datetimeTz.SubsecNanos);
            Assert.AreEqual("Europe/London", datetimeTz.ZoneName);
            Assert.IsFalse(datetimeTz.IsFixedOffset);
        }

        [Test]
        public void DatetimeTZParseFixedOffset()
        {
            var datetimeTz = DatetimeTZ.Parse("2024-09-20T16:40:05.028129323+0545");
            Assert.AreEqual(2024, datetimeTz.DateTimeOffset.Year);
            Assert.AreEqual(9, datetimeTz.DateTimeOffset.Month);
            Assert.AreEqual(20, datetimeTz.DateTimeOffset.Day);
            Assert.AreEqual(16, datetimeTz.DateTimeOffset.Hour);
            Assert.AreEqual(40, datetimeTz.DateTimeOffset.Minute);
            Assert.AreEqual(5, datetimeTz.DateTimeOffset.Second);
            Assert.AreEqual(28129323u, datetimeTz.SubsecNanos);
            Assert.IsNull(datetimeTz.ZoneName);
            Assert.IsTrue(datetimeTz.IsFixedOffset);
            Assert.AreEqual(TimeSpan.FromHours(5) + TimeSpan.FromMinutes(45), datetimeTz.DateTimeOffset.Offset);
        }

        [Test]
        public void DatetimeTZEquality()
        {
            var dtz1 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            var dtz2 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            var dtz3 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456780 Europe/London");
            Assert.AreEqual(dtz1, dtz2);
            Assert.AreNotEqual(dtz1, dtz3);
        }

        // ============================================================================
        // Integration tests (require running TypeDB server)
        // ============================================================================

        [Test]
        public void TestAllValueTypes()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            driver.Databases.Create(DatabaseName);

            var attributeValueTypes = new Dictionary<string, string>
            {
                { "root", "none" },
                { "age", "integer" },
                { "name", "string" },
                { "is-new", "boolean" },
                { "success", "double" },
                { "balance", "decimal" },
                { "birth-date", "date" },
                { "birth-time", "datetime" },
                { "current-time", "datetime-tz" },
                { "current-time-off", "datetime-tz" },
                { "expiration", "duration" }
            };

            var attributeValues = new Dictionary<string, string>
            {
                { "age", "25" },
                { "name", "\"John\"" },
                { "is-new", "true" },
                { "success", "66.6" },
                { "balance", "1234567890.0001234567890dec" },
                { "birth-date", "2024-09-20" },
                { "birth-time", "1999-02-26T12:15:05" },
                { "current-time", "2024-09-20T16:40:05 Europe/Belfast" },
                { "current-time-off", "2024-09-20T16:40:05.028129323+0545" },
                { "expiration", "P1Y10M7DT15H44M5.00394892S" }
            };

            // Schema transaction: define attributes
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                foreach (var (attribute, valueType) in attributeValueTypes)
                {
                    var query = valueType == "none"
                        ? $"define attribute {attribute} @abstract;"
                        : $"define attribute {attribute}, value {valueType}; entity person owns {attribute};";
                    var answer = tx.Query(query).Resolve()!;
                    Assert.IsTrue(answer.IsOk);
                }
                tx.Commit();
            }

            // Read transaction: verify attribute types
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Read))
            {
                var answer = tx.Query("match attribute $a;").Resolve()!;
                Assert.IsTrue(answer.IsConceptRows);

                var rows = answer.AsConceptRows().ToList();
                Assert.AreEqual(attributeValueTypes.Count, rows.Count);

                foreach (var row in rows)
                {
                    var concept = row.Get("a");
                    Assert.IsNotNull(concept);
                    Assert.IsTrue(concept!.IsAttributeType());
                    var label = concept.GetLabel();
                    var expectedValueType = attributeValueTypes[label];
                    var actualValueType = concept.TryGetValueType() ?? "none";
                    Assert.AreEqual(expectedValueType, actualValueType);
                }
            }

            // Write transaction: insert attribute values
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                foreach (var (attribute, value) in attributeValues)
                {
                    var query = $"insert $a isa person, has {attribute} {value};";
                    var answer = tx.Query(query).Resolve()!;
                    Assert.IsTrue(answer.IsConceptRows);

                    var rows = answer.AsConceptRows().ToList();
                    Assert.AreEqual(1, rows.Count);

                    var row = rows[0];
                    var columnNames = row.ColumnNames.ToList();
                    Assert.AreEqual(1, columnNames.Count);
                    Assert.IsTrue(row.GetIndex(0)?.IsEntity());
                }
                tx.Commit();
            }

            // Read transaction: verify attribute values
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Read))
            {
                var answer = tx.Query("match attribute $t; $a isa! $t;").Resolve()!;
                Assert.IsTrue(answer.IsConceptRows);

                var rows = answer.AsConceptRows().ToList();
                Assert.AreEqual(attributeValues.Count, rows.Count);

                int checkedCount = 0;
                foreach (var row in rows)
                {
                    var concept = row.Get("a");
                    Assert.IsNotNull(concept);
                    Assert.IsTrue(concept!.IsAttribute());
                    var attributeName = concept.GetLabel();
                    var valueType = concept.TryGetValueType();
                    var expectedType = attributeValueTypes[attributeName];
                    Assert.AreEqual(expectedType, valueType);

                    var expectedStr = attributeValues[attributeName];

                    if (concept.IsInteger())
                    {
                        Assert.AreEqual(long.Parse(expectedStr), concept.TryGetInteger());
                        checkedCount++;
                    }
                    else if (concept.IsString())
                    {
                        Assert.AreEqual(expectedStr.Trim('"'), concept.TryGetString());
                        checkedCount++;
                    }
                    else if (concept.IsBoolean())
                    {
                        Assert.AreEqual(bool.Parse(expectedStr), concept.TryGetBoolean());
                        checkedCount++;
                    }
                    else if (concept.IsDouble())
                    {
                        Assert.IsTrue(Math.Abs(double.Parse(expectedStr) - concept.TryGetDouble()!.Value) < double.Epsilon);
                        checkedCount++;
                    }
                    else if (concept.IsDecimal())
                    {
                        Assert.IsNotNull(concept.TryGetDecimal());
                        checkedCount++;
                    }
                    else if (concept.IsDate())
                    {
                        var expectedDate = DateOnly.Parse(expectedStr);
                        Assert.AreEqual(expectedDate, concept.TryGetDate());
                        checkedCount++;
                    }
                    else if (concept.IsDatetime())
                    {
                        Assert.IsNotNull(concept.TryGetDatetime());
                        checkedCount++;
                    }
                    else if (concept.IsDatetimeTZ())
                    {
                        Assert.IsNotNull(concept.TryGetDatetimeTZ());
                        checkedCount++;
                    }
                    else if (concept.IsDuration())
                    {
                        var expected = Duration.Parse(expectedStr);
                        Assert.AreEqual(expected, concept.TryGetDuration());
                        checkedCount++;
                    }
                }

                Assert.AreEqual(attributeValues.Count, checkedCount);
            }
        }

        [Test]
        public void TestDatetimeNaive()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            driver.Databases.Create(DatabaseName);

            // Define datetime attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dt, value datetime;").Resolve();
                tx.Commit();
            }

            // Test: 2024-10-09T13:07:38.123456789
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 2024-10-09T13:07:38.123456789;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.IsNotNull(datetime);
                Assert.AreEqual(2024, datetime!.DateTime.Year);
                Assert.AreEqual(10, datetime.DateTime.Month);
                Assert.AreEqual(9, datetime.DateTime.Day);
                Assert.AreEqual(13, datetime.DateTime.Hour);
                Assert.AreEqual(7, datetime.DateTime.Minute);
                Assert.AreEqual(38, datetime.DateTime.Second);
                Assert.AreEqual(123456789u, datetime.SubsecNanos);
            }

            // Test: Unix epoch 1970-01-01T00:00:00
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 1970-01-01T00:00:00;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.IsNotNull(datetime);
                Assert.AreEqual(1970, datetime!.DateTime.Year);
                Assert.AreEqual(1, datetime.DateTime.Month);
                Assert.AreEqual(1, datetime.DateTime.Day);
                Assert.AreEqual(0, datetime.DateTime.Hour);
                Assert.AreEqual(0, datetime.DateTime.Minute);
                Assert.AreEqual(0, datetime.DateTime.Second);
                Assert.AreEqual(0u, datetime.SubsecNanos);
                Assert.AreEqual(0, datetime.Seconds);
            }

            // Test: max value 9999-12-31T23:59:59.999999999
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 9999-12-31T23:59:59.999999999;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.IsNotNull(datetime);
                Assert.AreEqual(9999, datetime!.DateTime.Year);
                Assert.AreEqual(12, datetime.DateTime.Month);
                Assert.AreEqual(31, datetime.DateTime.Day);
                Assert.AreEqual(23, datetime.DateTime.Hour);
                Assert.AreEqual(59, datetime.DateTime.Minute);
                Assert.AreEqual(59, datetime.DateTime.Second);
                Assert.AreEqual(999999999u, datetime.SubsecNanos);
            }
        }

        [Test]
        public void TestDatetimeTZIana()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            driver.Databases.Create(DatabaseName);

            // Define datetime-tz attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dtz, value datetime-tz;").Resolve();
                tx.Commit();
            }

            // Test: IANA timezone
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dtz isa dtz 2024-10-09T13:07:38.123456789 Europe/London;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dtz");
                var datetimeTz = concept?.TryGetDatetimeTZ();

                Assert.IsNotNull(datetimeTz);
                Assert.AreEqual(2024, datetimeTz!.DateTimeOffset.Year);
                Assert.AreEqual(10, datetimeTz.DateTimeOffset.Month);
                Assert.AreEqual(9, datetimeTz.DateTimeOffset.Day);
                Assert.AreEqual(13, datetimeTz.DateTimeOffset.Hour);
                Assert.AreEqual(7, datetimeTz.DateTimeOffset.Minute);
                Assert.AreEqual(38, datetimeTz.DateTimeOffset.Second);
                Assert.AreEqual(123456789u, datetimeTz.SubsecNanos);
                Assert.IsFalse(datetimeTz.IsFixedOffset);
            }
        }

        [Test]
        public void TestDatetimeTZFixedOffset()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            driver.Databases.Create(DatabaseName);

            // Define datetime-tz attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dtz, value datetime-tz;").Resolve();
                tx.Commit();
            }

            // Test: Fixed offset timezone
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dtz isa dtz 2024-09-20T16:40:05.028129323+0545;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dtz");
                var datetimeTz = concept?.TryGetDatetimeTZ();

                Assert.IsNotNull(datetimeTz);
                Assert.AreEqual(2024, datetimeTz!.DateTimeOffset.Year);
                Assert.AreEqual(9, datetimeTz.DateTimeOffset.Month);
                Assert.AreEqual(20, datetimeTz.DateTimeOffset.Day);
                Assert.AreEqual(16, datetimeTz.DateTimeOffset.Hour);
                Assert.AreEqual(40, datetimeTz.DateTimeOffset.Minute);
                Assert.AreEqual(5, datetimeTz.DateTimeOffset.Second);
                Assert.AreEqual(28129323u, datetimeTz.SubsecNanos);
                Assert.IsTrue(datetimeTz.IsFixedOffset);
                Assert.AreEqual(TimeSpan.FromHours(5) + TimeSpan.FromMinutes(45), datetimeTz.DateTimeOffset.Offset);
            }
        }

        [Test]
        public void TestDurationViaDatabase()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(DriverTlsConfig.Disabled()));
            driver.Databases.Create(DatabaseName);

            // Define duration attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute d, value duration;").Resolve();
                tx.Commit();
            }

            // Test: P1Y
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1Y;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(12, duration!.Months);
                Assert.AreEqual(0, duration.Days);
                Assert.AreEqual(0, duration.Nanos);
            }

            // Test: P1M
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1M;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(1, duration!.Months);
                Assert.AreEqual(0, duration.Days);
                Assert.AreEqual(0, duration.Nanos);
            }

            // Test: P1D
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1D;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(0, duration!.Months);
                Assert.AreEqual(1, duration.Days);
                Assert.AreEqual(0, duration.Nanos);
            }

            // Test: P0DT1H
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT1H;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(0, duration!.Months);
                Assert.AreEqual(0, duration.Days);
                Assert.AreEqual(3_600_000_000_000L, duration.Nanos);
            }

            // Test: P0DT1S
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT1S;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(0, duration!.Months);
                Assert.AreEqual(0, duration.Days);
                Assert.AreEqual(1_000_000_000L, duration.Nanos);
            }

            // Test: P0DT0.000000001S (1 nanosecond)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT0.000000001S;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(0, duration!.Months);
                Assert.AreEqual(0, duration.Days);
                Assert.AreEqual(1L, duration.Nanos);
            }

            // Test: P7W (weeks)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P7W;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(0, duration!.Months);
                Assert.AreEqual(49, duration.Days);
                Assert.AreEqual(0, duration.Nanos);
            }

            // Test: P999Y12M31DT24H59M59.999999999S (complex)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P999Y12M31DT24H59M59.999999999S;").Resolve()!;
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.IsNotNull(duration);
                Assert.AreEqual(12000, duration!.Months);
                Assert.AreEqual(31, duration.Days);
                Assert.AreEqual(89999_999_999_999L, duration.Nanos);
            }
        }
    }
}
