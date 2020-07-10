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
load("@graknlabs_common//test/server:rules.bzl", "grakn_test")

grakn_test(
    name = "test-assembly-query",
    srcs = [
        "QueryTest.java",
    ],
    test_class = "grakn.client.test.assembly.QueryTest",
    deps = [
        # Internal dependencies
        "//:client-java",

        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",

        # External dependencies from Maven
        "@maven//:org_hamcrest_hamcrest_library",
    ],
    grakn_artifact = "@graknlabs_grakn_core_artifact//file",
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":test-assembly-query",
    ],
)
