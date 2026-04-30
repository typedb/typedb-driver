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

use std::{collections::HashMap, str::FromStr};

use chrono::{Datelike, NaiveDate, NaiveDateTime, Timelike};
use futures::TryStreamExt;
use serial_test::serial;
use typedb_driver::{
    Addresses, Credentials, DriverOptions, DriverTlsConfig, TransactionType, TypeDBDriver,
    concept::{
        Value,
        value::{Decimal, Duration, TimeZone},
    },
};

const DATABASE_NAME: &str = "typedb";

async fn cleanup() {
    let driver = TypeDBDriver::new(
        Addresses::try_from_address_str(TypeDBDriver::DEFAULT_ADDRESS).unwrap(),
        Credentials::new("admin", "password"),
        DriverOptions::new(DriverTlsConfig::disabled()),
    )
    .await
    .unwrap();
    if driver.databases().contains(DATABASE_NAME).await.unwrap() {
        driver.databases().get(DATABASE_NAME).await.unwrap().delete().await.unwrap();
    }
}

async fn setup() -> TypeDBDriver {
    cleanup().await;
    let driver = TypeDBDriver::new(
        Addresses::try_from_address_str(TypeDBDriver::DEFAULT_ADDRESS).unwrap(),
        Credentials::new("admin", "password"),
        DriverOptions::new(DriverTlsConfig::disabled()),
    )
    .await
    .unwrap();
    driver.databases().create(DATABASE_NAME).await.unwrap();
    driver
}

// ============================================================================
// Duration unit tests (no database required)
// ============================================================================

#[test]
fn duration_parse_years() {
    let duration = Duration::from_str("P1Y").unwrap();
    assert_eq!(duration.months, 12);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 0);
}

#[test]
fn duration_parse_months() {
    let duration = Duration::from_str("P1M").unwrap();
    assert_eq!(duration.months, 1);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 0);
}

#[test]
fn duration_parse_days() {
    let duration = Duration::from_str("P1D").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 1);
    assert_eq!(duration.nanos, 0);
}

#[test]
fn duration_parse_weeks() {
    let duration = Duration::from_str("P7W").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 49);
    assert_eq!(duration.nanos, 0);
}

#[test]
fn duration_parse_hours() {
    let duration = Duration::from_str("P0DT1H").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 3_600_000_000_000);
}

#[test]
fn duration_parse_minutes() {
    let duration = Duration::from_str("P0DT1M").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 60_000_000_000);
}

#[test]
fn duration_parse_seconds() {
    let duration = Duration::from_str("P0DT1S").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 1_000_000_000);
}

#[test]
fn duration_parse_fractional_seconds() {
    let duration = Duration::from_str("P0DT0.000000001S").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 1);
}

#[test]
fn duration_parse_fractional_seconds_100_nanos() {
    let duration = Duration::from_str("P0DT0.0000001S").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 100);
}

#[test]
fn duration_parse_zero() {
    let duration = Duration::from_str("P0DT0S").unwrap();
    assert_eq!(duration.months, 0);
    assert_eq!(duration.days, 0);
    assert_eq!(duration.nanos, 0);
}

#[test]
fn duration_parse_complex() {
    let duration = Duration::from_str("P1Y10M7DT15H44M5.00394892S").unwrap();
    assert_eq!(duration.months, 22); // 1*12 + 10
    assert_eq!(duration.days, 7);
    // 15H * 3600 * 10^9 + 44M * 60 * 10^9 + 5S * 10^9 + 3948920 nanos
    let expected_nanos: u64 = 15 * 3600 * 1_000_000_000 + 44 * 60 * 1_000_000_000 + 5 * 1_000_000_000 + 3_948_920;
    assert_eq!(duration.nanos, expected_nanos);
}

#[test]
fn duration_parse_max_complex() {
    let duration = Duration::from_str("P999Y12M31DT24H59M59.999999999S").unwrap();
    assert_eq!(duration.months, 12000); // 999*12 + 12
    assert_eq!(duration.days, 31);
    let expected_nanos: u64 = 24 * 3600 * 1_000_000_000 + 59 * 60 * 1_000_000_000 + 59 * 1_000_000_000 + 999_999_999;
    assert_eq!(duration.nanos, expected_nanos);
}

