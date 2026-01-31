# TypeDB C# Driver
The C# driver is based on the cross-platform .NET 6 framework.

## Use TypeDB Driver for C#
The driver is distributed as a series of [Nuget](https://www.nuget.org) packages. To use the driver, import the latest versions of [the driver](https://www.nuget.org/packages/TypeDB.Driver) and its [Pinvoke runtime](https://www.nuget.org/packages?q=TypeDB.Driver.Pinvoke) suitable for your platform. Here is an example from a `.csproj` for MacOS x86-64:
```xml
<PackageReference Include="TypeDB.Driver" Version={VERSION} />
<PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version={VERSION} />
```

If you aim to build a platform-independent package, reference all the needed runtimes (it will affect the size of your application by downloading a respective set of platform-specific dynamic libraries):
```xml
<PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version={VERSION} />
<PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version={VERSION} />
<PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version={VERSION} />
<PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version={VERSION} />
<PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version={VERSION} />
```

See [Examples](#examples) and [API Reference](#api-reference) to set up your first application with the TypeDB driver.

## API Reference
To learn about the methods available for executing queries and retrieving their answers using C#, refer to the [API Reference](https://typedb.com/docs/drivers/csharp/api-reference).

## Driver Architecture
The C# driver is a thin wrapper around the TypeDB Rust driver, introducing classes for a more intuitive interface. Each C# object holds a reference to a corresponding native Rust object, using [SWIG for C#](https://www.swig.org/Doc4.2/SWIGDocumentation.html#CSharp) to generate P/Invoke wrappers and manage native resources.

Any error encountered will throw a `TypeDBDriverException`. Note that methods which return an `IEnumerable` or a `Promise` and encounter a server-side error will only throw when the return objects are evaluated (e.g. iterate over or call a `Linq` method for an `IEnumerable` and call `Resolve()` for a `Promise`).

### Pinvoke Layer

The SWIG-generated P/Invoke bindings can be built and inspected with:
```bash
# Build the generated C# source file
bazel build //csharp:__typedb_driver_pinvoke.cs

# The output is at: bazel-bin/csharp/__typedb_driver_pinvoke.cs
```

### Native Memory Management

The SWIG-generated wrapper classes (`Credentials`, `DriverOptions`, `Transaction`, `QueryOptions`, etc.) each follow a standard C# disposable + finalizer pattern to manage native (Rust) memory:

- **`swigCPtr`** — `HandleRef` holding the pointer to the native Rust object.
- **`swigCMemOwn`** — `bool` flag indicating whether this wrapper owns (and is responsible for freeing) the native memory.
- **`~ClassName()` (finalizer)** — Called by the GC when the wrapper is collected. Invokes `Dispose(false)`, which calls the native `delete_ClassName()` P/Invoke to free the Rust-side memory. This means native memory is automatically cleaned up even if `Dispose()` is never called explicitly.
- **`Dispose()`** — Can be called manually for deterministic cleanup. Calls `Dispose(true)` and suppresses the finalizer via `GC.SuppressFinalize`.
- **`Released()`** — Transfers ownership of the native pointer out of the wrapper. Clears `swigCPtr` and `swigCMemOwn` so the finalizer becomes a no-op. Used when passing ownership to Rust (e.g. `transaction_commit` consumes the transaction).
- **`IsOwned()`** — Returns whether this wrapper still owns the native memory.

**Key design decisions:**

1. **No `GC.KeepAlive()` needed for FFI calls.** The Rust FFI layer (`c/src/`) clones or copies all parameters at the boundary — `Credentials` and `DriverOptions` are `.clone()`'d, while `TransactionOptions` and `QueryOptions` derive `Copy` and are bitwise-copied. This means the original SWIG wrapper can be safely collected after the FFI call returns without causing use-after-free.

2. **No manual `IDisposable` on high-level classes.** The higher-level C# classes (`TypeDBDriver`, `TypeDBTransaction`, etc.) do not need to implement `IDisposable` for memory safety. The SWIG finalizers on the underlying `Pinvoke.*` objects handle cleanup automatically. The `Close()` methods on `TypeDBDriver` and `TypeDBTransaction` exist for semantic correctness (graceful shutdown, callback cleanup) rather than memory management.

3. **`NativeObjectWrapper<T>`** is the base class used by high-level C# classes to hold a SWIG-generated native object. It exposes `NativeObject` (the underlying SWIG wrapper) but does not itself implement `IDisposable` — it delegates lifecycle to the SWIG finalizer.

## Build TypeDB Driver for C# from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Use TypeDB Driver for C#"_ section above.

1. Make sure you have [Bazel](https://docs.bazel.build/versions/master/install.html) installed on your machine.
2. Build the shared library:
   ```
   bazel build //csharp:driver-csharp
   ```
   All the needed C# libraries will be produced at: `bazel-bin/csharp/`, with the main one being: `bazel-bin/csharp/driver-csharp/{target_framework}/TypeDB.Driver.dll`. 
3. Examples of building and using Bazel-based C# applications with the produced dependencies can be found in `csharp/Test/Integration/Examples`.

## Example usage

```cs
using System;
using System.Collections.Generic;
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Example
{
    public class Example
    {
        public void Run()
        {
            // Open a driver connection. Using statements can be used for automatic driver connection management
            using (var driver = TypeDB.Driver("127.0.0.1:1729", new Credentials("admin", "password"), new DriverOptions(false, null)))
            {
                // Create a database
                driver.Databases.Create("typedb");
                var database = driver.Databases.Get("typedb");

                // Open transactions of 3 types
                var tx = driver.Transaction(database.Name, TransactionType.Read);

                // Use try blocks to catch driver exceptions
                try
                {
                    // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit exception
                    var answer = tx.Query("define entity i-cannot-be-defined-in-read-transactions;");

                    Console.WriteLine("The query has been sent, iterating or committing will surface errors!");
                    // Iterating the answer would throw an exception
                }
                catch (TypeDBDriverException expectedException)
                {
                    Console.WriteLine("Query execution revealed the exception: " + expectedException);
                }
                finally
                {
                    // Don't forget to close the transaction!
                    tx.Close();
                }

                // Open a schema transaction to make schema changes
                // Transactions can be opened with configurable options. This option limits its lifetime
                var transactionOptions = new TransactionOptions { TransactionTimeoutMillis = 10_000 };

                // Use using blocks to forget about close operations (similarly to connections)
                using (var transaction = driver.Transaction(database.Name, TransactionType.Schema, transactionOptions))
                {
                    var defineQuery = @"define
                        entity person, owns name, owns age;
                        attribute name, value string;
                        attribute age, value integer;";

                    var answer = transaction.Query(defineQuery);

                    // Commit automatically closes the transaction. It can still be safely called inside using blocks
                    transaction.Commit();
                }

                // Open a read transaction to safely read anything without database modifications
                using (var transaction = driver.Transaction(database.Name, TransactionType.Read))
                {
                    var entityAnswer = transaction.Query("match entity $x;");

                    // Collect concept rows that represent the answer as a table
                    var entityRows = entityAnswer.AsConceptRows().ToList();
                    var entityRow = entityRows[0];

                    // Collect column names to get concepts by index if the variable names are lost
                    var entityHeader = entityRow.ColumnNames.ToList();
                    var columnName = entityHeader[0];

                    // Get concept by the variable name (column name)
                    var conceptByName = entityRow.Get(columnName);

                    // Get concept by the header's index
                    var conceptByIndex = entityRow.GetIndex(0);

                    Console.WriteLine($"Getting concepts by variable names ({conceptByName?.TryGetLabel()}) and indexes ({conceptByIndex?.TryGetLabel()}) is equally correct. ");

                    // Check if it's an entity type before the conversion
                    if (conceptByName != null && conceptByName.IsEntityType())
                    {
                        Console.WriteLine($"Both represent the defined entity type: '{conceptByName.AsEntityType().GetLabel()}' (in case of a doubt: '{conceptByIndex?.AsEntityType().GetLabel()}')");
                    }

                    // Continue querying in the same transaction if needed
                    var attributeAnswer = transaction.Query("match attribute $a;");

                    // IConceptRowIterator can be used as any other IEnumerable
                    foreach (var attributeRow in attributeAnswer.AsConceptRows())
                    {
                        // Column names are an IEnumerable, so they can be used in a similar way
                        var columnNames = attributeRow.ColumnNames.ToList();
                        columnName = columnNames[0];

                        conceptByName = attributeRow.Get(columnName);

                        // Check if it's an attribute type before the conversion
                        if (conceptByName != null && conceptByName.IsAttributeType())
                        {
                            var attributeType = conceptByName.AsAttributeType();
                            Console.WriteLine($"Defined attribute type's label: '{attributeType.GetLabel()}', value type: '{attributeType.TryGetValueType()}'");

                            Console.WriteLine($"It is also possible to just print the concept itself: '{conceptByName}'");
                        }
                    }
                }

                // Open a write transaction to insert data
                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    var insertQuery = "insert $z isa person, has age 10; $x isa person, has age 20, has name \"John\";";
                    var answer = transaction.Query(insertQuery);

                    // Insert queries also return concept rows
                    var rows = answer.AsConceptRows().ToList();
                    var row = rows[0];
                    foreach (var colName in row.ColumnNames)
                    {
                        var insertedConcept = row.Get(colName);
                        Console.WriteLine($"Successfully inserted ${colName}: {insertedConcept}");
                        if (insertedConcept != null && insertedConcept.IsEntity())
                        {
                            Console.WriteLine("This time, it's an entity, not a type!");
                        }
                    }

                    // It is possible to ask for the column names again
                    var header = row.ColumnNames.ToList();

                    var x = row.GetIndex(header.IndexOf("x"));
                    Console.WriteLine($"As we expect an entity instance, we can try to get its IID (unique identification): {x?.TryGetIID()}. ");
                    if (x != null && x.IsEntity())
                    {
                        Console.WriteLine("It can also be retrieved directly and safely after a cast: " + x.AsEntity().IID);
                    }

                    // Do not forget to commit if the changes should be persisted
                    Console.WriteLine("CAUTION: Committing or closing (including leaving the using block) a transaction will invalidate all its uncollected answer iterators");
                    transaction.Commit();
                }

                // Open another write transaction to try inserting even more data
                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    // When loading a large dataset, it's often better to batch queries.
                    // Just call commit, which will wait for all ongoing operations to finish before executing.
                    var queries = new[] { "insert $a isa person, has name \"Alice\";", "insert $b isa person, has name \"Bob\";" };
                    foreach (var query in queries)
                    {
                        transaction.Query(query);
                    }
                    transaction.Commit();
                }

                using (var transaction = driver.Transaction(database.Name, TransactionType.Write))
                {
                    // In C#, query errors surface during query execution
                    try
                    {
                        var invalidQuery = "insert $c isa not-person, has name \"Chris\";";
                        transaction.Query(invalidQuery);
                    }
                    catch (TypeDBDriverException expectedException)
                    {
                        Console.WriteLine("Query execution revealed the error: " + expectedException);
                    }
                    // Transaction is still usable after catching the exception
                }

                // Open a read transaction to verify that the inserted data is saved
                using (var transaction = driver.Transaction(database.Name, TransactionType.Read))
                {
                    // Queries can also be executed with configurable options. This option forces the database
                    // to include types of instance concepts in ConceptRows answers
                    var queryOptions = new QueryOptions { IncludeInstanceTypes = true };
                    // A match query can be used for concept row outputs
                    var varName = "x";
                    var matchAnswer = transaction.Query($"match ${varName} isa person;", queryOptions);

                    // Simple match queries always return concept rows
                    var matchCount = 0;
                    foreach (var row in matchAnswer.AsConceptRows())
                    {
                        var x = row.Get(varName);
                        if (x != null && x.IsEntity())
                        {
                            var xType = x.AsEntity().Type.AsEntityType();
                            Console.WriteLine($"Found a person {x} of type {xType}");
                        }
                        matchCount++;
                    }
                    Console.WriteLine("Total persons found: " + matchCount);

                    // A fetch query can be used for concept document outputs with flexible structure
                    var fetchAnswer = transaction.Query(@"match
                        $x isa! person, has $a;
                        $a isa! $t;
                        fetch {
                            ""single attribute type"": $t,
                            ""single attribute"": $a,
                            ""all attributes"": { $x.* },
                        };");

                    // Fetch queries always return concept documents
                    var fetchCount = 0;
                    foreach (var document in fetchAnswer.AsConceptDocuments())
                    {
                        Console.WriteLine("Fetched a document: " + document);
                        Console.Write("This document contains an attribute of type: ");
                        Console.WriteLine(document.AsObject()["single attribute type"].AsObject()["label"]);

                        fetchCount++;
                    }
                    Console.WriteLine("Total documents fetched: " + fetchCount);
                }
            }

            Console.WriteLine("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
        }
    }
}
```
