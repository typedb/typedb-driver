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

typedb_behaviour_cpp_test(
    name = "rule-test",
    feature = "@vaticle_typedb_behaviour//concept/rule:rule.feature"
)

typedb_behaviour_cpp_test(
    name = "attribute-test",
    feature = "@vaticle_typedb_behaviour//concept/thing:attribute.feature"
)

typedb_behaviour_cpp_test(
    name = "entity-test",
    feature = "@vaticle_typedb_behaviour//concept/thing:entity.feature"
)

typedb_behaviour_cpp_test(
    name = "relation-test",
    feature = "@vaticle_typedb_behaviour//concept/thing:relation.feature"
)

typedb_behaviour_cpp_test(
    name = "attributetype-test",
    feature = "@vaticle_typedb_behaviour//concept/type:attributetype.feature"
)

typedb_behaviour_cpp_test(
    name = "entitytype-test",
    feature = "@vaticle_typedb_behaviour//concept/type:entitytype.feature"
)

typedb_behaviour_cpp_test(
    name = "relationtype-test",
    feature = "@vaticle_typedb_behaviour//concept/type:relationtype.feature"
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
    size = "small",
)
