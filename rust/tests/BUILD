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
load("@vaticle_bazel_distribution//artifact:rules.bzl", "artifact_extractor")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_typedb_common//test:rules.bzl", "native_typedb_artifact")

rust_test(
    name = "tests",
    srcs = glob(["**/*.rs"]),
    timeout = "eternal",
    deps = [
        "//rust:typedb_client",
        "@vaticle_typeql//rust:typeql_lang",
        "@crates//:async-std",
        "@crates//:chrono",
        "@crates//:cucumber",
        "@crates//:futures",
        "@crates//:regex",
        "@crates//:serial_test",
        "@crates//:smol",
        "@crates//:tokio",
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
        "@vaticle_typedb_behaviour//typeql/language:define.feature",
        "@vaticle_typedb_behaviour//typeql/language:undefine.feature",
        "@vaticle_typedb_behaviour//typeql/language:insert.feature",
        "@vaticle_typedb_behaviour//typeql/language:delete.feature",
        "@vaticle_typedb_behaviour//typeql/language:update.feature",
        "@vaticle_typedb_behaviour//typeql/language:match.feature",
        "@vaticle_typedb_behaviour//typeql/language:get.feature",
        "@vaticle_typedb_behaviour//typeql/language:expression.feature",
        "@vaticle_typedb_behaviour//typeql/language:rule-validation.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:attribute-attachment.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:compound-queries.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:concept-inequality.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:negation.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:recursion.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:relation-inference.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:rule-interaction.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:schema-queries.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:type-hierarchy.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:value-predicate.feature",
        "@vaticle_typedb_behaviour//typeql/reasoner:variable-roles.feature",
    ],
)

native_typedb_artifact(
    name = "native-typedb-artifact",
    mac_artifact = "@vaticle_typedb_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_artifact_windows//file",
    output = "typedb-artifact.tar.gz"
)

native_typedb_artifact(
    name = "native-typedb-cluster-artifact",
    mac_artifact = "@vaticle_typedb_cluster_artifact_mac//file",
    linux_artifact = "@vaticle_typedb_cluster_artifact_linux//file",
    windows_artifact = "@vaticle_typedb_cluster_artifact_windows//file",
    output = "typedb-cluster-artifact.tar.gz"
)

artifact_extractor(
    name = "typedb-extractor",
    artifact = ":native-typedb-artifact",
)

artifact_extractor(
    name = "typedb-cluster-extractor",
    artifact = ":native-typedb-cluster-artifact",
)

rustfmt_test(
    name = "rustfmt_test",
    targets = ["tests"]
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "**/*"]),
    license_type = "apache-header",
    size = "small",
)
