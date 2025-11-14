Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.7.0-rc3
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.7.0-rc3/a=noarch;xg=com.typedb/)
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
        <version>3.7.0-rc3</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.7.0rc3
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/@typedb/driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install @typedb/driver-http@3.7.0-rc3
```

### C driver

Compiled distributions comprising headers and shared libraries available at: https://cloudsmith.io/~typedb/repos/public-release/packages/?q=name:^typedb-driver-clib+version:3.7.0-rc3


## New Features
- **Implement GRPC protocol version extensions**
  We introduce the "extension" field into the protocol. This introduces a finer notion of "compatibility" and makes the protocol aware of it. A driver-server pair is compatible if they are on the same protocol version, and the server extension version is atleast that of the client.

- **Implement analyze endpoint in GRPC**
  Merges the implementation of analyze endpoints in all GRPC drivers, as well as aligning the HTTP response format with that used by GRPC. We also introduce an optional query structure into the GRPC response for pipelines without fetch.


## Bugs Fixed
- **Fix how we return involved conjunctions**
  Fix a bug where we used the wrong index when decoding involved_conjunctions.


- **Use naiive date in Python**

  Python `Date` objects were timezone-aware, which means that it was possible to insert, for example `2010-10-10` (recorded on the server-side in UTC :00-00-00), and read it back as `2010-10-09` when in a negative timezone relative to UTC!

  We now parse the date received from the server naiively as a datetime, using UTC as the set point, and extract the naiive date from there.




## Code Refactors
- **Re-export native Variable, ConjunctionID from typedb.analyze in python**
  Re-export variable from typedb.analyze in python



## Other Improvements
- **Fix server flag in Windows assembly tests**

- **Fix HTTP/TS driver trying to deploy as a private package**

  Fix a bug where the HTTP/TS driver was trying to deploy as a private package

- **Fix build; Build C driver in factory CI**
  Build C driver in factory CI



- **Make HTTP/TS driver use the "@typedb" org in NPM registry**

  The HTTP/TS driver has been moved - it was previously `typedb-driver-http`; now it is `@typedb/driver-http`.


- **Update c driver tests to 3.x and enable deployment**
  Update c driver tests to 3.x and enable deployment


