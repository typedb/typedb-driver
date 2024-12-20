Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.0
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
        <version>3.0.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.0
```

## New Features

- **Introduce 3.0 Rust, Java, and Python drivers**

  We introduce the updated Rust, Java, and Python drivers compatible with TypeDB 3.0.
  These updates bring significant opportunities and UX improvements, including:
  - **Simplified querying workflow**: The `Concept API` has been removed, offering a single `tx.query()` entry point.
  - **Streamlined transactions**: Sessions and transactions are now consolidated into standalone transactions.
  - **Expanded attribute value types**: Added support for `decimal`, `date`, `datetime-tz`, and `duration`.
  - **Unified authentication**: The updated authentication mechanism is shared across all drivers.
  - **New query result formats**: Introducing `Concept Row`s for table-like outputs and `Concept Document`s for structured outputs used in `fetch` queries.

  Some features are currently disabled due to limitations on the TypeDB Server side:
  - Options.
  - Replicas information.

  For examples and usage of the new drivers, see the `README` files in their respective language packages. Explore all the exciting features of TypeDB 3.0 [here](https://github.com/typedb/typedb/releases).


- **Add Python 3.13 support**

  We introduce support for Python 3.13. With this update, support for Python 3.8 has been discontinued, as it reached end-of-life ([details](https://devguide.python.org/versions/)). This change ensures consistent implementation of the `datetime-tz` value type introduced in TypeDB 3.0 across all supported Python versions.


## Bugs Fixed
- **Optimise driver dispatch loop**
  
  We optimise the driver dispatch loop: previously, we could dispatch at most 1000 serial messages per second, though in practice tokio would sometimes sleep longer than expected and the query and transaction latencies increased. This stems from Tokio's approximately millisecond resolution. By moving the collect-dispatch loop to a dedicated thread, we can now dispatch messages after microseconds of batching, improving overall driver performance significantly.


## Code Refactors
- **Rename long to integer**
  Renamed the TypeQL `long` value tpe to `integer`
  
  

## Other Improvements
- **Speed up transaction opening and fix parallel**
  * We fix transaction opening for all the supported drivers, speeding up the operation by 2x.
  * We eliminate database-related errors happening during concurrent database management (create and delete operations) by multiple drivers due to cache mismatches. 
  * We make transaction opening requests immediately return errors instead of waiting for additional transaction operations to be performed (e.g. commit or query).
  
  
- **Refactor concepts APIs to quickly access optional instances and values properties**
  We generalize the approach to getting concepts properties in TypeDB Drivers, introducing a set of new APIs for fetching optional values related to instances of `Concept` classes in Java and Python.
  
  Now, all subclasses of `Concept` have a set of new interfaces starting with `try` to access IIDs, labels, value types, and values without a need to cast to a specific `Instance` or `Value`. These methods can be useful if:
  * you have an established workflow with constant queries and always expect these properties to exist;
  * you want to implement a custom handling of cases where the expected values are missing shorter (without exceptions).
  
  Additionally, value type checks like `is_boolean`/`isBoolean` are also declared on the top-level `Concept`.
  
  Note that casting is still possible, and its benefits are, as usual:
  * static type checking for your programs using TypeDB Driver;
  * access to the non-optional `get` interfaces for specific subclasses of `Concept` like `get_iid`/`getIID` for `Entity` and `Relation`.
  
  Also note that some of the interfaces have changed (e.g., `asBoolean` -> `getBoolean` for `Attribute` and `Value` to separate value retrieval and concept casting), but their functioning has not.
  
  
- **Remove promises resolves in destructors to eliminate redundant exceptions. Cleanup Python exceptions formatting**
  We remove the feature of `Promise`s to call `resolve` in destructors in languages like Java and Python. Now, if a promise is destroyed, it's not resolved, and, thus, not awaited. This helps the driver to remove the excessive duplicated errors printing in destructors of promises, although the error is already returned from a commit operation.
  
  Now, if you just run queries and close the transaction, nothing will be awaited and persisted. However, if you commit your transaction, all the ongoing operations on the server side will finish before the actual commit. This lets you speed up the query execution (in case these queries don't collide with each other): 
  ```py
  for query in queries:
      tx.query(query)
  tx.commit()
  ```
  If one of the `queries` contains an error and it's not `resolve`d, it will be returned from the `commit` call, and no excessive errors will be printed on resource release.
  
  Detailed examples for each language supported are presented in `README`s.
  
  Additionally, Python Driver's `TypeDBDriverError` exceptions no longer show the excessive traceback of its implementation, and only the short informative version for your executed code is presented.
  
  
- **Update final reference of git org to @typedb**

- **Update typedb-runner maven coordinate to com.typedb**

- **Rename typedb-cloud workspace to @typedb_cloud**

- **Renamed workspace to typedb-driver**

- **Update dependant repositories: @typedb_dependencies, @typedb_protocol, and @typedb_behaviour**

- **Rename /dependencies/vaticle to /typedb**

- **Replaced Vaticle with TypeDB in strings and copyrights**

- **Update CI images to 'typedb-ubuntu'**

- **Unify transaction cleanup in driver BDDs when opening new transactions**
  
- **Add handler for initial TransactionOpen response instead of erroring**

- **Fix CircleCI jobs for Maven installation and python builds**
  We fix two CircleCI issues:
  * `brew install maven` [has been failing](https://app.circleci.com/pipelines/github/typedb/typedb-driver/911/workflows/9571978a-e2c7-40cc-afd6-372356f267f7/jobs/7072) for Rosetta configuration;
  * python installation for Linux machines required a different process after we updated from `python3.8` to `python3.9` as a minimal version.

- **Bumped API version in antora config**

- **Rename maven groupId "com.vaticle" -> "com.typedb". Return a part of Java driver's CI tests**
  We completely clean `com.vaticle` mentions in the `typedb-driver` repo:
  * generated `pom` files for the Java driver now contain `com.typedb` instead of `com.vaticle.typedb`;
  * docs for the Java driver are regenerated so it doesn't reference outdated `com.vaticle.typedb` package (other issues in these docs will be addressed in the following prs).
  
- **Fix native object ownership checks in python driver**
  Fixes native object ownership checks in python driver

  
- **Rename Java package com.vaticle.typedb to com.typedb and remove typeql dependencies.**
  We rename the Java driver's package from `com.vaticle.typedb` to `com.typedb`.
  We remove dependencies on `typeql` as it's no more needed. 

- **Add typedb 3 roadmap link to the release notes**

- **Apply fix that allows connecting to servers listening on 0.0.0.0**
    
