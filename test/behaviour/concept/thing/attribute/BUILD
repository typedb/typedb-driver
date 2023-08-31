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

load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//test/behaviour:rules.bzl", "typedb_behaviour_node_test", "behaviour_test_ts_config")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

ts_project(
    name = "steps",
    srcs = ["AttributeSteps.ts"],
    tsconfig = behaviour_test_ts_config(),
    deps = [
        "//:client-nodejs",
        "//:node_modules/@cucumber/cucumber",
        "//test/behaviour/concept/thing:steps",
        "//test/behaviour/config:parameters",
        "//test/behaviour/connection:steps-base",
        "//test/behaviour/util:util",
    ],
    resolve_json_module = True,
    visibility = ["//visibility:public"],
)

typedb_behaviour_node_test(
    name = "test",
    features = ["@vaticle_typedb_behaviour//concept/thing:attribute.feature"],
    data = [
        ":steps",
        "//test/behaviour/concept/type/attributetype:steps",
        "//test/behaviour/concept/type/thingtype:steps",
        "//test/behaviour/connection/database:steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/connection/transaction:steps",
        "//test/behaviour/util:steps",
    ],
)
