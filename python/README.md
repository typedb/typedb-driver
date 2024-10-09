# TypeDB Python Driver

## Driver Architecture

To learn about the mechanism that TypeDB drivers use to set up communication with databases running on the TypeDB
Server, refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

## API Reference

To learn about the methods available for executing queries and retrieving their answers using Python, refer to
the [API Reference](https://typedb.com/docs/drivers/python/api-reference).

## Install TypeDB Driver for Python through Pip

```
pip install typedb-driver
```

If multiple Python versions are available, you may wish to use

```
pip3 install typedb-driver
```

In your python program, import from `typedb.driver`:

```py
from typedb.driver import *

driver = TypeDB.core_driver(address=TypeDB.DEFAULT_ADDRESS)
```

## Examples
### TypeDB Core
<!-- CORE_EXAMPLE_START_MARKER -->
```py
from typedb.driver import *

def typedb_example():
    # Open a driver connection. The connection will be automatically closed on the "with" block exit
    with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
        # Create a database
        driver.databases.create("typedb")
        database = driver.databases.get("typedb")

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

            # Commit automatically closes the transaction. You can still safely call for it inside "with" blocks
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

            # Check if it's an entity type before the conversion
            if concept_by_name.is_entity_type():
                print(f"Getting concepts by variable names and indexes is equally correct. "
                      f"Both represent the defined entity type: '{concept_by_name.as_entity_type().get_label()}' "
                      f"(in case of a doubt: '{concept_by_index.as_entity_type().get_label()}')")

            # Continue querying in the same transaction if needed
            answer = tx.query("match attribute $a;").resolve()

            # Concept rows can be used as any other iterator
            rows = [row for row in answer.as_concept_rows()]
            row = rows[0]

            # Same for column names
            column_names_iter = row.column_names()
            column_name = next(column_names_iter)

            concept_by_name = row.get(column_name)

            # Check if it's an attribute type before the conversion
            if concept_by_name.is_attribute_type():
                attribute_type = concept_by_name.as_attribute_type()
                print(f"Defined attribute type's label: '{attribute_type.get_label()}', "
                      f"value type: '{attribute_type.get_value_type()}'")

            print(f"It is also possible to just print the concept itself: '{concept_by_name}'")

        # Open a write transaction to insert data
        with driver.transaction(database.name, TransactionType.WRITE) as tx:
            answer = tx.query("insert $z isa person, has age 10; $x isa person, has age 20;").resolve()

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
            if x.is_entity():
                print(f"Each entity receives a unique IID. It can be retrieved directly: {x.as_entity().get_iid()}")

            # Do not forget to commit if the changes should be persisted
            tx.commit()

        # Open a read transaction to verify that the inserted data is saved
        with driver.transaction(database.name, TransactionType.READ) as tx:
            var = "x"
            answer = tx.query(f"match ${var} isa person;").resolve()

            # Match queries always return concept rows
            count = 0
            for row in answer.as_concept_rows():
                x = row.get(var)
                x_type = x.as_entity().get_type().as_entity_type()
                count += 1
                print(f"Found a person {x} of type {x_type}")
            print(f"Total persons found: {count}")

    print("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!")
```
<!-- CORE_EXAMPLE_END_MARKER -->
