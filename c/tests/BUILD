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

package(default_visibility = ["//visibility:public"])
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

cc_test(
    name = "test-database",
    srcs = glob(["integration/*.*"]),
    includes = ["."],
    deps = [
        "//c:typedb_driver_clib_headers",
        "//c:typedb_driver_clib"
    ],
    env = {"RUST_BACKTRACE" : "full"},
    # `-fsanitize=undefined` address is useful memory mistakes
    copts = ["-fsanitize=address"],# ["-fsanitize=undefined"],
    linkopts = ["-fsanitize=address"],# ["-fsanitize=undefined"],
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        "src/**/*",
        "swig/*",
    ]),
    license_type = "apache-header",
)
