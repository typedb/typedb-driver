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

package(default_visibility = ["//visibility:public"])

load("//rust/tests/behaviour:rules.bzl", "rust_behaviour_test")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

rust_behaviour_test(
    name = "test_concept",
    srcs = ["concept.rs"],
    data = ["@typedb_behaviour//driver:concept.feature"],
    size = "large", # TODO: This is a sign of not optimal server. Remove when the execution takes less than 5 mins!
)

rust_behaviour_test(
    name = "test_connection",
    srcs = ["connection.rs"],
    data = ["@typedb_behaviour//driver:connection.feature"],
)

rust_behaviour_test(
    name = "test_migration",
    srcs = ["migration.rs"],
    data = ["@typedb_behaviour//driver:migration.feature"],
)

rust_behaviour_test(
    name = "test_query",
    srcs = ["query.rs"],
    data = ["@typedb_behaviour//driver:query.feature"],
)

rust_behaviour_test(
    name = "test_user",
    srcs = ["user.rs"],
    data = ["@typedb_behaviour//driver:user.feature"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    exclude = glob([
        "Cargo.*",
    ]),
    license_type = "apache-header",
    size = "small",
)
