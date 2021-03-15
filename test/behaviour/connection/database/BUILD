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

package(default_visibility = ["//test/behaviour:__subpackages__"])

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_common//test:rules.bzl", "grakn_java_test")

java_library(
    name = "steps",
    srcs = [
        "DatabaseSteps.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        # Internal Package Dependencies
        "//:client-java",
        "//api:api",
        "//test/behaviour/connection:steps-base",

        # Internal Repository Dependencies
        "@graknlabs_common//:common",

        # External Maven Dependencies
        "@maven//:junit_junit",
        "@maven//:io_cucumber_cucumber_java",
    ],
)

grakn_java_test(
    name = "test-core",
    srcs = [
        "DatabaseTestCore.java",
    ],
    test_class = "grakn.client.test.behaviour.connection.database.DatabaseTestCore",
    data = [
        "@graknlabs_behaviour//connection:database.feature",
    ],
    server_mac_artifact = "@graknlabs_grakn_core_artifact_mac//file",
    server_linux_artifact = "@graknlabs_grakn_core_artifact_linux//file",
    server_windows_artifact = "@graknlabs_grakn_core_artifact_windows//file",
    runtime_deps = [
        "//test/behaviour/connection:steps-core",

        ":steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/connection/transaction:steps",
        "//test/behaviour/graql:steps",

        "//test/behaviour/config:parameters",
    ],
    deps = [
        # Internal Package Dependencies
        "//test/behaviour:behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    size = "medium",
)

grakn_java_test(
    name = "test-cluster",
    srcs = [
        "DatabaseTestCluster.java",
    ],
    test_class = "grakn.client.test.behaviour.connection.database.DatabaseTestCluster",
    data = [
        "@graknlabs_behaviour//connection:database.feature",
    ],
    server_mac_artifact = "@graknlabs_grakn_cluster_artifact_mac//file",
    server_linux_artifact = "@graknlabs_grakn_cluster_artifact_linux//file",
    server_windows_artifact = "@graknlabs_grakn_cluster_artifact_windows//file",
    runtime_deps = [
        "//test/behaviour/connection:steps-cluster",

        ":steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/connection/transaction:steps",
        "//test/behaviour/graql:steps",

        "//test/behaviour/config:parameters",
    ],
    deps = [
        # Internal Package Dependencies
        "//test/behaviour:behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    size = "medium",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)
