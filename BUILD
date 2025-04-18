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

load("@typedb_dependencies//tool/release/deps:rules.bzl", "release_validate_deps")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

exports_files([
    "VERSION",
])

checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob([
        "*",
        ".circleci/**/*",
        ".factory/*",
        "tools/*",
    ]),
    exclude = glob([
        "*.md",
        ".bazelversion",
        ".bazel-remote-cache.rc",
        ".bazel-cache-credential.json",
        ".circleci/windows/git.patch",
        "LICENSE",
        "VERSION",
        "docs/**/*",
        "Cargo.*",
    ]),
    license_type = "apache-header",
)

checkstyle_test(
    name = "checkstyle-license",
    size = "small",
    include = ["LICENSE"],
    license_type = "apache-fulltext",
)

release_validate_deps(
    name = "release-validate-deps",
    refs = "@typedb_driver_workspace_refs//:refs.json",
    tagged_deps = [
        "@typedb_protocol",
    ],
    tags = ["manual"],  # in order for bazel test //... to not fail
    version_file = ":VERSION",
)

# Force tools to be built during `build //...`
filegroup(
    name = "tools",
    data = [
        "@typedb_dependencies//tool/checkstyle:test-coverage",
        "@typedb_dependencies//tool/bazelinstall:remote_cache_setup.sh",
        "@typedb_dependencies//tool/release/notes:create",
        "@typedb_dependencies//tool/ide:rust_sync",
        "@typedb_dependencies//tool/sonarcloud:code-analysis",
        "@typedb_dependencies//tool/unuseddeps:unused-deps",
        "@rust_analyzer_toolchain_tools//lib/rustlib/src:rustc_srcs",
        "@typedb_dependencies//tool/sync:dependencies",
    ],
)
