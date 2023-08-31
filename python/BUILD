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

exports_files(["requirements.txt", "deployment.bzl"])

load("@vaticle_typedb_client_python_pip//:requirements.bzl",
       vaticle_typedb_client_python_requirement = "requirement")

load("@vaticle_bazel_distribution//pip:rules.bzl", "assemble_pip", "deploy_pip")
load("@vaticle_bazel_distribution_pip//:requirements.bzl", vaticle_bazel_distribution_requirement = "requirement")
load("@vaticle_bazel_distribution//github:rules.bzl", "deploy_github")

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")


genrule(
    name = "native-client-wrapper",
    outs = ["typedb/native_client_wrapper.py"],
    srcs = ["//rust:native_client_python_wrapper"],
    cmd = "cp $< $@",
    visibility = ["//visibility:public"]
)

genrule(
    name = "native-client-binary",
    outs = ["typedb/native_client_python.so"],
    srcs = ["//rust:native_client_python"],
    cmd = "cp $< $@",
    visibility = ["//visibility:public"]
)

py_library(
    name = "client_python",
    srcs = glob(["typedb/**/*.py"]) + [":native-client-wrapper"],
    data = [":native-client-binary"],
    deps = ["//rust:native_client_python_wrapper"],
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
        "typedb/**/*",
    ]),
    license_type = "apache-header",
    size = "small",
)

assemble_pip(
    name = "assemble-pip",
    target = ":client_python",
    package_name = "typedb-client",
    classifiers = [
        "Programming Language :: Python",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.5",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
        "Intended Audience :: Developers",
        "Intended Audience :: Science/Research",
        "Environment :: Console",
        "Topic :: Database :: Front-Ends"
    ],
    url = "https://github.com/vaticle/typedb-driver-python/",
    author = "Vaticle",
    author_email = "community@vaticle.com",
    license = "Apache-2.0",
    requirements_file = "//python:requirements.txt",
    keywords = ["typedb", "database", "graph", "knowledgebase", "knowledge-engineering"],
    description = "TypeDB Client for Python",
    long_description_file = "//python:README.md",
)

deploy_pip(
    name = "deploy-pip",
    target = ":assemble-pip",
    snapshot = deployment["pypi.snapshot"],
    release = deployment["pypi.release"],
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

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@vaticle_dependencies//tool/checkstyle:test-coverage",
        "@vaticle_dependencies//distribution/artifact:create-netrc",
        "@vaticle_dependencies//tool/release/notes:create",
    ],
)
