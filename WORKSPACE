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

workspace(name = "typedb_driver")

##############################
# Load @typedb_dependencies #
##############################
load("//dependencies/typedb:repositories.bzl", "typedb_dependencies")
typedb_dependencies()

# Load //builder/bazel for RBE
load("@typedb_dependencies//builder/bazel:deps.bzl", "bazel_toolchain")
bazel_toolchain()

# Load //builder/python
load("@typedb_dependencies//builder/python:deps.bzl", "rules_python")
rules_python()
load("@rules_python//python:repositories.bzl", "py_repositories", "python_register_toolchains")
py_repositories()
load("//python:python_versions.bzl", "register_all_toolchains")
register_all_toolchains()

# Load //builder/java
load("@typedb_dependencies//builder/java:deps.bzl", "rules_jvm_external")
rules_jvm_external()

# Load //builder/kotlin
load("@typedb_dependencies//builder/kotlin:deps.bzl", "io_bazel_rules_kotlin")
io_bazel_rules_kotlin()
load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
kotlin_repositories()
load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")
kt_register_toolchains()

# Load //builder/antlr
load("@typedb_dependencies//builder/antlr:deps.bzl", "rules_antlr", "antlr_version")
rules_antlr()

load("@rules_antlr//antlr:lang.bzl", "JAVA")
load("@rules_antlr//antlr:repositories.bzl", "rules_antlr_dependencies")
rules_antlr_dependencies(antlr_version, JAVA)

# Load //builder/cpp
load("@typedb_dependencies//builder/cpp:deps.bzl", cpp_deps = "deps")
cpp_deps()

# Load //builder/csharp
load("@typedb_dependencies//builder/csharp:deps.bzl", "rules_dotnet")
rules_dotnet()

load(
    "@rules_dotnet//dotnet:repositories.bzl",
    "dotnet_register_toolchains",
    "rules_dotnet_dependencies",
)
rules_dotnet_dependencies()

dotnet_register_toolchains("dotnet", "6.0.413")
load("@rules_dotnet//dotnet:paket.rules_dotnet_nuget_packages.bzl", "rules_dotnet_nuget_packages")
rules_dotnet_nuget_packages()
load("@rules_dotnet//dotnet:paket.paket2bazel_dependencies.bzl", "paket2bazel_dependencies")
paket2bazel_dependencies()
load("//csharp/nuget:paket.csharp_deps.bzl", csharp_deps = "csharp_deps")
csharp_deps()

# Load //builder/go
load("@typedb_dependencies//builder/go:deps.bzl", go_deps = "deps")
go_deps()
load("@io_bazel_rules_go//go:deps.bzl", "go_rules_dependencies")
go_rules_dependencies()

load("//go:go_versions.bzl", "register_all_toolchains")
register_all_toolchains()

# gazelle:repo bazel_gazelle <- Used to tell gazelle that it is loaded in a macro.
load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")
load("//go:deps.bzl", "go_repositories")
# gazelle:repository_macro go/deps.bzl%go_repositories
go_repositories()
gazelle_dependencies()

