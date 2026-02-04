Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.8.0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.8.0/a=noarch;xg=com.typedb/)
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
        <version>3.8.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.8.0
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install @typedb/driver-http@3.8.0
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.8.0


## New Features


## Bugs Fixed
- **Fix memory leaks in C integration test example**
  
  We fix an issue in the c driver tests and examples which used the wrong transaction close method, causing memory leaks.
  
  
  

## Code Refactors


## Other Improvements
- **Update typedb artifact**

- **Create Bazel CI config**
  
  We create a separate Bazel CI config, which disables the disk cache. Disk cache is most useful on machines where we do many different builds on different branches and the standard bazel cache might be clobbered unnecessarily. In CI, where we do mostly one build per machine and also have a remote cache, this is not useful and on top of that hits the disk size limits.
  
  
- **Add bazel disk cache to speed up local builds when switching branches**

- **Improve readability of reference docs**
  
  Add `function` before the method name (or `method` in Java/Python/Rust), to help readability of the generated docs page.
  
  
- **Regenerate docs with new css classes**

- **Add doc-api-reference-driver class to classes in driver API reference pages**

- **Improve driver docs reference formatting**
  
  We improve the driver reference formatting by including a `class` or `enum` or `struct` description to help delineate these constructs from methods in very long documentation pages.
  
  
- **Update READMEs**

- **Update C driver documentation to TypeDB 3.x API**
    - Update C driver documentation to TypeDB 3.x API structure matching Rust/Java/Python
    - Add C driver example with full integration test coverage
    - Add example compile verification targets for Java, Rust, and C drivers
    - Fix Java Value API Javadoc to correctly describe return types
    - Add TransactionOptions and QueryOptions includes to Java/Python/Rust api-reference
  
  
  
- **Fix formatting**

- **Set up configurable FFI/Rust driver logging**
  
  Rather than having Python/Java/other wrappers invoke logging initialization explicitly, we now just do it on open of the Rust driver automatically.
  
  This should fix warnings in Python:
  ```
  Failed to initialize logging: attempted to set a logger after the logging system was already initialized
  ```
  
  We also remove uses of the `log` create in the `//c` layer, and use `tracing` instead.
  
  We can now configure logging in the Rust-driver component with the following variables: `TYPEDB_DRIVER_LOG=info|debug|warn|trace` or using the usual `RUST_LOG` variable. By default, ffi-based drivers calling `init_logging` will use INFO logging.
  
  Rust applications should continue set up their own logging subscriber, as usual.
  
  
- **Add banner to readme, fix link**

- **Add contributing guidelines to CONTRIBUTING.md**

- **Handle output directories properly for python BDD and docs rules (#831)**
  This enables the bazel `remote_download_toplevel` flag, which broke build due to badly declared outputs.
  
  
- **Update README.md by removing outdated links**

- **Handle output directories properly for python BDD and docs rules**
  This enables the bazel `remote_download_toplevel` flag, which broke build due to badly declared outputs.
  
  
- **Remove --remote_download_toplevel since not all bazel targets have predeclared outputs (docs)**

- **Improve bazel cache performance in CI with --remote_download_toplevel**

    
