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

load("@rules_dotnet//dotnet:defs.bzl", "csharp_binary", "csharp_library")
load(":build_opts.bzl", "target_frameworks", "targeting_packs")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load(":rules.bzl", "swig_native_csharp_library")


swig_native_csharp_library(
    name = "typedb_driver_pinvoke",
    lib = "//c:typedb_driver_clib_headers",
    namespace = "com.vaticle.typedb.driver.pinvoke",
    interface = "//c:typedb_driver.i",
    includes = ["//c:swig/typedb_driver_csharp.swg"],
    enable_cxx = True,
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
    visibility = ["//visibility:public"],
#    platforms = { # TODO: We'll need to build for different platforms?
#        "@vaticle_bazel_distribution//platform:is_linux_arm64": "linux-arm64",
#        "@vaticle_bazel_distribution//platform:is_linux_x86_64": "linux-x86_64",
#        "@vaticle_bazel_distribution//platform:is_mac_arm64": "macosx-arm64",
#        "@vaticle_bazel_distribution//platform:is_mac_x86_64": "macosx-x86_64",
#        "@vaticle_bazel_distribution//platform:is_windows_x86_64": "windows-x86_64",
#    },
)


csharp_library(
    name = "driver-csharp",
    srcs = glob(["*.cs"]),
    resources = ["//:LICENSE"],
    deps = [
        "//csharp/api",
        "//csharp/common",
        "//csharp/connection",
        "//csharp:typedb_driver_pinvoke",
    ],
    visibility = ["//visibility:public"],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)


checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = glob([
        "README.md",
        "docs/**/*.adoc",
    ]),
    license_type = "apache-header",
)
