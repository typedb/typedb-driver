Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

**ATTENTION:** since this is an alpha version of a clustered TypeDB, the API is unstable and may change significantly between versions.
Use this driver only for the same `XYZ-alpha-A` version of the server.

```sh
cargo add typedb-driver@{version}
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/{version}/a=noarch;xg=com.typedb/)
Documentation: https://typedb.com/docs/drivers/java/overview

**ATTENTION:** since this is an alpha version of a clustered TypeDB, the API is unstable and may change significantly between versions.
Use this driver only for the same `XYZ-alpha-A` version of the server.

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

**ATTENTION:** since this is an alpha version of a clustered TypeDB, the API is unstable and may change significantly between versions.
Use this driver only for the same `XYZaA` version of the server.

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver=={version}
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

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@{version}
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:{version}

{ release notes }
