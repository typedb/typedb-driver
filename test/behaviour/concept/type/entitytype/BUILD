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

package(default_visibility = ["//test/behaviour:__subpackages__"])
load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_common//test/server:rules.bzl", "grakn_java_test")

java_library(
    name = "steps",
    srcs = [
        "EntityTypeSteps.java"
    ],
    deps = [
    ],
    visibility = ["//visibility:public"],
)

grakn_java_test(
    name = "test",
    srcs = [
        "EntityTypeTest.java"
    ],
    test_class = "grakn.client.test.behaviour.concept.type.entitytype.EntityTypeTest",
    deps = [
        # Internal Package Dependencies
        "//test/behaviour",

        # External Maven Dependencies
        "@maven//:io_cucumber_cucumber_junit",
    ],
    runtime_deps = [
        ":steps",
        "//test/behaviour/config:parameters",
        "//test/behaviour/concept/thing/entity:steps",
        "//test/behaviour/concept/thing/attribute:steps",
        "//test/behaviour/concept/thing/relation:steps",
        "//test/behaviour/concept/type/attributetype:steps",
        "//test/behaviour/concept/type/relationtype:steps",
        "//test/behaviour/concept/type/thingtype:steps",
        "//test/behaviour/connection/database:steps",
        "//test/behaviour/connection/session:steps",
        "//test/behaviour/connection/transaction:steps",
    ],
    data = [
        "@graknlabs_behaviour//concept/type:entitytype.feature",
    ],
    native_grakn_artifact = "//test:native-grakn-artifact",
    size = "medium",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)
