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

load("@rules_python//python:defs.bzl", "py_test")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_driver_pip//:requirements.bzl", "requirement")

py_test(
    name = "test_failover",
    srcs = ["test_failover.py"],
    deps = [
        "//python:driver_python",
    ],
    data = [
        "//python:native-driver-binary-link",
        "//python:native-driver-wrapper-link",
        "//tool/test:cluster-server.sh",
        "//tool/test/resources:encryption/ext-grpc-root-ca.pem",
    ],
    env = {
        "CLUSTER_SERVER_SCRIPT": "$(rootpath //tool/test:cluster-server.sh)",
        "ROOT_CA": "$(rootpath //tool/test/resources:encryption/ext-grpc-root-ca.pem)",
    },
    # The test drives an out-of-sandbox cluster started by start-cluster-servers.sh:
    # it kills/restarts nodes via cluster-server.sh, which writes to log files and
    # data directories under $CLUSTER_DIR (the outer workspace). The Bazel sandbox
    # blocks writes to those paths, so this test must run unsandboxed and locally,
    # and it relies on host-level state so cached results would be misleading.
    tags = ["external", "no-cache", "no-sandbox"],
    size = "large",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
