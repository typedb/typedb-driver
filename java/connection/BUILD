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

package(default_visibility = ["//visibility:public"])

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "connection",
    srcs = glob(
        [
            "*.java",
            "*/*.java",
        ],
        exclude = ["test/**"],
    ),
    runtime_deps = [
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:io_netty_netty_tcnative_boringssl_static",
    ],
    deps = [
        # Internal dependencies
        "//java/api",
        "//java/common",
        "//java/concept",
        "//java/user",
        "//java:typedb_driver_jni",
    ],
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        "*/*",
    ]),
    license_type = "apache-header",
)
