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

package(default_visibility = ["//java/test/behaviour/typeql/language/define:__subpackages__"])

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "steps",
    srcs = [
        "TypeQLSteps.java",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//java/test/behaviour/connection/database:steps",
        "//java/test/behaviour/connection/transaction:steps",
        "@vaticle_typeql//java/pattern",
    ],
    deps = [
        # Package dependencies
        "//java/api:api",
        "//java/common:common",
        "//java/test/behaviour/connection:steps-base",
        "//java/test/behaviour/util:util",

        # Vaticle dependencies
        "@vaticle_typedb_common//:common",
        "@vaticle_typeql//java:typeql-lang",
        "@vaticle_typeql//java/common:common",
        "@vaticle_typeql//java/query:query",

        # External dependencies from Maven
        "@maven//:junit_junit",
        "@maven//:io_cucumber_cucumber_java",
        "@maven//:com_google_guava_guava",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
