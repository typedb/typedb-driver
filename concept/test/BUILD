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


package(default_visibility = ["//visibility:__pkg__"])

load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "remote-concept-it",
    srcs = ["ConceptIT.java"],
    test_class = "grakn.client.concept.test.RemoteConceptIT",
    deps = [
        # Grakn Core dependencies
        "//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_grakn_core//test-integration/rule:grakn-test-server",

        # Grakn Labs dependencies
        "@graknlabs_graql//java:graql",

        # Maven External dependencies
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api", # TODO: Do we still need this?
    ],
    size = "large",
    classpath_resources = [
        "@graknlabs_grakn_core//test-integration/resources:logback-test",
        "@graknlabs_grakn_core//test-integration/resources:cassandra-embedded",
        "@graknlabs_grakn_core//server:conf/grakn.properties"
    ]
)


checkstyle_test(
    name = "checkstyle",
    targets = [":remote-concept-it"],
    license_type = "apache"
)
