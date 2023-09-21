#!/usr/bin/env bash
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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_bazel_distribution//artifact:rules.bzl", "artifact_extractor")
load("@vaticle_typedb_common//test:rules.bzl", "native_typedb_artifact")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

native_typedb_artifact(
    name = "native-typedb-artifact",
    mac_artifact = "@vaticle_typedb_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_artifact_windows//file",
    output = "typedb-artifact.tar.gz",
    visibility = ["//visibility:public"],
)

native_typedb_artifact(
    name = "native-typedb-enterprise-artifact",
    mac_artifact = "@vaticle_typedb_enterprise_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_enterprise_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_enterprise_artifact_windows//file",
    output = "typedb-enterprise-artifact.tar.gz",
    visibility = ["//visibility:public"],
)

artifact_extractor(
    name = "typedb-extractor",
    artifact = ":native-typedb-artifact",
)

artifact_extractor(
    name = "typedb-enterprise-extractor",
    artifact = ":native-typedb-enterprise-artifact",
)
