Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.5
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.0.5/a=noarch;xg=com.typedb/)
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
        <version>3.0.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.0.5
```

## New Features
- **Introduce optional concepts to Concept Rows**

  `get()` and `get_index()` interfaces of Concept Rows always return optional Concepts (previously, the Java and Python drivers used to return non-optional `Concept` instances). If the requested variable name or index exists in the column names of a row, but the actual value for this variable is empty, an empty optional value is returned. This is a natural behavior for optionals (coming to TypeDB soon!) and is already useful for queries like (where the variable `$empty` won't have values):
  ```
  match not {$empty isa user;}; insert $u isa user, has username "Hi";
  ```
  
  If the requested variable name or index does not exist in the column names of a row, an error is returned.

  
  

## Bugs Fixed
- **Mark query answer accessors as allocating for SWIG to prevent memory leaks in the Python driver**
  
  We mark `query_answer_into_rows` and `query_answer_into_documents` as creating a new allocation that needs to be freed by SWIG. Previously, the iterators extracted from the `QueryAnswer` would have been ignored by SWIG and not deallocated when the wrapper is freed.
  
  

## Code Refactors

- **Cleanup driver errors**

  Query errors are dissolved in the Java and Python drivers.
  All the arguments passed to the external interfaces are validated to be non-null and of the correct format (e.g., non-negative for `concept_row.get_index(column_index)`).


## Other Improvements

- **Automate README examples updates**

  Cloud driver usage examples are added to all the available READMEs. Additionally, these examples are officially available in the repo as separate files.

  The process of README examples updating is automated by unifying all language updates in a single script. A new CI job is introduced to verify that the README examples are up to date.
