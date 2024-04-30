Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.28.0
```


### Java driver

Available through https://repo.typedb.com
Documentation: https://typedb.com/docs/drivers/java/overview

```xml
<repositories>
    <repository>
        <id>repo.typedb.com</id>
        <url>https://repo.typedb.com/public/public-release/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>2.28.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.28.0
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.28.0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.28.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.28.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.28.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.28.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.28.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.28.0" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.28.0
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.28.0


## New Features
- **Implement a method to convert a JSON object to a JSON string**
  
  The `toString()` method is callable for JSON class objects. It returns a string.
  
  
- **Address translation**
  
  We allow the user to provide a translation map from the advertised server addresses (now treated as generic server names) to the actual addresses the driver shall use to connect to the cloud instances. We require the user to provide the full mapping.
  
  Example usage:
  
  Python:
  ```python
  with TypeDB.cloud_driver({
              "0.deployment-UUID.cloud.typedb.com:1729": "localhost:11729",
              "1.deployment-UUID.cloud.typedb.com:1729": "localhost:21729",
              "2.deployment-UUID.cloud.typedb.com:1729": "localhost:31729"
          }, credential) as driver:
      pass
  ```
  
  Rust:
  ```rust
  Connection::new_cloud_with_translation(                                                        
      [                                                                                          
          ("0.deployment-UUID.cloud.typedb.com:1729", "localhost:11729"),
          ("1.deployment-UUID.cloud.typedb.com:1729", "localhost:21729"),
          ("2.deployment-UUID.cloud.typedb.com:1729", "localhost:31729"),
      ].into(),                                                                                  
      credential                                                                                   
  )                                                                                              
  ```
  
