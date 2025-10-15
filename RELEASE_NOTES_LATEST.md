Documentation: https://typedb.com/docs/core-concepts/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.5.5
```


### Java driver

Available through [https://repo.typedb.com](https://cloudsmith.io/~typedb/repos/public-release/packages/detail/maven/typedb-driver/3.5.5/a=noarch;xg=com.typedb/)
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
        <version>3.5.5</version>
    </dependency>
</dependencies>
```

### Python driver

PyPI package: https://pypi.org/project/typedb-driver
Documentation: https://typedb.com/docs/drivers/python/overview

Available through https://pypi.org

[//]: # (TODO: Python's RC/Alpha/Beta versions are formatted differently. Don't foget to update manually until we make an automation)
```
pip install typedb-driver==3.5.5
```

### HTTP Typescript driver

[//]: # (TODO: Update docs link)

NPM package: https://www.npmjs.com/package/typedb-driver-http
Documentation: https://typedb.com/docs/drivers/

```
npm install typedb-driver-http@3.5.5
```


## New Features


## Bugs Fixed
- **Fix transaction on_close and Java and Python block on close()**
  
  We notice that calling `transaction.close()` does not wait until the server has freed up resource. This makes quick sequences, such as tests where transactions open and are followed by database deletes, unreliable. Further investigation that workarounds using the existing `on_close` callbacks in Python and Java caused segfaults. We fix both:
  
  1) `Transaction.close()` in Python and Java now blocks for 1 round trip. In Rust, this now returns a promise/future. In Java/Python, we pick the most relevant default and resolve the promise from Java/Python.
  2) We fix segfaults that occur when the Rust driver calls into Python/Java once the user attaches `.on_close` callbacks to transactions. 
  
  We also fix nondeterministic errors:
  1) adding `on_close` callbacks must return a promise, since the implementation injects the callback into our lowest-level listener loop which may register the callback later. Not awaiting the `on_close()` registration will lead to hit or miss execution of the callback when registering on_close callbacks, not awaiting, and then closing the transaction immediately
  2) we add `keepalive` to the channel, without which messages sometimes get "stuck" on the client-side receiving end of responses from the server. No further clues found as to why this happens. See comments for more detail.
  
  We also add one major feature enhancement: configurable logging. All logging should now go through the `tracing` crate. We can configure logging levels for just the driver library with the `TYPEDB_DRIVER_LOG` or general `RUST_LOG` environment variables. By default we set it to `info`.
  
  

## Code Refactors


## Other Improvements
- **Fix Config**

- **Trigger ci**

- **Try to fix snapshot tests python 3.9**

- **Update dependencies after servers relation index fix for CI tests**
  Ensure drivers run new BDD tests for migration written with the core's relation index fix https://github.com/typedb/typedb/pull/7594
  
  
- **Correct link in README**
  
  Correct a link in the README that linked to `nodejs` instead of `http-ts`
  
  
- **HTTP/TS: Don't enforce Node 22 for installing the package**

- **HTTP/TS: Fix response type checks throwing on certain inputs**
  
  In the HTTP/TS driver, `isApiErrorResponse` and `isOkResponse` should never throw errors anymore for any input.
  
  
    
