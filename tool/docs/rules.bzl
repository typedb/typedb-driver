#
# Copyright (C) 2022 Vaticle
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

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")
load("@bazel_skylib//rules:run_binary.bzl", "run_binary")
load("@vaticle_dependencies_tool_doc//:requirements.bzl", "requirement")

def html_doc_parser(name, data, language):
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
        outs = [name + "_adoc"],
        args = [
            "$(location %s)" % data,
            "$(location %s_adoc)" % name,
        ],
        srcs = [data],
    )
