Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.15.0-rc1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.15.0-rc1/a=noarch;xg=com.typedb/)
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
        <version>3.15.0-rc1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.15.0rc0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.15.0-rc1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.15.0-rc1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.15.0-rc1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.15.0-rc1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.15.0-rc1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.15.0-rc1" />
</ItemGroup>
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.15.0-rc1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.15.0-rc1


## New Features
- **Introduce IsSet from the analyze response to driver**
  Introduce IsSet constraint in the analyze response to the various driver datastructure
  
  
- **Add TLS config to http-ts for CA certificates in Node.js**
  The HTTP driver can trust a private CA per driver instance in Node.js, like the root CA option of the other TypeDB drivers. Previously this was only possible process-wide, with `NODE_EXTRA_CA_CERTS`.
  
  ```ts
  new TypeDBHttpDriver({ username, password, addresses, tls: { rootCAPath: "ca.pem" } });
  ```
  
  When set, only this CA is trusted, and every address must use `https`. Unlike in the gRPC drivers, TLS follows the address scheme, and the file is read on the first request rather than when the driver is created: a file that cannot be read or holds no valid PEM certificate, or an `http` address, fails with `HDR3`. In browsers, the operating system's or browser's trust store is still used, and requests with `rootCAPath` set also fail with `HDR3`.
  
  When no server can be reached, `HDR1` now includes the last connection error, for example a certificate verification failure, instead of only "Cannot connect to any server".
  
  
- **Add import from zstd-compressed archives**
  Introduce support for importing `zstd`-compressed archives of `.typedb` data. 
  
  `zstd` allows storing exported TypeDB data in a compressed format, while supporting streaming from these files, just like for raw byte data.
  
  Currently, we support `zstd`-compressed archives prepared by separate tools, but driver's `export` commands could support creating these archives themselves in the future. 
  
  Additionally, update the protocol ref to support https://github.com/typedb/typedb-protocol/pull/259.
  
  

## Bugs Fixed
- **Fix retried exported files corruption**
  Fix `export_to_file` writing a corrupted data file when the export is retried after a primary replica failover. The retry reopened the files without truncating them, so a shorter second export left the tail of the first one behind, and importing the file failed with a decoding error.
  
  

## Code Refactors
- **Let the driver failover work for a broader range of error codes for clusters**
  Let the driver's failover work in clustered environment for a broader range of error codes.
  
  Introduce a common `ServerUnavailable` connection error for gRPC drivers, that can let the driver retry single requests against the same or a new primary without depending on our internal error codes. Now, this classification depends on the HTTP status code of the error.
    
