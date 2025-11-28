Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.7.0-rc4
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.7.0-rc4/a=noarch;xg=com.typedb/)
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
        <version>3.7.0-rc4</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.7.0-rc4
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install @typedb/driver-http@3.7.0-rc4
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.7.0-rc4


## New Features


## Bugs Fixed
- **Fix python Value equality**
  Fixes the implementation of `__eq__` in `_Value` to use `try_get_value_type` instead of the non-existent `get_value_type`
  
  

## Code Refactors


## Other Improvements
- **Align HTTP driver analyze type names with rust**
  Aligns HTTP driver analyze type names with rust. Any code using the driver will break. Major changes are:
  * QueryConstraint* is renamed to Constraint*
  * QueryVertex* is renamed to ConstraintVertex*
  
  The older names are preserved in `legacy.ts` but not exported to make updating to the new names easier.
  TypeDB 3.7.0 introduces a few minor breaking changes in the HTTP API. 
  * Expression constraints now return a single variable instead of a list of variables.
  * The structure returned with a ConceptRowResponse has the `blocks` field renamed to `conjunctions`
  For applications which must operate with both versions before and after 3.7.0, certain types are exported with 'Legacy' as suffix.

