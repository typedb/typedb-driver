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
load("//cpp/test/behaviour:tests.bzl", "typedb_behaviour_cpp_test")

# TODO: Create typedb_behaviour_cpp_test rule
typedb_behaviour_cpp_test(
    name = "define-test",
    feature = "@vaticle_typedb_behaviour//query/language:define.feature"
)

typedb_behaviour_cpp_test(
    name = "undefine-test",
    feature = "@vaticle_typedb_behaviour//query/language:undefine.feature"
)

typedb_behaviour_cpp_test(
    name = "rule-validation-test",
    feature = "@vaticle_typedb_behaviour//query/language:rule-validation.feature"
)

typedb_behaviour_cpp_test(
    name = "insert-test",
    feature = "@vaticle_typedb_behaviour//query/language:insert.feature"
)

typedb_behaviour_cpp_test(
    name = "delete-test",
    feature = "@vaticle_typedb_behaviour//query/language:delete.feature"
)

typedb_behaviour_cpp_test(
    name = "update-test",
    feature = "@vaticle_typedb_behaviour//query/language:update.feature"
)

typedb_behaviour_cpp_test(
    name = "get-test",
    feature = "@vaticle_typedb_behaviour//query/language:get.feature"
)

typedb_behaviour_cpp_test(
    name = "fetch-test",
    feature = "@vaticle_typedb_behaviour//query/language:fetch.feature"
)

typedb_behaviour_cpp_test(
    name = "match-test",
    feature = "@vaticle_typedb_behaviour//query/language:match.feature"
)

typedb_behaviour_cpp_test(
    name = "modifiers-test",
    feature = "@vaticle_typedb_behaviour//query/language:modifiers.feature"
)

typedb_behaviour_cpp_test(
    name = "expression-test",
    feature = "@vaticle_typedb_behaviour//query/language:expression.feature"
)
