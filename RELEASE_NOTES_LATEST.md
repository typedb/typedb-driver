Documentation: https://typedb.com/docs/drivers/overview

## Distribution

#### Rust driver

Available from https://crates.io/crates/typedb-driver
Documentation: https://typedb.com/docs/drivers/rust/overview


```sh
cargo add typedb-driver@3.0.0-alpha-4
```


### Java driver

Available through https://repo.typedb.com
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
        <version>3.0.0-alpha-4</version>
    </dependency>
</dependencies>
```

## New Features
- **Introduce 3.0 Java driver**
  We introduce the updated Java driver for the upcoming 3.0 release, featuring all the Rust driver's features in another language.
  Learn more about TypeDB 3.0 incoming features here: https://typedb.com/blog/typedb-3-roadmap


## Bugs Fixed
- **Rust driver refinements**
  We fix major issues:

  1. correctly passing the driver version string into the driver via the build system, instead of hard-coding it into the sources. This use a Cargo environment variable, which will always be available in released versions and is provided from the crate's Cargo.toml. During development, we just set the version to `0.0.0` because we don't particularly care about it!
  2. correctly request more answers from the query stream once a BatchContinue flag has been read by the user, as they consume the query answer stream. Previously, we immediately request more answers from the server as soon as we see the StreamContinue signal, in the network layer, which meant the whole stream was actually not lazy at all!

- **Fix decimal, datetime, datetime-tz value types processing**
  

## Code Refactors
- **Rename Java package com.vaticle.typedb to com.typedb and remove typeql dependencies.**
  We rename the Java driver's package from `com.vaticle.typedb` to `com.typedb`.
  We remove excessive dependencies on `typeql`.


## Other Improvements
    
