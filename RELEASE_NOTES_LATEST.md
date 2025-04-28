Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.2.0-rc2
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.2.0-rc2/a=noarch;xg=com.typedb/)
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
        <version>3.2.0-rc2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

```
pip install typedb-driver==3.2.0rc2
```

## New Features
- **Introduce transaction and query options**
  **Introduce transaction options:**
  * `transaction_timeout`: If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions,
  * `schema_lock_acquire_timeout`: If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
  
  Rust examples:
  ```rust
  let options = TransactionOptions::new().transaction_timeout(Duration::from_secs(10));
  let transaction =
      driver.transaction_with_options(database.name(), TransactionType::Schema, options).await.unwrap();
  ```
  
  Python example:
  ```py
  options = TransactionOptions(transaction_timeout_millis=10_000)
  tx = driver.transaction(database.name, TransactionType.SCHEMA, options)
  ```
  
  Java example:
  ```java
  TransactionOptions transactionOptions = new TransactionOptions().transactionTimeoutMillis(10_000);
  Transaction transaction = driver.transaction(database.name(), Transaction.Type.SCHEMA, transactionOptions);
  ```
  
  **Introduce query options:**
  * `include_instance_types`: If set, specifies if types should be included in instance structs returned in ConceptRow answers,
  * `prefetch_size`: If set, specifies the number of extra query responses sent before the client side has to re-request more responses. Increasing this may increase performance for queries with a huge number of answers, as it can reduce the number of network round-trips at the cost of more resources on the server side.
  
  Rust examples:
  ```rust
  let options = QueryOptions::new().include_instance_types(true);
  let answer = transaction.query_with_options("match $x isa person;", options).await.unwrap();
  ```
  
  Python example:
  ```py
  options = QueryOptions(include_instance_types=True)
  answer = tx.query("match $x isa person;").resolve()
  ```
  
  Java example:
  ```java
  QueryOptions queryOptions = new QueryOptions().includeInstanceTypes(true);                
  QueryAnswer matchAnswer = transaction.query("match $x isa person;", queryOptions).resolve();                
  ```
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements

    
