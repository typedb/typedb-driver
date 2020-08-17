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
        "SessionSteps.java",
    ],
    deps = [
        # Internal Package Dependencies
        "//:client-java",
        "//test/behaviour/connection:steps",

        # Internal Repository Dependencies
        "@graknlabs_common//:common",

        # External Maven Dependencies
        "@maven//:junit_junit",
        "@maven//:io_cucumber_cucumber_java",
    ],
)

grakn_test(
    name = "test-core",
    srcs = [
        "SessionTest.java"
    ],
    test_class = "grakn.client.test.behaviour.connection.session.SessionTest",
    deps = [
        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        ":steps",
        "//test/behaviour/config:parameters",
        "//test/behaviour/connection/keyspace:steps",
    ],
    data = [
        "@graknlabs_verification//behaviour/connection:session.feature",
    ],
    grakn_artifact = "@graknlabs_grakn_core_artifact//file",
    size = "medium",
)


checkstyle_test(
    name = "checkstyle",
    targets = [
        ":steps",
        ":test-core",
    ],
    license_type = "apache",
)
