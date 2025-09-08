Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.5.0-rc0
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.5.0-rc0/a=noarch;xg=com.typedb/)
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
        <version>3.5.0-rc0</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.5.0-rc0
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/typedb-driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install typedb-driver-http@3.5.0-rc0
```

## New Features
- **Update the HTTP-TS driver with analyze endpoint response**
  Update the HTTP-TS driver with the response structure for the analyze endpoint

## Bugs Fixed
- **Fix database importer decoding error**
  Stabilize the database import function by eliminating rare decoding errors that could occur during the import of large datasets. All the exported files that the import function could not process are still valid and should be correctly imported after the proposed changes.

## Other Improvements
- **Fix links**

- **Update READMEs**
