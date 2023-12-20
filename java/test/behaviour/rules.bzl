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

load("@vaticle_dependencies//builder/java:rules.bzl", "typedb_java_test")

def typedb_behaviour_java_test(
        name,
        connection_steps_core,
        connection_steps_cloud,
        steps,
        runtime_deps = [],
        **kwargs):

    typedb_java_test(
        name = name + "-core",
        server_artifacts = {
            "@vaticle_bazel_distribution//platform:is_linux_arm64": "@vaticle_typedb_artifact_linux-arm64//file",
            "@vaticle_bazel_distribution//platform:is_linux_x86_64": "@vaticle_typedb_artifact_linux-x86_64//file",
            "@vaticle_bazel_distribution//platform:is_mac_arm64": "@vaticle_typedb_artifact_mac-arm64//file",
            "@vaticle_bazel_distribution//platform:is_mac_x86_64": "@vaticle_typedb_artifact_mac-x86_64//file",
            "@vaticle_bazel_distribution//platform:is_windows_x86_64": "@vaticle_typedb_artifact_windows-x86_64//file",
        },
        runtime_deps = runtime_deps + [connection_steps_core] + steps,
        **kwargs,
    )

    typedb_java_test(
        name = name + "-cloud",
        server_artifacts = {
            "@vaticle_bazel_distribution//platform:is_linux_arm64": "@vaticle_typedb_cloud_artifact_linux-arm64//file",
            "@vaticle_bazel_distribution//platform:is_linux_x86_64": "@vaticle_typedb_cloud_artifact_linux-x86_64//file",
            "@vaticle_bazel_distribution//platform:is_mac_arm64": "@vaticle_typedb_cloud_artifact_mac-arm64//file",
            "@vaticle_bazel_distribution//platform:is_mac_x86_64": "@vaticle_typedb_cloud_artifact_mac-x86_64//file",
            "@vaticle_bazel_distribution//platform:is_windows_x86_64": "@vaticle_typedb_cloud_artifact_windows-x86_64//file",
        },
        runtime_deps = runtime_deps + [connection_steps_cloud] + steps,
        **kwargs,
    )
