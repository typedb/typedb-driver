Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Breaking changes
This release breaks backwards compatibility.
This version is only compatible with TypeDB server versions >= 3.11.0.
Connections to older servers will be rejected.

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.11.2
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.11.2/a=noarch;xg=com.typedb/)
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
        <version>3.11.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.11.2
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.11.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.11.2" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.11.2
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.11.2


## New Features


## Bugs Fixed
- **Use connection address for standalone connections**
  
  Make the driver use the connection address in case the server is not a part of a cluster and does not advertise an address for connection. 
  
  This feature is required to allow easy connections to servers behind a proxy or inside a container -- otherwise, they'd have to provide advertise addresses, which makes the initial TypeDB set up more complicated (requires https://github.com/typedb/typedb/pull/7817).
  
  For clustered connections, all clustered servers are still expected to provide the address through the protocol. 
  
  

## Code Refactors


## Other Improvements
- **Prepare release 3.11.2**
  Bump version & prepare release notes
  
  
  
    
