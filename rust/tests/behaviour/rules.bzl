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

load("@rules_rust//rust:defs.bzl", "rust_test")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load(":defs.bzl", "crate_features_common")

behaviour_test_deps = [
    "//rust/tests/behaviour/steps",
    "//rust/tests/behaviour/config",

    "@crates//:tokio",
    "@crates//:serial_test",
];

def rust_behaviour_test(name, srcs, data, deps=[], crate_features=[], **kwargs):
    rust_test(
        name = name,
        srcs = srcs,
        data = data,
        deps = behaviour_test_deps + [] + deps,
        crate_features = crate_features_common + crate_features,
        **kwargs,
    )
