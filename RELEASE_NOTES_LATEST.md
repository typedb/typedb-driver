Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.25.2
```


### Java driver

Available through https://repo.vaticle.com
Documentation: https://typedb.com/docs/clients/java-driver

```xml
<repositories>
    <repository>
        <id>repo.vaticle.com</id>
        <url>https://repo.vaticle.com/repository/maven/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupid>com.vaticle.typedb</groupid>
        <artifactid>typedb-driver</artifactid>
        <version>2.25.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.25.2
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@2.25.2
```


## New Features
- **UX improvements: FFI logging, error message fix, Rust stream borrow checks**
  
  We improve the UX of Driver usage in several ways:
  
  - [Rust/Java/Python] Fix the formatting of error messages received from the server during a transaction.
  - [Java] Fix native error message formatting.
  - [Java/Python] Add the ability to enable Rust logging over FFI (resolves #482). 
    -  The granularity of the logs is controlled by the `TYPEDB_DRIVER_LOG_LEVEL` environment variable, which can be set to any value of `error` (default), `warn`, `info`, `debug`, or `trace`, in order of increasing verbosity. Setting the value to `"typedb_driver=info"` will show only the typedb-driver messages.
    - More advanced syntax is described in [the env_logger documentation](https://docs.rs/env_logger/latest/env_logger/index.html#enabling-logging) 
  - [Rust] Network streams now borrow the transaction, so that the transaction can't be mistakenly dropped. (resolves #449) 
  

## Bugs Fixed

- **Fix NodeJS isEnterprise flag computation**
- 
- **Update protocol version in package.json for Node driver**

## Code Refactors


## Other Improvements
  
- **Switch to our patched version of rules_rust (based off v0.30.0)**
  
  We update rules_rust to v0.30.0 with a patch that resolves gherkin build errors.
    
