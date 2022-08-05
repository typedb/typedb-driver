#
# Copyright (C) 2021 Vaticle
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

load("@rules_rust//rust:defs.bzl", "rust_library")
load("@vaticle_bazel_distribution//crates:rules.bzl", "assemble_crate")

rust_library(
    name = "typedb_client",
    srcs = glob(["src/**/*.rs"], exclude = glob(["src/typedb_protocol/**/*.rs"])),
    deps = [
        "//typedb_protocol",

        "@vaticle_dependencies//library/crates:futures",
        "@vaticle_dependencies//library/crates:grpc",
        "@vaticle_dependencies//library/crates:log",
        "@vaticle_dependencies//library/crates:protobuf",
        "@vaticle_dependencies//library/crates:uuid",
    ],
    proc_macro_deps = [
        "@vaticle_dependencies//library/crates:derivative",
    ],
)

assemble_crate(
    name = "assemble",
    target = "typedb_client",
    description = "TypeDB Client API for Rust",
    homepage = "https://github.com/vaticle/typedb-client-rust",
    license = "apache",
    repository = "https://github.com/vaticle/typedb-client-rust",
)