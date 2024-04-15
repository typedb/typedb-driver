#!/usr/bin/env bash
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
load("@vaticle_bazel_distribution//artifact:rules.bzl", "artifact_extractor")
load("@vaticle_dependencies//builder/java:rules.bzl", "native_typedb_artifact")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

java_binary(
    name = "echo-java-home",
    srcs = ["EchoJavaHome.java"],
    deps = [],
    main_class = "com.vaticle.typedb.driver.tool.test.EchoJavaHome"
)

native_typedb_artifact(
    name = "native-typedb-artifact",
    native_artifacts = {
        "@vaticle_bazel_distribution//platform:is_linux_arm64": ["@vaticle_typedb_artifact_linux-arm64//file"],
        "@vaticle_bazel_distribution//platform:is_linux_x86_64": ["@vaticle_typedb_artifact_linux-x86_64//file"],
        "@vaticle_bazel_distribution//platform:is_mac_arm64": ["@vaticle_typedb_artifact_mac-arm64//file"],
        "@vaticle_bazel_distribution//platform:is_mac_x86_64": ["@vaticle_typedb_artifact_mac-x86_64//file"],
        "@vaticle_bazel_distribution//platform:is_windows_x86_64": ["@vaticle_typedb_artifact_windows-x86_64//file"],
    },
    output = "typedb-artifact.tar.gz",
    visibility = ["//visibility:public"],
)

native_typedb_artifact(
    name = "native-typedb-cloud-artifact",
    native_artifacts = {
        "@vaticle_bazel_distribution//platform:is_linux_arm64": ["@vaticle_typedb_cloud_artifact_linux-arm64//file"],
        "@vaticle_bazel_distribution//platform:is_linux_x86_64": ["@vaticle_typedb_cloud_artifact_linux-x86_64//file"],
        "@vaticle_bazel_distribution//platform:is_mac_arm64": ["@vaticle_typedb_cloud_artifact_mac-arm64//file"],
        "@vaticle_bazel_distribution//platform:is_mac_x86_64": ["@vaticle_typedb_cloud_artifact_mac-x86_64//file"],
        "@vaticle_bazel_distribution//platform:is_windows_x86_64": ["@vaticle_typedb_cloud_artifact_windows-x86_64//file"],
    },
    output = "typedb-cloud-artifact.tar.gz",
    visibility = ["//visibility:public"],
)

artifact_extractor(
    name = "typedb-extractor",
    artifact = ":native-typedb-artifact",
)

artifact_extractor(
    name = "typedb-cloud-extractor",
    artifact = ":native-typedb-cloud-artifact",
)
