Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.1.0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.1.0/a=noarch;xg=com.typedb/)
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
        <version>3.1.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.1.0
```

## New Features

- **Add Python 3.13 release jobs**
  TypeDB Driver for Python 3.13 is now officially published.


- **Introduce a single driver creation endpoint for all editions of TypeDB**
  Introduce a single driver creation endpoint for all editions of TypeDB: all `new_core`, `new_cloud`, `TypeDB.core`, `TypeDB.cloud`, and other alternatives in TypeDB drivers now have a single `new` / `driver` method that accepts a single string as an address.

  Use it for any edition of TypeDB 3.x (Community Edition, Cloud, Enterprise) the following way (check out `README` or driver documentation for full usage examples):
  Rust:
  ```rust
  let driver = TypeDBDriver::new(
      TypeDBDriver::DEFAULT_ADDRESS,
      Credentials::new("admin", "password"),
      DriverOptions::new(false, None).unwrap(),
  )
  .await
  .unwrap();
  ```

  Python:
  ```py
  driver = TypeDB.driver(TypeDB.DEFAULT_ADDRESS, Credentials("admin", "password"), DriverOptions())
  ```

  Java:
  ```java
  Driver driver = TypeDB.driver(TypeDB.DEFAULT_ADDRESS, new Credentials("admin", "password"), new DriverOptions(false, null));
  ```

  Currently, TypeDB 3.x supports only a single server address, so the list-based method overloading is unnecessary. We plan to preserve the same simplified API after introducing multi-node servers, extending the accepted formats of the input address string instead. Stay tuned for details!

## Bugs Fixed


## Code Refactors



## Other Improvements

- **Fix python example build**

- **Update dependencies for the 3.0.1 release**
  Update dependencies.

- **RustFmt**

- **Clean up Driver field, update core artifact to 3.0.6**

- **Fix checkstyle**

- **Check in Cargo.toml files**

- **Update Rust version to 1.81.0**

