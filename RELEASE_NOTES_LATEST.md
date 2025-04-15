Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.2.0-rc0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.2.0-rc0/a=noarch;xg=com.typedb/)
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
        <version>3.2.0-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.2.0rc0
```


## New Features
- **Introduce Rust driver token-based authentication**
  We update the protocol version and introduce token-based authentication for all the available drivers. Now, instead of sending usernames and passwords for authentication purposes, authorization tokens are implicitly added to every network request to a TypeDB server. It enhances the authentication speed and security. 
  
  These tokens are acquired as a result of driver instantiation and are renewed automatically. This feature does not require any user-side changes.
  
  Additionally, as a part of the HTTP client introduction work, we rename Concept Documents key style from snake_case to camelCase, which is a common convention for JSONs (it affects only value types: `value_type` -> `valueType`).
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements
- **Update dependencies**
  After a recent update of the crates, rustls added aws-lc-rs as a default dependency. This caused runtime issues during loading of the shared library where the dynamic linker could not find some symbols from aws-lc: 
  
  ```
  ImportError: dlopen(.../native_driver_python.so, 0x0002): symbol not found in flat namespace '_aws_lc_0_28_0_EVP_aead_aes_128_gcm'
  ```
  
  We decided to force the use of ring as the cryptographic provider for rustls instead.
  
  
    
