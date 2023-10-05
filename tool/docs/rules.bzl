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

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")
load("@bazel_skylib//rules:run_binary.bzl", "run_binary")
load("@vaticle_dependencies_tool_docs//:requirements.bzl", "requirement")

def html_docs_parser(name, data, language):
    script_name = language.title() + "DocParser"

    kt_jvm_library(
        name = name + "_lib",
        srcs = [
            "//tool/docs:" + language + "/" + script_name + ".kt",
            "//tool/docs:common/Argument.kt",
            "//tool/docs:common/Class.kt",
            "//tool/docs:common/EnumConstant.kt",
            "//tool/docs:common/Helper.kt",
            "//tool/docs:common/Method.kt",
        ],
        deps = [
            "@maven//:org_jsoup_jsoup",
        ],
    )

    native.java_binary(
        name = name + "_script",
        runtime_deps = [name + "_lib"],
        main_class = "com.vaticle.typedb.client.tool.doc." + language + "." + script_name + "Kt",
        visibility = ["//visibility:public"]
    )

    run_binary(
        name = name,
        tool = name + "_script",
        outs = [name + "_parsed"],
        args = [
            "$(location %s)" % data,
            "$(location %s_parsed)" % name,
        ],
        srcs = [data],
    )
