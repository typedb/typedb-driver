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

load("//python/tests/integration:cloud_test_rule.bzl", "typedb_cloud_py_test")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_typedb_driver_pip//:requirements.bzl", "requirement")
load("@rules_python//python:defs.bzl", "py_test")

py_test(
    name = "test_core_connection",
    srcs = ["test_core_connection.py"],
    deps = [
        "//python:driver_python",
        requirement("PyHamcrest"),
    ],
    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
    python_version = "PY3"
)

py_test(
    name = "test_query",
    srcs = ["test_query.py"],
    deps = [
        "//python:driver_python",
    ],
    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
    python_version = "PY3"
)

py_test(
    name = "test_stream",
    srcs = ["test_stream.py"],
    deps = [
        "//python:driver_python",
    ],
    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
    python_version = "PY3"
)

#typedb_cloud_py_test(
#    name = "test_cloud_failover",
#    srcs = ["test_cloud_failover.py"],
#    deps = [
#        "//python:driver_python",
#    ],
#    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
#    size = "medium",
#    native_typedb_cloud_artifact = "//tool/test:native-typedb-cloud-artifact",
#)

#py_test(
#    name = "test_cloud_connection",
#    srcs = ["test_cloud_connection.py"],
#    deps = [
#        "//python:driver_python",
#        requirement("PyHamcrest"),
#    ],
#    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
#    python_version = "PY3"
#)

py_test(
    name = "test_debug",
    srcs = ["test_debug.py"],
    deps = [
        "//python:driver_python",
    ],
    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
    python_version = "PY3"
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
