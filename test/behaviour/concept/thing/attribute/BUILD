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

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("//test/behaviour:rules.bzl", "grakn_behaviour_node_test")

exports_files(["AttributeSteps.ts"])

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)

grakn_behaviour_node_test(
    name = "test",
    features = ["@graknlabs_behaviour//concept/thing:attribute.feature"],
    node_modules = "//:node_modules",
    package_json = "//:package.json",
    native_grakn_artifact_core = "//test:native-grakn-core-artifact",
    native_grakn_artifact_cluster = "//test:native-grakn-cluster-artifact",
    client = "//:client-nodejs-targz",
    steps_core = "//test/behaviour:steps-core-targz",
    steps_cluster = "//test/behaviour:steps-cluster-targz",
)
