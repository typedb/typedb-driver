# TypeDB Go Driver

### In progress:
The current state has managed to implement basic database features for the driver. 
A very simple example is provided in `db_creation_example.go` and a go_test target has been made to test this (needs
working on). 

The library has been successfully made, but there is an issue with getting the relative path from the bazel directory
so we have used the absolute path at the moment.

The Database can be created and deleted, and a variety of functions can be run with the generated dynamic library.
To expand on this, we need to implement the other functions, similar to the other drivers. 


### How to run (in current state):
To run, in `go/BUILD.bazel`, in the `clinkopts` of the go_library `driver-go`, set the -L linker path to your bazel
output. This is configured in the [dependencies](https://github.com/vaticle/dependencies) repo, and should be in `go/`.

You can then uncomment the go_library and go_binary in `go/BUILD.bazel` and run
```bash
bazel run go:driver-test
```

to run the binary, and to run the file `db_creation_example.go`.