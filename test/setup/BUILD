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

package(default_visibility = ["//test:__subpackages__"])
load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "grakn-setup",
    srcs = [
        "GraknSetup.java",
        "GraknSetupCore.java",
        "GraknSetupKGMS.java",
    ],
    deps = [
        ":grakn-properties",
        "//dependencies/maven/artifacts/commons-io:commons-io",
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/org/zeroturnaround:zt-exec",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api"
    ],
)

java_library(
    name = "grakn-properties",
    srcs = ["GraknProperties.java"],
)

filegroup(
    name = "logback",
    srcs = ["logback.xml"],
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":grakn-setup",
        ":grakn-properties",
    ],
    license_type = "apache"
)