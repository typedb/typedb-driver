**This is an alpha release for CLUSTERED TypeDB 3.x. Do not use this as a stable version of TypeDB.**
**Instead, reference a non-alpha release of the same major and minor versions.**

Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.10.0-alpha-1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.10.0-alpha-1/a=noarch;xg=com.typedb/)
Documentation: https://typedb.com/docs/drivers/java/overview

**ATTENTION:** since this is an alpha version of a clustered TypeDB, the API is unstable and can drastically change between versions.
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
        <version>3.10.0-alpha-1</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

**ATTENTION:** since this is an alpha version of a clustered TypeDB, the API is unstable and can drastically change between versions.
Use this driver only for the same `XYZ-alpha-A` version of the server.

Available through https://pypi.org

```
pip install typedb-driver==3.10.0a1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.10.0-alpha-1

## New Features

### Introduce TypeDB cluster support
Introduce cluster support across all TypeDB 3.x driver languages (Rust, Java, Python, C#, C/C++, HTTP-TypeScript).

#### gRPC

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

#### HTTP-TS

**The HTTP-TS driver IS backwards compatible. Updating it will not introduce build or runtime errors, but will let your applications support automatic redirects when putting multiple addresses in the constructor.**

- Implement cluster-aware connection management. Unlike gRPC, its retry logic is not configurable, but follows a similar pattern: the driver tries executing a request against the first address specified in the constructor; in case of an explicit redirect in the error response it reauthenticates & redirects the request to the specified location; in case the error is preserved the driver tries the rest of the addresses in the list; if no addresses work, the request fails.
- Add new messages for retrieving `Server` statuses.
