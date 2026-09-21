Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.13.5
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.13.5/a=noarch;xg=com.typedb/)
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
        <version>3.13.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.13.5
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.13.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.13.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.13.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.13.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.13.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.13.5" />
</ItemGroup>
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.13.5
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.13.5


## New Features
- **Add delete concepts analyze**
  
  This PR adds the new `DeleteConcepts` support across every driver: Rust, Python, Java, C#, and the standalone HTTP TypeScript driver. This matches the updated protocol which gained a `DeleteConcepts` constraint in the query-analysis structure ([typedb-protocol #258](https://github.com/typedb/typedb-protocol/commit/310fef4e4ed2e735ed0fb2dc3248b4a35e604806)), though TypeDB server already emits it correctly.
  
  

## Bugs Fixed
- **Fix document Kind decoding to match protocol wire values**
  
  Fix mapping of `fetch` leaf kinds, which incorrectly mapped Attributes to Roles
  
  
- **Limit database import and export memory footprint**
  
  Database import and export can no longer use gigabytes of RAM on large databases. A 25 GB / 5.6M-instance import that used to drive the client to 22.7 GB (and get OOM-killed) now holds flat at ~44 MB, with identical data and no speed change. 
  
  Before, database import used unbounded channels, which, with slower server-side processing (which does much more work compared to direct file reads), placed almost the whole file into RAM, awaiting its data reads. 
  
  The export did not suffer from this issue, because its unbounded channel depends on the inputs from the slower server. However, in case of a unique mismatch between the client/server hardware, its network channel became bounded, too, which did not affect the performance of exports in regular situations.
  
  Important: the size of the RAM depends on the size of entries we don't currently check, but the existing caps for the buffers are safe enough to consider potential memory blowouts not at all critical.
  
  

## Code Refactors


## Other Improvements
- **Revert to compatible homebrew version for CI**

- **Update bazel mod dependency tag vs commit**

- **Update VERSION to 3.13.4**

- **Bump typedb snapshots**
  
  Update core and cluster snapshots to get CI green!
  
- **Disable implicit init py file creation for bazel python targets**
  Disable implicit init py file creation for bazel python targets, as advised by: bazel-contrib/rules_python#2945 
  
  
- **Extend migration tests**
  Extend the migration tests for the Rust driver to include more corner cases of incomplete database imports.
  
  
- **Update server 3.12.3**
  Update references to a pre-3.12.3 server to test the combination of the server and the driver against the updated set of migration BDD scenarios.
  
  
    
