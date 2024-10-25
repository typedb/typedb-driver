Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.0-alpha-7
```


### Java driver

Available through https://repo.typedb.com
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
        <version>3.0.0-alpha-7</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.0-alpha-7
```

## New Features


## Bugs Fixed
- **Add handler for initial TransactionOpen response instead of erroring**

  We eliminate the wrong error shown in logs when opening a transaction for all the 3.0 drivers.

## Code Refactors
- **Receive `QueryAnswer`'s `QueryType` from the server for all answer types**
  
  The protocol has been updated, and `Ok` `QueryAnswer`s receive correct `QueryType`s from the server instead of the client-side hardcode usage.
  

## Other Improvements
- **Introduce 3.0 Java driver docs and tests. Implement new fetch BDD steps in 3.0 drivers**

  We introduce updated documentation, usage examples, and automated tests for the Java driver, to cover all the existing driver's APIs by sustainable validations.
  
  Additionally, we implement additional BDD steps to check concept documents in BDDs for other 3.0 drivers: Rust and Python. 
  


    
