# TypeDB Go Driver (WIP)

## Current state

`TypeDB Go Driver` is not implemented yet. This repository contains proofs of concept, including driver's library build
phase and basic database manipulation functionality. A usage example could be found
in [driver_proof_of_concept.go](driver_proof_of_concept.go) (database creation and deletion).

### Issues

- [BUILD](BUILD) expects manual actions to build on a specific machine and is not fully automated yet (more details
  below and inside the file)
- Only basic connection and database manipulation features are implemented and not tested
- There is no resource management, exceptions handling and some of the needed Rust structs wrappers on the SWIG side

### Run

- Run `bazel build //go:libtypedb_driver_go_native`
- Uncomment `driver-go` (and `driver-temp-exe`) targets in [BUILD](BUILD)
- Specify your local path to the `typedb_driver_go_native` dynamic library instead
  of `$TYPEDB_DRIVER_PATH/typedb-driver/bazel-bin/go`
- Run `bazel build //go:driver-go` (or `bazel run //go:driver-temp-exe`). Make sure that you have a `TypeDB` server
  running to connect to it

```bash
bazel build //go:libtypedb_driver_go_native
bazel build //go:driver-go
```