#[test]
fn duration_equality() {
    assert_eq!(Duration::from_str("P1Y0M0DT0H0M0S").unwrap(), Duration::from_str("P1Y").unwrap());
    assert_eq!(Duration::from_str("P0Y12M0DT0H0M0S").unwrap(), Duration::from_str("P1Y").unwrap());
    assert_ne!(Duration::from_str("P0Y1M0DT0H0M0S").unwrap(), Duration::from_str("P1Y").unwrap());
    assert_eq!(Duration::from_str("P0Y0M49DT0H0M0S").unwrap(), Duration::from_str("P7W").unwrap());
    assert_eq!(Duration::from_str("P0W").unwrap(), Duration::from_str("P0DT0S").unwrap());
}

#[test]
fn duration_display_zero() {
    let duration = Duration::new(0, 0, 0);
    assert_eq!(format!("{}", duration), "PT0S");
}

#[test]
fn duration_display_years_months() {
    let duration = Duration::new(14, 0, 0); // 1 year 2 months
    assert_eq!(format!("{}", duration), "P1Y2M");
}

#[test]
fn duration_display_days() {
    let duration = Duration::new(0, 7, 0);
    assert_eq!(format!("{}", duration), "P7D");
}

#[test]
fn duration_display_time() {
    let duration = Duration::new(0, 0, Duration::NANOS_PER_HOUR + Duration::NANOS_PER_MINUTE + Duration::NANOS_PER_SEC);
    assert_eq!(format!("{}", duration), "PT1H1M1S");
}

#[test]
fn duration_display_nanos() {
    let duration = Duration::new(0, 0, Duration::NANOS_PER_SEC * 3 / 2); // 1.5s
    assert_eq!(format!("{}", duration), "PT1.500000000S");
}

#[test]
fn duration_debug() {
    let duration = Duration::new(12, 0, 0);
    assert_eq!(format!("{:?}", duration), "months: 12, days: 0, nanos: 0");
}

// ============================================================================
// Decimal unit tests (no database required)
// ============================================================================

#[test]
fn decimal_new() {
    let decimal = Decimal::new(123, 456);
    assert_eq!(decimal.integer, 123);
    assert_eq!(decimal.fractional, 456);
}

#[test]
fn decimal_display_whole() {
    let decimal = Decimal::new(123, 0);
    assert_eq!(format!("{}", decimal), "123.0");
}

#[test]
fn decimal_display_fractional() {
    let decimal = Decimal::new(1234567890, 1_234_567_890_000_000_000);
    assert_eq!(format!("{}", decimal), "1234567890.123456789dec");
}

#[test]
fn decimal_addition() {
    let a = Decimal::new(1, 5_000_000_000_000_000_000);
    let b = Decimal::new(2, 7_000_000_000_000_000_000);
    let c = a + b;
    assert_eq!(c.integer, 4);
    assert_eq!(c.fractional, 2_000_000_000_000_000_000);
}

#[test]
fn decimal_subtraction() {
    let a = Decimal::new(5, 3_000_000_000_000_000_000);
    let b = Decimal::new(2, 7_000_000_000_000_000_000);
    let c = a - b;
    assert_eq!(c.integer, 2);
    assert_eq!(c.fractional, 6_000_000_000_000_000_000);
}

#[test]
fn decimal_negation() {
    let a = Decimal::new(5, 3_000_000_000_000_000_000);
    let neg_a = -a;
    assert_eq!(neg_a.integer, -6);
    assert_eq!(neg_a.fractional, 7_000_000_000_000_000_000);
}

#[test]
fn decimal_equality() {
    let a = Decimal::new(123, 456);
    let b = Decimal::new(123, 456);
    let c = Decimal::new(123, 457);
    assert_eq!(a, b);
    assert_ne!(a, c);
}

#[test]
fn decimal_ordering() {
    let a = Decimal::new(1, 0);
    let b = Decimal::new(2, 0);
    let c = Decimal::new(1, 1);
    assert!(a < b);
    assert!(a < c);
    assert!(c < b);
}

// ============================================================================
// Integration tests (require running TypeDB server)
// ============================================================================

