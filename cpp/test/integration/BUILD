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

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_dependencies//builder/cpp:rules.bzl", "clang_format_test")
load("//cpp:build_opts.bzl", "cxxopts")

cc_test(
    name = "test-cpp-driver-core",
    srcs = ["test_core.cpp"],
    deps = [
        "//cpp:typedb-driver-cpp-import",
        # External
        "@gtest//:gtest",
        "@gtest//:gtest_main"
    ],
    copts = cxxopts,
    includes = ["include"],
    env = {"RUST_BACKTRACE": "full"},
    linkstatic = True
)

cc_test(
    name = "test-cpp-driver-cloud",
    srcs = ["test_cloud.cpp"],
    deps = [
        "//cpp:typedb-driver-cpp-import",
        # External
        "@gtest//:gtest",
        "@gtest//:gtest_main"
    ],
    copts = cxxopts,
    includes = ["include"],
    env = {"RUST_BACKTRACE": "full"},
    linkstatic = True
)

clang_format_test(
    name = "clang_format",
    include = glob(["*"]),
    exclude = ["BUILD"]
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    license_type = "apache-header",
)
