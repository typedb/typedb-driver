# TypeDB Rust Driver

## Driver Architecture

To learn about the mechanism that TypeDB drivers use set up communication with databases running on the TypeDB Server,
refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

The TypeDB Rust Driver provides a fully async API that supports multiple async runtimes or a synchronous interface gated
by the `sync` feature.

## API Reference

To learn about the methods available for executing queries and retrieving their answers using Rust, refer to
the [API Reference](https://typedb.com/docs/drivers/rust/api-reference).

## Install TypeDB Rust Driver through Cargo

1. Import `typedb-driver` through Cargo:

```bash
cargo add typedb-driver
```

2. Make sure the [TypeDB Server](https://docs.typedb.com/docs/running-typedb/install-and-run#start-the-typedb-server) is
   running.
3. Use TypeDB Driver in your program (see [Example usage](#example-usage) or `tests/integration` for examples):

```rust
use typedb_driver::TypeDBDriver;

let driver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();
```

## Build from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"
Quickstart"_ section above.

1. Make sure you have [Bazel](https://docs.bazel.build/versions/master/install.html) installed on your machine.

2. Build the library:

   a) to build the native/raw rlib:
   ```
   bazel build //rust:typedb_driver
   ```
   The rlib will be produced at: `bazel-bin/libtypedb_driver-{hash}.rlib`.

   b) to build the crate for a Cargo project:
   ```
   bazel build //rust:assemble_crate
   ```
   The Cargo crate will be produced at:
   ```
   bazel-bin/assemble_crate.crate
   ```
   You can then unzip this crate to retrieve `Cargo.toml`. **Please note**: this process has not yet been thoroughly
   tested. The generated `Cargo.toml` may not be fully correct. See the `Cargo.toml` of the `typedb-driver` crate for
   reference.

## Example usage

### TypeDB Core

<!-- CORE_EXAMPLE_START_MARKER -->

```rust
use futures::{StreamExt, TryStreamExt};
use typedb_driver::{
    answer::{
        concept_document::{Leaf, Node},
        ConceptRow, QueryAnswer,
    },
    concept::{Concept, ValueType},
    Error, TransactionType, TypeDBDriver,
};

fn typedb_example() {
    async_std::task::block_on(async {
        // Open a driver connection
        let driver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();

        // Create a database
        driver.databases().create("typedb").await.unwrap();
        let database = driver.databases().get("typedb").await.unwrap();

        // Dropped transactions are closed
        {
            // Open transactions of 3 types
            let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();

            // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit error
            let result = transaction.query("define entity i-cannot-be-defined-in-read-transactions;").await;
            match result {
                Ok(_) => println!("This line will not be printed"),
                // Handle errors
                Err(error) => match error {
                    Error::Connection(connection) => println!("Could not connect: {connection}"),
                    Error::Server(server) => {
                        println!("Received a detailed server error regarding the executed query: {server}")
                    }
                    Error::Internal(_) => panic!("Unexpected internal error"),
                    _ => println!("Received an unexpected external error: {error}"),
                },
            }
        }

        // Open a schema transaction to make schema changes
        let transaction = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
        let define_query = r#"
        define
          entity person, owns name, owns age;
          attribute name, value string;
          attribute age, value long;
        "#;
        let answer = transaction.query(define_query).await.unwrap();

        // Work with the driver's enums in a classic way or using helper methods
        if answer.is_ok() && matches!(answer, QueryAnswer::Ok(_)) {
            println!("OK results do not give any extra interesting information, but they mean that the query is successfully executed!");
        }

        // Commit automatically closes the transaction (don't forget to await the result!)
        transaction.commit().await.unwrap();

        // Open a read transaction to safely read anything without database modifications
        let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
        let answer = transaction.query("match entity $x;").await.unwrap();

        // Collect concept rows that represent the answer as a table
        let rows: Vec<ConceptRow> = answer.into_rows().try_collect().await.unwrap();
        let row = rows.get(0).unwrap();

        // Retrieve column names to get concepts by index if the variable names are lost
        let column_names = row.get_column_names();

        let column_name = column_names.get(0).unwrap();

        // Get concept by the variable name (column name)
        let concept_by_name = row.get(column_name).unwrap();

        // Get concept by the header's index
        let concept_by_index = row.get_index(0).unwrap();

        // Check if it's an entity type
        if concept_by_name.is_entity_type() {
            print!("Getting concepts by variable names and indexes is equally correct. ");
            println!(
                "Both represent the defined entity type: '{}' (in case of a doubt: '{}')",
                concept_by_name.get_label(),
                concept_by_index.get_label()
            );
        }

        // Continue querying in the same transaction if needed
        let answer = transaction.query("match attribute $a;").await.unwrap();

        // Concept rows can be used as a stream of results
        let mut rows_stream = answer.into_rows();
        while let Some(Ok(row)) = rows_stream.next().await {
            let mut column_names_iter = row.get_column_names().into_iter();
            let column_name = column_names_iter.next().unwrap();

            let concept_by_name = row.get(column_name).unwrap();

            // Check if it's an attribute type to safely retrieve its value type
            if concept_by_name.is_attribute_type() {
                let label = concept_by_name.get_label();
                let value_type = concept_by_name.get_value_type().unwrap();
                println!("Defined attribute type's label: '{label}', value type: '{value_type}'");
            }

            println!("It is also possible to just print the concept itself: '{}'", concept_by_name);
        }

        // Open a write transaction to insert data
        let transaction = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
        let answer = transaction
            .query("insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";")
            .await
            .unwrap();

        // Insert queries also return concept rows
        let mut rows = Vec::new();
        let mut rows_stream = answer.into_rows();
        while let Some(Ok(row)) = rows_stream.next().await {
            rows.push(row);
        }
        let row = rows.get(0).unwrap();

        for column_name in row.get_column_names() {
            let inserted_concept = row.get(column_name).unwrap();
            println!("Successfully inserted ${}: {}", column_name, inserted_concept);
            if inserted_concept.is_entity() {
                println!("This time, it's an entity, not a type!");
            }
        }

        // It is possible to ask for the column names again
        let column_names = row.get_column_names();

        let x = row.get_index(column_names.iter().position(|r| r == "x").unwrap()).unwrap();
        if let Some(iid) = x.get_iid() {
            println!("Each entity receives a unique IID. It can be retrieved directly: {}", iid);
        }

        // Do not forget to commit if the changes should be persisted
        transaction.commit().await.unwrap();

        // Open another write transaction to try inserting even more data
        let transaction = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
        // When loading a large dataset, it's often better not to resolve every query's promise immediately.
        // Instead, collect promises and handle them later. Alternatively, if a commit is expected in the end,
        // just call `commit`, which will wait for all ongoing operations to finish before executing.
        let queries = ["insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";"];
        for query in queries {
            transaction.query(query);
        }
        transaction.commit().await.unwrap();

        {
            let transaction = driver.transaction(database.name(), TransactionType::Write).await.unwrap();

            // Commit will still fail if at least one of the queries produce an error.
            let queries =
                ["insert $c isa not-person, has name \"Chris\";", "insert $d isa person, has name \"David\";"];
            let mut promises = vec![];
            for query in queries {
                promises.push(transaction.query(query));
            }

            let result = transaction.commit().await;
            println!("Commit result will contain the unresolved query's error: {}", result.unwrap_err());
        }

        // Open a read transaction to verify that the inserted data is saved
        let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();

        // A match query can be used for concept row outputs
        let var = "x";
        let answer = transaction.query(format!("match ${} isa person;", var)).await.unwrap();

        // Simple match queries always return concept rows
        let mut count = 0;
        let mut stream = answer.into_rows().map(|result| result.unwrap());
        while let Some(row) = stream.next().await {
            let x = row.get(var).unwrap();
            match x {
                Concept::Entity(x_entity) => {
                    let x_type = x_entity.type_().unwrap();
                    count += 1;
                    println!("Found a person {} of type {}", x, x_type);
                }
                _ => unreachable!("An entity is expected"),
            }
        }
        println!("Total persons found: {}", count);

        // A fetch query can be used for concept document outputs with flexible structure
        let fetch_query = r#"
        match
          $x isa! person, has $a;
          $a isa! $t;
        fetch {
          "single attribute type": $t,
          "single attribute": $a,
          "all attributes": { $x.* },
        };
        "#;
        let answer = transaction.query(fetch_query).await.unwrap();

        // Fetch queries always return concept documents
        let mut count = 0;
        let mut stream = answer.into_documents().map(|result| result.unwrap());
        while let Some(document) = stream.next().await {
            // The content of the document can be explored in details
            match document.root.as_ref().unwrap() {
                Node::Map(map) => {
                    println!("Found a map document:\n{{");
                    for (parameter_name, node) in map {
                        print!("  \"{parameter_name}\": ");
                        match node {
                            Node::Map(map) => println!("map of {} element(s)", map.len()),
                            Node::Leaf(leaf) => match leaf.as_ref().unwrap() {
                                Leaf::Concept(concept) => match concept {
                                    Concept::AttributeType(type_) => println!("attribute type '{}'", type_.label()),
                                    Concept::Attribute(attribute) => println!("attribute '{}'", attribute.value),
                                    _ => unreachable!("Unexpected concept is fetched"),
                                },
                                _ => unreachable!("Unexpected leaf type is fetched"),
                            },
                            _ => unreachable!("Expected lists in inner maps"),
                        }
                        print!("")
                    }
                    println!("}}");
                }
                _ => unreachable!("Unexpected document type is fetched"),
            }

            // The document can be converted to a JSON
            count += 1;
            println!("JSON representation of the fetched document:\n{}", document.into_json().to_string());
        }
        println!("Total documents fetched: {}", count);
        println!("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
    })
}
```

<!-- CORE_EXAMPLE_END_MARKER -->
