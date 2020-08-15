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
exports_files(["VERSION"], visibility = ["//visibility:public"])

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_dependencies//library/maven:artifacts.bzl", "maven_overrides", maven_overrides_org = "artifacts")
load("@graknlabs_bazel_distribution//maven:rules.bzl", "deploy_maven", "assemble_maven")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")
load("//dependencies/maven:artifacts.bzl", maven_overrides_repo = "overrides")

exports_files(["VERSION", "RELEASE_TEMPLATE.md", "deployment.properties"])

java_library(
    name = "client-java",
    srcs = glob([
        "answer/*.java",
        "concept/**/*.java",
        "exception/*.java",
        "rpc/*.java",
        "test/*.java",
        "GraknClient.java"
    ]),
    deps = [
        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",
        "@graknlabs_protocol//grpc/java:protocol",

        "@graknlabs_grabl_tracing//client",

        # External dependencies from Maven
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:io_grpc_grpc_core",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:io_grpc_grpc_api",
        "@maven//:org_slf4j_slf4j_api" # TODO: Do we still need this?
    ],
    runtime_deps = [
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:io_grpc_grpc_netty",
        "@maven//:io_netty_netty_all",
        "@maven//:io_netty_netty_tcnative_boringssl_static",
    ],
    resources = ["LICENSE"],
    tags = ["maven_coordinates=io.grakn.client:grakn-client:{pom_version}"],
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
    workspace_refs = "@graknlabs_client_java_workspace_refs//:refs.json",
    version_overrides = maven_overrides(maven_overrides_org, maven_overrides_repo),
    project_name = "Grakn Client Java",
    project_description = "Grakn Client API for Java",
    project_url = "https://github.com/graknlabs/client-java",
    scm_url = "https://github.com/graknlabs/client-java",
)

deploy_maven(
    name = "deploy-maven",
    target = ":assemble-maven",
    deployment_properties = "@graknlabs_dependencies//distribution:deployment.properties"
)

deploy_github(
    name = "deploy-github",
    release_description = "//:RELEASE_TEMPLATE.md",
    title = "Grakn Client Java",
    title_append_version = True,
    deployment_properties = "//:deployment.properties",
)
