Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.8.4-rc0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.8.4-rc0/a=noarch;xg=com.typedb/)
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
        <version>3.8.4-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.8.4rc0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.8.4-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.8.4-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.8.4-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.8.4-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.8.4-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.8.4-rc0" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.8.4-rc0
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.8.4-rc0


## New Features


## Bugs Fixed


## Code Refactors


## Other Improvements
- **Update Rust toolchain**
  
  We update the Rust toolchain version to 1.93 and edition to 2024. We also update the source to make it compile with this new toolchain, and resolve the warnings.
  
  
- **Raise gRPC message size limits to 1 GB**
  
  The default tonic limit of 4 MB is too restrictive for database workloads. Large fetch documents, query result batches, and export streams can legitimately exceed 4 MB, causing decode failures on the client side with: "decoded message length too large: found N bytes, the limit is: 4194304 bytes"
  
  Set both encoding and decoding limits to 1 GB on the TypeDbClient stub, matching the corresponding server-side change in https://github.com/typedb/typedb/pull/7745
  
  
- **Add security framework for c integration test**
  
  Add `-framework Security` linkopt for macOS to the exposed C lib target, fixing linker errors with undefined Security framework symbols when building with Bazel 8.   
  
  
- **Fetch TypeDB server using native_artifact_files module extension**
  Fetches the TypeDB server artifact using `native_artifacts_files`, close to how we used to with bazel 6.
  
    
