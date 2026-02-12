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

using Xunit;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Test.Integration
{
    public class ValuesTest : IDisposable
    {
        private const string ServerAddress = "127.0.0.1:1729";
        private const string DatabaseName = "typedb";

        public ValuesTest()
        {
            Cleanup();
        }

        public void Dispose()
        {
            Cleanup();
        }

        private void Cleanup()
        {
            try
            {
                using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
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

        [Fact]
        public void DurationParseYears()
        {
            var duration = Duration.Parse("P1Y");
            Assert.Equal(12, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(0, duration.Nanos);
        }

        [Fact]
        public void DurationParseMonths()
        {
            var duration = Duration.Parse("P1M");
            Assert.Equal(1, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(0, duration.Nanos);
        }

        [Fact]
        public void DurationParseDays()
        {
            var duration = Duration.Parse("P1D");
            Assert.Equal(0, duration.Months);
            Assert.Equal(1, duration.Days);
            Assert.Equal(0, duration.Nanos);
        }

        [Fact]
        public void DurationParseWeeks()
        {
            var duration = Duration.Parse("P7W");
            Assert.Equal(0, duration.Months);
            Assert.Equal(49, duration.Days);
            Assert.Equal(0, duration.Nanos);
        }

        [Fact]
        public void DurationParseHours()
        {
            var duration = Duration.Parse("P0DT1H");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(3_600_000_000_000L, duration.Nanos);
        }

        [Fact]
        public void DurationParseMinutes()
        {
            var duration = Duration.Parse("P0DT1M");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(60_000_000_000L, duration.Nanos);
        }

        [Fact]
        public void DurationParseSeconds()
        {
            var duration = Duration.Parse("P0DT1S");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(1_000_000_000L, duration.Nanos);
        }

        [Fact]
        public void DurationParseFractionalSeconds()
        {
            var duration = Duration.Parse("P0DT0.000000001S");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(1L, duration.Nanos);
        }

        [Fact]
        public void DurationParseFractionalSeconds100Nanos()
        {
            var duration = Duration.Parse("P0DT0.0000001S");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(100L, duration.Nanos);
        }

        [Fact]
        public void DurationParseZero()
        {
            var duration = Duration.Parse("P0DT0S");
            Assert.Equal(0, duration.Months);
            Assert.Equal(0, duration.Days);
            Assert.Equal(0, duration.Nanos);
        }

        [Fact]
        public void DurationParseComplex()
        {
            var duration = Duration.Parse("P1Y10M7DT15H44M5.00394892S");
            Assert.Equal(22, duration.Months); // 1*12 + 10
            Assert.Equal(7, duration.Days);
            // 15H * 3600 * 10^9 + 44M * 60 * 10^9 + 5S * 10^9 + 3948920 nanos
            long expectedNanos = 15L * 3600 * 1_000_000_000 + 44L * 60 * 1_000_000_000 + 5L * 1_000_000_000 + 3_948_920;
            Assert.Equal(expectedNanos, duration.Nanos);
        }

        [Fact]
        public void DurationParseMaxComplex()
        {
            var duration = Duration.Parse("P999Y12M31DT24H59M59.999999999S");
            Assert.Equal(12000, duration.Months); // 999*12 + 12
            Assert.Equal(31, duration.Days);
            long expectedNanos = 24L * 3600 * 1_000_000_000 + 59L * 60 * 1_000_000_000 + 59L * 1_000_000_000 + 999_999_999;
            Assert.Equal(expectedNanos, duration.Nanos);
        }

        [Fact]
        public void DurationEquality()
        {
            Assert.Equal(Duration.Parse("P1Y0M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.Equal(Duration.Parse("P0Y12M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.NotEqual(Duration.Parse("P0Y1M0DT0H0M0S"), Duration.Parse("P1Y"));
            Assert.Equal(Duration.Parse("P0Y0M49DT0H0M0S"), Duration.Parse("P7W"));
        }

        [Fact]
        public void DurationToString()
        {
            var duration = new Duration(12, 0, 0);
            Assert.Equal("P1Y", duration.ToString());

            var duration2 = new Duration(14, 0, 0); // 1 year 2 months
            Assert.Equal("P1Y2M", duration2.ToString());

            var duration3 = new Duration(0, 7, 0);
            Assert.Equal("P7D", duration3.ToString());

            var duration4 = new Duration(0, 0, 3661_000_000_000L); // 1h 1m 1s
            Assert.Equal("PT1H1M1S", duration4.ToString());
        }

        // ============================================================================
        // Datetime unit tests (no database required)
        // ============================================================================

        [Fact]
        public void DatetimeParse()
        {
            var datetime = Datetime.Parse("2024-10-09T13:07:38.123456789");
            Assert.Equal(2024, datetime.DateTime.Year);
            Assert.Equal(10, datetime.DateTime.Month);
            Assert.Equal(9, datetime.DateTime.Day);
            Assert.Equal(13, datetime.DateTime.Hour);
            Assert.Equal(7, datetime.DateTime.Minute);
            Assert.Equal(38, datetime.DateTime.Second);
            Assert.Equal(123456789u, datetime.SubsecNanos);
        }

        [Fact]
        public void DatetimeParseNoFractional()
        {
            var datetime = Datetime.Parse("1970-01-01T00:00:00");
            Assert.Equal(1970, datetime.DateTime.Year);
            Assert.Equal(1, datetime.DateTime.Month);
            Assert.Equal(1, datetime.DateTime.Day);
            Assert.Equal(0, datetime.DateTime.Hour);
            Assert.Equal(0, datetime.DateTime.Minute);
            Assert.Equal(0, datetime.DateTime.Second);
            Assert.Equal(0u, datetime.SubsecNanos);
            Assert.Equal(0, datetime.Seconds);
        }

        [Fact]
        public void DatetimeEquality()
        {
            var dt1 = Datetime.Parse("2024-10-09T13:07:38.123456789");
            var dt2 = Datetime.Parse("2024-10-09T13:07:38.123456789");
            var dt3 = Datetime.Parse("2024-10-09T13:07:38.123456780");
            Assert.Equal(dt1, dt2);
            Assert.NotEqual(dt1, dt3);
        }

        [Fact]
        public void DatetimeToString()
        {
            var datetime = new Datetime(1728479258, 123456789);
            Assert.Contains("123456789", datetime.ToString());
        }

        // ============================================================================
        // DatetimeTZ unit tests (no database required)
        // ============================================================================

        [Fact]
        public void DatetimeTZParseIana()
        {
            var datetimeTz = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            Assert.Equal(2024, datetimeTz.DateTimeOffset.Year);
            Assert.Equal(10, datetimeTz.DateTimeOffset.Month);
            Assert.Equal(9, datetimeTz.DateTimeOffset.Day);
            Assert.Equal(13, datetimeTz.DateTimeOffset.Hour);
            Assert.Equal(7, datetimeTz.DateTimeOffset.Minute);
            Assert.Equal(38, datetimeTz.DateTimeOffset.Second);
            Assert.Equal(123456789u, datetimeTz.SubsecNanos);
            Assert.Equal("Europe/London", datetimeTz.ZoneName);
            Assert.False(datetimeTz.IsFixedOffset);
        }

        [Fact]
        public void DatetimeTZParseFixedOffset()
        {
            var datetimeTz = DatetimeTZ.Parse("2024-09-20T16:40:05.028129323+0545");
            Assert.Equal(2024, datetimeTz.DateTimeOffset.Year);
            Assert.Equal(9, datetimeTz.DateTimeOffset.Month);
            Assert.Equal(20, datetimeTz.DateTimeOffset.Day);
            Assert.Equal(16, datetimeTz.DateTimeOffset.Hour);
            Assert.Equal(40, datetimeTz.DateTimeOffset.Minute);
            Assert.Equal(5, datetimeTz.DateTimeOffset.Second);
            Assert.Equal(28129323u, datetimeTz.SubsecNanos);
            Assert.Null(datetimeTz.ZoneName);
            Assert.True(datetimeTz.IsFixedOffset);
            Assert.Equal(TimeSpan.FromHours(5) + TimeSpan.FromMinutes(45), datetimeTz.DateTimeOffset.Offset);
        }

        [Fact]
        public void DatetimeTZEquality()
        {
            var dtz1 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            var dtz2 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456789 Europe/London");
            var dtz3 = DatetimeTZ.Parse("2024-10-09T13:07:38.123456780 Europe/London");
            Assert.Equal(dtz1, dtz2);
            Assert.NotEqual(dtz1, dtz3);
        }

        // ============================================================================
        // Integration tests (require running TypeDB server)
        // ============================================================================

        [Fact]
        public void TestAllValueTypes()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
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
                    var answer = tx.Query(query);
                    Assert.True(answer.IsOk);
                }
                tx.Commit();
            }

            // Read transaction: verify attribute types
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Read))
            {
                var answer = tx.Query("match attribute $a;");
                Assert.True(answer.IsConceptRows);

                var rows = answer.AsConceptRows().ToList();
                Assert.Equal(attributeValueTypes.Count, rows.Count);

                foreach (var row in rows)
                {
                    var concept = row.Get("a");
                    Assert.NotNull(concept);
                    Assert.True(concept!.IsAttributeType());
                    var label = concept.GetLabel();
                    var expectedValueType = attributeValueTypes[label];
                    var actualValueType = concept.TryGetValueType() ?? "none";
                    Assert.Equal(expectedValueType, actualValueType);
                }
            }

            // Write transaction: insert attribute values
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                foreach (var (attribute, value) in attributeValues)
                {
                    var query = $"insert $a isa person, has {attribute} {value};";
                    var answer = tx.Query(query);
                    Assert.True(answer.IsConceptRows);

                    var rows = answer.AsConceptRows().ToList();
                    Assert.Single(rows);

                    var row = rows[0];
                    var columnNames = row.ColumnNames.ToList();
                    Assert.Single(columnNames);
                    Assert.True(row.GetIndex(0)?.IsEntity());
                }
                tx.Commit();
            }

            // Read transaction: verify attribute values
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Read))
            {
                var answer = tx.Query("match attribute $t; $a isa! $t;");
                Assert.True(answer.IsConceptRows);

                var rows = answer.AsConceptRows().ToList();
                Assert.Equal(attributeValues.Count, rows.Count);

                int checkedCount = 0;
                foreach (var row in rows)
                {
                    var concept = row.Get("a");
                    Assert.NotNull(concept);
                    Assert.True(concept!.IsAttribute());
                    var attributeName = concept.GetLabel();
                    var valueType = concept.TryGetValueType();
                    var expectedType = attributeValueTypes[attributeName];
                    Assert.Equal(expectedType, valueType);

                    var expectedStr = attributeValues[attributeName];

                    if (concept.IsInteger())
                    {
                        Assert.Equal(long.Parse(expectedStr), concept.TryGetInteger());
                        checkedCount++;
                    }
                    else if (concept.IsString())
                    {
                        Assert.Equal(expectedStr.Trim('"'), concept.TryGetString());
                        checkedCount++;
                    }
                    else if (concept.IsBoolean())
                    {
                        Assert.Equal(bool.Parse(expectedStr), concept.TryGetBoolean());
                        checkedCount++;
                    }
                    else if (concept.IsDouble())
                    {
                        Assert.True(Math.Abs(double.Parse(expectedStr) - concept.TryGetDouble()!.Value) < double.Epsilon);
                        checkedCount++;
                    }
                    else if (concept.IsDecimal())
                    {
                        Assert.NotNull(concept.TryGetDecimal());
                        checkedCount++;
                    }
                    else if (concept.IsDate())
                    {
                        var expectedDate = DateOnly.Parse(expectedStr);
                        Assert.Equal(expectedDate, concept.TryGetDate());
                        checkedCount++;
                    }
                    else if (concept.IsDatetime())
                    {
                        Assert.NotNull(concept.TryGetDatetime());
                        checkedCount++;
                    }
                    else if (concept.IsDatetimeTZ())
                    {
                        Assert.NotNull(concept.TryGetDatetimeTZ());
                        checkedCount++;
                    }
                    else if (concept.IsDuration())
                    {
                        var expected = Duration.Parse(expectedStr);
                        Assert.Equal(expected, concept.TryGetDuration());
                        checkedCount++;
                    }
                }

                Assert.Equal(attributeValues.Count, checkedCount);
            }
        }

        [Fact]
        public void TestDatetimeNaive()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
            driver.Databases.Create(DatabaseName);

            // Define datetime attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dt, value datetime;");
                tx.Commit();
            }

            // Test: 2024-10-09T13:07:38.123456789
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 2024-10-09T13:07:38.123456789;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.NotNull(datetime);
                Assert.Equal(2024, datetime!.DateTime.Year);
                Assert.Equal(10, datetime.DateTime.Month);
                Assert.Equal(9, datetime.DateTime.Day);
                Assert.Equal(13, datetime.DateTime.Hour);
                Assert.Equal(7, datetime.DateTime.Minute);
                Assert.Equal(38, datetime.DateTime.Second);
                Assert.Equal(123456789u, datetime.SubsecNanos);
            }

            // Test: Unix epoch 1970-01-01T00:00:00
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 1970-01-01T00:00:00;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.NotNull(datetime);
                Assert.Equal(1970, datetime!.DateTime.Year);
                Assert.Equal(1, datetime.DateTime.Month);
                Assert.Equal(1, datetime.DateTime.Day);
                Assert.Equal(0, datetime.DateTime.Hour);
                Assert.Equal(0, datetime.DateTime.Minute);
                Assert.Equal(0, datetime.DateTime.Second);
                Assert.Equal(0u, datetime.SubsecNanos);
                Assert.Equal(0, datetime.Seconds);
            }

            // Test: max value 9999-12-31T23:59:59.999999999
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dt isa dt 9999-12-31T23:59:59.999999999;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dt");
                var datetime = concept?.TryGetDatetime();

                Assert.NotNull(datetime);
                Assert.Equal(9999, datetime!.DateTime.Year);
                Assert.Equal(12, datetime.DateTime.Month);
                Assert.Equal(31, datetime.DateTime.Day);
                Assert.Equal(23, datetime.DateTime.Hour);
                Assert.Equal(59, datetime.DateTime.Minute);
                Assert.Equal(59, datetime.DateTime.Second);
                Assert.Equal(999999999u, datetime.SubsecNanos);
            }
        }

        [Fact]
        public void TestDatetimeTZIana()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
            driver.Databases.Create(DatabaseName);

            // Define datetime-tz attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dtz, value datetime-tz;");
                tx.Commit();
            }

            // Test: IANA timezone
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dtz isa dtz 2024-10-09T13:07:38.123456789 Europe/London;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dtz");
                var datetimeTz = concept?.TryGetDatetimeTZ();

                Assert.NotNull(datetimeTz);
                Assert.Equal(2024, datetimeTz!.DateTimeOffset.Year);
                Assert.Equal(10, datetimeTz.DateTimeOffset.Month);
                Assert.Equal(9, datetimeTz.DateTimeOffset.Day);
                Assert.Equal(13, datetimeTz.DateTimeOffset.Hour);
                Assert.Equal(7, datetimeTz.DateTimeOffset.Minute);
                Assert.Equal(38, datetimeTz.DateTimeOffset.Second);
                Assert.Equal(123456789u, datetimeTz.SubsecNanos);
                Assert.False(datetimeTz.IsFixedOffset);
            }
        }

        [Fact]
        public void TestDatetimeTZFixedOffset()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
            driver.Databases.Create(DatabaseName);

            // Define datetime-tz attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute dtz, value datetime-tz;");
                tx.Commit();
            }

            // Test: Fixed offset timezone
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $dtz isa dtz 2024-09-20T16:40:05.028129323+0545;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("dtz");
                var datetimeTz = concept?.TryGetDatetimeTZ();

                Assert.NotNull(datetimeTz);
                Assert.Equal(2024, datetimeTz!.DateTimeOffset.Year);
                Assert.Equal(9, datetimeTz.DateTimeOffset.Month);
                Assert.Equal(20, datetimeTz.DateTimeOffset.Day);
                Assert.Equal(16, datetimeTz.DateTimeOffset.Hour);
                Assert.Equal(40, datetimeTz.DateTimeOffset.Minute);
                Assert.Equal(5, datetimeTz.DateTimeOffset.Second);
                Assert.Equal(28129323u, datetimeTz.SubsecNanos);
                Assert.True(datetimeTz.IsFixedOffset);
                Assert.Equal(TimeSpan.FromHours(5) + TimeSpan.FromMinutes(45), datetimeTz.DateTimeOffset.Offset);
            }
        }

        [Fact]
        public void TestDurationViaDatabase()
        {
            using var driver = TypeDB.Driver(ServerAddress, new Credentials("admin", "password"), new DriverOptions(false, null));
            driver.Databases.Create(DatabaseName);

            // Define duration attribute
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Schema))
            {
                tx.Query("define attribute d, value duration;");
                tx.Commit();
            }

            // Test: P1Y
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1Y;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(12, duration!.Months);
                Assert.Equal(0, duration.Days);
                Assert.Equal(0, duration.Nanos);
            }

            // Test: P1M
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1M;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(1, duration!.Months);
                Assert.Equal(0, duration.Days);
                Assert.Equal(0, duration.Nanos);
            }

            // Test: P1D
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P1D;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(0, duration!.Months);
                Assert.Equal(1, duration.Days);
                Assert.Equal(0, duration.Nanos);
            }

            // Test: P0DT1H
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT1H;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(0, duration!.Months);
                Assert.Equal(0, duration.Days);
                Assert.Equal(3_600_000_000_000L, duration.Nanos);
            }

            // Test: P0DT1S
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT1S;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(0, duration!.Months);
                Assert.Equal(0, duration.Days);
                Assert.Equal(1_000_000_000L, duration.Nanos);
            }

            // Test: P0DT0.000000001S (1 nanosecond)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P0DT0.000000001S;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(0, duration!.Months);
                Assert.Equal(0, duration.Days);
                Assert.Equal(1L, duration.Nanos);
            }

            // Test: P7W (weeks)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P7W;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(0, duration!.Months);
                Assert.Equal(49, duration.Days);
                Assert.Equal(0, duration.Nanos);
            }

            // Test: P999Y12M31DT24H59M59.999999999S (complex)
            using (var tx = driver.Transaction(DatabaseName, TransactionType.Write))
            {
                var answer = tx.Query("insert $d isa d P999Y12M31DT24H59M59.999999999S;");
                var rows = answer.AsConceptRows().ToList();
                var concept = rows[0].Get("d");
                var duration = concept?.TryGetDuration();

                Assert.NotNull(duration);
                Assert.Equal(12000, duration!.Months);
                Assert.Equal(31, duration.Days);
                Assert.Equal(89999_999_999_999L, duration.Nanos);
            }
        }
    }
}
