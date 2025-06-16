Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.4.0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.4.0/a=noarch;xg=com.typedb/)
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
        <version>3.4.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.4.0
```


## New Features
- **Introduce file-based database export and import**
  Introduce interfaces to export databases into schema definition and data files and to import databases using these files. Database import supports files exported from both TypeDB 2.x and TypeDB 3.x.
  
  Both operations are blocking and may take a significant amount of time to execute for large databases. Use parallel connections to continue operating with the server and its other databases.
  
  Usage examples in Rust:
  ```rust
  // export
  let db = driver.databases().get(db_name).await.unwrap();
  db.export_to_file(schema_file_path, data_file_path).await.unwrap();
  
  // import
  let schema = read_to_string(schema_file_path).unwrap();
  driver.databases().import_from_file(db_name2, schema, data_file_path).await.unwrap();
  ```
  
  Usage examples in Python:
  ```py
  # export
  database = driver.databases.get(db_name)
  database.export_to_file(schema_file_path, data_file_path)
  
  # import
  with open(schema_file_path, 'r', encoding='utf-8') as f:
      schema = f.read()
  driver.databases.import_from_file(db_name2, schema, data_file_path)
  ```
  
  Usage examples in Java:
  ```java
  // export
  Database database = driver.databases().get(dbName);
  database.exportToFile(schemaFilePath, dataFilePath);
  
  // import
  String schema = Files.readString(Path.of(schemaFilePath));
  driver.databases().importFromFile(dbName2, schema, dataFilePath);
  ```
  
  

## Bugs Fixed
- **Handle "Unexpected response type for remote procedure call: Close" on query stream opening**
  Fix a rare `InternalError` returned by mistake when a client sends a query request while the transaction is being closed. Now, an expected "The transaction is closed and no further operation is allowed." error is returned instead.
  
  Additionally, wait for specific transaction responses in `rollback`, `commit`, and `query` to solidify the protocol and ensure that the server acts as expected.
  
  

## Code Refactors


## Other Improvements

- **Update zlib dependency**
  Support build on Apple Clang 17+ by updating dependencies (details: https://github.com/typedb/typedb-dependencies/pull/577). 
  
  
