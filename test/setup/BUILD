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

package(default_visibility = ["//test:__subpackages__"])
load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_dependencies//distribution/artifact:rules.bzl", "artifact_extractor")

artifact_extractor(
    name = "grakn-extractor",
    target = "@graknlabs_grakn_core_artifact//file",
)

java_library(
    name = "grakn-setup",
    srcs = [
        "GraknSetup.java",
        "GraknSetupCore.java",
        "GraknSetupKGMS.java",
    ],
    deps = [
        ":grakn-properties",
        "@maven//:commons_io_commons_io",
        "@maven//:junit_junit",
        "@maven//:org_zeroturnaround_zt_exec",
        "@maven//:org_slf4j_slf4j_api"
    ],
)

java_library(
    name = "grakn-properties",
    srcs = ["GraknProperties.java"],
)

filegroup(
    name = "logback",
    srcs = ["logback.xml"],
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":grakn-setup",
        ":grakn-properties",
    ],
    license_type = "apache"
)