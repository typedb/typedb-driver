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

package(default_visibility = ["//python/tests/behaviour:__subpackages__"])
load("//python/tool:behave_rule.bzl", "typedb_behaviour_py_test")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

typedb_behaviour_py_test(
    name = "test",
    feats = ["@vaticle_typedb_behaviour//typeql/language:update.feature"],
    background_core = ["//python/tests/behaviour/background:core"],
    background_cluster = ["//python/tests/behaviour/background:cluster"],
    steps = [
        "//python/tests/behaviour/connection:steps",
        "//python/tests/behaviour/connection/database:steps",
        "//python/tests/behaviour/connection/session:steps",
        "//python/tests/behaviour/connection/transaction:steps",
        "//python/tests/behaviour/typeql:steps",
    ],
    deps = [
        "//python:client_python",
        "//python/tests/behaviour:context",
        "//python/tests/behaviour/util:util",
        "//python/tests/behaviour/config:parameters",
        "//python/tests/behaviour/background",
        "//python:native-client-binary",
    ],
    native_typedb_artifact = "//python/tests:native-typedb-artifact",
    native_typedb_cluster_artifact = "//python/tests:native-typedb-cluster-artifact",
    size = "medium",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
