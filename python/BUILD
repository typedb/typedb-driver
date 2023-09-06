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
       vaticle_typedb_client_python_requirement = "requirement", "entry_point")

load("@vaticle_bazel_distribution//pip:rules.bzl", "assemble_pip", "deploy_pip")
load("@vaticle_bazel_distribution_pip//:requirements.bzl", vaticle_bazel_distribution_requirement = "requirement")
load("@vaticle_bazel_distribution//github:rules.bzl", "deploy_github")

load("@vaticle_dependencies//builder/swig:python.bzl", "swig_python", "dyn_lib")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")

load("//python/tool:multiversion.bzl", "build_and_deploy")
load("//python:python_versions.bzl", "python_versions")


build_and_deploy(python_versions = python_versions)

genrule(
    name = "native-client-wrapper",
    outs = ["typedb/native_client_wrapper.py"],
    srcs = [":native-client-wrapper39"],
    cmd = "cp $< $@",
    visibility = ["//visibility:public"]
)

dyn_lib(
    name = "native-client-binary",
    out = "typedb/native_client_python",
    src = ":native_client_python39",
    visibility = ["//visibility:public"]
)

#swig_python(
#    name = "native_client_python_wrapper",
#    shared_lib_name = "native_client_python",
#    lib = "//rust:typedb_client_clib_headers",
#    interface = "//rust:typedb_client.i",
#    includes = ["//rust:swig/typedb_client_python.swg"],
#    enable_cxx = True,
#    linkopts = select({
#        "@vaticle_dependencies//util/platform:is_windows": ["ntdll.lib"],
#        "//conditions:default": [],
#    })
#)
#
#genrule(
#    name = "native-client-wrapper",
#    outs = ["typedb/native_client_wrapper.py"],
#    srcs = [":native_client_python_wrapper"],
#    cmd = "cp $< $@",
#    visibility = ["//visibility:public"]
#)
#
#dyn_lib(
#    name = "native-client-binary",
#    out = "typedb/native_client_python",
#    src = ":native_client_python",
#    visibility = ["//visibility:public"]
#)

alias(
    name = "behave-exec",
    actual = entry_point(
        pkg = "behave",
        script = "behave",
    ),
    visibility = ["//visibility:public"],
)

#py_library(
#    name = "client_python",
#    srcs = glob(["typedb/**/*.py"]) + [":native-client-wrapper"],
#    data = [":native-client-binary"],
#    deps = [":native_client_python_wrapper"],
#    imports = ["."],
#    visibility = ["//visibility:public"]
#)

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

#assemble_pip(
#    name = "assemble-pip",
#    target = ":client_python",
#    package_name = "typedb-client",
#    classifiers = [
#        "Programming Language :: Python",
#        "Programming Language :: Python :: 3",
#        "Programming Language :: Python :: 3.5",
#        "Programming Language :: Python :: 3.6",
#        "Programming Language :: Python :: 3.7",
#        "Programming Language :: Python :: 3.8",
#        "Programming Language :: Python :: 3.9",
#        "License :: OSI Approved :: Apache Software License",
#        "Operating System :: OS Independent",
#        "Intended Audience :: Developers",
#        "Intended Audience :: Science/Research",
#        "Environment :: Console",
#        "Topic :: Database :: Front-Ends"
#    ],
#    url = "https://github.com/vaticle/typedb-driver-python/",
#    author = "Vaticle",
#    author_email = "community@vaticle.com",
#    license = "Apache-2.0",
#    requirements_file = "//python:requirements.txt",
#    keywords = ["typedb", "database", "graph", "knowledgebase", "knowledge-engineering"],
#    description = "TypeDB Client for Python",
#    long_description_file = "//python:README.md",
#)
#
#deploy_pip(
#    name = "deploy-pip",
#    target = ":assemble-pip",
#    snapshot = deployment["pypi.snapshot"],
#    release = deployment["pypi.release"],
#    distribution_tag = select({
#        "@vaticle_dependencies//util/platform:is_mac_arm64": "py3-none-macosx_11_0_arm64",
#        "@vaticle_dependencies//util/platform:is_mac_x86_64": "py3-none-macosx_11_0_x86_64",
#        "@vaticle_dependencies//util/platform:is_linux_arm64": "py3-none-linux_aarch64",
#        "@vaticle_dependencies//util/platform:is_linux_x86_64": "py3-none-linux_x86_64",
#        "@vaticle_dependencies//util/platform:is_windows": "py3-none-win_amd64",
#    })
#)

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
