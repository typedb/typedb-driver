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

# Note: Do NOT rename this folder to 'test' as it conflicts with a built-in Python package!
load("//python/tool:cluster_test_rule.bzl", "typedb_cluster_py_test")
load("@vaticle_bazel_distribution//artifact:rules.bzl", "artifact_extractor")
load("@vaticle_typedb_common//test:rules.bzl", "native_typedb_artifact")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

native_typedb_artifact(
    name = "native-typedb-artifact",
    mac_artifact = "@vaticle_typedb_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_artifact_windows//file",
    output = "typedb-server-artifact.tar.gz",
    visibility = ["//python/tests:__subpackages__"],
)

native_typedb_artifact(
    name = "native-typedb-cluster-artifact",
    mac_artifact = "@vaticle_typedb_cluster_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_cluster_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_cluster_artifact_windows//file",
    output = "typedb-cluster-server-artifact.tar.gz",
    visibility = ["//python/tests:__subpackages__"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        "*",
        "deployment/*",
    ]),
    license_type = "apache-header",
    size = "small",
)

artifact_extractor(
    name = "typedb-extractor",
    artifact = ":native-typedb-artifact",
)
