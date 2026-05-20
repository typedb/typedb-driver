Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview

```sh
cargo add typedb-driver@3.11.1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.11.1/a=noarch;xg=com.typedb/)
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
        <version>3.11.1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.11.1
```

### C# driver

NuGet package: https://www.nuget.org/packages/TypeDB.Driver
Documentation: https://typedb.com/docs/drivers/csharp/overview

```xml
<ItemGroup>
    <PackageReference Include="TypeDB.Driver" Version="3.11.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-x64" Version="3.11.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-x64" Version="3.11.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.win-x64" Version="3.11.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.osx-arm64" Version="3.11.1" />
    <PackageReference Include="TypeDB.Driver.Pinvoke.linux-arm64" Version="3.11.1" />
</ItemGroup>
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/home/install/drivers/

```
npm install @typedb/driver-http@3.11.1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.11.1


## New Features
- **Introduce TypeDB cluster support**
  
  Introduce cluster support across all TypeDB 3.x driver languages (Rust, Java, Python, C#, C/C++, HTTP-TypeScript).
  
  ### gRPC
  
  **This release contains breaking changes to public interfaces and will require minor code updates as a part of the migration.**
  
  **This release is not compatible with the old GRPC protocol. The C headers have changed significantly, requiring updates in the dependent drivers.**
  
  - Implement cluster-aware connection management. The driver discovers all replicas on connection open and automatically routes operations to the primary replica for strong consistency. You can specify multiple replicas of a cluster or just a single address -- even one replica is enough to get the whole information about the cluster and establish a consistent and automatically redirecting connection.
  - Change `DriverOptions`. Add `DriverTlsConfig` for explicit TLS configuration: `disabled()`, `enabled_with_native_root_ca()`, `enabled_with_root_ca(path)`. Replaces the previous `is_tls_enabled` boolean.
  - Add `ServerRouting` for controlling request routing:
    - `Auto` — driver selects the primary replica automatically, with failover on leadership changes. Currently, always strongly consistent.
    - `Direct(address)` — route to a specific replica. Useful for read-only workloads on secondaries where it's possible.
  - Add `Server` API exposing replica metadata: `id`, `role` (Primary/Candidate/Secondary), `term`, `address`, and `version`.
  - Add address translation for cloud/NAT deployments. Supports mapping public addresses to private connection addresses.
  - Add primary failover with configurable retries (`DriverOptions::primary_failover_retries`, default: 1). On leadership change, the driver seeks the new primary and retries the operation.
  - Refactor `Database` API: remove unused `ReplicaInfo` / `DatabaseReplicas`. Replica information is now server-level via `driver.servers()`.
  - Add `request_timeout` to `DriverOptions` to ensure any operation of the driver (excluding operations within already opened transactions: queries, commits, ...) is executed within the set time limit. The driver returns an error if the time limit is reached without a response from a server. This prevents the request from hanging indefinitely if the server is online, but does not respond. 
  - Rename `community` back to `core` to highlight the technical side of the edition used.
  - Complete the format and the content of the API references.
  
  ### HTTP-TS
  
  **The HTTP-TS driver IS backwards compatible. Updating it will not introduce build or runtime errors, but will let your applications support automatic redirects when putting multiple addresses in the constructor.** 
  
  - Implement cluster-aware connection management. Unlike gRPC, its retry logic is not configurable, but follows a similar pattern: the driver tries executing a request against the first address specified in the constructor; in case of an explicit redirect in the error response it reauthenticates & redirects the request to the specified location; in case the error is preserved the driver tries the rest of the addresses in the list; if no addresses work, the request fails. 
  - Add new messages for retrieving `Server` statuses.
  
  

## Bugs Fixed
- **Restore C# cluster BDD and integration tests**
  Restore C# tests in order to maintain quality control.
  
  

## Code Refactors


## Other Improvements
- **Prepare release 3.11.0**
  Bump version & prepare release notes
  
  
  
- **Prepare release 3.11.0-rc1**
  Retry release 3.11.0-rc0 with a shorter TEMP path for C#
  
  
- **Prepare release 3.11.0-rc0**
  Bump version & update release notes
  
  
- **Finish csharp Bazel 8 reintroduction**
  Update C# driver docs, README, and CI jobs.
  
  
- **Fix http-ts failover logic**
  
  When the cluster primary is no longer responsive, the HTTP-TS driver may route its next signin to a secondary that's briefly returning `SRV14 "Not yet initialised"` — e.g. a just-restarted node whose system DB isn't loaded into memory yet, or a node mid-promotion.
  
  This made the failover incomplete and the `cluster-failover.ts` test flaky in CI: when fallback landed on a stuck secondary before a healthy origin, the driver burned the test budget on a single dead-end.
  
  
- **Wire C# driver into Bzlmod build for Bazel 8 compatibility**

- **Update Rust dependencies**
  
  
  
    
