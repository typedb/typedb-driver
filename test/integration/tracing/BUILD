#
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
#


package(default_visibility = ["//visibility:__subpackages__"])

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "tracing-it",
    srcs = ["TracingIT.java"],
    test_class = "grakn.client.test.integration.tracing.TracingIT",
    deps = [
        # Grakn Core dependencies
        "//:client-java",
        "//test/setup:grakn-setup",
        "//test/setup:grakn-properties",

        # Grakn Labs dependencies
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client",

        # Maven External dependencies
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_slf4j_slf4j_api", # TODO: Do we still need this?
    ],
    classpath_resources = [
        "//test/setup:logback",
    ],
    data = [
        "@graknlabs_grakn_core//:assemble-linux-targz", # Make sure to pass the path in args below
    ],
    args = [ # The order of the arguments matter
        "grakn-core", # Keep at index 0, will be accessible at args[1]
        "$(location @graknlabs_grakn_core//:assemble-linux-targz)", # Keep at index 1, will be accessible at args[2]
    ],
)


checkstyle_test(
    name = "checkstyle",
    targets = [":tracing-it"],
    license_type = "apache"
)
