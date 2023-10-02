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
load("@rules_rust//rust:defs.bzl", "rust_doc")
load("//tool/docs:rules.bzl", "html_doc_parser")

def rust_doc_parser(name, feature):
    rust_doc(
        name = "driver_doc_zip_" + feature,
        crate = ":typedb_driver",
        # TODO: Replace with rustdoc_flags after upgrading rules_rust to >=0.20
        rustc_flags = ["--cfg", "feature=\"" + feature + "\""],
    )

    native.genrule(
        name = "driver_doc_" + feature,
        srcs = [":driver_doc_zip_" + feature],
        outs = ["driver_doc_rustdoc_" + feature],
        cmd = "mkdir $@ && tar -xf $< -C $@",
    )

    html_doc_parser(
        name = name,
        data = ":driver_doc_" + feature,
        language = "rust",
    )
