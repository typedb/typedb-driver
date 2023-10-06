Documentation: https://typedb.com/docs/clients/2.x/clients

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/rust/rust-api-ref


```sh
cargo add typedb-driver@2.24.14
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
        <version>2.24.14</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/python/python-api-ref

Available through https://pypi.org

```
pip install typedb-driver==2.24.14
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/2.x/node-js/node-js-api-ref

```
npm install typedb-driver@2.24.14
```


## New Features


## Bugs Fixed
- **Set release compilation mode to optimized**
  
  We set the Bazel compilation mode for releases to `opt` to ensure that native-wrapped driver is maximally performant.
  

## Code Refactors


## Other Improvements
- **Remove spurious print lines from node client**

- **Update README.md**
  
- **New issue template: Language Driver Request**
  
  We added a new issue template for requesting a language to support.
  