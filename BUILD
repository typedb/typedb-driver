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

package(default_visibility = ["//visibility:public"])
exports_files(["VERSION"], visibility = ["//visibility:public"])

load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_build_tools//distribution/maven:rules.bzl", "deploy_maven", "assemble_maven")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")

exports_files(["VERSION", "RELEASE_TEMPLATE.md", "deployment.properties"])

java_library(
    name = "client-java",
    srcs = glob([
        "concept/*.java",
        "exception/*.java",
        "rpc/*.java",
        "test/*.java",
        "GraknClient.java"
    ]),
    deps = [
        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",
        "@graknlabs_protocol//:protocol-java",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_grakn_core//concept:concept",

        # External dependencies from Maven
        "//dependencies/maven/artifacts/com/google/code/findbugs:jsr305",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/io/grpc:grpc-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-stub",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-core",
        "//dependencies/maven/artifacts/io/grpc:grpc-netty",
        "//dependencies/maven/artifacts/io/netty:netty-all",
    ],
    resources = ["LICENSE"],
    tags = ["maven_coordinates=io.grakn.client:api:{pom_version}"],
)

checkstyle_test(
    name = "checkstyle",
    targets = [":client-java"],
    license_type = "apache"
)

assemble_maven(
    name = "assemble-maven",
    target = ":client-java",
    package = "client-java",
    version_file = "//:VERSION",
    workspace_refs = "@graknlabs_client_java_workspace_refs//:refs.json"
)

deploy_maven(
    name = "deploy-maven",
    target = ":assemble-maven",
)

deploy_github(
    name = "deploy-github",
    release_description = "//:RELEASE_TEMPLATE.md",
    deployment_properties = "//:deployment.properties",
    version_file = "//:VERSION"
)
