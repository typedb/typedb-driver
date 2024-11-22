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

load("//dependencies/maven:artifacts.bzl", artifacts_repo = "overrides")
load("@typedb_bazel_distribution//maven:rules.bzl", "assemble_maven", "deploy_maven")
load("@typedb_dependencies//distribution/maven:version.bzl", "version")
load("@typedb_dependencies//distribution:deployment.bzl", "deployment")
load("@typedb_dependencies//library/maven:artifacts.bzl", artifacts_org = "artifacts")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load(":rules.bzl", "swig_native_java_library")
load("@google_bazel_common//tools/javadoc:javadoc.bzl", "javadoc_library")
load("//tool/docs:java/rules.bzl", "javadoc_to_adoc")
load("//java:docs_structure.bzl", "dir_mapping")
load("//tool/docs:examples/rules.bzl",  "test_to_example", "update_markdown_example")

java_library(
    name = "driver-java",
    srcs = glob(["*.java"]),
    resources = ["//:LICENSE"],
    tags = ["maven_coordinates=com.typedb:typedb-driver:{pom_version}"],
    deps = [
        # Internal dependencies
        "//java/api",
        "//java/connection",
    ],
)

assemble_maven(
    name = "assemble-maven",
    project_description = "TypeDB Java Driver",
    project_name = "TypeDB Java Driver",
    project_url = "https://github.com/typedb/typedb-driver-java",
    scm_url = "https://github.com/typedb/typedb-driver-java",
    target = ":driver-java",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@typedb_driver_workspace_refs//:refs.json",
    platform_overrides = {
        ":typedb_driver_jni": json.encode([
           "com.typedb:typedb-driver-jni-linux-arm64:{pom_version}",
           "com.typedb:typedb-driver-jni-linux-x86_64:{pom_version}",
           "com.typedb:typedb-driver-jni-macosx-arm64:{pom_version}",
           "com.typedb:typedb-driver-jni-macosx-x86_64:{pom_version}",
           "com.typedb:typedb-driver-jni-windows-x86_64:{pom_version}",
        ])
    }
)

deploy_maven(
    name = "deploy-maven",
    release = deployment['maven']['release']['upload'],
    snapshot = deployment['maven']['snapshot']['upload'],
    target = ":assemble-maven",
)

swig_native_java_library(
    name = "typedb_driver_jni",
    lib = "//c:typedb_driver_clib_headers",
    package = "com.typedb.driver.jni",
    interface = "//c:typedb_driver.i",
    includes = ["//c:swig/typedb_driver_java.swg"],
    enable_cxx = True,
    maven_coordinates = "com.typedb:typedb-driver-jni-{platform}:{pom_version}",
    platforms = {
        "@typedb_bazel_distribution//platform:is_linux_arm64": "linux-arm64",
        "@typedb_bazel_distribution//platform:is_linux_x86_64": "linux-x86_64",
        "@typedb_bazel_distribution//platform:is_mac_arm64": "macosx-arm64",
        "@typedb_bazel_distribution//platform:is_mac_x86_64": "macosx-x86_64",
        "@typedb_bazel_distribution//platform:is_windows_x86_64": "windows-x86_64",
    },
)

assemble_maven(
    name = "assemble-maven-jni",
    project_description = "Java TypeDB Driver JNI",
    project_name = "Java TypeDB Driver JNI",
    project_url = "https://github.com/typedb/typedb-driver-java",
    scm_url = "https://github.com/typedb/typedb-driver-java",
    target = ":typedb_driver_jni",
    version_overrides = version(artifacts_org, artifacts_repo),
    workspace_refs = "@typedb_driver_workspace_refs//:refs.json",
)

deploy_maven(
    name = "deploy-maven-jni",
    release = deployment['maven']['release']['upload'],
    snapshot = deployment['maven']['snapshot']['upload'],
    target = ":assemble-maven-jni",
)

filegroup(
    name = "docs_source_files",
    srcs = [":TypeDB.java"],
)

javadoc_library(
    name = "driver_docs_library",
    srcs = [":docs_source_files", "//java/api:docs_source_files", "//java/common:docs_source_files"],
    deps = [
        "//java:driver-java",
        "//java/api",
        "//java/common",
    ],
)

genrule(
    name = "docs_html",
    srcs = [":driver_docs_library"],
    outs = ["driver_docs_javadoc"],
    cmd = "mkdir $@ && unzip $< -d $@",
)

javadoc_to_adoc(
    name = "docs_adoc",
    data = [":docs_html"],
    docs_dirs = dir_mapping,
)

test_to_example(
    name = "example_core",
    input = "//java/test/integration/core:ExampleTest.java",
    output = "java/core_example",
    removed_lines = ["assert", "fail(", "todo"],
    changed_words = {
        "ExampleTest": "TypeDBExample",
    },
    header_comment_sign = "//",
)

# Temporary manual usage:
# 1. Run: bazel run //java:example_core; bazel run //java:readme_example_core
# 2. Enjoy your updated README.md!
update_markdown_example(
    name = "readme_example_core",
    # TODO: Use ":example_core" as a rule with a single output instead
    input = ":core_example",
    output = ":README.md",
    start_marker = "CORE_EXAMPLE_START_MARKER",
    end_marker = "CORE_EXAMPLE_END_MARKER",
    language_tag = "java",
    removed_header = "//",
)

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = glob([
        "README.md",
        "docs/**/*.adoc",
        "core_example",
    ]),
    license_type = "apache-header",
)
