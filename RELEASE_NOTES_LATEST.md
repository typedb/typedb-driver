Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.2
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.0.2/a=noarch;xg=com.typedb/)
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
        <version>3.0.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.2
```

## New Features


## Bugs Fixed
- **Introduce 3.0 cloud driver tests**
  We fix cloud drivers creation interfaces and introduce integration and behavior tests to cover these public methods.

- **BUGFIX python-3.0.0: remove extra argument in call to internal _Driver.cloud**


## Code Refactors
- **Add version filter for maven packages for Java release notes**
  Add version filter for maven packages for Java release notes.

- **Update Java APIs with marker throwing runtime TypeDBDriverExceptions**
  

## Other Improvements
- **Regenerate docs**

- **Fix rust format**

- **Update logback to a non-vulnerable version**

- **Remove debug println**

- **Update typedb behavior dependency and typedb server artifact to test the unblocked server features**
  Update typedb behavior dependency and typedb server artifact to test the unblocked server features of rollback and write query fetches.

- **Remove transaction pop from java tests on rollback**

- **Update examples to fix their build**

    
