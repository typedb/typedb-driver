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

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//http-ts/tests/behaviour:rules.bzl", "typedb_behaviour_http_ts_test")

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)

typedb_behaviour_http_ts_test(
    name = "query",
    features = ["@typedb_behaviour//driver:query.feature"],
    data = [],
)

typedb_behaviour_http_ts_test(
    name = "user",
    features = ["@typedb_behaviour//driver:user.feature"],
    data = [],
)

typedb_behaviour_http_ts_test(
    name = "connection",
    features = ["@typedb_behaviour//driver:connection.feature"],
    data = [],
)

typedb_behaviour_http_ts_test(
    name = "concept",
    features = ["@typedb_behaviour//driver:concept.feature"],
    data = [],
)
