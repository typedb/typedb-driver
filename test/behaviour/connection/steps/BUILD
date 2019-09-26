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

package(default_visibility = ["//test/behaviour:__subpackages__"])
load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "connection-keyspace",
    srcs = [
        "Connection.java",
        "Keyspace.java",
    ],
    deps = [
        # Package dependencies
        "//:client-java",
        "//common:common",
        "//test/setup:grakn-properties",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-java",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
)

java_library(
    name = "connection-session",
    srcs = [
        "Connection.java",
        "Session.java",
    ],
    deps = [
        # Package dependencies
        "//:client-java",
        "//common:common",
        "//test/setup:grakn-properties",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-java",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
)

java_library(
    name = "connection-transaction",
    srcs = [
        "Connection.java",
        "Transaction.java",
    ],
    deps = [
        # Package dependencies
        "//:client-java",
        "//common:common",
        "//test/setup:grakn-properties",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-core",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-java",
        "//dependencies/maven/artifacts/io/cucumber:cucumber-junit",
    ],
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":connection-keyspace",
        ":connection-session",
        ":connection-transaction",
    ],
    license_type = "apache"
)
