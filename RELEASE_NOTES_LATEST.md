Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.12.0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.12.0/a=noarch;xg=com.typedb/)
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
        <version>3.12.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.12.0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.12.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.12.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.12.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.12.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.12.0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.12.0" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.12.0
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.12.0


## New Features
- **Implement "given rows" as inputs to queries**
  Adds the machinery required for passing rows of concepts to TypeQL queries with 'given' stages, and updates analyzed query datastructures for the given stage.
  
  #### Examples:
  (Note: `eugene` and `fred` are concepts returned from previous queries)
  **rust**
  ```rust
  let mut given_rows = GivenRows::new(vec!["x".to_string(), "v".to_string()], 2);
  given_rows.push_row(vec![GivenRowEntry::Entity(eugene), GivenRowEntry::Value(Value::Integer(12))]).unwrap();
  given_rows.push_row(vec![fred.into(), 34.into()]).unwrap(); // From<T> is implemented for many native values, and concepts.
  let query = "given $x: person, $v: integer; insert $x has age == $v;";
  let inserted_answer = transaction.query_with_rows(query, given_rows).await.unwrap();
  ```
  
  **Java**
  ```java
  String query = "given $x: person, $v: integer; insert $x has age == $v;";
  List<Map<String, Object>> givenRows = List.of(
      java.util.Map.ofEntries(
          Map.entry("x", personEugene),
          Map.entry("v", TypeDB.Concept.newInteger(12))
      ),
      java.util.Map.ofEntries(
          Map.entry("x", personFred),
          Map.entry("v", TypeDB.Concept.newInteger(34))
      )
  );
  QueryAnswer inserted = transaction.query(query, givenRows).resolve();
  ```
  
  **python**
  ```
  query = "given $x: person, $v: integer; insert $x has age == $v;"
  given_rows = [
      {"x": person_eugene, "v": TypeDB.Concept.new_integer(12)},
      {"x": person_fred, "v": TypeDB.Concept.new_integer(34)},
  ]
  inserted = tx.query(query, given_rows=given_rows).resolve()
  ```
  
  **C#**
  ```c#
   var query = "given $x: person, $v: integer; insert $x has age == $v;";
  var givenRows = new List<Dictionary<string, object?>>
  {
      new Dictionary<string, object?> { { "x", personEugene }, { "v", TypeDB.Concept.NewInteger(12) } },
      new Dictionary<string, object?> { { "x", personFred }, { "v", TypeDB.Concept.NewInteger(34) } }
  };
    var inserted = transaction.Query(query, new QueryOptions(), givenRows).Resolve()!;
  ```
  
  The HTTP QueryRequest has expects the (backward compatible) format:
  ```typescript
  export type QueryRequest = { 
      query: string,
      queryOptions: QueryOptions | undefined, 
      givenRows:  GivenRows | undefined,
  }
  
  export type GivenRows = { [varName: string]: GivenRowEntry }[];
  
  export type GivenRowEntry = Value | Entity | Relation | Attribute
      | boolean // for boolean values
      | number  // For integer and double values
      | string; // For all other types, as well as IIDs.
  ```
  
  

## Bugs Fixed
- **Fix transaction on_close callback panic**
  
  A race between on_close and transaction teardown could cause the driver to panic and crash the user's application. We fix this by assuming an error from the register/acknowledgement channel means 'transaction already closed' instead of panicking. 
  
  
- **Fix string marshalling over JNI**
  
  We forcibly re-encode Java strings from MUTF-8 (modified UTF-8 that uses surrogate characters for code points starting at U+10000) to UTF-8 before passing them over JNI.
  
  
- **Fix address sanitizer errors in C integration tests**
  Use proper transaction cleanup functions in the C integration tests to avoid address sanitizer errors in CI.
  
  
- **Enhance redirection logic in grpc drivers**
  
  Fixes a failover hole: when a clustered driver's cached primary is alive but no longer primary, the server's `CSV9` redirect ("not primary, here's who is") was bubbled up to the caller as a generic `Error::Server` with no retry.
  
  The driver now uses the correct error codes and routes the retry directly to the hinted node when it's presented, falling back to the existing seek-primary path otherwise.
  
  

## Code Refactors
- **Use Bazel `exclusive` tag for tests instead of CLI --jobs=1**
  Replace the CI Bazel `--jobs=1` flag to ensure jobs aren't running in parallel and clashing for resources with the `exclusive` bazel tag.
  
  Note that the `exclusive` flag allows parallel builds but blocks parallel test phase execution. In addition, if we ever add it, it prevents remote execution.
  
  

## Other Improvements
- **Update language references in README**
  Refresh the table of supported languages with the latest information on 3.x support and documentation links.
  
- **Add deployment steps for Python 3.14 driver**
  Add deployment steps for Python 3.14 driver
  
  
- **Add driver for python 3.14**
  The python driver now deploys a package supporting python 3.14
  
  
- **Inline maven dependencies**
  Inline maven dependencies, since `include` can't be used outside the root module.
  
  
- **Specify tar.gz extension for clib packages so they're compressed again**
  Adds `extension = "tar.gz"` to the linux clib packages so they get compressed.
  
  
- **Add limits to bazel cache size and age**

- **Fix CI pipelines after rules_python upgrade**
  Fixes our CircleCI deployment pipelines after the rules_python upgrade
  
  
- **Speed up CI with various fixes**
  Speeds up CI with various fixes, including a smaller fetch by moving away from a monolithic maven dependencies bazel repo, and using precompiled protoc.
  
  
    
