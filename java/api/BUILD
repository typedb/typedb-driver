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

load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "api",
    srcs = glob(["*.java", "*/*.java", "*/*/*.java"], exclude=["test/**"]),
    deps = [
        # Internal dependencies
        "//java/common:common",

        "//java:typedb_driver_jni",

        # External dependencies from Maven
        "@maven//:com_eclipsesource_minimal_json_minimal_json",
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

filegroup(
    name = "docs_source_files",
    srcs = glob(["*.java", "*/*.java", "*/*/*.java"], exclude=["test/**"]),
    visibility = ["//java:__pkg__"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*", "*/*/*"]),
    license_type = "apache-header",
    size = "small",
)
