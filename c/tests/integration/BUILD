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

package(default_visibility = ["//visibility:public"])
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_dependencies//builder/cpp:rules.bzl", "clang_format_test")

cc_library(
    name = "lib-test-driver",
    srcs = glob(["*.c", "*.h"]),
    deps = [
        "//c:typedb_driver_clib_headers",
        "//c:typedb_driver_clib"
    ],
    copts = ["-fsanitize=address"], # Remove this if you're running leaks
    linkopts = ["-fsanitize=address"], # Remove this if you're running leaks
    linkstatic = True,
)

cc_test(
    name = "test-driver",
    srcs = glob(["gtest_runner.cpp"]),
    deps = [
        ":lib-test-driver",
        "//c:typedb_driver_clib_headers",
        "//c:typedb_driver_clib",

        # External
        "@gtest//:gtest",
        "@gtest//:gtest_main"
    ],
    env = {"RUST_BACKTRACE": "full"}
)

clang_format_test(
    name = "clang_format",
    include = glob(["*.c", "*.h"])
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
    ]),
    license_type = "apache-header",
)
