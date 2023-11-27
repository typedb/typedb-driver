#
# Copyright (C) 2022 Vaticle
#
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
load("@vaticle_dependencies//builder/cpp:rules.bzl", "clang_format_test")
load("@vaticle_bazel_distribution//common:rules.bzl", "assemble_targz", "assemble_zip", "checksum", "assemble_versioned")
load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@rules_pkg//pkg:mappings.bzl", "pkg_files", "strip_prefix")

cc_library(
    name = "typedb-driver-lib",
    srcs = glob([
        "lib/**/*.cpp",
        "lib/inc/*.hpp"
    ]),
    hdrs = glob(["include/typedb/**/*"]),
    strip_include_prefix = "include",
    includes = ["lib/"], # private includes - # TODO: Check if these are indeed private.
    deps = [
        "//c:typedb_driver_clib",
        "//c:typedb_driver_clib_headers"
    ],
    visibility = ["//visibility:public"],
    copts = ["-g -std=c++17"], # TODO:  Remove -g?
    linkopts = select({
        "@vaticle_bazel_distribution//platform:is_windows": ["ntdll.lib", "secur32.lib", "crypt32.lib", "ncrypt.lib"],
        "//conditions:default": [],
    }),
)

pkg_files(
    name = "typedb-driver-cpp-lib",
    srcs = glob(["include/**/*"]),
    strip_prefix = ""
)

pkg_files(
    name = "typedb-driver-cpp-headers",
    srcs = [":typedb-driver-lib"],
    prefix = "lib"
)

pkg_tar(
    name = "typedb-driver-cpp-tar",
    srcs = [":typedb-driver-cpp-headers", ":typedb-driver-cpp-lib"],
)

assemble_targz(
    name = "assemble-linux-arm64-targz",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-linux-arm64",
)

assemble_targz(
    name = "assemble-linux-x86_64-targz",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-linux-x86_64",
)

assemble_zip(
    name = "assemble-mac-arm64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-mac-arm64",
)

assemble_zip(
    name = "assemble-mac-x86_64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-mac-x86_64",
)

assemble_zip(
    name = "assemble-windows-x86_64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-windows-x86_64",
)


clang_format_test(
    name = "clang_format",
    include = glob(["include/typedb/**/*", "lib/**/*"]),
    exclude = glob(["test/**/*"])
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*/*"]),
    exclude = ["README.md"],
    license_type = "apache-header",
)
