Documentation: https://typedb.com/docs/clients/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/clients/rust-driver


```sh
cargo add typedb-driver@2.25.8
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
        <version>2.25.8</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/clients/python-driver

Available through https://pypi.org

```
pip install typedb-driver==2.25.8
```

### NodeJS driver

NPM package: https://www.npmjs.com/package/typedb-driver
Documentation: https://typedb.com/docs/clients/nodejs-driver

```
npm install typedb-driver@2.25.8
```

## API Changes

1. 'Fetch' attribute value type moves from the outer layer to the 'type' - see 'Code Refactors'
2. TypeDB Core sessions automatically and lazily reconnect on network failure or timeout on the server-side


## New Features
- **Session callbacks: on reopen, persistent on close; FFI bug fixes**
  
  **Session callbacks**
  - All drivers:
    - implement session reopen callbacks, executed when a session closed on the server side successfully reconnects;
    - core session now attempts to reconnect if it is closed on the remote server, in line with enterprise behaviour;
    - `Session::on_close()` callbacks are now executed each time the session closes (rather than just once);
  - NodeJS:
    - implement session and transaction callbacks (`onClose()`, `Session::onReopen()`);
  
  **Miscellaneous fixes**
  - Java, Python:
    - prevent exceptions in callbacks from crashing the native layer;
    - fix the issue where static root types could not be used with Concept APi;
    - reintroduce `ConceptMap.map()` to retrieve the full mapping;
  - Rust:
    - convert error messages from tuple enum variants to struct, allowing the fields to be named;
  - All drivers:
    -  fix the issue where session closed on remote server would not register automatically on the client side until a transaction open attempt.
  
  

## Bugs Fixed


## Code Refactors
- **Fetch value type**
  
  We update the expected output of TypeQL Fetch queries: attribute type serialization now includes its value_type.
  This change makes the output symmetric between raw values and attributes.
  
  Old output format:
  ```json
  {
      "attribute_type": { "label": "T", "root": "attribute" },
      "raw_value": { "value": "...", "value_type": "string" },
      "attribute": { "value": "...", "value_type": "string", "type": { "label": "T", "root": "attribute" } }
  }
  ```
  New output format:
  ```json
  {
      "attribute_type": { "label": "T", "value_type": "string", "root": "attribute" },
      "raw_value": { "value": "...", "value_type": "string" },
      "attribute": { "value": "...", "type": { "label": "T", "value_type": "string", "root": "attribute" } }
  }
  ```
  
  We also fix related JSON string serialization issue in which the hexadecimal escape sequences were not conformant to the JSON standard (`\u0000`).
  
  

## Other Improvements
- **Generate Rust documentation tabs and italics correctly**
  
  We generate documentation using the correct syntax for code examples, fixing errors in the generated Rust examples.
  
  
- **Fix documentation and for Trait Promise**

- **Update vaticle_dependencies with upgraded rules_rust**

- **Generate unique documentation anchors**
  
  Documentation generation now produces strictly unique anchors, using a combination of class/struct name, method name, and arguments signature as part of the anchor. This strategies means that all overloads and variations of method, for example in Java, now have uniquely referrable links.
  
  
- **Increase CircleCI windows machine sizes from medium to xlarge**

    
