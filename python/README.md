# TypeDB Python Driver

## Driver Architecture

To learn about the mechanism that TypeDB drivers use to set up communication with databases running on the TypeDB
Server, refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

## API Reference

To learn about the methods available for executing queries and retrieving their answers using Python, refer to
the [API Reference](https://typedb.com/docs/drivers/python/api-reference).

## Install TypeDB Python Driver through Pip
1. Install `typedb-driver` using `pip`:
```bash
pip install typedb-driver
```
2. If multiple Python versions are available, you may wish to use:
```
pip3 install typedb-driver
```
3. Make sure the [TypeDB Server](https://docs.typedb.com/docs/running-typedb/install-and-run#start-the-typedb-server) is running.
4. In your python program, import from `typedb.driver` (see [Example usage](#example-usage) or `tests/integration` for examples):
```py
from typedb.driver import *

driver = TypeDB.driver(address=TypeDB.DEFAULT_ADDRESS, ...)
```

## Example usage

<!-- EXAMPLE_START_MARKER -->

```py
from typedb.driver import *


class TypeDBExample:

    def typedb_example(self):
        # Open a driver connection. Specify your parameters if needed
        # The connection will be automatically closed on the "with" block exit
        with TypeDB.driver(TypeDB.DEFAULT_ADDRESS, Credentials("admin", "password"), DriverOptions()) as driver:
            # Create a database
            driver.databases.create("typedb")
            database = driver.databases.get("typedb")

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
            # Transactions can be opened with configurable options. This option limits its lifetime
            options = TransactionOptions(transaction_timeout_millis=10_000)

            # Use "with" blocks to forget about "close" operations (similarly to connections)
            with driver.transaction(database.name, TransactionType.SCHEMA, options) as tx:
                define_query = """
                define 
                  entity person, owns name, owns age; 
                  attribute name, value string;
                  attribute age, value integer;
                """
                answer = tx.query(define_query).resolve()
                if answer.is_ok():
                    print(f"OK results do not give any extra interesting information, but they mean that the query "
                          f"is successfully executed!")

                # Commit automatically closes the transaction. It can still be safely called inside "with" blocks
                tx.commit()

            # Open a read transaction to safely read anything without database modifications
            with driver.transaction(database.name, TransactionType.READ) as tx:
                answer = tx.query("match entity $x;").resolve()

                # Collect concept rows that represent the answer as a table
                rows = list(answer.as_concept_rows())
                row = rows[0]

                # Collect column names to get concepts by index if the variable names are lost
                header = list(row.column_names())

                column_name = header[0]

                # Get concept by the variable name (column name)
                concept_by_name = row.get(column_name)

                # Get concept by the header's index
                concept_by_index = row.get_index(0)

                print(f"Getting concepts by variable names ({concept_by_name.get_label()}) and "
                      f"indexes ({concept_by_index.get_label()}) is equally correct. ")

                # Check if it's an entity type before the conversion
                if concept_by_name.is_entity_type():
                    print(f"Both represent the defined entity type: '{concept_by_name.as_entity_type().get_label()}' "
                          f"(in case of a doubt: '{concept_by_index.as_entity_type().get_label()}')")

                # Continue querying in the same transaction if needed
                answer = tx.query("match attribute $a;").resolve()

                # Concept rows can be used as any other iterator
                rows = [row for row in answer.as_concept_rows()]

                for row in rows:
                    # Same for column names
                    column_names_iter = row.column_names()
                    column_name = next(column_names_iter)

                    concept_by_name = row.get(column_name)

                    # Check if it's an attribute type before the conversion
                    if concept_by_name.is_attribute_type():
                        attribute_type = concept_by_name.as_attribute_type()
                        print(f"Defined attribute type's label: '{attribute_type.get_label()}', "
                              f"value type: '{attribute_type.try_get_value_type()}'")


                    print(f"It is also possible to just print the concept itself: '{concept_by_name}'")

            # Open a write transaction to insert data
            with driver.transaction(database.name, TransactionType.WRITE) as tx:
                insert_query = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";"
                answer = tx.query(insert_query).resolve()

                # Insert queries also return concept rows
                rows = list(answer.as_concept_rows())
                row = rows[0]

                for column_name in row.column_names():
                    inserted_concept = row.get(column_name)
                    print(f"Successfully inserted ${column_name}: {inserted_concept}")
                    if inserted_concept.is_entity():
                        print("This time, it's an entity, not a type!")

                # It is possible to ask for the column names again
                header = [name for name in row.column_names()]

                x = row.get_index(header.index("x"))
                print("As we expect an entity instance, we can try to get its IID (unique identification): "
                      "{x.try_get_iid()}. ")
                if x.is_entity():
                    print(f"It can also be retrieved directly and safely after a cast: {x.as_entity().get_iid()}")

                # Do not forget to commit if the changes should be persisted
                print('CAUTION: Committing or closing (including leaving the "with" block) a transaction will '
                      'invalidate all its uncollected answer iterators')
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
                # Queries can also be executed with configurable options. This option forces the database
                # to include types of instance concepts in ConceptRows answers
                options = QueryOptions(include_instance_types=True)

                # A match query can be used for concept row outputs
                var = "x"
                answer = tx.query(f"match ${var} isa person;", options).resolve()

                # Simple match queries always return concept rows
                count = 0
                for row in answer.as_concept_rows():
                    x = row.get(var)
                    x_type = x.as_entity().get_type().as_entity_type()
                    count += 1
                    print(f"Found a person {x} of type {x_type}")
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

                # Fetch queries always return concept documents
                count = 0
                for document in answer.as_concept_documents():
                    count += 1
                    print(f"Fetched a document: {document}.")
                    print(f"This document contains an attribute of type: {document['single attribute type']['label']}")
                print(f"Total documents fetched: {count}")

        print("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!")
```

<!-- EXAMPLE_END_MARKER -->
