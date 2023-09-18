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
load(":rules.bzl", "swig_native_java_library")
load("@google_bazel_common//tools/javadoc:javadoc.bzl", "javadoc_library")

java_library(
    name = "driver-java",
    srcs = glob(["*.java"]),
    resources = ["//:LICENSE"],
    tags = ["maven_coordinates=com.vaticle.typedb:typedb-driver:{pom_version}"],
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
    project_description = "TypeDB Java Driver",
    project_name = "TypeDB Java Driver",
    project_url = "https://github.com/vaticle/typedb-driver-java",
    scm_url = "https://github.com/vaticle/typedb-driver-java",
    target = ":driver-java",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@vaticle_typedb_driver_workspace_refs//:refs.json",
    platform_overrides = {
        ":typedb_driver_jni": json.encode([
           "com.vaticle.typedb:typedb-driver-jni-linux-arm64:{pom_version}",
           "com.vaticle.typedb:typedb-driver-jni-linux-x86_64:{pom_version}",
           "com.vaticle.typedb:typedb-driver-jni-macosx-arm64:{pom_version}",
           "com.vaticle.typedb:typedb-driver-jni-macosx-x86_64:{pom_version}",
           "com.vaticle.typedb:typedb-driver-jni-windows-x86_64:{pom_version}",
        ])
    }
)

deploy_maven(
    name = "deploy-maven",
    release = deployment["maven.release"],
    snapshot = deployment["maven.snapshot"],
    target = ":assemble-maven",
)

swig_native_java_library(
    name = "typedb_driver_jni",
    lib = "//c:typedb_driver_clib_headers",
    package = "com.vaticle.typedb.driver.jni",
    interface = "//c:typedb_driver.i",
    includes = ["//c:swig/typedb_driver_java.swg"],
    enable_cxx = True,
    maven_coordinates = "com.vaticle.typedb:typedb-driver-jni-{platform}:{pom_version}",
    platforms = {
        "@vaticle_bazel_distribution//platform:is_linux_arm64": "linux-arm64",
        "@vaticle_bazel_distribution//platform:is_linux_x86_64": "linux-x86_64",
        "@vaticle_bazel_distribution//platform:is_mac_arm64": "macosx-arm64",
        "@vaticle_bazel_distribution//platform:is_mac_x86_64": "macosx-x86_64",
        "@vaticle_bazel_distribution//platform:is_windows_x86_64": "windows-x86_64",
    },
)

assemble_maven(
    name = "assemble-maven-jni",
    project_description = "Java TypeDB Driver JNI",
    project_name = "Java TypeDB Driver JNI",
    project_url = "https://github.com/vaticle/typedb-driver-java",
    scm_url = "https://github.com/vaticle/typedb-driver-java",
    target = ":typedb_driver_jni",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@vaticle_typedb_driver_workspace_refs//:refs.json",
)

deploy_maven(
    name = "deploy-maven-jni",
    release = deployment["maven.release"],
    snapshot = deployment["maven.snapshot"],
    target = ":assemble-maven-jni",
)

javadoc_library(
    name = "driver_doc",
    srcs = glob(["*.java"]) + ["api"],
#    root_packages = ["com.vaticle.typedb.client"],
    deps = [
        "//java/api",
        "//java/connection",
        "@vaticle_typedb_common//:common",
    ],
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = ["README.md"],
    license_type = "apache-header",
)
