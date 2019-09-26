#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2019 Grakn Labs Ltd
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

package(default_visibility = ["//visibility:__subpackages__"])
load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "test",
    srcs = [
        "DebugTest.java",
    ],
    test_class = "grakn.client.test.behaviour.debug.DebugTest",
    deps = [
        # Package dependencies
        "//test/behaviour/common:connection",
        "//test/behaviour/config:type-registry-config",
        "//test/setup:grakn-setup",

        # TODO: Add your addition debugging dependencies here
        # e.g. "//test/behaviour/connection/steps:connection-keyspace",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
    data = [
        ":debug.feature",
        "@graknlabs_grakn_core//:assemble-linux-targz", # Make sure to pass the path in args below
    ],
    args = [ # The order of the arguments matter
        "grakn-core", # Keep at index 0, will be accessible at args[1]
        "$(location @graknlabs_grakn_core//:assemble-linux-targz)", # Keep at index 1, will be accessible at args[2]
    ],
    classpath_resources = ["//test/setup:logback"],
    size = "medium",
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":test"
    ],
    license_type = "apache"
)