- **Introduce packaging, distribution and documentation for the C# driver**
  
  We introduce packaging, distribution and documentation C# driver for TypeDB ([the original driver PR](https://github.com/vaticle/typedb-driver/pull/609)). It is built using the cross-platform [.NET 6 framework](https://dotnet.microsoft.com/en-us/download/dotnet/6.0).
  
  ### Usage: 
  The driver is distributed as a series of [Nuget](https://www.nuget.org) packages. To use the driver, import the latest versions of the driver (TypeDB.Driver) and its Pinvoke runtime (TypeDB.Driver.Pinvoke) suitable for your platform. 
  
  **CS project**: 
  Here is an example from a `.csproj` for MacOS x86-64:
  ```xml
  <PackageReference Include="TypeDB.Driver" Version={VERSION} />
  <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version={VERSION} />
  ```
  
  If you aim to build a platform-independent package, reference all the needed runtimes (it will affect the size of your application by downloading a respective set of platform-specific dynamic libraries):
  ```xml
  <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version={VERSION} />
  <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version={VERSION} />
  <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version={VERSION} />
  <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version={VERSION} />
  ...
  ```
  
  **Bazel**: 
  1. Import both the `TypeDB.Driver` and `TypeDB.Driver.Pinvoke` nuget packages as dependencies to your build rule.
  2. For development, you can also use a [csharp bazel rule](https://github.com/bazelbuild/rules_dotnet/tree/master), passing targets `//csharp:driver-csharp` (the driver itself), `//csharp/Api:api` (exposed Api namespace), `//csharp/Common:common` (exposed Common namespace) as dependencies. 
  
  **A simple usage example** (see `csharp/Test/Integration/Examples` for more):
  ```
  using TypeDB.Driver.Api;
  using TypeDB.Driver.Common;
  
  public class TypeDBExample
  {
          public void SetupTypeDB()
          {
              string dbName = "access-management-db";
              string serverAddr = "127.0.0.1:1729";
  
              try
              {
                  using (ITypeDBDriver driver = Drivers.CoreDriver(serverAddr))
                  {
                      driver.Databases.Create(dbName);
                      IDatabase database = driver.Databases.Get(dbName);
  
                      using (ITypeDBSession session = driver.Session(dbName, SessionType.Schema))
                      {
                          using (ITypeDBTransaction transaction = session.Transaction(TransactionType.Write))
                          {
                              transaction.Query.Define("define person sub entity;").Resolve();
  
                              string longQuery = "define name sub attribute, value string; person owns name;";
                              transaction.Query.Define(longQuery).Resolve();
  
                              transaction.Commit();
                          }
                      }
  
                      database.Delete();
                  }
              }
              catch (TypeDBDriverException e)
              {
                  Console.WriteLine($"Caught TypeDB Driver Exception: {e}");
              }
          }
  }
  ```
  
  
- **Introduce C# driver without documentation and deployment**
  
  We introduce the C# driver for TypeDB. It is built using the cross-platform [.NET 6 framework](https://dotnet.microsoft.com/en-us/download/dotnet/6.0).
  
  **Usage**: Deployment and usage examples will be provided in a separate pull request. Current state of the code lets you compiling the driver + writing and running behaviour and integration tests for it. 
  The driver is expected to be a Nuget package, which could be added as a dependency to a project and referenced via "using" statements inside the users' code for all platforms.
  
  **Architecture**: The C# driver is a thin wrapper around the TypeDB Rust driver, introducing classes for a more intuitive interface. Mostly each C# object holds a reference to the corresponding native Rust object, using an FFI ([SWIG for C#](https://www.swig.org/Doc4.2/SWIGDocumentation.html#CSharp)) for the native object wrappers generation and resource management.
  
  Any error encountered will throw a `TypeDBDriverException`. Note that methods which return an `IEnumerable` or a Promise and encounter a server-side error will only throw when the return objects are evaluated (e.g. iterate over or call a `Linq` method for an `IEnumerable` and call `Resolve()` for a `Promise`).
  
  A simple usage example:
  ```
  // Inside a try-catch block
  using (ITypeDBDriver driver = TypeDB.CoreDriver(TypeDB.DEFAULT_ADDRESS))
  {
      string dbName = "mydb";
      driver.Databases.Create(dbName);
      IDatabase mydb = driver.Databases.Get(dbName);
      System.Console.WriteLine(mydb.Name);
  
      using (ITypeDBSession schemaSession = driver.Session(dbName, SessionType.SCHEMA))
      {
          using (ITypeDBTransaction writeTransaction = schemaSession.Transaction(TransactionType.WRITE))
          {
              string defineQuery = "...some define query...";
              writeTransaction.Query.Define(defineQuery).Resolve();
              writeTransaction.Commit();
          }
      }
  
      mydb.Delete();
  }
  ```
  
  

## Bugs Fixed

## Code Refactors
- **Implement new steps for getting answers from templated get**
  
  We implement a new steps in every driver for a new step: 'get answers of templated typeql get', which is a modification of the existing 'templated typeql get; throws exception'
  
  
- **Remove copyright year from apache license headers**
  
  To simplify maintanance, we remove the copyright year from the Apache license headers. These aren't (to our best knowledge) actually legally required, since copyright is granted automatically from the moment a work is created (US law).
  
  

## Other Improvements
- **Fixes to C++ structure based on feedback from website**
  Various improvements to the document generation tools to make the C++ and C driver docs on the website more readable
  
  
- **Update the README.md for the C driver**
  
  Update the readme file for the latest filenames.
  
  
- **Fix C# README examples and add issues refs to recent TODOs**
  C# driver's examples should be able to be built in any namespace now.
  
  Some of the recent `TODO`s left in the codebase have been cleaned or marked with a specific GitHub issue number.  
  
  
- **Python: ABC and typing are not interchangeable**

- **Increase wait time in start-core-server to 10 s, add error message**

- **Increase bootup timeout to 5 seconds in CI tests**

- **Deploy C# driver in a separate job**
  
  Currently, Bazel generates three different build configurations for the underlying FFI library during C# driver deployment. As that requires compilation of the binding generator and lengthy project analysis, that step is prone to random failures (`socket closed`).
  
  As the full analysis of the issue proved time-consuming, we instead opt to make the C# driver deployment step easily retriable.
  
- **Fix parsing & formatting issues for C, C++ documentation**
  Fixes the automatic documentation generation tools for the C & C++ drivers (1) to be  consistent with other languages in terms of structure, and (2) removes stray duplication of parameter documentation in method descriptions.
  
  
- **Add README for the C# driver with examples**
  This PR adds `README` with usage examples for the incoming release of the C# driver.
  
  
- **Update C docs reference in README.md**

- **Added C# to README.md**

- **Add setSupertype docs to nodejs thing types**
  
  We add missing documentation for the `setSupertype` concept API in the NodeJS Driver.
  
