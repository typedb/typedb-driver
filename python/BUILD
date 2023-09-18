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

load("@vaticle_bazel_distribution_pip//:requirements.bzl", vaticle_bazel_distribution_requirement = "requirement")
load("@vaticle_bazel_distribution//github:rules.bzl", "deploy_github")

load("@vaticle_dependencies//builder/swig:python.bzl", "py_native_lib_rename")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//:deployment.bzl", github_deployment = "deployment")

load("//python:rules.bzl", "native_driver_versioned")
load("//python:python_versions.bzl", "python_versions")


native_driver_versioned(python_versions = python_versions)

alias(
    name = "client_python",
    actual = ":client_python39",
    visibility = ["//visibility:public"],
)

genrule(
    name = "native-client-wrapper-link",
    outs = ["typedb/native_client_wrapper.py"],
    srcs = [":native-client-wrapper39"],
    cmd = "cp $< $@",
    visibility = ["//visibility:public"]
)

py_native_lib_rename(
    name = "native-client-binary-link",
    out = "typedb/native_client_python",
    src = ":native_client_python39",
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        ".bazelrc",
        ".gitignore",
        ".factory/*",
        "BUILD",
        "WORKSPACE",
        "deployment.bzl",
        "requirements*.txt",
        "python_versions.bzl",
        "typedb/**/*",
    ]),
    license_type = "apache-header",
    size = "small",
)

deploy_github(
    name = "deploy-github",
    release_description = "//:RELEASE_NOTES_LATEST.md",
    title = "TypeDB Client Python",
    title_append_version = True,
    organisation = github_deployment["github.organisation"],
    repository = github_deployment["github.repository"],
    draft = False
)

# CI targets that are not declared in any BUILD_ file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@vaticle_dependencies//tool/checkstyle:test-coverage",
        "@vaticle_dependencies//distribution/artifact:create-netrc",
        "@vaticle_dependencies//tool/release/notes:create",
    ],
)
