#
# Copyright (C) 2022 Vaticle
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

exports_files(
    ["VERSION"],
    visibility = ["//visibility:public"],
)

load("@vaticle_dependencies//tool/release/deps:rules.bzl", "release_validate_deps")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_dependencies//distribution/maven:version.bzl", "version")
load("@vaticle_dependencies//library/maven:artifacts.bzl", artifacts_org = "artifacts")
load("//dependencies/maven:artifacts.bzl", artifacts_repo = "overrides")
load("@vaticle_bazel_distribution//maven:rules.bzl", "assemble_maven", "deploy_maven")
load("@vaticle_bazel_distribution//github:rules.bzl", "deploy_github")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")

exports_files([
    "VERSION",
    "deployment.bzl",
])

java_library(
    name = "client-java",
    srcs = glob(["*.java"]),
    resources = ["LICENSE"],
    tags = ["maven_coordinates=com.vaticle.typedb:typedb-client:{pom_version}"],
    deps = [
        # Internal dependencies
        "//api",
        "//connection",

        # External dependencies from @vaticle
        "@vaticle_typedb_common//:common",
    ],
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        ".factory/*",
    ]),
    exclude = glob([
        "*.md",
        ".bazelversion",
        ".bazel-remote-cache.rc",
        ".bazel-cache-credential.json",
        "LICENSE",
        "VERSION",
        "docs/*",
    ]),
    license_type = "apache-header",
)

checkstyle_test(
    name = "checkstyle-license",
    size = "small",
    include = ["LICENSE"],
    license_type = "apache-fulltext",
)

assemble_maven(
    name = "assemble-maven",
    project_description = "TypeDB Client API for Java",
    project_name = "TypeDB Client Java",
    project_url = "https://github.com/vaticle/typedb-client-java",
    scm_url = "https://github.com/vaticle/typedb-client-java",
    target = ":client-java",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@vaticle_typedb_client_java_workspace_refs//:refs.json",
)

deploy_maven(
    name = "deploy-maven",
    release = deployment["maven.release"],
    snapshot = deployment["maven.snapshot"],
    target = ":assemble-maven",
)

deploy_github(
    name = "deploy-github",
    draft = False,
    title = "TypeDB Client Java",
    release_description = "//:RELEASE_NOTES_LATEST.md",
    organisation = github_deployment["github.organisation"],
    repository = github_deployment["github.repository"],
    title_append_version = True,
)

release_validate_deps(
    name = "release-validate-deps",
    refs = "@vaticle_typedb_client_java_workspace_refs//:refs.json",
    tagged_deps = [
        "@vaticle_typedb_common",
        "@vaticle_typeql",
        "@vaticle_typedb_protocol",
        "@vaticle_factory_tracing",
    ],
    tags = ["manual"],  # in order for bazel test //... to not fail
)

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@vaticle_dependencies//tool/checkstyle:test-coverage",
        "@vaticle_dependencies//tool/bazelinstall:remote_cache_setup.sh",
        "@vaticle_dependencies//tool/release/notes:create",
        "@vaticle_dependencies//tool/sonarcloud:code-analysis",
        "@vaticle_dependencies//tool/unuseddeps:unused-deps",
    ],
)
