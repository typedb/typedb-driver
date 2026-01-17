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

package(default_visibility = ["//visibility:public"])

load("@rules_rust//rust:defs.bzl", "rust_static_library", "rust_shared_library", "rustfmt_test")
load("@typedb_dependencies//builder/rust:rules.bzl", "rust_cbindgen")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

load("@typedb_bazel_distribution//common:rules.bzl", "assemble_targz", "assemble_zip", "checksum", "assemble_versioned")
load("@typedb_bazel_distribution//docs:doxygen/rules.bzl", "doxygen_docs")
load("@typedb_bazel_distribution//artifact:rules.bzl", "deploy_artifact")
load("@typedb_dependencies//distribution:deployment.bzl", "deployment")
load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@rules_pkg//pkg:mappings.bzl", "pkg_files", "strip_prefix")
load("@typedb_bazel_distribution//platform:constraints.bzl", "constraint_linux_arm64", "constraint_linux_x86_64",
     "constraint_mac_arm64", "constraint_mac_x86_64", "constraint_win_x86_64")
load("//c:docs_structure.bzl", "dir_mapping", "force_file")
load("//tool/docs:c/rules.bzl", "doxygen_c_to_adoc")
load("//tool/docs:examples/rules.bzl", "test_to_example", "update_markdown_example")

clib_srcs = glob(["src/**/*.rs"])

rust_static_library(
    name = "typedb_driver_clib",
    srcs = clib_srcs,
    aliases = {
        "//rust:typedb_driver_sync": "typedb_driver",
    },
    deps = [
        "//rust:typedb_driver_sync",

        "@crates//:chrono",
        "@crates//:itertools",
        "@crates//:tracing",
        "@crates//:tracing-subscriber",
    ],
    tags = ["crate-name=typedb_driver_clib"],
)

rust_cbindgen(
    name = "typedb_driver_clib_headers",
    lib = ":typedb_driver_clib",
    header_name = "typedb_driver.h",
    config = "cbindgen.toml",
)


# Assembly & distribution
rust_shared_library(
    name = "typedb-driver-clib",
    srcs = clib_srcs,
    aliases = {
        "//rust:typedb_driver_sync": "typedb_driver",
    },
    deps = [
        "//rust:typedb_driver_sync",

        "@crates//:chrono",
        "@crates//:itertools",
        "@crates//:tracing",
        "@crates//:tracing-subscriber",
    ],
    tags = ["crate-name=typedb_driver_clib"],
)

filegroup(
    name = "typedb-driver-clib.if.lib",
    srcs = [":typedb-driver-clib"],
    output_group = "interface_library",
)

pkg_files(
    name = "typedb-driver-clib-headers",
    srcs = [":typedb_driver_clib_headers"],
    excludes = [":typedb_driver_clib"],
    prefix = "include",
)

pkg_files(
    name = "typedb-driver-clib-libraries",
    srcs = [":typedb-driver-clib", ":typedb-driver-clib.if.lib"],
    prefix = "lib",
)

pkg_tar(
    name = "typedb-driver-clib-tar",
    srcs = [":typedb-driver-clib-headers", ":typedb-driver-clib-libraries"],
)

assemble_targz(
    name = "assemble-linux-arm64-targz",
    targets = [
        ":typedb-driver-clib-tar"
    ],
    output_filename = "typedb-driver-clib-linux-arm64",
    target_compatible_with = constraint_linux_arm64,
)

assemble_targz(
    name = "assemble-linux-x86_64-targz",
    targets = [
        ":typedb-driver-clib-tar"
    ],
    output_filename = "typedb-driver-clib-linux-x86_64",
    target_compatible_with = constraint_linux_x86_64,
)

assemble_zip(
    name = "assemble-mac-arm64-zip",
    targets = [
        ":typedb-driver-clib-tar"
    ],
    output_filename = "typedb-driver-clib-mac-arm64",
    target_compatible_with = constraint_mac_arm64,
)

assemble_zip(
    name = "assemble-mac-x86_64-zip",
    targets = [
        ":typedb-driver-clib-tar"
    ],
    output_filename = "typedb-driver-clib-mac-x86_64",
    target_compatible_with = constraint_mac_x86_64,
)

assemble_zip(
    name = "assemble-windows-x86_64-zip",
    targets = [
        ":typedb-driver-clib-tar"
    ],
    output_filename = "typedb-driver-clib-windows-x86_64",
    target_compatible_with = constraint_win_x86_64,
)

# Deployment to repo.typedb.com
deploy_artifact(
    name = "deploy-linux-arm64-targz",
    target = ":assemble-linux-arm64-targz",
    artifact_group = "typedb-driver-clib-linux-arm64",
    artifact_name = "typedb-driver-clib-linux-arm64-{version}.tar.gz",
    release = deployment['artifact']['release']['upload'],
    snapshot = deployment['artifact']['snapshot']['upload'],
)

