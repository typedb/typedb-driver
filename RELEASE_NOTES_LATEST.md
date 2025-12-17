**This is an alpha release for CLUSTERED TypeDB 3.x. Do not use this as a stable version of TypeDB.**
**Instead, reference a non-alpha release of the same major and minor versions.**

Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.7.0-alpha-3
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.7.0-alpha-3/a=noarch;xg=com.typedb/)
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
        <version>3.7.0-alpha-3</version>
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

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.7.0-alpha-3


## New Features

### Refactor DriverOptions' TLS configuration

Introduce `DriverTlsConfig` with three possible constructors to eliminate ambiguity of TLS settings:

- disabled
- enabled using default native root CA
- enabled using custom root CA

Now, `DriverOptions` contain a separate `tls_config` field instead of `is_tls_enabled` and `tls_root_ca`, and every
`DriverOptions` object requires an instance of `DriverTlsConfig` on construction to provide explicit TLS connection
preferences.

#### Examples

Rust:
```rust
// Default: TLS enabled with native system trust roots
let options = DriverOptions::new(DriverTlsConfig::enabled_with_native_root_ca()).use_replication(true);

// Custom CA (PEM) for private PKI / self-signed deployments
let options_custom_ca = DriverOptions::new(
    DriverTlsConfig::enabled_with_root_ca(Path::new("path/to/ca-certificate.pem")).unwrap(),
);

// Disable TLS (NOT recommended)
let options_insecure = DriverOptions::new(DriverTlsConfig::disabled());
```

Java:
```java
// Default: TLS enabled with native system trust roots
DriverOptions options = new DriverOptions(DriverTlsConfig.enabledWithNativeRootCA()).useReplication(true);

// Custom CA (PEM) for private PKI / self-signed deployments
DriverOptions optionsCustomCA = new DriverOptions(DriverTlsConfig.enabledWithRootCA("path/to/ca-certificate.pem"));

// Disable TLS (NOT recommended)
DriverOptions optionsInsecure = new DriverOptions(DriverTlsConfig.disabled());
```

Python:
```py
# Default/recommended: TLS enabled with native system trust roots
options = DriverOptions(DriverTlsConfig.enabled_with_native_root_ca(), use_replication=True)

# Custom CA (PEM) for private PKI / self-signed deployments
options_custom_ca = DriverOptions(DriverTlsConfig.enabled_with_root_ca("path/to/ca-certificate.pem"))
options_custom_ca.use_replication = True # Post-construction setter

# Disable TLS (NOT recommended)
options_insecure = DriverOptions(DriverTlsConfig.disabled())
```
