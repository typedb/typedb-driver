// This file is automatically generated.
// It is not intended for manual editing.
public class TypeDBCloudExample {
    public void example() {
        // Open a driver connection. Try-with-resources can be used for automatic driver connection management
        try (Driver driver = TypeDB.cloudDriver(TypeDB.DEFAULT_ADDRESS, new Credentials("admin", "password"), new DriverOptions(false, null))) {
            // Create a database
            driver.databases().create("typedb");
            Database database = driver.databases().get("typedb");

            // Open transactions of 3 types
            Transaction tx = driver.transaction(database.name(), Transaction.Type.READ);

            // Use "try" blocks to catch driver exceptions
            try {
                // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit exception
                Promise<? extends QueryAnswer> promise = tx.query("define entity i-cannot-be-defined-in-read-transactions;");

                System.out.println("The result is still promised, so it needs resolving even in case of errors!");
                promise.resolve();
            } catch (TypeDBDriverException expectedException) {
                System.out.println("Once the query's promise is resolved, the exception is revealed: " + expectedException);
            } finally {
                // Don't forget to close the transaction!
                tx.close();
            }

            // Open a schema transaction to make schema changes
            // Use try-with-resources blocks to forget about "close" operations (similarly to connections)
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.SCHEMA)) {
                String defineQuery = "define " +
                        "entity person, owns name, owns age; " +
                        "attribute name, value string;\n" +
                        "attribute age, value integer;";

                QueryAnswer answer = transaction.query(defineQuery).resolve();

                // Commit automatically closes the transaction. It can still be safely called inside "try" blocks
                transaction.commit();
            }

            // Open a read transaction to safely read anything without database modifications
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.READ)) {
                QueryAnswer entityAnswer = transaction.query("match entity $x;").resolve();

                // Collect concept rows that represent the answer as a table
                List<ConceptRow> entityRows = entityAnswer.asConceptRows().stream().collect(Collectors.toList());
                ConceptRow entityRow = entityRows.get(0);

                // Collect column names to get concepts by index if the variable names are lost
                List<String> entityHeader = entityRow.columnNames().collect(Collectors.toList());

                String columnName = entityHeader.get(0);

                // Get concept by the variable name (column name)
                Concept conceptByName = entityRow.get(columnName);

                // Get concept by the header's index
                Concept conceptByIndex = entityRow.getIndex(0);

                System.out.printf("Getting concepts by variable names (%s) and indexes (%s) is equally correct. ",
                        conceptByName.getLabel(),
                        conceptByIndex.getLabel());

                // Check if it's an entity type before the conversion
                if (conceptByName.isEntityType()) {
                    System.out.printf("Both represent the defined entity type: '%s' (in case of a doubt: '%s')%n",
                            conceptByName.asEntityType().getLabel(),
                            conceptByIndex.asEntityType().getLabel());
                }

                // Continue querying in the same transaction if needed
                QueryAnswer attributeAnswer = transaction.query("match attribute $a;").resolve();

                // ConceptRowIterator can be used as any other Iterator
                ConceptRowIterator attributeRowIterator = attributeAnswer.asConceptRows();

                while (attributeRowIterator.hasNext()) {
                    ConceptRow attributeRow = attributeRowIterator.next();

                    // Column names are a stream, so they can be used in a similar way
                    Iterator<String> columnNameIterator = attributeRow.columnNames().iterator();
                    columnName = columnNameIterator.next();

                    conceptByName = attributeRow.get(columnName);

                    // Check if it's an attribute type before the conversion
                    if (conceptByName.isAttributeType()) {
                        AttributeType attributeType = conceptByName.asAttributeType();
                        System.out.printf("Defined attribute type's label: '%s', value type: '%s'%n", attributeType.getLabel(), attributeType.tryGetValueType().get());

                        System.out.printf("It is also possible to just print the concept itself: '%s'%n", conceptByName);
                    }
                }
            }

            // Open a write transaction to insert data
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                String insertQuery = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";";
                QueryAnswer answer = transaction.query(insertQuery).resolve();

                // Insert queries also return concept rows
                List<ConceptRow> rows = answer.asConceptRows().stream().collect(Collectors.toList());
                ConceptRow row = rows.get(0);
                row.columnNames().iterator().forEachRemaining(columnName -> {
                    Concept insertedConcept = row.get(columnName);
                    System.out.printf("Successfully inserted $%s: %s%n", columnName, insertedConcept);
                    if (insertedConcept.isEntity()) {
                        System.out.println("This time, it's an entity, not a type!");
                    }
                });

                // It is possible to ask for the column names again
                List<String> header = row.columnNames().collect(Collectors.toList());

                Concept x = row.getIndex(header.indexOf("x"));
                System.out.printf("As we expect an entity instance, we can try to get its IID (unique identification): %s. ", x.tryGetIID());
                if (x.isEntity()) {
                    System.out.println("It can also be retrieved directly and safely after a cast: " + x.asEntity().getIID());
                }

                // Do not forget to commit if the changes should be persisted
                transaction.commit();
            }

            // Open another write transaction to try inserting even more data
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                // When loading a large dataset, it's often better not to resolve every query's promise immediately.
                // Instead, collect promises and handle them later. Alternatively, if a commit is expected in the end,
                // just call `commit`, which will wait for all ongoing operations to finish before executing.
                List<String> queries = List.of("insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";");
                for (String query : queries) {
                    transaction.query(query);
                }
                transaction.commit();
            }

            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.WRITE)) {
                // Commit will still fail if at least one of the queries produce an error.
                List<String> queries = List.of("insert $c isa not-person, has name \"Chris\";", "insert $d isa person, has name \"David\";");
                List<Promise<? extends QueryAnswer>> promises = new ArrayList<>();
                for (String query : queries) {
                    promises.add(transaction.query(query));
                }

                try {
                    transaction.commit();
                } catch (TypeDBDriverException expectedException) {
                    System.out.println("Commit result will contain the unresolved query's error: " + expectedException);
                }
            }

            // Open a read transaction to verify that the inserted data is saved
            try (Transaction transaction = driver.transaction(database.name(), Transaction.Type.READ)) {
                // A match query can be used for concept row outputs
                String var = "x";
                QueryAnswer matchAnswer = transaction.query(String.format("match $%s isa person;", var)).resolve();

                // Simple match queries always return concept rows
                AtomicInteger matchCount = new AtomicInteger(0);
                matchAnswer.asConceptRows().stream().forEach(row -> {
                    Concept x = row.get(var);
                    EntityType xType = x.asEntity().getType().asEntityType();
                    matchCount.incrementAndGet();
                    System.out.printf("Found a person %s of type %s%n", x, xType);
                });
                System.out.println("Total persons found: " + matchCount.get());

                // A fetch query can be used for concept document outputs with flexible structure
                QueryAnswer fetchAnswer = transaction.query("match" +
                        "  $x isa! person, has $a;" +
                        "  $a isa! $t;" +
                        "fetch {" +
                        "  \"single attribute type\": $t," +
                        "  \"single attribute\": $a," +
                        "  \"all attributes\": { $x.* }," +
                        "};").resolve();

                // Fetch queries always return concept documents
                AtomicInteger fetchCount = new AtomicInteger(0);
                fetchAnswer.asConceptDocuments().stream().forEach(document -> {
                    System.out.println("Fetched a document: " + document);
                    System.out.print("This document contains an attribute of type: ");
                    System.out.println(document.asObject().get("single attribute type").asObject().get("label"));

                    fetchCount.incrementAndGet();
                });
                System.out.println("Total documents fetched: " + fetchCount.get());
            }
        }

        System.out.println("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
    }
}