deploy_artifact(
    name = "deploy-linux-x86_64-targz",
    target = ":assemble-linux-x86_64-targz",
    artifact_group = "typedb-driver-clib-linux-x86_64",
    artifact_name = "typedb-driver-clib-linux-x86_64-{version}.tar.gz",
    release = deployment['artifact']['release']['upload'],
    snapshot = deployment['artifact']['snapshot']['upload'],
)

deploy_artifact(
    name = "deploy-mac-arm64-zip",
    target = ":assemble-mac-arm64-zip",
    artifact_group = "typedb-driver-clib-mac-arm64",
    artifact_name = "typedb-driver-clib-mac-arm64-{version}.zip",
    release = deployment['artifact']['release']['upload'],
    snapshot = deployment['artifact']['snapshot']['upload'],
)

deploy_artifact(
    name = "deploy-mac-x86_64-zip",
    target = ":assemble-mac-x86_64-zip",
    artifact_group = "typedb-driver-clib-mac-x86_64",
    artifact_name = "typedb-driver-clib-mac-x86_64-{version}.zip",
    release = deployment['artifact']['release']['upload'],
    snapshot = deployment['artifact']['snapshot']['upload'],
)

deploy_artifact(
    name = "deploy-windows-x86_64-zip",
    target = ":assemble-windows-x86_64-zip",
    artifact_group = "typedb-driver-clib-windows-x86_64",
    artifact_name = "typedb-driver-clib-windows-x86_64-{version}.zip",
    release = deployment['artifact']['release']['upload'],
    snapshot = deployment['artifact']['snapshot']['upload'],
)

alias(
    name = "assemble-clib-driver",
    actual = select({
        "@typedb_bazel_distribution//platform:is_linux_arm64" : ":assemble-linux-arm64-targz",
        "@typedb_bazel_distribution//platform:is_linux_x86_64" : ":assemble-linux-x86_64-targz",
        "@typedb_bazel_distribution//platform:is_mac_arm64" : ":assemble-mac-arm64-zip",
        "@typedb_bazel_distribution//platform:is_mac_x86_64" : ":assemble-mac-x86_64-zip",
        "@typedb_bazel_distribution//platform:is_windows_x86_64" : ":assemble-windows-x86_64-zip"
    })
)

alias(
    name = "deploy-clib-driver",
    actual = select({
        "@typedb_bazel_distribution//platform:is_linux_arm64" : ":deploy-linux-arm64-targz",
        "@typedb_bazel_distribution//platform:is_linux_x86_64" : ":deploy-linux-x86_64-targz",
        "@typedb_bazel_distribution//platform:is_mac_arm64" : ":deploy-mac-arm64-zip",
        "@typedb_bazel_distribution//platform:is_mac_x86_64" : ":deploy-mac-x86_64-zip",
        "@typedb_bazel_distribution//platform:is_windows_x86_64" : ":deploy-windows-x86_64-zip"
    })
)

filegroup(
    name = "_typedb_driver_h_only",
    srcs = [":typedb_driver_clib_headers"],
    output_group = "header"
)

# TODO: Not hermetic - requires doxygen on the host (https://github.com/typedb/bazel-distribution/issues/412)
doxygen_docs(
    name = "docs_html",
    project_name = "TypeDB C Driver",
    sources = [":_typedb_driver_h_only"],
    main_page_md = ":README.md",
    tags = ["manual"],
)

doxygen_c_to_adoc(
    name = "docs_adoc",
    data = [":docs_html"],
    docs_dirs = dir_mapping,
    force_file = force_file,
    output_dir = "docs/modules/ROOT/partials/c",
    tags = ["manual"]
)

test_to_example(
    name = "example",
    input = "//c/tests/integration:example.c",
    output = "c/example.c",
    removed_lines = ["assert", "FAILED()", "delete_database_if_exists", "common.h", "c/typedb_driver.h"],
    changed_words = {
        "TYPEDB_EXAMPLE_FUNC": "main",
    },
    header_comment_sign = "//",
)

update_markdown_example(
    name = "readme_example",
    input = ":example.c",
    output = ":README.md",
    start_marker = "EXAMPLE_START_MARKER",
    end_marker = "EXAMPLE_END_MARKER",
    language_tag = "c",
    removed_header = "//",
)

cc_library(
    name = "driver-example-lib",
    srcs = ["example.c"],
    deps = [
        ":typedb_driver_clib_headers",
        ":typedb_driver_clib",
    ],
    includes = ["."],
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        "src/**/*",
        "swig/*",
    ]),
    exclude = [".clang-format", "README.md", "Cargo.lock", "Cargo.toml", "example.c"] + glob(["docs/**/*.adoc"]),
    license_type = "apache-header",
)

rustfmt_test(
    name = "rustfmt_test",
    targets = [":typedb_driver_clib"],
    size = "small",
)
