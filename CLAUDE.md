# TypeDB Driver Repository

Multi-language client drivers for TypeDB.

## Git Configuration

- **Base branch**: `master`

## Driver Overview

| Driver | Architecture | Status |
|--------|--------------|--------|
| **Rust** | Core implementation (pure Rust) | 3.x supported |
| **C** | FFI wrapper around Rust core | 3.x supported |
| **Python** | SWIG bindings → C FFI → Rust core | 3.x supported |
| **Java** | JNI/SWIG bindings → C FFI → Rust core | 3.x supported |
| **TypeScript HTTP** | **Standalone** (pure TypeScript, HTTP API) | 3.x supported |
| **C++** | Wrapper around Rust core | 2.x only |
| **C#** | SWIG/P/Invoke → Rust core | 2.x only |
| **Node.js** | gRPC driver (Rust-based) | 2.x only |
| **Go** | Proof of concept | Not production-ready |

## Repository Structure

| Directory | Description |
|-----------|-------------|
| `rust/` | Core Rust driver (async + blocking APIs) |
| `c/` | C FFI layer wrapping Rust core |
| `python/` | Python driver (via SWIG/C FFI) |
| `java/` | Java driver (via JNI/C FFI) |
| `http-ts/` | TypeScript HTTP driver (**standalone, no FFI**) |
| `cpp/` | C++ driver (2.x only) |
| `csharp/` | C# driver (2.x only) |
| `nodejs/` | Node.js driver (2.x only) |
| `go/` | Go driver (proof of concept) |
| `docs/` | API documentation partials (AsciiDoc) |
| `tool/` | Development and release tooling |

## Architecture

### FFI-based drivers (Rust core)

Most drivers share a common Rust core via FFI:

```
Python/Java applications
         ↓
    Language bindings (SWIG/JNI)
         ↓
    C FFI layer (c/)
         ↓
    Rust core (rust/)
         ↓
    gRPC to TypeDB server
```

- **Rust core** (`rust/`): Implements all driver logic, connection management, query execution
- **C layer** (`c/`): Exposes Rust functionality via `extern "C"` functions
- **Python**: SWIG generates Python bindings that call C FFI
- **Java**: SWIG generates JNI code that calls C FFI

### Standalone drivers (no FFI)

The **TypeScript HTTP driver** (`http-ts/`) is a completely independent implementation:

```
TypeScript/JavaScript applications
         ↓
    Pure TypeScript driver (http-ts/)
         ↓
    HTTP API to TypeDB server
```

- Does **not** use the Rust core or C FFI layer
- Uses TypeDB's HTTP API instead of gRPC
- No native code dependencies - runs in any JavaScript environment
- Suitable for browsers and Node.js

Each language driver directory contains its own `README.md` with language-specific architecture details, memory management notes, and usage examples. Read the relevant README before making changes to a driver package.

## Build Commands

```bash
# Rust driver
bazel build //rust:typedb_driver

# Python driver (specify Python version: 39, 310, 311, 312, 313)
bazel build //python:assemble-pip311

# Java driver
bazel build //java:assemble-maven

# C driver
bazel build //c:typedb_driver_clib

# TypeScript HTTP driver (standalone build)
cd http-ts && pnpm install && pnpm build
```

## Testing

```bash
# Start test server first
tool/test/start-community-server.sh

# Run Rust tests
bazel test //rust/tests/behaviour/driver:test_query

# Run Python tests
bazel test //python/tests/behaviour/driver:test_query

# Run Java tests
bazel test //java/tests/behaviour/driver:test_query
```

BDD tests use Gherkin specifications from the `typedb-behaviour` repository.

## Key Files

| File | Purpose |
|------|---------|
| `rust/src/driver.rs` | Main TypeDBDriver struct and connection logic |
| `rust/src/transaction.rs` | Transaction management |
| `c/src/lib.rs` | C FFI entry points and logging initialization |
| `c/swig/typedb_driver_python.swg` | SWIG bindings for Python |
| `c/swig/typedb_driver_java.swg` | SWIG/JNI bindings for Java |
| `http-ts/src/index.ts` | TypeScript HTTP driver entry point |

## Logging

Driver logging is controlled via environment variables:

- `TYPEDB_DRIVER_LOG`: Fine-grained control using the same syntax as `RUST_LOG`.
  Example: `TYPEDB_DRIVER_LOG=typedb_driver=debug,typedb_driver_clib=trace`

- `TYPEDB_DRIVER_LOG_LEVEL`: Simple log level (`debug`, `info`, `warn`, `error`, `trace`) that
  applies to both `typedb_driver` and `typedb_driver_clib` crates. Overrides `TYPEDB_DRIVER_LOG`.
  Example: `TYPEDB_DRIVER_LOG_LEVEL=debug`

If neither is set, the default level is INFO.

For FFI-based drivers (Python/Java/C), call `init_logging()` to enable logging output.

**Note**: The TypeScript HTTP driver has its own logging mechanism independent of the Rust logging system.

## Code Conventions

- License: Apache 2.0
- Rust edition: 2021
- Line width: 120 characters
- Error handling: Structured error codes with `typedb_error!` macro

## Before Pushing

Run these commands before pushing changes:

```bash
# Format Rust code
bazel run @rules_rust//:rustfmt --@rules_rust//:rustfmt.toml=//rust:rustfmt.toml

# Regenerate API documentation (if API changes were made)
tool/docs/update.sh
```
