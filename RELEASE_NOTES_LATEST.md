Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@2.28.1
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
        <version>2.28.1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==2.28.1
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@2.28.1
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="2.28.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="2.28.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="2.28.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="2.28.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="2.28.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="2.28.1" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:2.28.1
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:2.28.1


## New Features
- **Copy jni library from input stream rather than file**
  We now copy the jni library by reading from it as an input stream rather than as a file copy. This approach works when the JNI jars are nested, such as in Spring Boot projects.
  

## Code Refactors
- **Fix misleading cloud encryption error message**
  The old version of the cloud encryption error message confused the user in case, for example, their endpoint is not encrypted, but the connection is. There are also other potential causes of the `received corrupt message` status message, that we can't understand on a deeper level, so it's more correct to have a less specific error message here.
  

## Other Improvements
- **Each API reference is combined in a single partial for easier preview and usage**
  
  For each driver, we move all AsciiDoc `include` directives to a dedicated partial called `api-reference.adoc` in the drivers repo.
  This way, all the content can be previewed from the typedb-driver repo and included in the docs web content with a single include directive.
  
