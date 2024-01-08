Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.26.2
```


### Java driver

Available through https://repo.vaticle.com
Documentation: https://typedb.com/docs/clients/java-driver

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
        <version>2.26.2</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.26.2
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@
```

### C++ driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.2

### C driver

Compiled distributions comprising headers and shared libraries available at: https://github.com/vaticle/typedb-driver/releases/tag/2.26.2



## New Features


## Bugs Fixed
- **Fix fetch sub-query aggregation null pointer**
  
  We fix a bug where a Fetch query with a Get-Aggregate subquery that returned an empty (ie. undefined) answer throw a null pointer exception.
  
  For example this used to incorrectly throw an exception if 'Alice' doesn't have any salaries in the database, since a 'sum' is undefined for 0 entries.
  ```
  match
  $x isa person, has name $n; $n == "Alice";
  fetch
  $n as "name";
  total-salary: {
    match $x has salary $s;
    get $s; 
    sum $s;
  };
  ```
  
  We now correctly return the following JSON structure
  ```
  [
    {
      "name": {"value": "Alice",  "type":  {"label": "name", "root": "attribute", "value_type": "string"}},
      "total-salary": null
    }
  ]
  ```
  
  
- **Update to tonic 1.28**
  
  We fix a bug in the installation of the latest typedb-driver Rust by upgrading `tonic` to version 1.28.
  
  

## Code Refactors
- **Release with Ubuntu 18.04 in to lower GLIBC requirement to 2.27.0**
  
  We downgrade from Ubuntu 20.04 to 18.04 in CircleCI assembly and deployment jobs, using a Docker image. This lowers the minimum supported glibc version from 2.31 to 2.27.

- **Rename typedb.hpp to typedb_driver.hpp**
  

## Other Improvements
- **C++ driver documentation and add missing APIs**
  Documents the C++ code & adds ascii docs generated via doxygen. Adds a few methods to the classes which were missing.
  
- **Update python credential documentation**

- **Update C and CPP entry in README.md**

- **Update C++ readme with new 'cloud' terminology**

  