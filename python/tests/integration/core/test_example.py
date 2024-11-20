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
# EXAMPLE START MARKER
from typedb.driver import *

# EXAMPLE END MARKER


class TestExample(TestCase):

    def setUp(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            if driver.databases.contains("typedb"):
                driver.databases.get("typedb").delete()

    # EXAMPLE START MARKER

    def test_example(self):
        # Open a driver connection. The connection will be automatically closed on the "with" block exit
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            # Create a database
            driver.databases.create("typedb")
            database = driver.databases.get("typedb")
            assert_that(database.name, is_("typedb"))

            # Use "try" blocks to catch driver exceptions
            try:
                # Open transactions of 3 types
                tx = driver.transaction(database.name, TransactionType.READ)

                # Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit exception
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
                define_query = """
                define 
                  entity person, owns name, owns age; 
                  attribute name, value string;
                  attribute age, value long;
                """
                answer = tx.query(define_query).resolve()
                if answer.is_ok():
                    print(f"OK results do not give any extra interesting information, but they mean that the query "
                          f"is successfully executed!")
                assert_that(answer.is_ok(), is_(True))
                assert_that(answer.query_type, is_(QueryType.SCHEMA))

                # Commit automatically closes the transaction. It can still be safely called inside "with" blocks
                tx.commit()

            # Open a read transaction to safely read anything without database modifications
            with driver.transaction(database.name, TransactionType.READ) as tx:
                answer = tx.query("match entity $x;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))
                assert_that(answer.is_concept_documents(), is_(False))
                assert_that(answer.query_type, is_(QueryType.READ))

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

                print(f"Getting concepts by variable names ({concept_by_name.get_label()}) and "
                      f"indexes ({concept_by_index.get_label()}) is equally correct. ")

                # Check if it's an entity type before the conversion
                if concept_by_name.is_entity_type():
                    print(f"Both represent the defined entity type: '{concept_by_name.as_entity_type().get_label()}' "
                          f"(in case of a doubt: '{concept_by_index.as_entity_type().get_label()}')")
                assert_that(concept_by_name.is_entity_type(), is_(True))
                assert_that(concept_by_name.is_type(), is_(True))
                assert_that(concept_by_name.get_label(), is_("person"))
                assert_that(concept_by_name.as_entity_type().get_label(), is_("person"))
                assert_that(concept_by_name.as_entity_type().get_label(), is_not("not person"))
                assert_that(concept_by_name.as_entity_type().get_label(), is_not("age"))

                # Continue querying in the same transaction if needed
                answer = tx.query("match attribute $a;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))
                assert_that(answer.query_type, is_(QueryType.READ))

                # Concept rows can be used as any other iterator
                rows = [row for row in answer.as_concept_rows()]
                assert_that(len(rows), is_(2))

                for row in rows:
                    # Same for column names
                    column_names_iter = row.column_names()
                    column_name = next(column_names_iter)
                    assert_that(lambda: next(column_names_iter), raises(StopIteration))

                    concept_by_name = row.get(column_name)

                    # Check if it's an attribute type before the conversion
                    if concept_by_name.is_attribute_type():
                        attribute_type = concept_by_name.as_attribute_type()
                        print(f"Defined attribute type's label: '{attribute_type.get_label()}', "
                              f"value type: '{attribute_type.try_get_value_type()}'")

                        assert_that(attribute_type.is_long() or attribute_type.is_string(), is_(True))
                        assert_that(attribute_type.try_get_value_type(), is_in(["long", "string"]))
                        assert_that(attribute_type.get_label(), is_in(["age", "name"]))
                        assert_that(attribute_type.get_label(), is_not("person"))
                        assert_that(attribute_type.get_label(), is_not("person:age"))

                    print(f"It is also possible to just print the concept itself: '{concept_by_name}'")
                    assert_that(concept_by_name.is_attribute_type(), is_(True))
                    assert_that(concept_by_name.is_type(), is_(True))

            # Open a write transaction to insert data
            with driver.transaction(database.name, TransactionType.WRITE) as tx:
                insert_query = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";"
                answer = tx.query(insert_query).resolve()
                assert_that(answer.is_concept_rows(), is_(True))
                assert_that(answer.query_type, is_(QueryType.WRITE))

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
                print(
                    "As we expect an entity instance, we can try to get its IID (unique identification): {x.try_get_iid()}. ")
                if x.is_entity():
                    print(f"It can also be retrieved directly and safely after a cast: {x.as_entity().get_iid()}")

                # Do not forget to commit if the changes should be persisted
                tx.commit()

            # Open another write transaction to try inserting even more data
            with driver.transaction(database.name, TransactionType.WRITE) as tx:
                # When loading a large dataset, it's often better not to resolve every query's promise immediately.
                # Instead, collect promises and handle them later. Alternatively, if a commit is expected in the end,
                # just call `commit`, which will wait for all ongoing operations to finish before executing.
                queries = ["insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";"]
                for query in queries:
                    tx.query(query)
                tx.commit()

            with driver.transaction(database.name, TransactionType.WRITE) as tx:
                # Commit will still fail if at least one of the queries produce an error.
                queries = ["insert $c isa not-person, has name \"Chris\";", "insert $d isa person, has name \"David\";"]
                promises = []
                for query in queries:
                    promises.append(tx.query(query))

                try:
                    tx.commit()
                    assert False, "TypeDBDriverException is expected"
                except TypeDBDriverException as expected_exception:
                    print(f"Commit result will contain the unresolved query's error: {expected_exception}")

            # Open a read transaction to verify that the previously inserted data is saved
            with driver.transaction(database.name, TransactionType.READ) as tx:
                # A match query can be used for concept row outputs
                var = "x"
                answer = tx.query(f"match ${var} isa person;").resolve()
                assert_that(answer.is_concept_rows(), is_(True))
                assert_that(answer.query_type, is_(QueryType.READ))

                # Simple match queries always return concept rows
                count = 0
                for row in answer.as_concept_rows():
                    x = row.get(var)
                    assert_that(x.is_entity(), is_(True))
                    assert_that(x.is_entity_type(), is_(False))
                    assert_that(x.is_attribute(), is_(False))
                    assert_that(x.is_type(), is_(False))
                    assert_that(x.is_instance(), is_(True))
                    x_type = x.as_entity().get_type().as_entity_type()
                    assert_that(x_type.get_label(), is_("person"))
                    assert_that(x_type.get_label(), is_not("not person"))
                    count += 1
                    print(f"Found a person {x} of type {x_type}")
                assert_that(count, is_(4))
                print(f"Total persons found: {count}")

                # A fetch query can be used for concept document outputs with flexible structure
                fetch_query = """
                match
                  $x isa! person, has $a;
                  $a isa! $t;
                fetch {
                  "single attribute type": $t,
                  "single attribute": $a,
                  "all attributes": { $x.* },
                };
                """
                answer = tx.query(fetch_query).resolve()
                assert_that(answer.is_concept_documents(), is_(True))
                assert_that(answer.query_type, is_(QueryType.READ))

                # Fetch queries always return concept documents
                count = 0
                for document in answer.as_concept_documents():
                    count += 1
                    print(f"Fetched a document: {document}.")
                    print(f"This document contains an attribute of type: {document['single attribute type']['label']}")
                assert_that(count, is_(5))
                print(f"Total documents fetched: {count}")

        print("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!")


# EXAMPLE END MARKER

if __name__ == "__main__":
    unittest.main(verbosity=2)
