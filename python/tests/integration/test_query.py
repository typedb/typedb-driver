# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License") you may not use this file except in compliance
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

from hamcrest import *
import unittest
from unittest import TestCase

from typedb.driver import *
import sys
TYPEDB = "typedb"
WRITE = TransactionType.WRITE
READ = TransactionType.READ
SCHEMA = TransactionType.SCHEMA


class TestDebug(TestCase):

    def setUp(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            if driver.databases.contains(TYPEDB):
                driver.databases.get(TYPEDB).delete()
            driver.databases.create(TYPEDB)

    def test_query(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            database = driver.databases.get(TYPEDB)
            assert_that(database.name, is_(TYPEDB))

            with driver.transaction(database.name, SCHEMA) as tx:
                answer = tx.query("define entity person, owns age; attribute age, value long;").resolve()
                assert_that(answer.is_ok(), is_(True))

                tx.commit()

            with driver.transaction(database.name, READ) as tx:
                answer = tx.query("match entity $x;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(1))

                row = rows[0]
                header = [name for name in row.column_names()]
                assert_that(len(header), is_(1))

                column_name = header[0]
                concept_by_name = row.get(column_name)
                concept_by_index = row.get_index(0)
                assert_that(concept_by_name, is_(equal_to(concept_by_index)))

                assert_that(concept_by_name.is_entity_type(), is_(True))
                assert_that(concept_by_name.is_entity(), is_(False))
                assert_that(concept_by_name.is_attribute_type(), is_(False))
                assert_that(concept_by_name.is_type(), is_(True))
                assert_that(concept_by_name.is_thing(), is_(False))
                assert_that(concept_by_name.as_entity_type().get_label().scoped_name(), is_("person"))
                assert_that(concept_by_name.as_entity_type().get_label().name, is_("person"))
                assert_that(concept_by_name.as_entity_type().get_label().scope, is_(None))
                assert_that(concept_by_name.as_entity_type().get_label().scoped_name(), is_not("not person"))
                assert_that(concept_by_name.as_entity_type().get_label().scoped_name(), is_not("age"))

            with driver.transaction(database.name, READ) as tx:
                print("read")
                sys.stdout.flush()
                answer = tx.query("match attribute $a;").resolve()
                print("after read")
                sys.stdout.flush()
                assert_that(answer.is_concept_rows(), is_(True))

                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(1))

                row = rows[0]
                header = [name for name in row.column_names()]
                assert_that(len(header), is_(1))

                column_name = header[0]
                concept_by_name = row.get(column_name)
                concept_by_index = row.get_index(0)
                assert_that(concept_by_name, is_(concept_by_index))

                assert_that(concept_by_name.is_attribute_type(), is_(True))
                assert_that(concept_by_name.is_attribute(), is_(False))
                assert_that(concept_by_name.is_entity_type(), is_(False))
                assert_that(concept_by_name.is_type(), is_(True))
                assert_that(concept_by_name.is_thing(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_boolean(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_struct(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_string(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_decimal(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_double(), is_(False))
                assert_that(concept_by_name.as_attribute_type().is_long(), is_(True))
                assert_that(concept_by_name.as_attribute_type().get_label().scoped_name(), is_("age"))
                assert_that(concept_by_name.as_attribute_type().get_label().name, is_("age"))
                assert_that(concept_by_name.as_attribute_type().get_label().scope, is_(None))
                assert_that(concept_by_name.as_attribute_type().get_label().scoped_name(), is_not("person"))

            with driver.transaction(database.name, WRITE) as tx:
                answer = tx.query("insert $z isa person, has age 10; $x isa person, has age 20;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(1))

                row = rows[0]
                header = [name for name in row.column_names()]
                assert_that(len(header), is_(2))
                assert_that("x" in header, is_(True))
                assert_that("z" in header, is_(True))

                x = row.get_index(header.indexOf("x"))
                assert_that(x.is_entity(), is_(True))
                assert_that(x.is_entity_type(), is_(False))
                assert_that(x.is_attribute(), is_(False))
                assert_that(x.is_type(), is_(False))
                assert_that(x.is_thing(), is_(True))
                assert_that(x.as_entity().getType().as_entity_type().get_label().scoped_name(), is_("person"))
                assert_that(x.as_entity().getType().as_entity_type().get_label().name, is_("person"))
                assert_that(x.as_entity().getType().as_entity_type().get_label().scope, is_(None))
                assert_that(x.as_entity().getType().as_entity_type().get_label().scoped_name(), is_not("not person"))

                z = row.get("z")
                assert_that(z.is_entity(), is_(True))
                assert_that(z.is_entity_type(), is_(False))
                assert_that(z.is_attribute(), is_(False))
                assert_that(z.is_type(), is_(False))
                assert_that(z.is_thing(), is_(True))
                zEntity = z.as_entity()
                assert_that(zEntity.getType().as_entity_type().get_label().scoped_name(), is_("person"))
                assert_that(zEntity.getType().as_entity_type().get_label().name, is_("person"))
                assert_that(zEntity.getType().as_entity_type().get_label().scope, is_(None))
                assert_that(zEntity.getType().as_entity_type().get_label().scoped_name(), is_not("not person"))

                tx.commit()

            with driver.transaction(database.name, READ) as tx:
                var = "x"
                answer = tx.query(f"match ${var} isa person;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                count = 0
                for row in answer.as_concept_rows():
                    x = row.get(var)
                    assert_that(x.is_entity(), is_(True))
                    assert_that(x.is_entity_type(), is_(False))
                    assert_that(x.is_attribute(), is_(False))
                    assert_that(x.is_type(), is_(False))
                    assert_that(x.is_thing(), is_(True))
                    xType = x.as_entity().get_type().as_entity_type()
                    assert_that(xType.get_label().scoped_name(), is_("person"))
                    assert_that(xType.get_label().name, is_("person"))
                    assert_that(xType.get_label().scope, is_(None))
                    assert_that(xType.get_label().scoped_name(), is_not("not person"))
                    count += 1
                assert_that(count, is_(2))
        print("FINISH")
        sys.stdout.flush()



if __name__ == "__main__":
    unittest.main(verbosity=2)
