# TypeDB Driver Bzlmod Migration Status

## Overview

Migration of `typedb-driver` from WORKSPACE-based Bazel 6.2.0 to Bzlmod with Bazel 8.0.0.

## Migration Scope

**In scope (3.x drivers):**
- Rust core driver
- C FFI layer
- Python driver (multi-version 3.9-3.13)
- Java driver
- TypeScript HTTP driver

**Excluded from default build:**
- C# (`csharp/`) - 2.x only, `rules_dotnet` has limited Bzlmod support
- Node.js gRPC (`nodejs/`) - 2.x only, deprecated
- C++ (`cpp/`) - 2.x only
- Go (`go/`) - POC only, lower priority

## Build Status

| Component | Target | Status | Notes |
|-----------|--------|--------|-------|
| Rust | `//rust:typedb_driver` | ✅ SUCCESS | Core library builds |
| C FFI | `//c:typedb_driver_clib` | ✅ SUCCESS | Static library builds |
| C Headers | `//c:typedb_driver_clib_headers` | ✅ SUCCESS | cbindgen generates headers |
| Java | `//java:driver-java` | ✅ SUCCESS | SWIG bindings build |
| Python | `//python:driver_python311` | ✅ SUCCESS | All Python versions build |
| TypeScript HTTP | `//http-ts:driver-lib` | ⚠️ PARTIAL | Library builds, npm assembly has versioning issue |

## Files Modified

| File | Change |
|------|--------|
| `.bazelversion` | `6.2.0` → `8.0.0` |
| `.bazelrc` | Added `--noincompatible_disallow_empty_glob` workaround |
| `MODULE.bazel` | **Created** - full Bzlmod configuration |
| `WORKSPACE` | Emptied (kept workspace name for backward compat) |
| `python/python_versions.bzl` | Updated repo refs `@python39` → `@python_3_9` etc. |

### BUILD File Fixes - Load Paths

Fixed invalid load paths for bazel-distribution docs (subpackage structure):

| File | Before | After |
|------|--------|-------|
| `c/BUILD` | `//docs:doxygen/rules.bzl` | `//docs/doxygen:rules.bzl` |
| `python/BUILD` | `//docs:python/rules.bzl` | `//docs/python:rules.bzl` |
| `cpp/BUILD` | `//docs:doxygen/rules.bzl` | `//docs/doxygen:rules.bzl` |
| `csharp/BUILD` | `//docs:doxygen/rules.bzl` | `//docs/doxygen:rules.bzl` |

### BUILD File Fixes - Empty Globs

Added `allow_empty = True` to glob patterns that may not match files:

| File | Pattern |
|------|---------|
| `c/BUILD` | `docs/**/*.adoc` |
| `java/BUILD` | `docs/**/*.adoc`, `README.md`, `TypeDBExample.java` |
| `java/connection/BUILD` | `*/*.java` |
| `java/answer/BUILD` | `*/*.java` |
| `java/analyze/BUILD` | `*/*` |
| `java/user/BUILD` | `*/*` |
| `python/BUILD` | checkstyle include/exclude patterns |
| `rust/BUILD` | `docs/**/*.adoc`, `**/Cargo.*` |
| `rust/tests/BUILD` | `**/Cargo.*` |
| `rust/tests/behaviour/driver/BUILD` | `Cargo.*` |
| `rust/tests/behaviour/connection/BUILD` | `Cargo.*` |
| `http-ts/BUILD` | `docs/**/*`, `**/*.md` |
| `http-ts/tests/BUILD` | `**/*.md` |

## Dependencies Repository Fixes

The following fixes were made to `typedb_dependencies` repo to support Bzlmod:

### Rust Cargo Project Aspect (`builder/rust/cargo/project_aspect.bzl`)

1. **Bzlmod label handling**: Updated `_should_generate_cargo_project()` to handle `@@//` canonical labels
2. **Crate universe detection**: Updated `_is_universe_crate()` to detect Bzlmod-style crate labels (`crate+crates__`)
3. **Crate name extraction**: Fixed to use `target.label.name` instead of string parsing, with hyphen/underscore conversion

### SWIG Rules (`builder/swig/java.bzl`, `builder/swig/python.bzl`)

1. **SWIG_LIB environment**: Added `SWIG_LIB` environment variable to override compiled-in path
2. **Template files**: Added `_swig_templates` attribute for library file access

## Verification Commands

Verification should be done by a subagent, building each language separately with the CI configuration:

```bash
cd /opt/project/repositories/typedb-driver

bazel build --config=ci //c/...
bazel build --config=ci //rust/...
bazel build --config=ci //java/...
bazel build --config=ci //python/...
bazel build --config=ci //http-ts/...
```

## MODULE.bazel Configuration

Key components configured:

1. **BCR Dependencies:**
   - rules_rust 0.56.0
   - rules_python 1.0.0
   - rules_jvm_external 6.6
   - rules_nodejs 6.3.2
   - aspect_rules_js 2.1.2
   - aspect_rules_ts 3.4.0
   - googletest 1.14.0 (repo_name = "gtest")

2. **TypeDB Internal Dependencies** (via `local_path_override`):
   - `typedb_dependencies` (../dependencies)
   - `typedb_bazel_distribution` (../bazel-distribution)
   - `typedb_protocol` (../typedb-protocol)
   - `typedb_behaviour` (../typedb-behaviour)

3. **Python Multi-Version Toolchains:**
   - Python 3.9 (default)
   - Python 3.10, 3.11, 3.12, 3.13

4. **Rust Toolchain:**
   - Edition 2021
   - Version 1.81.0
   - Extra targets: aarch64-apple-darwin, aarch64-unknown-linux-gnu, x86_64-apple-darwin, x86_64-pc-windows-msvc, x86_64-unknown-linux-gnu

5. **Node.js/NPM:**
   - Node.js 22.11.0
   - NPM repos: `nodejs_npm`, `http-ts_npm`
   - `typedb_protocol_npm` inherited from typedb_protocol module

6. **External Archives:**
   - `google_bazel_common` for javadoc generation

## Known Issues and Resolutions

### Issue 1: `node_repositories` attribute not supported
**Error:** `unknown attribute node_repositories provided`
**Resolution:** Removed `node_repositories` from `node.toolchain()` - custom repositories are not supported in Bzlmod extension API.

### Issue 2: Duplicate `typedb_protocol_npm` repository
**Error:** `A repo named typedb_protocol_npm is already generated by this module extension`
**Resolution:** Removed duplicate definition from MODULE.bazel. The repo is inherited from `typedb_protocol` module.

### Issue 3: Missing `@google_bazel_common` repository
**Error:** `No repository visible as '@google_bazel_common'`
**Resolution:** Added `http_archive` for `google_bazel_common` in MODULE.bazel.

### Issue 4: Invalid load paths for bazel-distribution docs
**Error:** `Label '//docs:doxygen/rules.bzl' is invalid because '//docs/doxygen' is a subpackage`
**Resolution:** Updated load paths in BUILD files (see table above).

### Issue 5: Empty glob patterns (Bazel 8 default)
**Warning:** Various glob patterns don't match any files
**Status:** Added `--noincompatible_disallow_empty_glob` to `.bazelrc` as a workaround.
**TODO:** Fix all BUILD files to use `allow_empty = True` explicitly, then remove the flag.

### Issue 6: Bzlmod canonical labels in cargo project aspect
**Error:** `'//c:typedb_driver_clib' does not have mandatory providers: 'CargoProjectInfo'`
**Resolution:** Updated `_should_generate_cargo_project()` in dependencies repo to handle `@@//` labels.

### Issue 7: Crate name extraction for Bzlmod
**Error:** `invalid character '@' in package name`
**Resolution:** Updated crate name extraction to use `target.label.name` with hyphen conversion.

### Issue 8: SWIG library path not found
**Error:** `Unable to find 'java.swg'`
**Resolution:** Added `SWIG_LIB` environment variable in SWIG rules to override compiled-in path.

### Issue 9: Node.js version not available for linux_arm64
**Error:** `No nodejs is available for linux_arm64 at version 22.16.0`
**Resolution:** Changed to Node.js 22.11.0 (LTS with linux_arm64 support).

### Issue 10: TypeScript npm assembly versioning
**Error:** `Cannot index array with string "STABLE_VERSION"`
**Status:** Package.json uses workspace status variables for versioning. This is not a Bzlmod issue.
**TODO:** Fix versioning in http-ts/BUILD or package.json.

### Issue 11: Java/C tests require WORKSPACE artifacts
**Error:** `No repository visible as '@typedb_artifact_linux-arm64'`
**Status:** Tests that require TypeDB server artifacts still use WORKSPACE-based `@typedb_artifact_*` repos.
**TODO:** Migrate test artifact repos to Bzlmod or exclude from default build.

## Environment Requirements

- Bazel 8.0.0 (via Bazelisk)
- System Java (OpenJDK 11+) for Maven resolution
- System GCC/Clang for native compilation
- pnpm for TypeScript driver development

## Next Steps

1. ✅ Core driver builds verified (Rust, C, Java, Python)
2. 🔲 Fix TypeScript npm assembly versioning issue
3. 🔲 Migrate test artifact repositories to Bzlmod
4. 🔲 Remove `--noincompatible_disallow_empty_glob` workaround after fixing all globs
5. 🔲 Test driver functionality with a running TypeDB server
