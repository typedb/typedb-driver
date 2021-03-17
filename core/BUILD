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

package(default_visibility = ["//visibility:public"])

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "core",
    srcs = glob(["*.java"], exclude=["test/**"]),
    deps = [
        # Internal dependencies
        "//api:api",
        "//common:common",
        "//concept:concept",
        "//logic:logic",
        "//query:query",
        "//stream:stream",

        # External dependencies from @graknlabs
        "@graknlabs_common//:common",
        "@graknlabs_protocol//grpc/java:protocol",

        # External dependencies from Maven
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:io_grpc_grpc_api",
    ],
    runtime_deps = [
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:io_grpc_grpc_netty",
        "@maven//:io_netty_netty_all",
        "@maven//:io_netty_netty_tcnative_boringssl_static",
        # "@maven//:io_grpc_grpc_core", // TODO: verify whether we need this or not
    ],
    tags = ["maven_coordinates=io.grakn.client:grakn-client-core:{pom_version}"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
    size = "small",
)