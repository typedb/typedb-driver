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
load("@vaticle_bazel_distribution//artifact:rules.bzl", "deploy_artifact")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@rules_pkg//pkg:mappings.bzl", "pkg_files", "strip_prefix")
load("@vaticle_bazel_distribution//platform:constraints.bzl", "constraint_linux_arm64", "constraint_linux_x86_64",
     "constraint_mac_arm64", "constraint_mac_x86_64", "constraint_win_x86_64")
load(":build_opts.bzl", "cxxopts")

genrule(
    name = "version-header",
    outs = ["include/typedb/common/version.hpp"],
    srcs = ["//:VERSION"],
    cmd_bash = "echo \"static const char* const TYPEDB_DRIVER_VERSION = \\\"$$(cat $<)\\\";\" > $@",
    cmd_bat = "ECHO \"static const char* const TYPEDB_DRIVER_VERSION = \\\"\" > $@; TYPE $< >> $@; ECHO \"\\\";\" >> $@",
)

pkg_files(
    name = "typedb-driver-cpp-headers",
    srcs = glob(["include/**/*"]),
    strip_prefix = "",
    visibility = ["//visibility:public"],
)

cc_binary(
    name = "typedb-driver-cpp",
    srcs = glob([
        "lib/**/*.cpp",
        "lib/**/*.hpp",
    ]) + [":typedb-driver-cpp-headers", ":version-header"],
    includes = ["include"], 
    deps = [
        "//c:typedb_driver_clib",
        "//c:typedb_driver_clib_headers",
        "@nlohmann_json//:json",
    ],
    copts = cxxopts,
    linkopts = select({
        "@vaticle_bazel_distribution//platform:is_windows": ["ntdll.lib", "secur32.lib", "crypt32.lib", "ncrypt.lib"],
        "//conditions:default": [],
    }),
    local_defines = ["_COMPILING_TYPEDB_DRIVER"],
    features = ["windows_export_all_symbols"],
    linkshared = True,
)

# cc_binary outputs don't contain the .if.lib by default. This adds a target to it.
filegroup(
    name = "typedb-driver-cpp.if.lib",
    srcs = [":typedb-driver-cpp"],
    output_group = "interface_library",
)

cc_import(
    name = "typedb-driver-cpp-import",
    interface_library = ":typedb-driver-cpp.if.lib",
    shared_library = ":typedb-driver-cpp",
    hdrs = [":typedb-driver-cpp-headers"],
    includes = ["cpp/include"],
    visibility = ["//visibility:public"],
)

# Assembly & distribution
pkg_files(
    name = "typedb-driver-cpp-libraries",
    srcs = [":typedb-driver-cpp", ":typedb-driver-cpp.if.lib"],
    prefix = "lib",
)

pkg_tar(
    name = "typedb-driver-cpp-tar",
    srcs = [":typedb-driver-cpp-headers", ":typedb-driver-cpp-libraries"],
)

assemble_targz(
    name = "assemble-linux-arm64-targz",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-linux-arm64",
    target_compatible_with = constraint_linux_arm64,
)

assemble_targz(
    name = "assemble-linux-x86_64-targz",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-linux-x86_64",
    target_compatible_with = constraint_linux_x86_64,
)

assemble_zip(
    name = "assemble-mac-arm64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-mac-arm64",
    target_compatible_with = constraint_mac_arm64,
)

assemble_zip(
    name = "assemble-mac-x86_64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-mac-x86_64",
    target_compatible_with = constraint_mac_x86_64,
)

assemble_zip(
    name = "assemble-windows-x86_64-zip",
    targets = [
        ":typedb-driver-cpp-tar"
    ],
    output_filename = "typedb-driver-cpp-windows-x86_64",
    target_compatible_with = constraint_win_x86_64,
)

# Deployment to repo.vaticle.com
deploy_artifact(
    name = "deploy-linux-arm64-targz",
    target = ":assemble-linux-arm64-targz",
    artifact_group = "vaticle_typedb_driver_cpp",
    artifact_name = "typedb-driver-cpp-linux-arm64-{version}.tar.gz",
    release = deployment['artifact.release'],
    snapshot = deployment['artifact.snapshot'],
)

deploy_artifact(
    name = "deploy-linux-x86_64-targz",
    target = ":assemble-linux-x86_64-targz",
    artifact_group = "vaticle_typedb_driver_cpp",
    artifact_name = "typedb-driver-cpp-linux-x86_64-{version}.tar.gz",
    release = deployment['artifact.release'],
    snapshot = deployment['artifact.snapshot'],
)

deploy_artifact(
    name = "deploy-mac-arm64-zip",
    target = ":assemble-mac-arm64-zip",
    artifact_group = "vaticle_typedb_driver_cpp",
    artifact_name = "typedb-driver-cpp-mac-arm64-{version}.zip",
    release = deployment['artifact.release'],
    snapshot = deployment['artifact.snapshot'],
)

deploy_artifact(
    name = "deploy-mac-x86_64-zip",
    target = ":assemble-mac-x86_64-zip",
    artifact_group = "vaticle_typedb_driver_cpp",
    artifact_name = "typedb-driver-cpp-mac-x86_64-{version}.zip",
    release = deployment['artifact.release'],
    snapshot = deployment['artifact.snapshot'],
)

deploy_artifact(
    name = "deploy-windows-x86_64-zip",
    target = ":assemble-windows-x86_64-zip",
    artifact_group = "vaticle_typedb_driver_cpp",
    artifact_name = "typedb-driver-cpp-windows-x86_64-{version}.zip",
    release = deployment['artifact.release'],
    snapshot = deployment['artifact.snapshot'],
)

alias(
    name = "assemble-cpp-driver",
    actual = select({
        "@vaticle_bazel_distribution//platform:is_linux_arm64" : ":assemble-linux-arm64-targz",
        "@vaticle_bazel_distribution//platform:is_linux_x86_64" : ":assemble-linux-x86_64-targz",
        "@vaticle_bazel_distribution//platform:is_mac_arm64" : ":assemble-mac-arm64-zip",
        "@vaticle_bazel_distribution//platform:is_mac_x86_64" : ":assemble-mac-x86_64-zip",
        "@vaticle_bazel_distribution//platform:is_windows_x86_64" : ":assemble-windows-x86_64-zip"
    })
)

alias(
    name = "deploy-cpp-driver",
    actual = select({
        "@vaticle_bazel_distribution//platform:is_linux_arm64" : ":deploy-linux-arm64-targz",
        "@vaticle_bazel_distribution//platform:is_linux_x86_64" : ":deploy-linux-x86_64-targz",
        "@vaticle_bazel_distribution//platform:is_mac_arm64" : ":deploy-mac-arm64-zip",
        "@vaticle_bazel_distribution//platform:is_mac_x86_64" : ":deploy-mac-x86_64-zip",
        "@vaticle_bazel_distribution//platform:is_windows_x86_64" : ":deploy-windows-x86_64-zip"
    })
)

# Formatting tests
clang_format_test(
    name = "clang_format",
    include = glob(["include/typedb/**/*", "lib/**/*"]),
    exclude = glob(["test/**/*"])
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*", "include/**/*", "lib/**/*"]),
    exclude = ["README.md"],
    license_type = "apache-header",
)
