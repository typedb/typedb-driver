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
    name = "test-connection-core",
    srcs = [
        "TestConnectionCore.java",
    ],
    test_class = "grakn.client.test.behaviour.TestConnectionCore",
    deps = [
        # Package dependencies
        "//test/common:grakn-core-runner",
        "//test/common:grakn-properties",
        "//test/behaviour/config:type-registry-config",
        "//test/behaviour/steps:connection",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
    data = [
        "@graknlabs_behaviour//connection:features",
        "@graknlabs_grakn_core//:assemble-linux-targz", # Make sure to pass the path in args below
    ],
    args = [ # The order of the arguments matter
        "$(location @graknlabs_grakn_core//:assemble-linux-targz)", # Keep at index 0, will be accessible at args[1]
    ],
    classpath_resources = ["//test/common:logback"],
    size = "large",
)

# To run this test, you need to provide GRAKN_KGMS_ADDRESS, GRAKN_KGMS_USERNAME and GRAKN_KGMS_PASSWORD
# bazel build //test/session:session-kgms-test \
#             --test_env GRAKN_KGMS_ADDRESS=*** \
#             --test_env GRAKN_KGMS_USERNAME=**** \
#             --test_env GRAKN_KGMS_PASSWORD=****
java_test(
    name = "test-connection-kgms",
    srcs = glob([
        "TestConnectionKGMS.java",
    ]),
    test_class = "grakn.client.test.behaviour.ConnectionTestKGMS",
    deps = [
        # Package dependencies
        "//test/behaviour/config:type-registry-config",
        "//test/behaviour/steps:connection",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
    data = [
        "@graknlabs_behaviour//connection:features",
    ],
    classpath_resources = ["//test/common:logback"],
    size = "medium",
)

java_test(
    name = "test-debug",
    srcs = [
        "TestDebug.java",
        "TestConnectionCore.java",
    ],
    test_class = "grakn.client.test.behaviour.TestDebug",
    deps = [
        # Package dependencies
        "//test/common:grakn-core-runner",
        "//test/common:grakn-properties",
        "//test/behaviour/config:type-registry-config",
        "//test/behaviour/steps:connection",

        # Add your addition debugging dependencies here

        # External dependencies from Maven
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
    data = [
        ":TestDebug.feature",
        "@graknlabs_grakn_core//:assemble-linux-targz", # Make sure to pass the path in args below
    ],
    args = [ # The order of the arguments matter
        "$(location @graknlabs_grakn_core//:assemble-linux-targz)", # Keep at index 0, will be accessible at args[1]
    ],
    classpath_resources = ["//test/common:logback"],
    size = "medium",
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":test-connection-core",
        ":test-connection-kgms",
        ":test-debug"
    ],
    license_type = "apache"
)
