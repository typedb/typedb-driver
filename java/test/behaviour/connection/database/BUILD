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

package(default_visibility = ["//java/test/behaviour:__subpackages__"])

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_typedb_common//test:rules.bzl", "typedb_java_test")

java_library(
    name = "steps",
    srcs = [
        "DatabaseSteps.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        # Internal Package Dependencies
        "//java/api:api",
        "//java/test/behaviour/connection:steps-base",

        # Internal Repository Dependencies
        "@vaticle_typedb_common//:common",

        # External Maven Dependencies
        "@maven//:junit_junit",
        "@maven//:io_cucumber_cucumber_java",
    ],
)

typedb_java_test(
    name = "test-core",
    srcs = [
        "DatabaseTestCore.java",
    ],
    test_class = "com.vaticle.typedb.client.test.behaviour.connection.database.DatabaseTestCore",
    data = [
        "@vaticle_typedb_behaviour//connection:database.feature",
    ],
    server_mac_artifact = "@vaticle_typedb_artifact_mac//file",
    server_linux_artifact = "@vaticle_typedb_artifact_linux//file",
    server_windows_artifact = "@vaticle_typedb_artifact_windows//file",
    runtime_deps = [
        "//java/test/behaviour/connection:steps-core",

        ":steps",
        "//java/test/behaviour/connection/session:steps",
        "//java/test/behaviour/connection/transaction:steps",
        "//java/test/behaviour/typeql:steps",

        "//java/test/behaviour/config:parameters",
    ],
    deps = [
        # Internal Package Dependencies
        "//java/test/behaviour:behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    size = "large",
)

typedb_java_test(
    name = "test-cluster",
    srcs = [
        "DatabaseTestCluster.java",
    ],
    test_class = "com.vaticle.typedb.client.test.behaviour.connection.database.DatabaseTestCluster",
    data = [
        "@vaticle_typedb_behaviour//connection:database.feature",
    ],
    server_mac_artifact = "@vaticle_typedb_cluster_artifact_mac//file",
    server_linux_artifact = "@vaticle_typedb_cluster_artifact_linux//file",
    server_windows_artifact = "@vaticle_typedb_cluster_artifact_windows//file",
    runtime_deps = [
        "//java/test/behaviour/connection:steps-cluster",

        ":steps",
        "//java/test/behaviour/connection/session:steps",
        "//java/test/behaviour/connection/transaction:steps",
        "//java/test/behaviour/typeql:steps",

        "//java/test/behaviour/config:parameters",
    ],
    deps = [
        # Internal Package Dependencies
        "//java/test/behaviour:behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    size = "large",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
