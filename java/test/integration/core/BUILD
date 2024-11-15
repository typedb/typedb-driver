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

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_dependencies//builder/java:rules.bzl", "typedb_java_test")

exports_files(
    ["ExampleTest.java"],
    visibility = ["//java:__subpackages__"],
)

typedb_java_test(
    name = "test-example",
    srcs = ["ExampleTest.java"],
    server_artifacts = {
        "@typedb_bazel_distribution//platform:is_linux_arm64": "@typedb_artifact_linux-arm64//file",
        "@typedb_bazel_distribution//platform:is_linux_x86_64": "@typedb_artifact_linux-x86_64//file",
        "@typedb_bazel_distribution//platform:is_mac_arm64": "@typedb_artifact_mac-arm64//file",
        "@typedb_bazel_distribution//platform:is_mac_x86_64": "@typedb_artifact_mac-x86_64//file",
#        "@typedb_bazel_distribution//platform:is_windows_x86_64": "@typedb_artifact_windows-x86_64//file",
    },
    test_class = "com.typedb.driver.test.integration.core.ExampleTest",
    deps = [
        # Internal dependencies
        "//java:driver-java",
        "//java/api",
        "//java/common",

        # External dependencies from @vaticle
        "@maven//:org_slf4j_slf4j_api",
#        "@maven//:com_vaticle_typedb_typedb_runner",
    ],
)

typedb_java_test(
    name = "test-value",
    srcs = ["ValueTest.java"],
    server_artifacts = {
        "@typedb_bazel_distribution//platform:is_linux_arm64": "@typedb_artifact_linux-arm64//file",
        "@typedb_bazel_distribution//platform:is_linux_x86_64": "@typedb_artifact_linux-x86_64//file",
        "@typedb_bazel_distribution//platform:is_mac_arm64": "@typedb_artifact_mac-arm64//file",
        "@typedb_bazel_distribution//platform:is_mac_x86_64": "@typedb_artifact_mac-x86_64//file",
#        "@typedb_bazel_distribution//platform:is_windows_x86_64": "@typedb_artifact_windows-x86_64//file",
    },
    test_class = "com.typedb.driver.test.integration.core.ValueTest",
    deps = [
        # Internal dependencies
        "//java:driver-java",
        "//java/api",
        "//java/common",

        # External dependencies from @vaticle
        "@maven//:org_slf4j_slf4j_api",
#        "@maven//:com_vaticle_typedb_typedb_runner",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