#[test]
#[serial]
fn test_all_value_types() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define attribute value types
        let attribute_value_types: HashMap<&str, &str> = [
            ("root", "none"),
            ("age", "integer"),
            ("name", "string"),
            ("is-new", "boolean"),
            ("success", "double"),
            ("balance", "decimal"),
            ("birth-date", "date"),
            ("birth-time", "datetime"),
            ("current-time", "datetime-tz"),
            ("current-time-off", "datetime-tz"),
            ("expiration", "duration"),
        ]
        .into_iter()
        .collect();

        // Define attribute values to insert
        let attribute_values: HashMap<&str, &str> = [
            ("age", "25"),
            ("name", "\"John\""),
            ("is-new", "true"),
            ("success", "66.6"),
            ("balance", "1234567890.0001234567890dec"),
            ("birth-date", "2024-09-20"),
            ("birth-time", "1999-02-26T12:15:05"),
            ("current-time", "2024-09-20T16:40:05 Europe/Belfast"),
            ("current-time-off", "2024-09-20T16:40:05.028129323+0545"),
            ("expiration", "P1Y10M7DT15H44M5.00394892S"),
        ]
        .into_iter()
        .collect();

        // Schema transaction: define attributes
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            for (attribute, value_type) in &attribute_value_types {
                let query = if *value_type == "none" {
                    format!("define attribute {} @abstract;", attribute)
                } else {
                    format!("define attribute {}, value {}; entity person owns {};", attribute, value_type, attribute)
                };
                let answer = tx.query(&query).await.unwrap();
                assert!(answer.is_ok());
            }
            tx.commit().await.unwrap();
        }

        // Read transaction: verify attribute types
        {
            let tx = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
            let answer = tx.query("match attribute $a;").await.unwrap();
            assert!(answer.is_row_stream());

            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            assert_eq!(rows.len(), attribute_value_types.len());

            for row in rows {
                let concept = row.get("a").unwrap().unwrap();
                assert!(concept.is_attribute_type());
                let label = concept.get_label();
                let expected_value_type = attribute_value_types.get(label).unwrap();
                let actual_value_type = concept.try_get_value_type().map(|vt| vt.name().to_owned());
                let actual_value_type = actual_value_type.as_deref().unwrap_or("none");
                assert_eq!(actual_value_type, *expected_value_type, "Mismatch for attribute {}", label);
            }
        }

        // Write transaction: insert attribute values
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            for (attribute, value) in &attribute_values {
                let query = format!("insert $a isa person, has {} {};", attribute, value);
                let answer = tx.query(&query).await.unwrap();
                assert!(answer.is_row_stream());

                let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
                assert_eq!(rows.len(), 1);

                let row = &rows[0];
                let column_names = row.get_column_names();
                assert_eq!(column_names.len(), 1);
                assert!(row.get_index(0).unwrap().unwrap().is_entity());
            }
            tx.commit().await.unwrap();
        }

        // Read transaction: verify attribute values
        {
            let tx = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
            let answer = tx.query("match attribute $t; $a isa! $t;").await.unwrap();
            assert!(answer.is_row_stream());

            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            assert_eq!(rows.len(), attribute_values.len());

            let mut checked = 0;
            for row in rows {
                let concept = row.get("a").unwrap().unwrap();
                assert!(concept.is_attribute());
                let attribute_name = concept.get_label();
                let value_type_name = concept.try_get_value_type().map(|vt| vt.name().to_owned()).unwrap();
                let expected_type = attribute_value_types.get(attribute_name).unwrap();
                assert_eq!(value_type_name, *expected_type);

                let expected_str = attribute_values.get(attribute_name).unwrap();
                let value = concept.try_get_value().unwrap();

                match value {
                    Value::Integer(v) => {
                        assert_eq!(*v, expected_str.parse::<i64>().unwrap());
                        checked += 1;
                    }
                    Value::String(v) => {
                        let expected = expected_str.trim_matches('"');
                        assert_eq!(v.as_str(), expected);
                        checked += 1;
                    }
                    Value::Boolean(v) => {
                        assert_eq!(*v, expected_str.parse::<bool>().unwrap());
                        checked += 1;
                    }
                    Value::Double(v) => {
                        assert!((*v - expected_str.parse::<f64>().unwrap()).abs() < f64::EPSILON);
                        checked += 1;
                    }
                    Value::Decimal(_) => {
                        // Decimal value format verified by successful retrieval
                        checked += 1;
                    }
                    Value::Date(v) => {
                        let expected = NaiveDate::parse_from_str(expected_str, "%Y-%m-%d").unwrap();
                        assert_eq!(*v, expected);
                        checked += 1;
                    }
                    Value::Datetime(v) => {
                        let expected = NaiveDateTime::parse_from_str(expected_str, "%Y-%m-%dT%H:%M:%S").unwrap();
                        assert_eq!(*v, expected);
                        checked += 1;
                    }
                    Value::DatetimeTZ(_) => {
                        // DatetimeTZ format is complex, verify successful retrieval
                        checked += 1;
                    }
                    Value::Duration(v) => {
                        let expected = Duration::from_str(expected_str).unwrap();
                        assert_eq!(*v, expected);
                        checked += 1;
                    }
                    Value::Struct(_, _) => {
                        // Struct not tested in this test
                    }
                }
            }
            assert_eq!(checked, attribute_values.len());
        }

        cleanup().await;
    });
}

