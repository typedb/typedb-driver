Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.25.7
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
        <version>2.25.7</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.25.7
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@2.25.7
```


## New Features

- **Add untyped value getter for Java values and fix Python type hints**

  We add a simple untyped API to Java's `Value` concepts, which return the value inside of the Value regardless of its type (double/string/etc.). This value is returned as an Object, and useful for equality checks, printing, etc. Additionally, the same API exists in Python and Node already.

  We also fix the Python hints for setting the name of a Type, which was incorrectly hinting the type 'Label' when it should have been a simple string.

## Bugs Fixed
  

## Code Refactors


## Other Improvements
- **Optimise CI times by retaining server between Java BDD scenarios**
  
  We optimise Java CI time by not shutting down the TypeDB server between scenarios. Instead, we delete the existing databases each test, which is much faster. 
  
  During this work, we also discovered some sub-par UX in terms of error messages thrown, and missing BDD steps that needed to be implemented.
  
  
- **Increase ulimits on unix CircleCI machines**

- **Update README links to docs**

- **Simplify Github PR and issue templates**

