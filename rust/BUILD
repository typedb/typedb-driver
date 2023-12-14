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

load("@rules_rust//rust:defs.bzl", "rust_library", "rust_static_library", "rustfmt_test", "rust_test")
load("@vaticle_bazel_distribution//crates:rules.bzl", "assemble_crate", "deploy_crate")
load("@vaticle_dependencies//builder/rust:rules.bzl", "rust_cbindgen")
load("@vaticle_dependencies//builder/swig:java.bzl", "swig_java")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//tool/docs:rust/rules.bzl", "rust_docs", "rustdoc_to_adoc")
load("//rust:docs_structure.bzl", "dir_mapping")

typedb_driver_srcs = glob(["src/**/*.rs"])
typedb_driver_compile_data = [
    "src/VERSION",
]
typedb_driver_tags = ["crate-name=typedb-driver"]
typedb_driver_deps = [
    "@crates//:chrono",
    "@crates//:crossbeam",
    "@crates//:futures",
    "@crates//:http",
    "@crates//:itertools",
    "@crates//:log",
    "@crates//:prost",
    "@crates//:tokio",
    "@crates//:tokio-stream",
    "@crates//:tonic",
    "@crates//:uuid",
    "@vaticle_typedb_protocol//grpc/rust:typedb_protocol",
    "@vaticle_typeql//rust:typeql",
]
typedb_driver_proc_macro_deps = [
    "@crates//:maybe-async",
]

rust_library(
    name = "typedb_driver",
    srcs = typedb_driver_srcs,
    compile_data = typedb_driver_compile_data,
    deps = typedb_driver_deps,
    proc_macro_deps = typedb_driver_proc_macro_deps,
    tags = typedb_driver_tags,
    visibility = ["//visibility:public"]
)

rust_library(
    name = "typedb_driver_sync",
    srcs = typedb_driver_srcs,
    compile_data = typedb_driver_compile_data,
    deps = typedb_driver_deps,
    proc_macro_deps = typedb_driver_proc_macro_deps,
    tags = typedb_driver_tags,
    crate_features = ["sync"],
    visibility = ["//visibility:public"]
)

rust_test(
    name = "typedb_driver_unit_tests",
    crate = ":typedb_driver",
    deps = ["@crates//:serde_json"],
)

assemble_crate(
    name = "assemble_crate",
    description = "TypeDB Rust Driver",
    homepage = "https://github.com/vaticle/typedb-driver",
    license = "Apache-2.0",
    license_file = "//:LICENSE",
    readme_file = ":README.md",
    repository = "https://github.com/vaticle/typedb-driver",
    target = ":typedb_driver",
    universe_manifests = ["@vaticle_dependencies//library/crates:Cargo.toml"],
    crate_features = { "sync": [] },
    workspace_refs = "@vaticle_typedb_driver_workspace_refs//:refs.json",
)

deploy_crate(
    name = "deploy_crate",
    release = deployment["crate.release"],
    snapshot = deployment["crate.snapshot"],
    target = ":assemble_crate",
)

rust_docs(
    name = "docs_html_async",
    feature = "async",
)

rust_docs(
    name = "docs_html_sync",
    feature = "sync",
)

rustdoc_to_adoc(
    name = "docs_adoc",
    data = [":docs_html_async", ":docs_html_sync"],
    docs_dirs = dir_mapping,
    modes = {":docs_html_async": "async", ":docs_html_sync": "sync"},
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        "src/**/*",
    ]),
    exclude = glob([
        "README.md",
        "src/VERSION",
        "**/Cargo.toml",
        "**/Cargo.lock",
        "docs/**/*.adoc",
    ]),
    license_type = "apache-header",
)

filegroup(
    name = "rustfmt_config",
    srcs = ["rustfmt.toml"],
)

rustfmt_test(
    name = "rustfmt_test",
    targets = [":typedb_driver"],
    size = "small",
)
