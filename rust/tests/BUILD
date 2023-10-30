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

package(default_visibility = ["//visibility:public"])

load("@rules_rust//rust:defs.bzl", "rust_test", "rustfmt_test")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

rust_test(
    name = "tests",
    srcs = glob(["**/*.rs"]),
    timeout = "eternal",
    deps = [
        "//rust:typedb_driver",
        "@vaticle_typeql//rust:typeql",
        "@crates//:async-std",
        "@crates//:chrono",
        "@crates//:cucumber",
        "@crates//:futures",
        "@crates//:regex",
        "@crates//:serde_json",
        "@crates//:serial_test",
        "@crates//:smol",
        "@crates//:tokio",
        "@crates//:uuid",
    ],
    data = [
        "@vaticle_typedb_behaviour//concept/rule:rule.feature",
        "@vaticle_typedb_behaviour//concept/thing:entity.feature",
        "@vaticle_typedb_behaviour//concept/thing:relation.feature",
        "@vaticle_typedb_behaviour//concept/thing:attribute.feature",
        "@vaticle_typedb_behaviour//concept/type:entitytype.feature",
        "@vaticle_typedb_behaviour//concept/type:relationtype.feature",
        "@vaticle_typedb_behaviour//concept/type:attributetype.feature",
        "@vaticle_typedb_behaviour//connection:database.feature",
        "@vaticle_typedb_behaviour//connection:session.feature",
        "@vaticle_typedb_behaviour//connection:transaction.feature",
        "@vaticle_typedb_behaviour//connection:user.feature",
        "@vaticle_typedb_behaviour//driver:query.feature",
        "@vaticle_typedb_behaviour//query/language:define.feature",
        "@vaticle_typedb_behaviour//query/language:undefine.feature",
        "@vaticle_typedb_behaviour//query/language:insert.feature",
        "@vaticle_typedb_behaviour//query/language:delete.feature",
        "@vaticle_typedb_behaviour//query/language:update.feature",
        "@vaticle_typedb_behaviour//query/language:match.feature",
        "@vaticle_typedb_behaviour//query/language:get.feature",
        "@vaticle_typedb_behaviour//query/language:modifiers.feature",
        "@vaticle_typedb_behaviour//query/language:fetch.feature",
        "@vaticle_typedb_behaviour//query/language:expression.feature",
        "@vaticle_typedb_behaviour//query/language:rule-validation.feature",
        "@vaticle_typedb_behaviour//query/reasoner:attribute-attachment.feature",
        "@vaticle_typedb_behaviour//query/reasoner:compound-queries.feature",
        "@vaticle_typedb_behaviour//query/reasoner:concept-inequality.feature",
        "@vaticle_typedb_behaviour//query/reasoner:negation.feature",
        "@vaticle_typedb_behaviour//query/reasoner:recursion.feature",
        "@vaticle_typedb_behaviour//query/reasoner:relation-inference.feature",
        "@vaticle_typedb_behaviour//query/reasoner:rule-interaction.feature",
        "@vaticle_typedb_behaviour//query/reasoner:schema-queries.feature",
        "@vaticle_typedb_behaviour//query/reasoner:type-hierarchy.feature",
        "@vaticle_typedb_behaviour//query/reasoner:value-predicate.feature",
        "@vaticle_typedb_behaviour//query/reasoner:variable-roles.feature",
    ],
)

rustfmt_test(
    name = "rustfmt_test",
    targets = ["tests"],
    size = "small",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "**/*"]),
    license_type = "apache-header",
    size = "small",
)
