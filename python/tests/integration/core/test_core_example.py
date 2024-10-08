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
from unittest import TestCase

from hamcrest import *
from typedb.common.exception import TypeDBDriverException
from typedb.driver import *


class TestDebug(TestCase):

    def setUp(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            if driver.databases.contains("typedb"):
                driver.databases.get("typedb").delete()

    def test_example(self):
        # Open a driver connection. The connection will be automatically closed on the "with" block exit
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            # Create a database
            driver.databases.create("typedb")
            database = driver.databases.get("typedb")
            assert_that(database.name, is_("typedb"))

            # Use "try" blocks to catch driver exceptions
            try:
                tx = driver.transaction(database.name, TransactionType.READ)

                # Execute any TypeDB query using TypeQL.
                result_promise = tx.query("define entity i-cannot-be-defined-in-read-transactions;")

                print("The result is still promised, so it needs resolving even in case of errors!")
                result_promise.resolve()
            except TypeDBDriverException as expected_exception:
                print(f"Once the query's promise is resolved, the exception is revealed: {expected_exception}")
            finally:
                # Don't forget to close the transaction!
                tx.close()

            # Open a schema transaction to make schema changes
            # Use "with" blocks to forget about "close" operations (similarly to connections)
            with driver.transaction(database.name, TransactionType.SCHEMA) as tx:
                answer = tx.query("define entity person, owns age; attribute age, value long;").resolve()
                if answer.is_ok():
                    print(f"OK results do not give any extra interesting information, but they mean that the query "
                          f"is successfully executed!")
                assert_that(answer.is_ok(), is_(True))

                # Commit automatically closes the transaction. You can still safely call for it inside "with" blocks
                tx.commit()

            # Open a read transaction to safely read anything without database modifications
            with driver.transaction(database.name, TransactionType.READ) as tx:
                answer = tx.query("match entity $x;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                # Collect concept rows that represent the answer as a table
                rows = list(answer.as_concept_rows())
                assert_that(len(rows), is_(1))
                row = rows[0]

                # Collect column names to get concepts by index if the variable names are lost
                header = list(row.column_names())
                assert_that(len(header), is_(1))

                column_name = header[0]
                assert_that(column_name, is_("x"))

                # Get concept by the variable name (column name)
                concept_by_name = row.get(column_name)

                # Get concept by the header's index
                concept_by_index = row.get_index(0)
                assert_that(concept_by_name, is_(equal_to(concept_by_index)))

                # Check if it's an entity type before the conversion
                if concept_by_name.is_entity_type():
                    print(f"Getting concepts by variable names and indexes is equally correct. "
                          f"Both represent the defined entity type: '{concept_by_name.as_entity_type().get_label()}' "
                          f"(in case of a doubt: '{concept_by_index.as_entity_type().get_label()}')")
                assert_that(concept_by_name.is_entity_type(), is_(True))
                assert_that(concept_by_name.is_type(), is_(True))
                assert_that(concept_by_name.as_entity_type().get_label(), is_("person"))
                assert_that(concept_by_name.as_entity_type().get_label(), is_not("not person"))
                assert_that(concept_by_name.as_entity_type().get_label(), is_not("age"))

                # Continue querying in the same transaction if needed
                answer = tx.query("match attribute $a;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                # Concept rows can be used as any other iterator
                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(1))
                row = rows[0]

                # Same for column names
                column_names_iter = row.column_names()
                column_name = next(column_names_iter)
                assert_that(lambda: next(column_names_iter), raises(StopIteration))

                concept_by_name = row.get(column_name)

                # Check if it's an attribute type before the conversion
                if concept_by_name.is_attribute_type():
                    attribute_type = concept_by_name.as_attribute_type()
                    print(f"Defined attribute type's label: '{attribute_type.get_label()}', "
                          f"value type: '{attribute_type.get_value_type()}'")

                print(f"It is also possible to just print the concept itself: '{concept_by_name}'")
                assert_that(concept_by_name.is_attribute_type(), is_(True))
                assert_that(concept_by_name.is_type(), is_(True))
                assert_that(concept_by_name.as_attribute_type().is_long(), is_(True))
                assert_that(concept_by_name.as_attribute_type().get_value_type(), is_("long"))
                assert_that(concept_by_name.as_attribute_type().get_label(), is_("age"))
                assert_that(concept_by_name.as_attribute_type().get_label(), is_not("person"))
                assert_that(concept_by_name.as_attribute_type().get_label(), is_not("person:age"))

            # Open a write transaction to insert data
            with driver.transaction(database.name, TransactionType.WRITE) as tx:
                answer = tx.query("insert $z isa person, has age 10; $x isa person, has age 20;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                # Insert queries also return concept rows
                rows = list(answer.as_concept_rows())
                assert_that(len(rows), is_(1))
                row = rows[0]

                for column_name in row.column_names():
                    inserted_concept = row.get(column_name)
                    print(f"Successfully inserted ${column_name}: {inserted_concept}")
                    if inserted_concept.is_entity():
                        print("This time, it's an entity, not a type!")

                # It is possible to ask for the column names again
                header = [name for name in row.column_names()]
                assert_that(len(header), is_(2))
                assert_that("x" in header, is_(True))
                assert_that("z" in header, is_(True))

                x = row.get_index(header.index("x"))
                if x.is_entity():
                    print(f"Each entity receives a unique IID. It can be retrieved directly: {x.as_entity().get_iid()}")

                # Do not forget to commit if the changes should be persisted
                tx.commit()

            # Open a read transaction to verify that the inserted data is saved
            with driver.transaction(database.name, TransactionType.READ) as tx:
                var = "x"
                answer = tx.query(f"match ${var} isa person;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))

                # Match queries always return concept rows
                count = 0
                for row in answer.as_concept_rows():
                    x = row.get(var)
                    assert_that(x.is_entity(), is_(True))
                    assert_that(x.is_entity_type(), is_(False))
                    assert_that(x.is_attribute(), is_(False))
                    assert_that(x.is_type(), is_(False))
                    assert_that(x.is_thing(), is_(True))
                    x_type = x.as_entity().get_type().as_entity_type()
                    assert_that(x_type.get_label(), is_("person"))
                    assert_that(x_type.get_label(), is_not("not person"))
                    count += 1
                    print(f"Found a person {x} of type {x_type}")
                assert_that(count, is_(2))
                print(f"Total persons found: {count}")

                # TODO: We can add a fetch example here!

        print("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!")


if __name__ == "__main__":
    unittest.main(verbosity=2)
