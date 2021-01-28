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
load("//test/behaviour:rules.bzl", "grakn_core_and_cluster_behaviour_test")

java_library(
    name = "steps",
    srcs = [
        "TransactionSteps.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        # Internal Package Dependencies
        "//:client-java",
        "//test/behaviour/connection:steps-base",
        "//test/behaviour/util",

        # Internal Repository Dependencies
        "@graknlabs_common//:common",
        "@graknlabs_graql//java:graql",

        # External Maven Dependencies
        "@maven//:junit_junit",
        "@maven//:io_cucumber_cucumber_java",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

grakn_core_and_cluster_behaviour_test(
    name = "test",
    srcs = [
        "TransactionTest.java",
    ],
    test_class = "grakn.client.test.behaviour.connection.transaction.TransactionTest",
    data = [
        "@graknlabs_behaviour//connection:transaction.feature",
    ],
    native_grakn_artifact_core = "//test:native-grakn-core-artifact",
    native_grakn_artifact_cluster = "//test:native-grakn-cluster-artifact",
    connection_steps_core = "//test/behaviour/connection:steps-core",
    connection_steps_cluster = "//test/behaviour/connection:steps-cluster",
    steps = [
        ":steps",
        "//test/behaviour/connection/database:steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/graql:steps",
    ],
    deps = [
        # Internal Package Dependencies
        "//test/behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        "//test/behaviour/config:parameters",
    ],
    size = "small",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)
