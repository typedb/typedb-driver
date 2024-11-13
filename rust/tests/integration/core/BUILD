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

load("@rules_rust//rust:defs.bzl", "rust_test")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

exports_files(
    ["example.rs"],
    visibility = ["//rust:__subpackages__"],
)

# rust_test(
#     name = "test_example",
#     srcs = glob(["example.rs"]),
#     deps = [
#         "//rust:typedb_driver",
#         "@crates//:async-std",
#         "@crates//:chrono",
#         "@crates//:futures",
#         "@crates//:itertools",
#         "@crates//:regex",
#         "@crates//:serde_json",
#         "@crates//:serial_test",
#         "@crates//:smol",
#         "@crates//:tokio",
#         "@crates//:uuid",
#     ],
# )

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
