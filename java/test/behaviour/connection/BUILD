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

package(default_visibility = ["//java/test/behaviour:__subpackages__"])

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "steps-base",
    srcs = ["ConnectionStepsBase.java"],
    deps = [
        # Package dependencies
        "//java:driver-java",
        "//java/api",
        "//java/test/behaviour/config:parameters",

        # External dependencies from Maven
        "@maven//:junit_junit",
#        "@maven//:com_typedb_typedb_runner",
    ],
)

java_library(
    name = "steps-core",
    srcs = [
        "ConnectionStepsCore.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        # Package dependencies
        ":steps-base",
        "//java/test/behaviour/config:parameters",
        "//java:driver-java",
        "//java/api",
#        "@maven//:com_typedb_typedb_runner",
        "@maven//:io_cucumber_cucumber_java",
    ],
)

java_library(
    name = "steps-cloud",
    srcs = [
        "ConnectionStepsCloud.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        # Package dependencies
        ":steps-base",
        "//java/test/behaviour/config:parameters",
        "//java:driver-java",
        "//java/api",
#        "@maven//:com_typedb_typedb_runner",
        "@maven//:io_cucumber_cucumber_java",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
