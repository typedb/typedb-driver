# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import os
import time
import unittest
from datetime import datetime, date
from decimal import Decimal
from unittest import TestCase

from hamcrest import *
from typedb.driver import *

TYPEDB = "typedb"
WRITE = TransactionType.WRITE
READ = TransactionType.READ
SCHEMA = TransactionType.SCHEMA


class TestValues(TestCase):

    def setUp(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            if driver.databases.contains(TYPEDB):
                driver.databases.get(TYPEDB).delete()
            driver.databases.create(TYPEDB)

    def test_values(self):
        attribute_value_types = {
            "root": "none",
            "age": "long",
            "name": "string",
            "is-new": "boolean",
            "success": "double",
            "balance": "decimal",
            "birth-date": "date",
            "birth-time": "datetime",
            "current-time": "datetime-tz",
            "current-time-off": "datetime-tz",
            "expiration": "duration"
        }

        attribute_values = {
            "age": "25",
            "name": "\"John\"",
            "is-new": "true",
            "success": "66.6",
            "balance": "1234567890.0001234567890",
            "birth-date": "2024-09-20",
            "birth-time": "1999-02-26T12:15:05",
            "current-time": "2024-09-20T16:40:05 Europe/Belfast",
            "current-time-off": "2024-09-20T16:40:05.028129323+0545",
            "expiration": "P1Y10M7DT15H44M5.00394892S"
        }

        with (TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver):
            database = driver.databases.get(TYPEDB)

            with driver.transaction(database.name, SCHEMA) as tx:
                for attribute, value_type in attribute_value_types.items():
                    query = f"define attribute {attribute} @abstract;" if value_type == "none" \
                        else f"define attribute {attribute}, value {value_type}; entity person owns {attribute};"
                    assert_that(tx.query(query).resolve().is_ok(), is_(True))
                tx.commit()

            with driver.transaction(database.name, READ) as tx:
                answer = tx.query("match attribute $a;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                count = 0
                for row in answer.as_concept_rows():
                    a = row.get("a")
                    assert_that(a.is_attribute_type(), is_(True))
                    a_type = a.as_attribute_type()
                    a_type_value_type = a_type.try_get_value_type()
                    assert_that(a_type_value_type if a_type_value_type is not None else "none",
                                is_(equal_to(attribute_value_types[a_type.get_label()])))
                    count += 1
                assert_that(count, is_(len(attribute_value_types)))

            with driver.transaction(database.name, WRITE) as tx:
                for attribute, value in attribute_values.items():
                    answer = tx.query(f"insert $a isa person, has {attribute} {value};").resolve()
                    assert_that(answer.is_concept_rows(), is_(True))

                    rows = [row for row in answer.as_concept_rows()]
                    assert_that(len(rows), is_(1))

                    row = rows[0]
                    header = [name for name in row.column_names()]
                    assert_that(len(header), is_(1))
                    assert_that(row.get_index(header.index("a")).is_entity(), is_(True))

                tx.commit()

            with driver.transaction(database.name, READ) as tx:
                answer = tx.query("match attribute $t; $a isa! $t;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(len(attribute_values)))
                checked = 0
                for row in rows:
                    attribute = row.get("a").as_attribute()
                    attribute_name = attribute.get_type().get_label()
                    assert_that(attribute.get_value_type(), is_(attribute_value_types[attribute_name]))
                    value = attribute.get_value()
                    expected = attribute_values[attribute_name]

                    if attribute.is_long():
                        assert_that(value, is_(int(expected)))
                        checked += 1
                    elif attribute.is_string():
                        assert_that(value, is_(expected[1:-1]))
                        checked += 1
                    elif attribute.is_boolean():
                        assert_that(value, is_(bool(expected)))
                        checked += 1
                    elif attribute.is_double():
                        assert_that(value, is_(float(expected)))
                        checked += 1
                    elif attribute.is_decimal():
                        assert_that(value, is_(Decimal(expected)))
                        checked += 1
                    elif attribute.is_date():
                        date_format = "%Y-%m-%d"
                        assert_that(value, is_(datetime.strptime(expected, date_format).date()))
                        checked += 1
                    elif attribute.is_datetime():
                        assert_that(value, is_(Datetime.utcfromstring(expected)))
                        checked += 1
                    elif attribute.is_datetime_tz():
                        if "-off" in attribute_name:
                            offset_start = expected.rfind("+")
                            if offset_start == -1:
                                offset_start = expected.rfind("-")
                            if offset_start == -1:
                                raise ValueError("No IANA or Offset values for datetime-tz")
                            expected_dt, expected_offset = expected[:offset_start], Datetime.offset_seconds_fromstring(
                                expected[offset_start:])
                            assert_that(
                                value,
                                is_(Datetime.utcfromstring(expected_dt, offset_seconds=expected_offset)))
                        else:
                            expected_dt, expected_tz = expected.split(" ")
                            assert_that(value, is_(Datetime.utcfromstring(expected_dt, tz_name=expected_tz)))
                        checked += 1
                    elif attribute.is_duration():
                        assert_that(value, is_(Duration.fromstring(expected)))
                        checked += 1
                    # TODO: Add structs!

                assert_that(checked, is_(len(attribute_values)))  # Make sure that every attribute is checked!

    #####################################################
    # Python-specific value types implementations tests #
    #####################################################

    def test_datetime(self):
        # utcfromstring examples do not fail
        Datetime.utcfromstring("2024-09-21T18:34:22")
        Datetime.utcfromstring("2024-09-21T18:34:22.009257123")
        Datetime.utcfromstring("2024-09-21T18:34:22.009257123", tz_name="Europe/London")
        Datetime.utcfromstring("2024-09-21", datetime_fmt="%Y-%m-%d")
        Datetime.utcfromstring("21/09/24 18:34", tz_name="Europe/London", datetime_fmt="%d/%m/%y %H:%M")

        # fromstring examples do not fail
        Datetime.fromstring("2024-09-21T18:34:22", tz_name="America/New_York")
        Datetime.fromstring("2024-09-21T18:34:22.009257123", tz_name="Europe/London")
        Datetime.fromstring("2024-09-21", tz_name="Asia/Calcutta", datetime_fmt="%Y-%m-%d")
        Datetime.fromstring("21/09/24 18:34", tz_name="Africa/Cairo", datetime_fmt="%d/%m/%y %H:%M")

        with (TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver):
            database = driver.databases.get(TYPEDB)

            with driver.transaction(database.name, SCHEMA) as tx:
                tx.query("define attribute dt, value datetime; attribute dtz, value datetime-tz;").resolve()
                tx.commit()

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $dt 2024-10-09T13:07:38.123456789 isa dt;").resolve()
                # answer = tx.query("match $dt isa dt;").resolve() # TODO: Looks like it doesn't work...
                typedb_datetime = list(answer.as_concept_rows())[0].get("dt").as_attribute().get_datetime()

                assert_that(f"{typedb_datetime}", is_("2024-10-09T13:07:38.123456789"))
                assert_that(typedb_datetime.isoformat(), is_("2024-10-09T13:07:38.123456789"))
                assert_that(typedb_datetime.datetime_without_nanos,
                            is_(datetime.fromisoformat("2024-10-09T13:07:38.123456")))
                assert_that(typedb_datetime.tz_name, is_(None))
                assert_that(typedb_datetime.total_seconds, is_(1728479258.1234567))
                assert_that(typedb_datetime.year, is_(2024))
                assert_that(typedb_datetime.month, is_(10))
                assert_that(typedb_datetime.day, is_(9))
                assert_that(typedb_datetime.hour, is_(13))
                assert_that(typedb_datetime.minute, is_(7))
                assert_that(typedb_datetime.second, is_(38))
                assert_that(typedb_datetime.microsecond, is_(123456))
                assert_that(typedb_datetime.nanos, is_(123456789))
                assert_that(typedb_datetime.tzinfo, is_(None))
                assert_that(typedb_datetime.date, is_(date.fromisoformat("2024-10-09")))
                assert_that(typedb_datetime.weekday, is_(2))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789"), is_(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024/09/10 13:07:38.123456789", datetime_fmt="%Y/%d/%m %H:%M:%S"),
                            is_(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728479258, subsec_nanos=123456789),
                            is_(typedb_datetime))

                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456780"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.012345678"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:39.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:28.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:08:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:17:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T14:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T23:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-01T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-19T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-12-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-01-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2022-10-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2034-10-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2124-10-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("1024-10-09T13:07:38.123456789"), is_not(typedb_datetime))

                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789", "Europe/London"),
                            is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789", "UTC"), is_not(typedb_datetime))

                assert_that(Datetime.fromstring("2024-10-09T13:07:38.123456789", "Europe/London"),
                            is_not(typedb_datetime))
                assert_that(Datetime.fromstring("2024-10-09T13:07:38.123456789", "UTC"), is_not(typedb_datetime))

                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728479258, subsec_nanos=0),
                            is_not(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728479250, subsec_nanos=123456789),
                            is_not(typedb_datetime))
                assert_that(
                    Datetime.utcfromtimestamp(timestamp_seconds=1728479258, subsec_nanos=123456789, tz_name="UTC"),
                    is_not(typedb_datetime))

                assert_that(Datetime.fromtimestamp(timestamp_seconds=1728479258, subsec_nanos=123456789, tz_name="UTC"),
                            is_not(typedb_datetime))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $dt 0001-01-01T00:00:00.000000001 isa dt;").resolve()
                typedb_datetime = list(answer.as_concept_rows())[0].get("dt").as_attribute().get_datetime()

                assert_that(f"{typedb_datetime}", is_("0001-01-01T00:00:00.000000001"))
                assert_that(typedb_datetime.isoformat(), is_("0001-01-01T00:00:00.000000001"))
                assert_that(typedb_datetime.datetime_without_nanos, is_(datetime(1, 1, 1)))
                assert_that(typedb_datetime.tz_name, is_(None))
                assert_that(typedb_datetime.year, is_(1))
                assert_that(typedb_datetime.month, is_(1))
                assert_that(typedb_datetime.day, is_(1))
                assert_that(typedb_datetime.hour, is_(0))
                assert_that(typedb_datetime.minute, is_(0))
                assert_that(typedb_datetime.second, is_(0))
                assert_that(typedb_datetime.microsecond, is_(0))
                assert_that(typedb_datetime.nanos, is_(1))
                assert_that(typedb_datetime.tzinfo, is_(None))
                assert_that(typedb_datetime.date, is_(date.fromisoformat("0001-01-01")))
                assert_that(typedb_datetime.weekday, is_(0))
                assert_that(Datetime.utcfromstring("0001-01-01T00:00:00.000000001"), is_(typedb_datetime))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $dt 1970-01-01T00:00:00 isa dt;").resolve()
                typedb_datetime = list(answer.as_concept_rows())[0].get("dt").as_attribute().get_datetime()

                assert_that(f"{typedb_datetime}", is_("1970-01-01T00:00:00.000000000"))
                assert_that(typedb_datetime.isoformat(), is_("1970-01-01T00:00:00.000000000"))
                assert_that(typedb_datetime.datetime_without_nanos, is_(datetime.fromisoformat("1970-01-01T00:00:00")))
                assert_that(typedb_datetime.tz_name, is_(None))
                assert_that(typedb_datetime.total_seconds, is_(0))
                assert_that(typedb_datetime.year, is_(1970))
                assert_that(typedb_datetime.month, is_(1))
                assert_that(typedb_datetime.day, is_(1))
                assert_that(typedb_datetime.hour, is_(0))
                assert_that(typedb_datetime.minute, is_(0))
                assert_that(typedb_datetime.second, is_(0))
                assert_that(typedb_datetime.microsecond, is_(0))
                assert_that(typedb_datetime.nanos, is_(0))
                assert_that(typedb_datetime.tzinfo, is_(None))
                assert_that(typedb_datetime.date, is_(date.fromisoformat("1970-01-01")))
                assert_that(typedb_datetime.weekday, is_(3))
                assert_that(Datetime.utcfromstring("1970-01-01T00:00:00"), is_(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=0, subsec_nanos=0), is_(typedb_datetime))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $dt 9999-12-31T23:59:59.999999999 isa dt;").resolve()
                typedb_datetime = list(answer.as_concept_rows())[0].get("dt").as_attribute().get_datetime()

                assert_that(f"{typedb_datetime}", is_("9999-12-31T23:59:59.999999999"))
                assert_that(typedb_datetime.isoformat(), is_("9999-12-31T23:59:59.999999999"))
                assert_that(typedb_datetime.datetime_without_nanos,
                            is_(datetime.fromisoformat("9999-12-31T23:59:59.999999")))
                assert_that(typedb_datetime.tz_name, is_(None))
                assert_that(typedb_datetime.total_seconds, is_(253402300800))
                assert_that(typedb_datetime.year, is_(9999))
                assert_that(typedb_datetime.month, is_(12))
                assert_that(typedb_datetime.day, is_(31))
                assert_that(typedb_datetime.hour, is_(23))
                assert_that(typedb_datetime.minute, is_(59))
                assert_that(typedb_datetime.second, is_(59))
                assert_that(typedb_datetime.microsecond, is_(999999))
                assert_that(typedb_datetime.nanos, is_(999999999))
                assert_that(typedb_datetime.tzinfo, is_(None))
                assert_that(typedb_datetime.date, is_(date.fromisoformat("9999-12-31")))
                assert_that(Datetime.utcfromstring("9999-12-31T23:59:59.999999999"), is_(typedb_datetime))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $dtz 2024-10-09T13:07:38.123456789 Asia/Calcutta isa dtz;").resolve()
                typedb_datetime = list(answer.as_concept_rows())[0].get("dtz").as_attribute().get_datetime_tz()

                assert_that(f"{typedb_datetime}", is_("2024-10-09T13:07:38.123456789+05:30"))
                assert_that(typedb_datetime.isoformat(), is_("2024-10-09T13:07:38.123456789+05:30"))
                assert_that(typedb_datetime.datetime_without_nanos,
                            is_(datetime.fromisoformat("2024-10-09T13:07:38.123456+05:30")))
                assert_that(typedb_datetime.tz_name, is_("Asia/Calcutta"))
                assert_that(typedb_datetime.total_seconds, is_(1728459458.1234567))
                assert_that(typedb_datetime.year, is_(2024))
                assert_that(typedb_datetime.month, is_(10))
                assert_that(typedb_datetime.day, is_(9))
                assert_that(typedb_datetime.hour, is_(13))
                assert_that(typedb_datetime.minute, is_(7))
                assert_that(typedb_datetime.second, is_(38))
                assert_that(typedb_datetime.microsecond, is_(123456))
                assert_that(typedb_datetime.nanos, is_(123456789))
                assert_that(typedb_datetime.tzinfo, is_not(None))
                assert_that(typedb_datetime.date, is_(date.fromisoformat("2024-10-09")))
                assert_that(typedb_datetime.weekday, is_(2))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789", "Asia/Calcutta"),
                            is_(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024/09/10 13:07:38.123456789", "Asia/Calcutta",
                                                   datetime_fmt="%Y/%d/%m %H:%M:%S"), is_(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728479258, subsec_nanos=123456789,
                                                      tz_name="Asia/Calcutta"), is_(typedb_datetime))

                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789"), is_not(typedb_datetime))
                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789", "Asia/Colombo"),
                            is_(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728459458, subsec_nanos=123456789),
                            is_not(typedb_datetime))
                assert_that(Datetime.utcfromtimestamp(timestamp_seconds=1728479258, subsec_nanos=123456789),
                            is_not(typedb_datetime))

                old_tz = os.environ["TZ"]
                os.environ["TZ"] = "Asia/Calcutta"
                time.tzset()

                assert_that(Datetime.utcfromstring("2024-10-09T13:07:38.123456789", "Asia/Calcutta"),
                            is_(typedb_datetime))
                assert_that(Datetime.fromstring("2024-10-09T07:37:38.123456789", "Asia/Calcutta"),
                            is_(typedb_datetime))

                os.environ["TZ"] = old_tz
                time.tzset()

    def test_duration(self):
        # fromstring examples do not fail
        Duration.fromstring("P1Y10M7DT15H44M5.00394892S")
        Duration.fromstring("P55W")

        with (TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver):
            database = driver.databases.get(TYPEDB)

            with driver.transaction(database.name, SCHEMA) as tx:
                tx.query("define attribute d, value duration;").resolve()
                tx.commit()

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P1Y isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 12, days: 0, nanos: 0"))
                assert_that(typedb_duration.months, is_(12))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(0))
                assert_that(Duration.fromstring("P1Y0M0DT0H0M0S"), is_(typedb_duration))
                assert_that(Duration.fromstring("P0Y12M0DT0H0M0S"), is_(typedb_duration))
                assert_that(Duration.fromstring("P0Y1M0DT0H0M0S"), is_not(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P1M isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 1, days: 0, nanos: 0"))
                assert_that(typedb_duration.months, is_(1))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(0))
                assert_that(Duration.fromstring("P0Y1M0DT0H0M0S"), is_(typedb_duration))
                assert_that(Duration.fromstring("P0Y0M31DT0H0M0S"), is_not(typedb_duration))
                assert_that(Duration.fromstring("P0Y0M30DT0H0M0S"), is_not(typedb_duration))
                assert_that(Duration.fromstring("P0Y0M29DT0H0M0S"), is_not(typedb_duration))
                assert_that(Duration.fromstring("P0Y0M28DT0H0M0S"), is_not(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P1D isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 1, nanos: 0"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(1))
                assert_that(typedb_duration.nanos, is_(0))
                assert_that(Duration.fromstring("P0Y0M1DT0H0M0S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P0DT1H isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 0, nanos: 3600000000000"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(3600000000000))
                assert_that(Duration.fromstring("P0Y0M0DT1H0M0S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P0DT1S isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 0, nanos: 1000000000"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(1000000000))
                assert_that(Duration.fromstring("P0Y0M0DT0H0M1S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P0DT0.000000001S isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 0, nanos: 1"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(1))
                assert_that(Duration.fromstring("P0Y0M0DT0H0M0.000000001S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P0DT0.0000001S isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 0, nanos: 100"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(100))
                assert_that(Duration.fromstring("P0Y0M0DT0H0M0.0000001S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P0DT0S isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 0, nanos: 0"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(0))
                assert_that(typedb_duration.nanos, is_(0))
                assert_that(Duration.fromstring("P0Y0M0DT0H0M0S"), is_(typedb_duration))
                assert_that(Duration.fromstring("P0W"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P7W isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 0, days: 49, nanos: 0"))
                assert_that(typedb_duration.months, is_(0))
                assert_that(typedb_duration.days, is_(49))
                assert_that(typedb_duration.nanos, is_(0))
                assert_that(Duration.fromstring("P7W"), is_(typedb_duration))
                assert_that(Duration.fromstring("P0Y0M49DT0H0M0S"), is_(typedb_duration))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $d P999Y12M31DT24H59M59.999999999S isa d;").resolve()
                typedb_duration = list(answer.as_concept_rows())[0].get("d").as_attribute().get_duration()

                assert_that(f"{typedb_duration}", is_("months: 12000, days: 31, nanos: 89999999999999"))
                assert_that(typedb_duration.months, is_(12000))
                assert_that(typedb_duration.days, is_(31))
                assert_that(typedb_duration.nanos, is_(89999999999999))
                assert_that(Duration.fromstring("P999Y12M31DT24H59M59.999999999S"), is_(typedb_duration))


if __name__ == "__main__":
    unittest.main(verbosity=2)
