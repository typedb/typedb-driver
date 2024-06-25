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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@io_bazel_rules_go//go:def.bzl", "go_library")

go_library(
    name = "connection",
    srcs = glob(["*.go"],
    ),
    deps = [
        "//go/api/database:database",
        "//go:typedb_driver_go_native",
        "//go/api/user:user"
    ],
    importpath = "typedb_driver/go/connection",
    cgo = True
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    license_type = "apache-header",
)
