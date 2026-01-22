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

package(default_visibility = ["//java/test/behaviour/query/language:__subpackages__"])

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//java/test/behaviour:rules.bzl", "typedb_behaviour_java_test")

typedb_behaviour_java_test(
    name = "test",
    srcs = [
        "QueryTest.java",
    ],
    test_class = "com.typedb.driver.test.behaviour.driver.query.QueryTest",
    data = [
        "@typedb_behaviour//driver:query.feature",
    ],
    connection_steps_core = "//java/test/behaviour/connection:steps-core",
    connection_steps_cluster = "//java/test/behaviour/connection:steps-cluster",
    steps = [
        "//java/test/behaviour/query:steps",
        "//java/test/behaviour/util:steps",
    ],
    deps = [
        # Internal Package Dependencies
        "//java/test/behaviour:behaviour",

        # External dependencies from Maven
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        "//java/test/behaviour/config:parameters",
    ],
    size = "large",
    visibility = ["//visibility:public"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
