Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.29.2
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
        <version>2.29.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.29.2
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.29.2
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.29.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.29.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.29.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.29.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.29.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.29.2" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.29.2
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.29.2


## New Features


## Bugs Fixed


## Code Refactors


## Other Improvements
- **Native library names now include platform**
  Native library names now include platform. This avoids the case where flattening a java project causes libraries for from one architecture for a given os clobbers the other.
  
  
    
