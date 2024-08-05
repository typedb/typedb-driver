# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@io_bazel_rules_go//go:def.bzl", "go_binary", "go_library", "go_test")
load("//go:rules.bzl", "swig_native_go_library")

# TODO: Building this target doesn't put the dynamic library (libtypedb_driver_go_native.dylib for MacOS) into
# bazel-bin/go, while it's needed for the clinkopts below.
swig_native_go_library(
    name = "typedb_driver_go_native",
    lib = "//c:typedb_driver_clib_headers",
    package_name= "typedb_driver",
    interface = "//c:typedb_driver.i",
    includes = ["//c:swig/typedb_driver_go.swg"],
    enable_cxx = True,
    visibility = ["//visibility:public"],
)

# TODO: Cannot be built in CI because of the absolute path used in clinkopts.
# For now, requires manually running `bazel build //go:libtypedb_driver_go_native` before this building this target.
#go_library(
#    name = "driver-go",
#    srcs = glob(["*.go"]),
#    data = ["//:LICENSE"],
#    importpath = "typedb_driver/go",
#    clinkopts = [
#        "-L$TYPEDB_DRIVER_PATH/typedb-driver/bazel-bin/go", # TODO: Use relative path instead of the full path (couldn't make working for now)
#        "-ltypedb_driver_go_native",
#        "-framework", "CoreFoundation"
#    ],
#    deps = [
#        "//go:typedb_driver_go_native",
#        "//go/api/user:user",
#        "//go/api/database:database",
#        "//go/connection:connection",
#    ],
#    cgo = True,
#    visibility = ["//visibility:public"],
#)

# TODO: The targets below are proofs of concept and are placed here for a simpler linker debug process.
# It runs based on `driver_proof_of_concept.go` (NOTICE: this file is built inside the library, which is incorrect,
# but it was enough for a simple proof of concept. Need to verify if the dynamic library can be found by external packages.
#go_binary(
#    name = "driver-temp-exe",
#    embed = [":driver-go"],
#    visibility = ["//visibility:public"],
#)

# TODO: This test doesn't work because it can't find the driver's native dynamic library.
#go_test(
#    name = "db-connection-test",
#    srcs = ["//go/test/integration:db_connection_test.go"],
#    embed = ["//go:driver-go"],
#    visibility = ["//visibility:public"],
#)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = glob([
        "README.md",
        "docs/**/*.adoc",
    ]),
    license_type = "apache-header",
)
