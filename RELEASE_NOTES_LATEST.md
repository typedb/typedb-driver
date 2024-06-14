Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.28.4
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
        <version>2.28.4</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.28.4
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.28.4
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.28.4" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.28.4" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.28.4" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.28.4" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.28.4" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.28.4" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.28.4
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.28.4


## New Features


## Bugs Fixed
- **JNI library loading uses a predetermined file name**
  The unpacked JNI library now uses a pre-determined filename. This replaces logic to extract the filename from the packaged resource.
  
  

## Code Refactors


## Other Improvements
- **Turn on development mode for tests**
  We activate the newly introduced in `TypeDB` `--development-mode.enable` flag for all the CI builds of the driver.
  
- **Update `nodejs` driver dependencies and fix builds based on the updated `typescript` rules**
