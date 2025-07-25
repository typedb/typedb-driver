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

load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//http-ts/tests/behaviour:rules.bzl", "behaviour_test_ts_config")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

ts_project(
    name = "steps",
    srcs = ["connection.ts", "context.ts", "database.ts", "params.ts", "query.ts", "transaction.ts", "user.ts", "util.ts"],
    tsconfig = behaviour_test_ts_config(),
    deps = [
        "//http-ts:driver-lib-dev",
        "//http-ts:node_modules/@cucumber/cucumber",
        "//http-ts:node_modules/@types/node",
    ],
    transpiler = "tsc",
    declaration = True,
    resolve_json_module = True,
    visibility = ["//visibility:public"],
)
