# TypeDB Rust Driver

[![Factory](https://factory.vaticle.com/api/status/vaticle/typedb-driver/badge.svg)](https://factory.vaticle.com/vaticle/typedb-driver)
[![Discord](https://img.shields.io/discord/665254494820368395?color=7389D8&label=chat&logo=discord&logoColor=ffffff)](https://vaticle.com/discord)
[![Discussion Forum](https://img.shields.io/discourse/https/typedb.com/forum/topics.svg)](https://typedb.com/forum)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typedb-796de3.svg)](https://stackoverflow.com/questions/tagged/typedb)
[![Stack Overflow](https://img.shields.io/badge/stackoverflow-typeql-3dce8c.svg)](https://stackoverflow.com/questions/tagged/typeql)

## Driver Architecture
To learn about the mechanism that a TypeDB Driver uses to set up communication with databases running on the TypeDB Server, refer to the [Clients Overview](https://typedb.com/docs/clients/2.x/clients).

The TypeDB Driver for Rust provides a fully async API that supports multiple async runtimes or a synchronous interface gated by the `sync` feature.

## API Reference
To learn about the methods available for executing queries and retrieving their answers using Driver Rust, refer to the [API Reference](https://typedb.com/docs/clients/2.x/rust/rust-api-ref).

## Quickstart
1. Import `typedb-driver` through Cargo:
```bash
cargo add typedb-driver
```
2. Make sure the [TypeDB Server](https://docs.vaticle.com/docs/running-typedb/install-and-run#start-the-typedb-server) is running.
3. See `rust/tests/integration` for examples of usage.

## Build from Source
> Note: You don't need to compile TypeDB Driver from source if you just want to use it in your code. See the _"Quickstart"_ section above.

1. Make sure you have [Bazel](https://docs.bazel.build/versions/master/install.html) installed on your machine.

2. Build the library:

   a) to build the native/raw rlib:
   ```
   bazel build //rust:typedb_driver
   ```
   The rlib will be produced at: `bazel-bin/libtypedb_driver-{hash}.rlib`.

   b) to build the crate for a Cargo project:
   ```
   bazel build //rust:assemble_crate
   ```
   The Cargo crate will be produced at:
   ```
   bazel-bin/assemble_crate.crate
   ```
   You can then unzip this crate to retrieve `Cargo.toml`. **Please note**: this process has not yet been thoroughly tested. The generated `Cargo.toml` may not be fully correct. See the `Cargo.toml` of the `typedb-driver` crate for reference.