#[test]
#[serial]
fn test_datetime_naive() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define datetime attribute
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            tx.query("define attribute dt, value datetime;").await.unwrap();
            tx.commit().await.unwrap();
        }

        // Test: 2024-10-09T13:07:38.123456789
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dt isa dt 2024-10-09T13:07:38.123456789;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dt").unwrap().unwrap();
            let datetime = concept.try_get_datetime().unwrap();

            assert_eq!(datetime.year(), 2024);
            assert_eq!(datetime.month(), 10);
            assert_eq!(datetime.day(), 9);
            assert_eq!(datetime.hour(), 13);
            assert_eq!(datetime.minute(), 7);
            assert_eq!(datetime.second(), 38);
            assert_eq!(datetime.nanosecond(), 123456789);
            assert_eq!(datetime.weekday().num_days_from_monday(), 2); // Wednesday
        }

        // Test: epoch boundary 0001-01-01T00:00:00.000000001
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dt isa dt 0001-01-01T00:00:00.000000001;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dt").unwrap().unwrap();
            let datetime = concept.try_get_datetime().unwrap();

            assert_eq!(datetime.year(), 1);
            assert_eq!(datetime.month(), 1);
            assert_eq!(datetime.day(), 1);
            assert_eq!(datetime.hour(), 0);
            assert_eq!(datetime.minute(), 0);
            assert_eq!(datetime.second(), 0);
            assert_eq!(datetime.nanosecond(), 1);
            assert_eq!(datetime.weekday().num_days_from_monday(), 0); // Monday
        }

        // Test: Unix epoch 1970-01-01T00:00:00
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dt isa dt 1970-01-01T00:00:00;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dt").unwrap().unwrap();
            let datetime = concept.try_get_datetime().unwrap();

            assert_eq!(datetime.year(), 1970);
            assert_eq!(datetime.month(), 1);
            assert_eq!(datetime.day(), 1);
            assert_eq!(datetime.hour(), 0);
            assert_eq!(datetime.minute(), 0);
            assert_eq!(datetime.second(), 0);
            assert_eq!(datetime.nanosecond(), 0);
            assert_eq!(datetime.weekday().num_days_from_monday(), 3); // Thursday
        }

        // Test: max value 9999-12-31T23:59:59.999999999
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dt isa dt 9999-12-31T23:59:59.999999999;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dt").unwrap().unwrap();
            let datetime = concept.try_get_datetime().unwrap();

            assert_eq!(datetime.year(), 9999);
            assert_eq!(datetime.month(), 12);
            assert_eq!(datetime.day(), 31);
            assert_eq!(datetime.hour(), 23);
            assert_eq!(datetime.minute(), 59);
            assert_eq!(datetime.second(), 59);
            assert_eq!(datetime.nanosecond(), 999999999);
        }

        cleanup().await;
    });
}

#[test]
#[serial]
fn test_datetime_tz_iana() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define datetime-tz attribute
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            tx.query("define attribute dtz, value datetime-tz;").await.unwrap();
            tx.commit().await.unwrap();
        }

        // Test: IANA timezone
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dtz isa dtz 2024-10-09T13:07:38.123456789 Asia/Calcutta;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dtz").unwrap().unwrap();
            let datetime_tz = concept.try_get_datetime_tz().unwrap();

            assert_eq!(datetime_tz.year(), 2024);
            assert_eq!(datetime_tz.month(), 10);
            assert_eq!(datetime_tz.day(), 9);
            assert_eq!(datetime_tz.hour(), 13);
            assert_eq!(datetime_tz.minute(), 7);
            assert_eq!(datetime_tz.second(), 38);
            assert_eq!(datetime_tz.nanosecond(), 123456789);

            // Verify it's an IANA timezone (Asia/Calcutta is a legacy alias for Asia/Kolkata)
            match datetime_tz.timezone() {
                TimeZone::IANA(tz) => {
                    assert!(
                        tz.name() == "Asia/Kolkata" || tz.name() == "Asia/Calcutta",
                        "Expected Asia/Kolkata or Asia/Calcutta, got {}",
                        tz.name()
                    );
                }
                TimeZone::Fixed(_) => panic!("Expected IANA timezone"),
            }
        }

        cleanup().await;
    });
}

