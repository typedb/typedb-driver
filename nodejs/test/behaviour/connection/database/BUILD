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
load("//nodejs/test/behaviour:rules.bzl", "typedb_behaviour_node_test", "behaviour_test_ts_config")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

ts_project(
    name = "steps",
    srcs = ["DatabaseSteps.ts"],
    tsconfig = behaviour_test_ts_config(),
    deps = [
        "//nodejs:driver-nodejs",
        "//nodejs:node_modules/@cucumber/cucumber",
        "//nodejs/test/behaviour/util:util",
        "//nodejs/test/behaviour/connection:steps-base",
    ],
    transpiler = "tsc",
    resolve_json_module = True,
    visibility = ["//visibility:public"],
)

typedb_behaviour_node_test(
    name = "test",
    features = ["@typedb_behaviour//connection:database.feature"],
    data = [":steps"],
)
