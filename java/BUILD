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

load("//dependencies/maven:artifacts.bzl", artifacts_repo = "overrides")
load("@vaticle_bazel_distribution//maven:rules.bzl", "assemble_maven", "deploy_maven")
load("@vaticle_dependencies//distribution/maven:version.bzl", "version")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("@vaticle_dependencies//library/maven:artifacts.bzl", artifacts_org = "artifacts")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load(":rules.bzl", "platform_specific_swig_java")

java_library(
    name = "client-java",
    srcs = glob(["*.java"]),
    resources = ["//:LICENSE"],
    tags = ["maven_coordinates=com.vaticle.typedb:typedb-client:{pom_version}"],
    deps = [
        # Internal dependencies
        "//java/api",
        "//java/connection",

        # External dependencies from @vaticle
        "@vaticle_typedb_common//:common",
    ],
)

assemble_maven(
    name = "assemble-maven",
    project_description = "TypeDB Client API for Java",
    project_name = "TypeDB Client Java",
    project_url = "https://github.com/vaticle/typedb-driver-java",
    scm_url = "https://github.com/vaticle/typedb-driver-java",
    target = ":client-java",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@vaticle_typedb_driver_workspace_refs//:refs.json",
    profile_overrides = {
        ":typedb_client_jni": json.encode({
            "linux-aarch64": "com.vaticle.typedb:typedb-client-jni-linux-aarch64:{pom_version}",
            "linux-x86_64": "com.vaticle.typedb:typedb-client-jni-linux-x86_64:{pom_version}",
            "mac-aarch64": "com.vaticle.typedb:typedb-client-jni-macosx-aarch64:{pom_version}",
            "mac-x86_64": "com.vaticle.typedb:typedb-client-jni-macosx-x86_64:{pom_version}",
            "windows-x86_64": "com.vaticle.typedb:typedb-client-jni-windows-x86_64:{pom_version}",
        })
    }
)

deploy_maven(
    name = "deploy-maven",
    release = deployment["maven.release"],
    snapshot = deployment["maven.snapshot"],
    target = ":assemble-maven",
)

platform_specific_swig_java(
    name = "typedb_client_jni",
    lib = "//c:typedb_client_clib_headers",
    package = "com.vaticle.typedb.client.jni",
    interface = "//c:typedb_client.i",
    includes = ["//c:swig/typedb_client_java.swg"],
    enable_cxx = True,
    maven_coordinates = "com.vaticle.typedb:typedb-client-jni-{platform}:{pom_version}",
    platforms = {
        "@vaticle_dependencies//util/platform:is_linux_aarch64": "linux-aarch64",
        "@vaticle_dependencies//util/platform:is_linux_x86_64": "linux-x86_64",
        "@vaticle_dependencies//util/platform:is_mac_aarch64": "macosx-aarch64",
        "@vaticle_dependencies//util/platform:is_mac_x86_64": "macosx-x86_64",
        "@vaticle_dependencies//util/platform:is_win_x86_64": "windows-x86_64",
    },
)

assemble_maven(
    name = "assemble-maven-jni",
    project_description = "TypeDB Client JNI",
    project_name = "TypeDB Client Java",
    project_url = "https://github.com/vaticle/typedb-driver-java",
    scm_url = "https://github.com/vaticle/typedb-driver-java",
    target = ":typedb_client_jni",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@vaticle_typedb_driver_workspace_refs//:refs.json",
)

deploy_maven(
    name = "deploy-maven-jni",
    release = deployment["maven.release"],
    snapshot = deployment["maven.snapshot"],
    target = ":assemble-maven-jni",
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = ["README.md"],
    license_type = "apache-header",
)
