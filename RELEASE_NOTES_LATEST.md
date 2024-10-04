Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.0-alpha-5
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
        <version>3.0.0-alpha-5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.0-alpha-5
```

## New Features
- **Introduce TypeDB 3.0 Python driver**
  We introduce the updated Python driver for the upcoming 3.0 release. To align with the updated Rust driver, we removed `Concept API` (so you could simplify your querying workflow with a single `tx.query()` entry point), squeezed sessions and transactions to standalone transactions, and remodeled messaging with the server.
  
  As it's an alpha release, some of the features are temporarily disabled both on the server and the driver's side:
  * Options;
  * User management;
  * Cloud connection with replicas information.
  
  Moreover, we no longer support Python 3.8 as [its support comes to an end](https://devguide.python.org/versions/) and we want to offer the full support of our newly introduced timezones with the standard library equally for all the versions of the language.

## Bugs Fixed
- **Fix native object ownership checks in python driver**
  Multiple rarely used features of the python driver used to be broken because of the native object misuse. 

## Code Refactors


## Other Improvements 
- **Bumped API version in antora config**

- **Rename Maven groupId from "com.vaticle" to "com.typedb" to match the package path**

- **Fix CircleCI jobs for Maven installation and Python builds**
  