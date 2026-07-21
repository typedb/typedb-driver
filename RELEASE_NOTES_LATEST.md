Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.12.1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.12.1/a=noarch;xg=com.typedb/)
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
        <version>3.12.1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.12.1
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.12.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.12.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.12.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.12.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.12.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.12.1" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.12.1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.12.1


## New Features


## Bugs Fixed
- **Fix C integration tests reporting success on failed assertions**
  Fix incorrect successful test reporting in C integration tests.
  
  
- **Use saturating subtraction for network latency estimation**
  
  The driver estimates network latency by subtracting the server-reported processing duration from the client-observed elapsed time. The two durations come from different clocks that can tick at slightly different rates, so the server's value can exceed the client's. 
  
  This can cause panics and in other cases could cause the transaction-open latency to near u64::MAX, poisoning the latency tracker and inflating the network_latency_millis the server uses as its answer-streaming budget.
  
  

## Code Refactors


## Other Improvements
- **Build all python deployment jobs in one build step before we run them one by one**
  We can't `bazel run` multiple targets in parallel, but we can `build` them. Here we build in parallel and run just after that, one by one.
  
  
- **Custom C# transition to prevent rebuilding C driver**
  Introduces a custom bazel transition to revert dotnet configuration to defaults, allowing the C# driver to reuse the C driver built by the other jobs in the CI.
  
  
    
