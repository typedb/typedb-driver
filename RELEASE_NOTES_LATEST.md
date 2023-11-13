Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.25.5
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
        <version>2.25.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.25.5
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@2.25.5
```


## New Features
- **Node driver package automatic version stamping**
  
  We leverage Bazel's built-in workspace status and stamping capabilities to ensure that the version of TypeDB Protocol depended on by the node package doesn't go out of sync with the bazel dependency. 
  
  To that end, we also add a snapshot deployment test for the node driver, and fix a bug in process of opening a connection to TypeDB Core.
  
  

## Bugs Fixed


## Code Refactors


## Other Improvements

- **Add linker dependencies for windows**
