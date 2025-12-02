Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/reference/typedb-grpc-drivers/rust/


```sh
cargo add typedb-driver@3.7.0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.7.0/a=noarch;xg=com.typedb/)
Documentation: https://typedb.com/docs/reference/typedb-grpc-drivers/java/

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
        <version>3.7.0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/reference/typedb-grpc-drivers/python/

Available through https://pypi.org

```
pip install typedb-driver==3.7.0
```

### HTTP Typescript driver

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/reference/typedb-http-drivers/typescript/

```
npm install @typedb/driver-http@3.7.0
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.7.0


## New Features
- **Implement GRPC protocol version extensions**
  We introduce the "extension" field into the protocol. This introduces a finer notion of "compatibility" and makes the protocol aware of it. A driver-server pair is compatible if they are on the same protocol version, and the server extension version is atleast that of the client.


- **Implement analyze endpoint in GRPC**
  Implements analyze endpoints in all GRPC drivers, as well as aligning the HTTP response format with that used by GRPC.
  Analyze allows queries to be parsed & type-checked without executing them against the data. 
  We also introduce an optional query structure into the GRPC response for query pipelines.
  


## Bugs Fixed
- **Fix python Value equality**
  Fixes the implementation of `__eq__` in `_Value` to use `try_get_value_type` instead of the non-existent `get_value_type`
  
  
- **Fix how we return involved conjunctions**
  Fix a bug where we used the wrong index when decoding involved_conjunctions.
  
  
- **Use naiive date in Python**
  
  Python `Date` objects were timezone-aware, which means that it was possible to insert, for example `2010-10-10` (recorded on the server-side in UTC :00-00-00), and read it back as `2010-10-09` when in a negative timezone relative to UTC!
  
  We now parse the date received from the server naiively as a datetime, using UTC as the set point, and extract the naiive date from there.

- **Fix HTTP/TS driver trying to deploy as a private package**

  Fix a bug where the HTTP/TS driver was trying to deploy as a private package




## Code Refactors
- **Introduce hash, equality, formatting methods for certain analyze types**
  **Breaking changes from the earlier release candidates (till 3.7.0-rc3)**
  * Refactors the analyze types to introduce Variable and ConjunctionID wrappers. All signatures using the jni types directly change.
  * Refactors the ConstraintVertex types and underlying methods.
  
  Added features:
  * `ConstraintVertex` now implements equality, hash and formatting.
  * `Constraint` can also be formatted.
  * Introduces `NamedRole` across the FFI. 
  * Introduces `ConjunctionID`, `Variable` classes instead of reusing the SWIG generated types directly
    - This is needed to implement equality & hash
  
  
- **Re-export native Variable, ConjunctionID from typedb.analyze in python**
  Re-export variable from typedb.analyze in python



- **Align HTTP driver analyze type names with rust**
  Aligns HTTP driver analyze type names with rust. Any code using the driver will break. Major changes are:
  * QueryConstraint* is renamed to Constraint*
  * QueryVertex* is renamed to ConstraintVertex*

  The older names are preserved in `legacy.ts` but not exported to make updating to the new names easier.
  TypeDB 3.7.0 introduces a few minor breaking changes in the HTTP API.
  * Expression constraints now return a single variable instead of a list of variables.
  * The structure returned with a ConceptRowResponse has the `blocks` field renamed to `conjunctions`
    For applications which must operate with both versions before and after 3.7.0, certain types are exported with 'Legacy' as suffix.


## Other Improvements

- **Make HTTP/TS driver use the "@typedb" org in NPM registry**

  The HTTP/TS driver has been moved - it was previously `typedb-driver-http`; now it is `@typedb/driver-http`.


- **Prepare release 3.7.0-rc4**
  Update release notes & bump version to 3.7.0-rc4
  
  
- **Accept include_query_structure in python query option constructor**
  Adds the `include_query_structure` flag as a keyword-argument to the python QueryOption constructor.
  
  
- **Add isLegacyResponse method to http driver**
  Adds a `isLegacyResponse` method to the http-ts driver to help differentiate between responses returned by servers versioned < 3.7.0 and those versioned >= 3.7.0
  
  
- **Fix wrong array of union type**
  Fix wrong array of union type in legacy types.
  
- **Fix server flag in Windows assembly tests**

- **Update c driver tests to 3.x and enable deployment**
  Update c driver tests to 3.x and enable deployment

