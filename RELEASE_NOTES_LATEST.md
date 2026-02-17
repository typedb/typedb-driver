Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.29.5
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
        <version>2.29.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.29.5
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.29.5
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.29.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.29.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.29.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.29.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.29.5" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.29.5" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.29.5
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.29.5


## New Features


## Bugs Fixed

- **Fix 2.x Java and Python memory leaks (#844)**
    
    ## Usage and product changes
    
    We fix Java and Python driver memory leaks in 2.x, caused by mismatches
    in the SWIG definition layer when protocol and c-layer method names
    changed.
    
    ## Implementation
    
    - Update dependencies repo, using fully pinned versions of dependencies
    that would otherwise for a Rust 2024 upgrade
    - Update swig file to use new method names we didn't update
  

## Code Refactors


## Other Improvements


    
