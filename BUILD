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

load("@graknlabs_dependencies//tool/release:rules.bzl", "release_validate_deps")
load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_dependencies//distribution/maven:version.bzl", "version")
load("@graknlabs_dependencies//library/maven:artifacts.bzl", artifacts_org = "artifacts")
load("//dependencies/maven:artifacts.bzl", artifacts_repo = "overrides")
load("@graknlabs_bazel_distribution//maven:rules.bzl", "assemble_maven", "deploy_maven")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")
load("@graknlabs_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")

exports_files(["VERSION", "RELEASE_TEMPLATE.md", "deployment.bzl"])

java_library(
    name = "client-java",
    srcs = glob(["*.java"]),
    deps = [
        # Internal dependencies
        "//api:api",
        "//cluster:cluster",
        "//common:common",
        "//core:core",

        # External dependencies from @graknlabs
        "@graknlabs_common//:common",
    ],
    resources = ["LICENSE"],
    tags = ["maven_coordinates=io.grakn.client:grakn-client:{pom_version}"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", ".grabl/*"]),
    exclude = glob(["docs/*"]),
    license_type = "apache",
    size = "small",
)

assemble_maven(
    name = "assemble-maven",
    target = ":client-java",
    source_jar_prefix = "grakn/client/",
    workspace_refs = "@graknlabs_client_java_workspace_refs//:refs.json",
    version_overrides = version(artifacts_org, artifacts_repo),
    project_name = "Grakn Client Java",
    project_description = "Grakn Client API for Java",
    project_url = "https://github.com/graknlabs/client-java",
    scm_url = "https://github.com/graknlabs/client-java",
)

deploy_maven(
    name = "deploy-maven",
    target = ":assemble-maven",
    snapshot = deployment["maven.snapshot"],
    release = deployment["maven.release"],
)

deploy_github(
    name = "deploy-github",
    organisation = github_deployment["github.organisation"],
    repository = github_deployment["github.repository"],
    release_description = "//:RELEASE_TEMPLATE.md",
    title = "Grakn Client Java",
    title_append_version = True,
    draft = False
)

release_validate_deps(
    name = "release-validate-deps",
    refs = "@graknlabs_client_java_workspace_refs//:refs.json",
    tagged_deps = [
        "@graknlabs_common",
        "@graknlabs_graql",
        "@graknlabs_protocol",
        "@graknlabs_grabl_tracing",
    ],
    tags = ["manual"]  # in order for bazel test //... to not fail
)

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@graknlabs_dependencies//tool/checkstyle:test-coverage",
        "@graknlabs_dependencies//tool/release:create-notes",
        "@graknlabs_dependencies//tool/sonarcloud:code-analysis",
        "@graknlabs_dependencies//tool/unuseddeps:unused-deps",
    ],
)
