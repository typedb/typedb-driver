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
The C# driver is a thin wrapper around the TypeDB Rust driver, introducing classes for a more intuitive interface. Mostly each C# object holds a reference to the corresponding native Rust object, using an FFI ([SWIG for C#](https://www.swig.org/Doc4.2/SWIGDocumentation.html#CSharp)) for the native object wrappers generation and resource management.

Any error encountered will throw a `TypeDBDriverException`. Note that methods which return an `IEnumerable` or a `Promise` and encounter a server-side error will only throw when the return objects are evaluated (e.g. iterate over or call a `Linq` method for an `IEnumerable` and call `Resolve()` for a `Promise`).

## Build TypeDB Driver for C# from Source

> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Use TypeDB Driver for C#"_ section above.

1. Make sure you have [Bazel](https://docs.bazel.build/versions/master/install.html) installed on your machine.
2. Build the shared library:
   ```
   bazel build //csharp:driver-csharp
   ```
   All the needed C# libraries will be produced at: `bazel-bin/csharp/`, with the main one being: `bazel-bin/csharp/driver-csharp/{target_framework}/TypeDB.Driver.dll`. 
3. Examples of building and using Bazel-based C# applications with the produced dependencies can be found in `csharp/Test/Integration/Examples`.

## Examples
### TypeDB Core
Connect to TypeDB using `Drivers.CoreDriver` and perform basic read/write operations:
```cs
using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

class WelcomeToTypeDB
{
    static void Main(string[] args)
    {
        string dbName = "access-management-db";
        string serverAddr = "127.0.0.1:1729";

        try
        {
            using (ITypeDBDriver driver = Drivers.CoreDriver(serverAddr))
            {
                driver.Databases.Create(dbName);
                IDatabase database = driver.Databases.Get(dbName);

                // Example of one transaction for one session
                using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema))
                {
                    // Example of multiple queries for one transaction
                    using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                    {
                        transaction.Query.Define("define person sub entity;").Resolve();

                        string longQuery = "define name sub attribute, value string; person owns name;";
                        transaction.Query.Define(longQuery).Resolve();

                        transaction.Commit();
                    }
                }

                // Example of multiple transactions for one session
                using (ITypeDBSession session = driver.Session(dbName, SessionType.Data))
                {
                    // Examples of one query for one transaction
                    using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                    {
                        string query = "insert $p isa person, has name 'Alice';";
                        IEnumerable<IConceptMap> insertResults = transaction.Query.Insert(query);

                        Console.WriteLine($"Inserted with {insertResults.Count()} result(s)");

                        transaction.Commit();
                    }

                    using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                    {
                        IEnumerable<IConceptMap> insertResults =
                            transaction.Query.Insert("insert $p isa person, has name 'Bob';");

                        foreach (IConceptMap insertResult in insertResults)
                        {
                            Console.WriteLine($"Inserted: {insertResult}");
                        }

                        // transaction.Commit(); // Not committed
                    }

                    using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Read))
                    {
                        IConceptMap[] matchResults =
                            transaction.Query.Get("match $p isa person, has name $n; get $n;").ToArray();

                        // Matches only Alice as Bob has not been committed
                        var resultName = matchResults[0].Get("n");
                        Console.WriteLine($"Found the first name: {resultName.AsAttribute().Value.AsString()}");

                        if (matchResults.Length > 1) // Will work only if the previous transaction is committed
                        {
                            Console.WriteLine($"Found the second name as concept: {matchResults[1]}");
                        }
                    }
                }

                database.Delete();
            }
        }
        catch (TypeDBDriverException e)
        {
            Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
            // ...
        }
    }
}
```

### TypeDB Cloud
Connect to TypeDB cloud instances using `Drivers.CloudDriver`:
```cs
using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

class WelcomeToTypeDB
{
    static void Main(string[] args)
    {
        string dbName = "access-management-db";

        // You can also specify all node addresses like: {"localhost:11729", "localhost:21729", "localhost:31729"}
        string[] serverAddrs = new string[]{"localhost:11729"};

        try
        {
            TypeDBCredential connectCredential = new TypeDBCredential(
                "admin",
                "password",
                Environment.GetEnvironmentVariable("ROOT_CA")!);

            using (ITypeDBDriver driver = Drivers.CloudDriver(serverAddrs, connectCredential))
            {
                driver.Databases.Create(dbName);
                IDatabase database = driver.Databases.Get(dbName);

                TypeDBOptions options = new TypeDBOptions();

                // Example of one transaction for one session with options (options are optional)
                using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema, options))
                {
                    // Example of multiple queries for one transaction with options (options are optional)
                    using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write, options))
                    {
                        transaction.Query.Define("define person sub entity;").Resolve();

                        string longQuery = "define name sub attribute, value string; person owns name;";
                        transaction.Query.Define(longQuery).Resolve();

                        transaction.Commit();
                    }
                }
             }
        }
        catch (TypeDBDriverException e)
        {
            Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
            // ...
        }
    }
}
```
