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

load("//python/tool:cluster_test_rule.bzl", "typedb_cluster_py_test")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@rules_python//python:defs.bzl", "py_test")

typedb_cluster_py_test(
    name = "test_cluster_failover",
    srcs = ["test_cluster_failover.py"],
    deps = [
        "//python:client_python",
        "//python:native-client-binary",
        ],
    size = "medium",
    native_typedb_cluster_artifact = "//python/tests:native-typedb-cluster-artifact",
)

py_test(
    name = "test_debug",
    srcs = ["test_debug.py"],
    deps = [
        "//python:client_python",
        ],
    data = ["//python:native-client-binary"],
    python_version = "PY3"
)

py_test(
    name = "test_stream",
    srcs = ["test_stream.py"],
    deps = [
        "//python:client_python",
        ],
    data = ["//python:native-client-binary"],
    python_version = "PY3"
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
