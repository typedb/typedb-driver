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
load("@graknlabs_common//test/server:rules.bzl", "grakn_test")

java_library(
    name = "steps",
    srcs = [
        "TransactionSteps.java"
    ],
    deps = [
        # Internal Package Dependencies
        "//:client-java",
        "//test/behaviour/connection:steps",
        "//test/behaviour/util:util",

        # Internal Repository Dependencies
        "@graknlabs_common//:common",
        "@graknlabs_graql//java:graql",

        # External Maven Dependencies
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:io_cucumber_cucumber_java",
    ]
)

grakn_test(
    name = "test-core",
    srcs = [
        "TransactionTest.java",
    ],
    test_class = "grakn.client.test.behaviour.connection.transaction.TransactionTest",
    deps = [
        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        ":steps",
        "//test/behaviour/config:parameters",
        "//test/behaviour/connection/keyspace:steps",
        "//test/behaviour/connection/session:steps",
    ],
    data = [
        "@graknlabs_verification//behaviour/connection:transaction.feature",
    ],
    grakn_artifact = "@graknlabs_grakn_core_artifact//file",
    size = "large",
)

# To run this test, you need to provide environment variables:
# --test_env GRAKN_KGMS_ADDRESS=***
# --test_env GRAKN_KGMS_USERNAME=****
# --test_env GRAKN_KGMS_PASSWORD=****
grakn_test(
    name = "test-kgms",
    srcs = [
        "TransactionTest.java",
    ],
    test_class = "grakn.client.test.behaviour.connection.transaction.TransactionTest",
    deps = [
        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        ":steps",
        "//test/behaviour/config:parameters",
        "//test/behaviour/connection/keyspace:steps",
        "//test/behaviour/connection/session:steps",
    ],
    data = [
        "@graknlabs_verification//behaviour/connection:transaction.feature",
    ],
    type = "grakn-kgms",
    size = "large",
    tags = ["manual"]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":steps",
        ":test-core",
        ":test-kgms",
    ],
    license_type = "apache"
)
