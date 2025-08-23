Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@{version}
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/{version}/a=noarch;xg=com.typedb/)
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
        <version>{version}</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver=={version}
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/typedb-driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install typedb-driver-http@{version}
```

[//]: # (TODO: Please remove the unreleased drivers manually. Commenting them out in Markdown looks scary)

### NodeJS GRPC driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/drivers/nodejs/overview

```
npm install typedb-driver@{version}
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="{version}" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="{version}" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="{version}" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="{version}" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="{version}" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="{version}" />
</ItemGroup>
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-cpp+version:{version}
Documentation: https://typedb.com/docs/drivers/cpp/overview

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:{version}

{ release notes }
