Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.12.2
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.12.2/a=noarch;xg=com.typedb/)
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
        <version>3.12.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.12.2
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.12.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.12.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.12.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.12.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.12.2" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.12.2" />
</ItemGroup>
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.12.2
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.12.2


## New Features
- **Make the HTTP driver correctly always follow cluster 'misdirected request' redirects**
  Make the http-ts driver retry a couple of times when redirecting request (usually, it is needed when a new primary is selected, and there can be a lag between the states of the target cluster). 
  
  Additionally, simplify the address resolution logic and avoid appending excessive schemas to the target URLs, expecting correct input params from the users.

## Bugs Fixed
- **Update core and cluster artifacts**
  
  Update core and cluster artifacts to fix red CI
  
  

## Code Refactors
- **Update behaviour for new Given tests**
  Update behaviour dependency that includes all value type tests for 'given' rows, and rename the steps for setting 'given' rows to match existing naming conventions.
  
  

## Other Improvements
- **Fix remote cache setup**
  Moves python installation into sub-shell and does pushd/popd.
  Removes from windows because it doesn't look like we ever do it in windows.
  
- **Enable remote cache in circleci**
  Enable the remote cache in circleci, speeding up intermediate jobs such as `deploy-*-any`.
  
  
- **Update cluster ref**
  Update ref to the latest cluster 
  
  
    
