**This is an alpha release for CLUSTERED TypeDB 3.x. Do not use this as a stable version of TypeDB.**
**Instead, reference a non-alpha release of the same major and minor versions.**

Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.7.0-alpha-2
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.7.0-alpha-2/a=noarch;xg=com.typedb/)
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
        <version>3.7.0-alpha-2</version>
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
pip install typedb-driver==3.7.0a2
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.7.0-alpha-2


## New Features
**Support clustered TypeDB**

TypeDB Driver now supports connecting to multiple replicas of a clustered TypeDB database. This is an experimental feature,
and the API can change drastically between versions. The features related include, but are not limited by:

- Refactored connection addresses to allow using a single address, multiple addresses, or an address translation configuration.
- Automated connection to unspecified replicas of the server if present.
- Automated operation failover against replicas.
- Extended `DriverOptions` for better connection and failover configuration.
- Introduced `ConsistencyLevel` for most of the operations. If operations do not support consistency levels or are executed against a non-clustered TypeDB edition (CE), the default strong consistency is used. There are three levels: strong (execute only on the primary replica), eventual (can execute on secondary replicas until it fails), and replica dependent (to execute an operation on a specific replica, which is useful for debugging purposes).
- Added cluster management commands (registration and deregistration of peers is a temporary feature available only in alpha. Do not use it in TypeDB Cloud if you don't want to invalidate your cluster).

Please reference specific driver's documentation for more info and examples.
