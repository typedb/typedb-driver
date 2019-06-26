#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

package(default_visibility = ["//visibility:__pkg__"])
load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")


java_test(
    name = "grakn-client-test",
    srcs = [
        "GraknClientTest.java",
        "GraknServerRPCMock.java",
    ],
    test_class = "grakn.client.test.GraknClientTest",
    deps = [
        # Package dependencies
        "//:client-java",

        # Internal dependencies
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_grakn_core//server:server", # TODO: NEEDS TO GO
        "@graknlabs_protocol//:protocol-java",

        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-testing",
        "//dependencies/maven/artifacts/io/grpc:grpc-stub",
        "//dependencies/maven/artifacts/io/grpc:grpc-core",
        "//dependencies/maven/artifacts/com/google/guava:guava",
    ],
    size = "small",
)

java_test(
    name = "grakn-client-it",
    srcs = ["GraknClientIT.java"],
    test_class = "grakn.client.test.GraknClientIT",
    deps = [
        # Package dependencies
        "//:client-java",

        # Internal dependencies
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_grakn_core//server:server", # TODO: NEEDS TO GO
        "@graknlabs_grakn_core//test-integration/rule:grakn-test-server",

        # External depencies from @graknlabs
        "@graknlabs_graql//java:graql",

        # External depencncies from Maven
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/com/google/guava:guava"
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
    targets = [
        ":grakn-client-test",
        ":grakn-client-it",
    ],
    license_type = "apache"
)
