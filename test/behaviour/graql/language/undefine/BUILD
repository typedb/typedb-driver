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

package(default_visibility = ["//visibility:__subpackages__"])
load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_common//test/server:rules.bzl", "grakn_java_test")

grakn_java_test(
    name = "test-core",
    srcs = [
        "UndefineTest.java",
    ],
    test_class = "grakn.client.test.behaviour.graql.language.undefine.UndefineTest",
    deps = [
        # Internal Package Dependencies
        "//test/behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        "//test/behaviour/graql:steps",
        "//test/behaviour/connection:steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/config:parameters",
    ],
    data = [
        "@graknlabs_behaviour//graql/language:undefine.feature",
    ],
    grakn_artifact_linux = "@graknlabs_grakn_core_artifact_linux//file",
    grakn_artifact_mac = "@graknlabs_grakn_core_artifact_mac//file",
    size = "medium",
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)
