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

import unittest
from datetime import datetime
from decimal import Decimal
from unittest import TestCase

from hamcrest import *
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.driver import *

TYPEDB = "typedb"
WRITE = TransactionType.WRITE
READ = TransactionType.READ
SCHEMA = TransactionType.SCHEMA


class TestQuery(TestCase):

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
                    assert_that(a_type.get_value_type(),
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
                    assert_that(attribute.get_type().get_value_type(), is_(attribute_value_types[attribute_name]))
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
                        expected_dt, expected_tz = expected.split(" ")
                        assert_that(value, is_(Datetime.utcfromstring(expected_dt, expected_tz)))
                        checked += 1
                    elif attribute.is_duration():
                        assert_that(value, is_(Duration.fromstring(expected)))
                        checked += 1
                    # TODO: Add structs!

                assert_that(checked, is_(len(attribute_values)))  # Make sure that every attribute is checked!


if __name__ == "__main__":
    unittest.main(verbosity=2)
