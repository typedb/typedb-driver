Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.12.3
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.12.3/a=noarch;xg=com.typedb/)
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
        <version>3.12.3</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.12.3
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.12.3" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.12.3" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.12.3" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.12.3" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.12.3" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.12.3" />
</ItemGroup>
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.12.3
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.12.3


## New Features


## Bugs Fixed
- **Make the HTTP-TS driver optimally follow redirects to scheme-less primary addresses**
  
  Make the failover logic of the http-ts driver correctly follow the advertised primary address (both bare host:port and full-origin values) in priority over the list of replicas configured on connection. Previously, a bare advertised address was ignored: the driver brute-force scanned the configured replicas (retrying the one that had just redirected it), and could not reach a primary outside the configured list at all.
  
  

## Code Refactors


## Other Improvements

    
