# TypeDB Java Driver

## Driver Architecture
To learn about the mechanism that TypeDB drivers use set up communication with databases running on the TypeDB Server, refer to the [Drivers Overview](https://typedb.com/docs/drivers/overview).

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Java, refer to the [API Reference](https://typedb.com/docs/drivers/java/api-reference).

## Import TypeDB Driver for Java through Maven

```xml
<repositories>
    <repository>
        <id>repo.typedb.com</id>
        <url>https://repo.typedb.com/public/public-release/maven/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.typedb</groupId>
        <artifactId>typedb-driver</artifactId>
        <version>{version}</version>
    </dependency>
</dependencies>
```

Further documentation: https://typedb.com/docs/drivers/java/overview

## Build TypeDB Driver for Java from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Import TypeDB Driver for Java"_ section above.

1. Make sure you have the following dependencies installed on your machine:
    - Java JDK 11 or higher
    - [Bazel](https://docs.bazel.build/versions/master/install.html)

2. Build the JAR:

   a) to build the native/raw JAR:
   ```
   bazel build //java:driver-java
   ```
   The Java library JAR will be produced at: `bazel-bin/java/libdriver-java.jar`

   b) to build the JAR for a Maven application:
   ```
   bazel build //java/:assemble-maven
   ```
   The Maven JAR and POM will be produced at: 
   ```
   bazel-bin/java/com.typedb:api.jar
   bazel-bin/java/pom.xml
   ```

## Example usage

### TypeDB Core

<!-- CORE_EXAMPLE_START_MARKER -->

```java
import com.typedb.driver.TypeDB;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.ConceptRowIterator;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.Promise;
import com.typedb.driver.common.exception.TypeDBDriverException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TypeDBExample {
    public void example() {
        // Open a driver connection. Try-with-resources can be used for automatic driver connection management
        try (Driver driver = TypeDB.coreDriver(TypeDB.DEFAULT_ADDRESS)) {
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
                        "attribute age, value long;";

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

                // Check if it's an entity type before the conversion
                if (conceptByName.isEntityType()) {
                    System.out.printf("Getting concepts by variable names and indexes is equally correct. " +
                                    "Both represent the defined entity type: '%s' (in case of a doubt: '%s')%n",
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
                        System.out.printf("Defined attribute type's label: '%s', value type: '%s'%n", attributeType.getLabel(), attributeType.getValueType());

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
                if (x.isEntity()) {
                    System.out.println("Each entity receives a unique IID. It can be retrieved directly: " + x.asEntity().getIID());
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
```

<!-- CORE_EXAMPLE_END_MARKER -->

## FAQs

**Q:** I see a large number of Netty and gRPC log messages. How can I disable them?

**A:** Create a Logback configuration file and set the minimum log level to ERROR. You can do so with the following steps:
1. Create a file in your `resources` path (`src/main/resources` by default in a Maven project) named `logback.xml`.
2. Copy the following document into `logback.xml`:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="ERROR">
        <appender-ref ref="STDOUT"/>
    </root>

</configuration>
```
