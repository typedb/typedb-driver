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

load("@vaticle_dependencies//builder/swig:python.bzl", "py_native_lib_rename")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//python:doc/rules.bzl", "sphinx_doc")

load("//python:rules.bzl", "native_driver_versioned")
load("//python:python_versions.bzl", "python_versions")

load("@vaticle_dependencies_tool_doc//:requirements.bzl", doc_requirement = "requirement")

load("@rules_foreign_cc//foreign_cc:defs.bzl", "make")

native_driver_versioned(python_versions = python_versions)

alias(
    name = "driver_python",
    actual = ":driver_python39",
    visibility = ["//visibility:public"],
)

genrule(
    name = "native-driver-wrapper-link",
    outs = ["typedb/native_driver_wrapper.py"],
    srcs = [":native-driver-wrapper39"],
    cmd = "cp $< $@",
    visibility = ["//visibility:public"]
)

py_native_lib_rename(
    name = "native-driver-binary-link",
    out = "typedb/native_driver_python",
    src = ":native_driver_python39",
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        "*.bzl",
        ".bazelrc",
        ".factory/*",
        ".gitignore",
        "BUILD",
        "WORKSPACE",
        "requirements*.txt",
        "typedb/**/*",
    ]),
    license_type = "apache-header",
    size = "small",
)

# Documentation generation
py_binary(
    name = "sphinx_runner",
    srcs = [
        "doc/sphinx_html_builder_runner.py",
    ],
    main = "doc/sphinx_html_builder_runner.py",
    deps = [doc_requirement("sphinx")],
    visibility = ["//visibility:public"]
)

sphinx_doc(
    name = "driver_doc",
    script = ":sphinx_runner",
    target = ":assemble-pip39",
    srcs = glob(["doc/source/*"]),
    out = "doc/build",
)

#parse_python_doc(
#    name = "typedb_client_doc_parsed",
#    crate = ":typedb_client_doc",
#)

# CI targets that are not declared in any BUILD_ file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@vaticle_dependencies//tool/checkstyle:test-coverage",
        "@vaticle_dependencies//distribution/artifact:create-netrc",
        "@vaticle_dependencies//tool/release/notes:create",
    ],
)
