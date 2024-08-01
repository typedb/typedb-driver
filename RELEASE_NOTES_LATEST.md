Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.28.5-rc0
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
        <version>2.28.5-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.28.5rc0
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.28.5-rc0
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.28.5-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.28.5-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.28.5-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.28.5-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.28.5-rc0" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.28.5-rc0" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.28.5-rc0
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.28.5-rc0


## New Features


## Bugs Fixed
- **Python. Fix driver.close(). Add integration tests for connection closing for both core and cloud**
  We fix the issue https://github.com/vaticle/typedb-driver/issues/669, where the Python Driver didn't close the connection when calling `TypeDBDriver.close()`.
  
  

## Code Refactors
- **Invert address translation table: map public addresses to private**
  
  NOTE: The address translation table now represents mapping _from_ the desired connection addresses to the addresses the cloud servers are configured with. This change does not impact users of TypeDB Core or TypeDB Cloud through the TypeDB Cloud Platform (https://cloud.typedb.com/)
  
  

## Other Improvements
- **Hermetic npm deployment**
  
- **Build and deploy for Python 3.12**
  
  We enable support for python 3.12 driver build.
  
- **Partial go driver implementation**
  Implement Basic core driver functionality of creating and closing a database.
  
  
- **Make the author of the NodeJS and Python drivers "TypeDB Community"**
  
  The `author` field of our NodeJS and Python drivers (`package.json` and PyPi configuration) is now **TypeDB Community** with the email being **community@typedb.com**.
  
  
- **Fix CI builds with updated error messages from typedb and typedb-cloud artifacts**
  We update `typedb` and `typedb-cloud` artifacts references to match `TypeDB***Runner`s used in most of the languages with `typeql` versions used in Rust and Java drivers in CI. 
  
  Previously, the versions were mismatched, which caused errors in CI because of the different error messages received from drivers (Rust `typeql` for Rust, Java `typeql` for Java, and direct values from the server for all the other drivers). 
  
    
