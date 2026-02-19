**This is an alpha release for CLUSTERED TypeDB 3.x. Do not use this as a stable version of TypeDB.**
**Instead, reference a non-alpha release of the same major and minor versions.**

Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.8.0-alpha-1
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.8.0-alpha-1/a=noarch;xg=com.typedb/)
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
        <version>3.8.0-alpha-1</version>
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
pip install typedb-driver==3.8.0a1
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.8.0-alpha-1


## Bug fixes

### Fix various build-related bugs

Now, all features of the updated drivers must work correctly. Visit https://typedb.com/docs/reference/typedb-cluster/drivers/ for usage examples and more info.
