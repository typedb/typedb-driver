Documentation: https://typedb.com/docs/clients/2.x/clients

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/rust/rust-api-ref


```sh
cargo add typedb-driver@2.24.8
```


### Java driver

Available through https://repo.vaticle.com
Documentation: https://typedb.com/docs/clients/2.x/java/java-api-ref

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
        <version>2.24.8</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/python/python-api-ref

Available through https://pypi.org

```
pip install typedb-driver==2.24.8
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/node-js/node-js-api-ref

```
npm install typedb-driver@2.24.8
```


## New Features


## Bugs Fixed


## Code Refactors


## Other Improvements
- **Update pest 2.4.0 => 2.7.4**
  
  We update to pest and pest-derive v2.7.4, which among other things purports to fix the error where [deriving Parser fails on "undeclared crate or module `alloc`"](https://github.com/pest-parser/pest/issues/899) (https://github.com/pest-parser/pest/pull/900).
  
  
- **Merge master into development**
  
  Synchronise changes for release into the development branch.
  
    
