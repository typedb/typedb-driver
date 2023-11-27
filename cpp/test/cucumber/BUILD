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
load("@vaticle_dependencies//builder/cpp:rules.bzl", "clang_format_test")


cc_library(
    name = "cucumber-cpp",
    srcs = glob(["lib/**/*.cpp", "lib/**/*.hpp"]),
    hdrs = glob(["include/cucumber_bdd/*.hpp"]),
    deps = [
        "@gherkin_cpp//:gherkin-lib",
        "@gtest//:gtest",
    ],
    copts = ["-Iexternal/gherkin_cpp/include  -std=c++17"],
    strip_include_prefix = "include",
    #  additional_linker_inputs = ["@gherkin_cpp//:gherkin-lib"], # Need it for runtime if using .so
    visibility = ["//visibility:public"],
)

cc_test(
    name = "test-cucumber-lib",
    srcs = glob(["test/*.cpp", "test/*.hpp"]),
    deps = [
        ":cucumber-cpp",

        # External
        "@gherkin_cpp//:gherkin-lib",
        "@gtest//:gtest",
        "@gtest//:gtest_main"
    ],
    data = ["test/test.feature"],
    copts = ["-Iexternal/gherkin_cpp/include -Iinclude  -std=c++17"],
)

clang_format_test(
    name = "clang_format",
    include = glob(["include/**/*", "lib/**/*", "test/**/*"]),
    exclude = ["test/test.feature"]
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["**/*"]),
    license_type = "apache-header",
)
