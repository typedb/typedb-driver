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

load(":cluster_test_rule.bzl", "typedb_cloud_py_test")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_driver_pip//:requirements.bzl", "requirement")

# TODO: This package can contain cluster-specific integration tests. Tests available for running for both community
# edition and cluster should be run for both setups (and be limited: always prefer BDDs to integration tests)!

#typedb_cloud_py_test(
#    name = "test_failover",
#    srcs = ["test_failover.py"],
#    deps = [
#        "//python:driver_python",
#    ],
#    data = ["//python:native-driver-binary-link", "//python:native-driver-wrapper-link"],
#    size = "medium",
#    native_typedb_cloud_artifact = "//tool/test:native-typedb-cloud-artifact",
#)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
