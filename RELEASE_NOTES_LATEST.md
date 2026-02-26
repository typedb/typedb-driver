Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.8.1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.8.1/a=noarch;xg=com.typedb/)
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
        <groupid>com.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>3.8.1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.8.1
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.8.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.8.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.8.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.8.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.8.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.8.1" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install @typedb/driver-http@3.8.1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.8.1


## New Features
- **Csharp driver 3.0**
  
  This PR ports the C# driver to the TypeDB 3.0 API, aligning it with the Java and Rust drivers. The driver now uses the simplified 3.0 architecture where sessions are removed and transactions are opened directly from the driver. Query execution returns `IQueryAnswer` with streaming `IConceptRow` results instead of the previous `IConceptMap` model.
  
  Key API changes include:
  - `ITypeDBDriver` renamed to `IDriver` with direct transaction access via `Transaction(database, type, options)`
  - New `IQueryAnswer` and `IConceptRow` types replace `IConceptMap` for query results
  - Promise-based query submission
  - New `IJSON` type for fetch query results with document iteration
  - Query analysis via `ITypeDBTransaction.Analyze()` returning `IAnalyzedQuery` with full pipeline introspection
  - `DatetimeTZ` and `Duration` types for temporal values with nanosecond precision
  - Simplified concept API removing manager classes—operations are now methods directly on concepts
  - `QueryOptions` and `TransactionOptions` replace the monolithic `TypeDBOptions`
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements
- **Fix build**

- **Fix tests and add missing CSharp docs**
  
  We fix one broken Rust test and add missing CSharp docs
  
- **Handle output directory differently in python sphinx_docs rule**
  Update dependencies after merging https://github.com/typedb/typedb-dependencies/pull/599 to fix the Python Driver build.
  
  
    
