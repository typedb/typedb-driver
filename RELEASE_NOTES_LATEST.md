Documentation: https://typedb.com/docs/clients/overview

## Distribution

### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver/api-reference


```sh
cargo add typedb-driver@2.25.0
```


### Java driver

Available through https://repo.vaticle.com
Documentation: https://typedb.com/docs/clients/java-driver/api-reference

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
        <version>2.25.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver/api-reference

Available through https://pypi.org

```
pip install typedb-driver==2.25.0
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver/api-reference

```
npm install typedb-driver@2.25.0
```


## New Features
- **Implement Fetch query with JSON output**

  We implement the newly introduced `Fetch` query. The result of a Fetch query is a stream of serializable JSON objects.

  For more details, see https://github.com/vaticle/typeql/pull/300

  Other changes in this PR:
    - `Match` query is now called `Get`, to avoid confusion with a match clause;
    - `Numeric` is deprecated in favour of `Optional<Value>`, `NumericGroup` is replaced by a `ValueGroup` to reflect that change.
        - `ValueGroup.value()` now returns `Optional<Value>` as well.



- **Directly generate docs into directories and add missing doc comments**

  We had all generated docs files in one directory for each driver. It was not convenient for the user and for the following processing. We added files containing mapping from file names to directory names and organise files according to this mapping.


- **Docs generation**

  Drivers documentation on our website was created manually and therefore contained some errors and was difficult to maintain.

  We add docstrings for API methods, and now one can see them directly in the IDE. Moreover, we introduce scripts for drivers documentation generation for our website, that reduces the amount of errors and simplifies the docs updating process.



## Bugs Fixed
- **Fix filenames with incorrect symbols for Windows**

  We had some filenames in generated AsciiDoc files that are prohibited in Windows. It affected only NodeJS Driver, but cloning the repo was impossible in Windows. We fixed it.


- **Update TLS certificates for client tests**
  Updates expired TLS certificates which the client uses for tests against TypeDB enterprise.



## Code Refactors
- **Wrap native exceptions with python driver exception**

  We had two exception classes: `TypeDBDriverException`, that could be raised by native calls, and `TypeDBDriverExceptionExt(TypeDBDriverException)`, that was raised by python driver methods. In order to catch all driver exceptions we had to use `TypeDBDriverException`, but this class couldn't be commented properly, because it is defined in the SWIG-generated file, that we wouldn't like to expose.

  Now all raised exceptions are `TypeDBDriverException`s, and this class is properly documented.


- **Make the C driver interface more human-friendly**
  We refactor the C interface to make it more human-friendly. We also implement an integration test which doubles as an example.

  No structures are exposed in the C headers. All operations are done using pointers to rust objects in the underlying rust layer. Thus memory management involves:
    * releasing pointers involving a remote resource (Connection, Session, Transaction & Database) using the corresponding `*_close` method.
    *  releasing pointers to local resources (e.g. iterators & concepts) using the corresponding `*_drop` method.


- **Reduce the number of jobs in CI for forks**

  This repository contains four (and counting!) implementations of the driver for different languages, each of which is fully tested in CI. That leads to an unsustainable number of jobs (~50) being spawned in Factory on each push to a fork, which has caused us significant issues during development as each hotfix had the potential to completely stall our progress.

  As the first step to reducing that cost, we introduce a small set of query tests (as those were the ones taking the most time) to be run frequently, whereas the full suite is relegated to be run on the main vaticle repository branches only.


## Other Improvements

- **Move CircleCI jobs for MacOS x86_64 drivers to run on mac arm64 machines**
  We reconfigure our CircleCI pipeline to use MacOS arm64 machines for running build & test jobs for our TypeDB MacOS x86_64 drivers. This is in light of [CircleCI sunsetting MacOS (intel) x86_64 resources in January 2024](https://discuss.circleci.com/t/macos-intel-support-deprecation-in-january-2024/48718).

- **Document driver entry points**

  We fix an issue in our auto-generated API documentation that failed to document the entry points to the driver: `coreDriver()` and `enterpriseDriver()`

  These exist in the root namespace of the driver. Documenting these required some changes to the parsers for each language, except Rust, with some work-arounds to allow specifying method names to document according to regex filters.

- **Remove stray factory dependency**

  We remove the dependency of a snapshot deployment job on a recently removed test job.
   