# Load //builder/proto_grpc
load("@typedb_dependencies//builder/proto_grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load //builder/rust
load("@typedb_dependencies//builder/rust:deps.bzl", rust_deps = "deps")
rust_deps()

load("@rules_rust//rust:repositories.bzl", "rules_rust_dependencies", "rust_register_toolchains", "rust_analyzer_toolchain_tools_repository")
rules_rust_dependencies()
load("@rules_rust//tools/rust_analyzer:deps.bzl", "rust_analyzer_dependencies")
rust_analyzer_dependencies()
load("@rules_rust//rust:defs.bzl", "rust_common")
rust_register_toolchains(
    edition = "2021",
    extra_target_triples = [
        "aarch64-apple-darwin",
        "aarch64-unknown-linux-gnu",
        "x86_64-apple-darwin",
        "x86_64-pc-windows-msvc",
        "x86_64-unknown-linux-gnu",
    ],
    rust_analyzer_version = "1.81.0",
    versions = ["1.81.0"],
)

rust_analyzer_toolchain_tools_repository(
    name = "rust_analyzer_toolchain_tools",
    version = rust_common.default_version
)

load("@typedb_dependencies//library/crates:crates.bzl", "fetch_crates")
fetch_crates()
load("@crates//:defs.bzl", "crate_repositories")
crate_repositories()

load("@typedb_dependencies//tool/swig:deps.bzl", "swig")
swig()

# Load //tool/common
load("@typedb_dependencies//tool/common:deps.bzl", "typedb_dependencies_ci_pip")
typedb_dependencies_ci_pip()
load("@typedb_dependencies_ci_pip//:requirements.bzl", ci_pip_install_deps = "install_deps")
ci_pip_install_deps()

# Load //tool/checkstyle
load("@typedb_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

# Load //tool/sonarcloud
load("@typedb_dependencies//tool/sonarcloud:deps.bzl", "sonarcloud_dependencies")
sonarcloud_dependencies()

# Load //tool/unuseddeps
load("@typedb_dependencies//tool/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

####################################
# Load @typedb_bazel_distribution #
####################################

load("@typedb_dependencies//distribution:deps.bzl", "typedb_bazel_distribution")
typedb_bazel_distribution()

# Load //common
load("@typedb_bazel_distribution//common:deps.bzl", "rules_pkg")
rules_pkg()
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# Load //github
load("@typedb_bazel_distribution//github:deps.bzl", "ghr_linux_tar", "ghr_osx_zip")
ghr_linux_tar()
ghr_osx_zip()

# Load //pip
load("@typedb_bazel_distribution//pip:deps.bzl", "typedb_bazel_distribution_pip")
typedb_bazel_distribution_pip()
load("@typedb_bazel_distribution_pip//:requirements.bzl", pip_install_deps = "install_deps")
pip_install_deps()

# Load //docs
load("@typedb_bazel_distribution//docs:python/deps.bzl", "typedb_bazel_distribution_docs_py")
typedb_bazel_distribution_docs_py()
load("@typedb_bazel_distribution_docs_py//:requirements.bzl", docs_py_install_deps = "install_deps")
docs_py_install_deps()

load("@typedb_bazel_distribution//docs:java/deps.bzl", "google_bazel_common")
google_bazel_common()
load("@google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()

######################################
# Load @typedb_driver_python #
######################################

load("@rules_python//python:pip.bzl", "pip_parse")
pip_parse(
    name = "typedb_driver_pip",
    requirements_lock = "//python:requirements_dev.txt",
)
load("@typedb_driver_pip//:requirements.bzl", type_driver_install_deps = "install_deps")
type_driver_install_deps()

#############################
# Load @typedb dependencies #
#############################

# Load repositories
load("//dependencies/typedb:repositories.bzl", "typedb_behaviour", "typedb_protocol")
typedb_behaviour()
typedb_protocol()

# Load artifacts
load("//dependencies/typedb:artifacts.bzl", "typedb_artifact")
typedb_artifact()
#typedb_cloud_artifact()

####################
# Load npm modules #
####################

# Load //builder/nodejs
load("@typedb_dependencies//builder/nodejs:deps.bzl", nodejs_deps = "deps")
nodejs_deps()

load("@aspect_rules_js//js:repositories.bzl", "rules_js_dependencies")
rules_js_dependencies()

load("@rules_nodejs//nodejs:repositories.bzl", "DEFAULT_NODE_VERSION", "nodejs_register_toolchains")
nodejs_register_toolchains(
    name = "nodejs",
    node_version = DEFAULT_NODE_VERSION,
)

load("@aspect_rules_js//npm:repositories.bzl", "npm_translate_lock")

npm_translate_lock(
    name = "typedb_protocol_npm",
    bins = {
        "protoc-gen-ts": {
            "protoc-gen-ts-js": "./bin/protoc-gen-ts.js",
        },
    },
    pnpm_lock = "@typedb_protocol//grpc/nodejs:pnpm-lock.yaml",
)

load("@typedb_protocol_npm//:repositories.bzl", typedb_protocol_npm_repositories = "npm_repositories")
typedb_protocol_npm_repositories()

npm_translate_lock(
    name = "nodejs_npm",
    bins = {
        "@cucumber/cucumber": {
            "cucumber-js": "./bin/cucumber-js",
        },
    },
    pnpm_lock = "//nodejs:pnpm-lock.yaml",
)

load("@nodejs_npm//:repositories.bzl", "npm_repositories")
npm_repositories()

# Setup rules_ts
load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")

rules_ts_dependencies(
    ts_version_from = "//nodejs:package.json",
)

load("@aspect_bazel_lib//lib:repositories.bzl", "register_jq_toolchains")
register_jq_toolchains()

# Load @typedb_bazel_distribution_uploader
load("@typedb_bazel_distribution//common/uploader:deps.bzl", "typedb_bazel_distribution_uploader")
typedb_bazel_distribution_uploader()
load("@typedb_bazel_distribution_uploader//:requirements.bzl", uploader_install_deps = "install_deps")
uploader_install_deps()

###############
# Load @maven #
###############

# Load maven artifacts
load("@typedb_dependencies//tool/common:deps.bzl", typedb_dependencies_tool_maven_artifacts = "maven_artifacts")
load(
    "//dependencies/maven:artifacts.bzl",
    typedb_driver_maven_artifacts = "artifacts",
    typedb_driver_maven_overrides = "overrides",
)
load("@typedb_bazel_distribution//maven:deps.bzl", typedb_bazel_distribution_maven_artifacts = "maven_artifacts")

# Load TypeDB maven artifacts
#load("//dependencies/typedb:artifacts.bzl", typedb_driver_maven_artifacts = "maven_artifacts")

load("@typedb_dependencies//library/maven:rules.bzl", "maven")
maven(
    typedb_dependencies_tool_maven_artifacts +
    typedb_driver_maven_artifacts +
    typedb_bazel_distribution_maven_artifacts,
#    internal_artifacts = typedb_driver_maven_artifacts,
    override_targets = typedb_driver_maven_overrides,
)

################################################
# Create @typedb_driver_workspace_refs #
################################################
load("@typedb_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "typedb_driver_workspace_refs"
)
