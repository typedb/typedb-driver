Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.0-alpha-6
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
        <version>3.0.0-alpha-6</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.0-alpha-6
```

## New Features
- **Add concept documents for fetch queries.**
  We add concept documents to support the results of the reintroduced `fetch` queries.

  In Rust, results of `fetch` are streams of `ConceptDocument`s. It is possible to work with the structured document as a Rust `struct`, but it's also possible to convert it to a `JSON` document and/or its `String` representation.

  In Java, these results are presented as iterators over custom `JSON` class instances. A respective `toString` method is available.

  In Python, these results are presented as iterators over standard `dict` instances (able to be printed).

  Additionally, we add `QueryType` getters for general `QueryAnswer`s, so it's possible to check its type without collection.

  Usage examples are shown in `README` for the Rust and the Python drivers. Example integration tests are also available for all 3 drivers.


- **Introduce 3.0 datetime-tz offsets and Rust driver documentation and tests.**
  We introduce the second version of the Rust driver, adding TimeZone offsets for `datetime-tz` value types, fixing minor bugs and presenting the updated documentation and automated tests.

  Rust driver changes:
  * Add `datetime-tz` offsets;
  * Refactor test structure to separate integration and behaviour tests using Bazel, not Cargo flags;
  * Introduce example integration test for Rust and update README with the formatted code sample;
  * Introduce updated 3.0 bdds for `connection` and `driver`;
  * Introduce flags to run Rust bdds in `core` or `cloud` modes when TypeDB Cloud 3.x is implemented;
  * Fix Rust driver docs parser and update generated docs.

  Java and Python drivers changes:
  * Add `datetime-tz` offsets;
  * Rename `Thing` to `Instance`;
  * Remove `ThingType`,
  * Added `getLabel`/`get_label` for all `Concept` classes. Previously, it was only available for `Type`s.

  Python driver changes:
  * Add bdd steps to match the updated declarations.


- **Introduce 3.0 Python driver docs and tests.**
  We introduce the second version of the Python driver, fixing a number of minor bugs from the first version, enhancing existing APIs, and presenting the updated documentation and automated tests.

## Bugs Fixed


## Code Refactors


## Other Improvements