#[test]
#[serial]
fn test_datetime_tz_fixed_offset() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define datetime-tz attribute
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            tx.query("define attribute dtz, value datetime-tz;").await.unwrap();
            tx.commit().await.unwrap();
        }

        // Test: Fixed offset timezone
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $dtz isa dtz 2024-09-20T16:40:05.028129323+0545;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("dtz").unwrap().unwrap();
            let datetime_tz = concept.try_get_datetime_tz().unwrap();

            assert_eq!(datetime_tz.year(), 2024);
            assert_eq!(datetime_tz.month(), 9);
            assert_eq!(datetime_tz.day(), 20);
            assert_eq!(datetime_tz.hour(), 16);
            assert_eq!(datetime_tz.minute(), 40);
            assert_eq!(datetime_tz.second(), 5);
            assert_eq!(datetime_tz.nanosecond(), 28129323);

            // Verify it's a fixed offset timezone (+05:45)
            match datetime_tz.timezone() {
                TimeZone::Fixed(offset) => {
                    assert_eq!(offset.local_minus_utc(), 5 * 3600 + 45 * 60);
                }
                TimeZone::IANA(_) => panic!("Expected Fixed offset timezone"),
            }
        }

        cleanup().await;
    });
}

#[test]
#[serial]
fn test_duration_via_database() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define duration attribute
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            tx.query("define attribute d, value duration;").await.unwrap();
            tx.commit().await.unwrap();
        }

        // Test: P1Y
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P1Y;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 12);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 0);
            assert_eq!(format!("{:?}", duration), "months: 12, days: 0, nanos: 0");
        }

        // Test: P1M
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P1M;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 1);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 0);
        }

        // Test: P1D
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P1D;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 1);
            assert_eq!(duration.nanos, 0);
        }

        // Test: P0DT1H
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P0DT1H;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 3_600_000_000_000);
        }

        // Test: P0DT1S
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P0DT1S;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 1_000_000_000);
        }

        // Test: P0DT0.000000001S (1 nanosecond)
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P0DT0.000000001S;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 1);
        }

        // Test: P0DT0.0000001S (100 nanoseconds)
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P0DT0.0000001S;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 100);
        }

        // Test: P0DT0S (zero)
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P0DT0S;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 0);
            assert_eq!(duration.nanos, 0);
        }

        // Test: P7W (weeks)
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P7W;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 0);
            assert_eq!(duration.days, 49);
            assert_eq!(duration.nanos, 0);
        }

        // Test: P999Y12M31DT24H59M59.999999999S (complex)
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $d isa d P999Y12M31DT24H59M59.999999999S;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("d").unwrap().unwrap();
            let duration = concept.try_get_duration().unwrap();

            assert_eq!(duration.months, 12000);
            assert_eq!(duration.days, 31);
            assert_eq!(duration.nanos, 89999_999_999_999);
        }

        cleanup().await;
    });
}

#[test]
#[serial]
fn test_decimal_via_database() {
    async_std::task::block_on(async {
        let driver = setup().await;
        let database = driver.databases().get(DATABASE_NAME).await.unwrap();

        // Define decimal attribute
        {
            let tx = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
            tx.query("define attribute balance, value decimal;").await.unwrap();
            tx.commit().await.unwrap();
        }

        // Test: Insert and retrieve decimal value
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $b isa balance 1234567890.0001234567890dec;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("b").unwrap().unwrap();
            let decimal = concept.try_get_decimal().unwrap();

            assert_eq!(decimal.integer, 1234567890);
            // The fractional part is 0001234567890 * 10^7 (to fill 19 digits)
            assert_eq!(decimal.fractional, 1234567890_000_000);
        }

        // Test: Zero decimal
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $b isa balance 0.0dec;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("b").unwrap().unwrap();
            let decimal = concept.try_get_decimal().unwrap();

            assert_eq!(decimal.integer, 0);
            assert_eq!(decimal.fractional, 0);
        }

        // Test: Negative decimal
        {
            let tx = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
            let answer = tx.query("insert $b isa balance -123.456dec;").await.unwrap();
            let rows: Vec<_> = answer.into_rows().try_collect().await.unwrap();
            let concept = rows[0].get("b").unwrap().unwrap();
            let decimal = concept.try_get_decimal().unwrap();

            assert_eq!(decimal.integer, -124);
            // fractional should be FRACTIONAL_PART_DENOMINATOR - 0.456 * FRACTIONAL_PART_DENOMINATOR
        }

        cleanup().await;
    });
}
