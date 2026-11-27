Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.16.0-rc0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.16.0-rc0/a=noarch;xg=com.typedb/)
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
        <version>3.16.0-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.16.0rc0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.16.0-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.16.0-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.16.0-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.16.0-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.16.0-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.16.0-rc0" />
</ItemGroup>
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.16.0-rc0
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.16.0-rc0


## New Features
- **Introduce zstd compression for database export**
  Database export can now compress the data file with zstd through a new `FileCompression` option (`Uncompressed` by default). Import detects and handles compressed files automatically.
  
  This is a **breaking change** for the C driver and its `database_export_to_file` function, which acquires a new argument.
  
  
- **Support vector value type**
  
  Adds driver support for TypeDB's vector value type and vector similarity search across all driver
  languages (Rust, Java, Python, C#, C, HTTP-TS).
  
  1. Defining a vector attribute:
  ```
  define
    attribute embedding,
    value vector(3, "float32") @index(hnsw:cosine);
  ```
  
  2. Inserting vector values:
  
  ```
  insert $d isa document, has embedding vector([1.0, 0.0, 0.0], "float32");
  ```
  
  3. Similarity search, returning each matched attribute with its similarity score, ordered
  most-similar first:
  
  ```
  match
    let $e, $cosine in cosine_similarity_search(embedding, vector([1.0, 0.0, 0.0], "float32"), 0.9);
  ```
  
  4. Reading vector values back through each driver's value API.
  
  
## Bugs Fixed
- **Provide server migration errors for failed imports**
  
  Fix a bug where a database import rejected by the server could sometimes fail with a misleading connection error instead of the server's actual error:
  ```
  [CXN02] Connection Error: Unable to connect to TypeDB server(s).
  ... [CXN29] Connection Error: The database import channel is closed and no further operation is allowed.
  ```
  
  This happened when the client was sending data faster than the server consumed it, typically on larger imports.
  
  
## Code Refactors
- **Simplify workspace referencing and fix local_path_override**
  
  Each TypeDB dependency's commit or tag is now written once, in its `git_override` in `MODULE.bazel`. 
  
  Before, refs were duplicated in `workspace_refs`' commit and tag dicts, with commented-out alternatives for switching between the two.
  
  Additionally, a local checkout of `typedb-protocol` can now be used through a `local_path_override` in `MODULE.bazel` instead of failing on `sync.sh` in case no `git_override` is left commented out.
  
  
## Other Improvements
- **Update bdd ref**
  
  
  
    
