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
#

package(default_visibility = ["//visibility:public"])

load("@rules_rust//rust:defs.bzl", "rust_library", "rust_static_library", "rustfmt_test")
load("@vaticle_dependencies//builder/rust:rules.bzl", "rust_cbindgen")
load("@vaticle_dependencies//builder/swig:python.bzl", "swig_python")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

rust_static_library(
    name = "typedb_client_clib",
    srcs = glob(["src/**/*.rs"]),
    aliases = {
        "//rust:typedb_client_sync": "typedb_client",
    },
    deps = [
        "//rust:typedb_client_sync",

        "@crates//:chrono",
        "@crates//:itertools",
        "@crates//:log",

        "@vaticle_typeql//rust:typeql_lang",
    ],
)

rust_cbindgen(
    name = "typedb_client_clib_headers",
    lib = ":typedb_client_clib",
    header_name = "typedb_client.h",
    config = "cbindgen.toml",
)

swig_python(
    name = "native_client_python_wrapper",
    shared_lib_name = "native_client_python",
    lib = ":typedb_client_clib_headers",
    interface = "typedb_client.i",
    includes = ["swig/typedb_client_python.swg"],
    enable_cxx = True,
    python_headers = "@python39//:python_headers",
    libpython = "@python39//:libpython",
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        "src/**/*",
        "swig/*",
    ]),
    license_type = "apache-header",
)

#rustfmt_test(
#    name = "client_rustfmt_test",
#    targets = ["typedb_client_clib"],
#)
