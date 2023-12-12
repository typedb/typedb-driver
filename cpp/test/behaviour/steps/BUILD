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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//cpp/test/behaviour:tests.bzl", "typedb_behaviour_cpp_test")
load("@vaticle_dependencies//builder/cpp:rules.bzl", "clang_format_test")
load("//cpp:build_opts.bzl", "cxxopts")

cc_library(
    name = "core-steps",
    deps = [
        "//cpp:typedb-driver-cpp-import",
        "//cpp/test/cucumber:cucumber-cpp",

        # External
        "@gherkin_cpp//:gherkin-lib",
        "@gtest//:gtest",
        "@gtest//:gtest_main",
    ],
    copts = cxxopts,
    includes = ["external/gherkin_cpp/include"],
    srcs = glob(["main.cpp", "core_steps.cpp", "common/**/*.cpp"]),
    hdrs = glob(["include/*.hpp"]),
    strip_include_prefix = "include",
    visibility = ["//cpp/test/behaviour:__subpackages__"],
)

cc_library(
    name = "cloud-steps",
    deps = [
        "//cpp:typedb-driver-cpp-import",
        "//cpp/test/cucumber:cucumber-cpp",

        # External
        "@gherkin_cpp//:gherkin-lib",
        "@gtest//:gtest",
        "@gtest//:gtest_main",
    ],
    copts = ["-g -Iexternal/gherkin_cpp/include -std=c++17"],
    srcs = glob(["main.cpp", "cloud_steps.cpp", "common/**/*.cpp"]),
    hdrs = glob(["include/*.hpp"]),
    strip_include_prefix = "include",
    visibility = ["//cpp/test/behaviour:__subpackages__"],
)

clang_format_test(
    name = "clang_format",
    include = glob(["**/*"]),
    exclude = ["BUILD"]
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["**/*"]),
    license_type = "apache-header",
)
