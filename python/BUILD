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
load("@vaticle_dependencies//tool/doc:rules.bzl", "python_doc")

load("//python:rules.bzl", "native_driver_versioned")
load("//python:python_versions.bzl", "python_versions")


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

#python_doc(
#    name = "client_doc",
#    data = ":assemble-pip39",
#)

#parse_rust_doc(
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
