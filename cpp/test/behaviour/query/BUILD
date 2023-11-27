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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

# TODO: Create typedb_behaviour_cpp_test rule
cc_test(
    name = "define-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:define.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:define.feature)"]
)

cc_test(
    name = "undefine-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:undefine.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:undefine.feature)"]
)

cc_test(
    name = "rule-validation-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:rule-validation.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:rule-validation.feature)"]
)

cc_test(
    name = "insert-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:insert.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:insert.feature)"]
)

cc_test(
    name = "delete-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:delete.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:delete.feature)"]
)

cc_test(
    name = "update-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:update.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:update.feature)"]
)

cc_test(
    name = "get-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:get.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:get.feature)"]
)

cc_test(
    name = "fetch-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:fetch.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:fetch.feature)"]
)

cc_test(
    name = "match-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:match.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:match.feature)"]
)

cc_test(
    name = "modifiers-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:modifiers.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:modifiers.feature)"]
)

cc_test(
    name = "expression-test",
    deps = ["//cpp/test/behaviour/steps:core-steps"],
    data = ["@vaticle_typedb_behaviour//query/language:expression.feature"],
    args = ["$(location @vaticle_typedb_behaviour//query/language:expression.feature)"]
)
