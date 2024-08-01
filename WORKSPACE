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

workspace(name = "vaticle_typedb_driver")

##############################
# Load @vaticle_dependencies #
##############################
load("//dependencies/vaticle:repositories.bzl", "vaticle_dependencies")
vaticle_dependencies()

# Load //builder/bazel for RBE
load("@vaticle_dependencies//builder/bazel:deps.bzl", "bazel_toolchain")
bazel_toolchain()

# Load //builder/python
load("@vaticle_dependencies//builder/python:deps.bzl", python_deps = "deps")
python_deps()
load("@rules_python//python:repositories.bzl", "py_repositories", "python_register_toolchains")
py_repositories()
load("//python:python_versions.bzl", "register_all_toolchains")
register_all_toolchains()

# Load //builder/java
load("@vaticle_dependencies//builder/java:deps.bzl", java_deps = "deps")
java_deps()

# Load //builder/kotlin
load("@vaticle_dependencies//builder/kotlin:deps.bzl", kotlin_deps = "deps")
kotlin_deps()
load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
kotlin_repositories()
load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")
kt_register_toolchains()

# Load //builder/antlr
load("@vaticle_dependencies//builder/antlr:deps.bzl", antlr_deps = "deps", "antlr_version")
antlr_deps()

load("@rules_antlr//antlr:lang.bzl", "JAVA")
load("@rules_antlr//antlr:repositories.bzl", "rules_antlr_dependencies")
rules_antlr_dependencies(antlr_version, JAVA)

# Load //builder/cpp
load("@vaticle_dependencies//builder/cpp:deps.bzl", cpp_deps = "deps")
cpp_deps()

# Load //builder/csharp
load("@vaticle_dependencies//builder/csharp:deps.bzl", dotnet_deps = "deps")
dotnet_deps()

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
load("@vaticle_dependencies//builder/go:deps.bzl", go_deps = "deps")
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
load("@vaticle_dependencies//builder/proto_grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load //builder/rust
load("@vaticle_dependencies//builder/rust:deps.bzl", rust_deps = "deps")
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
    rust_analyzer_version = rust_common.default_version,
)

rust_analyzer_toolchain_tools_repository(
    name = "rust_analyzer_toolchain_tools",
    version = rust_common.default_version
)

load("@vaticle_dependencies//library/crates:crates.bzl", "fetch_crates")
fetch_crates()
load("@crates//:defs.bzl", "crate_repositories")
crate_repositories()

load("@vaticle_dependencies//tool/swig:deps.bzl", swig_deps = "deps")
swig_deps()

# Load //tool/common
load("//python:python_versions.bzl", "create_interpreter_symlinks")
create_interpreter_symlinks({"python39_symlink" : "python39"})
load("@vaticle_dependencies//tool/common:deps.bzl", "vaticle_dependencies_ci_pip")
vaticle_dependencies_ci_pip("@python39_symlink//:python")
load("@vaticle_dependencies_ci_pip//:requirements.bzl", "install_deps")
install_deps()

# Load //tool/checkstyle
load("@vaticle_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

# Load //tool/sonarcloud
load("@vaticle_dependencies//tool/sonarcloud:deps.bzl", "sonarcloud_dependencies")
sonarcloud_dependencies()

# Load //tool/unuseddeps
load("@vaticle_dependencies//tool/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

####################################
# Load @vaticle_bazel_distribution #
####################################

load("@vaticle_dependencies//distribution:deps.bzl", "vaticle_bazel_distribution")
vaticle_bazel_distribution()

# Load //common
load("@vaticle_bazel_distribution//common:deps.bzl", "rules_pkg")
rules_pkg()
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# Load //github
load("@vaticle_bazel_distribution//github:deps.bzl", github_deps = "deps")
github_deps()

# Load //pip
load("@vaticle_bazel_distribution//pip:deps.bzl", pip_deps = "deps")
pip_deps()
load("@vaticle_bazel_distribution_pip//:requirements.bzl", install_pip_deps = "install_deps")
install_pip_deps()

# Load //docs
load("@vaticle_bazel_distribution//docs:python/deps.bzl", docs_deps = "deps")
docs_deps()
load("@vaticle_dependencies_tool_docs//:requirements.bzl", install_doc_deps = "install_deps")
install_doc_deps()

load("@vaticle_bazel_distribution//docs:java/deps.bzl", java_doc_deps = "deps")
java_doc_deps()
load("@google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()

######################################
# Load @vaticle_typedb_driver_python #
######################################

load("@rules_python//python:pip.bzl", "pip_parse")
pip_parse(
    name = "vaticle_typedb_driver_pip",
    requirements_lock = "//python:requirements_dev.txt",
)
load("@vaticle_typedb_driver_pip//:requirements.bzl", "install_deps")
install_deps()

##############################
# Load @vaticle dependencies #
##############################

# Load repositories
load("//dependencies/vaticle:repositories.bzl", "vaticle_typeql", "vaticle_typedb_behaviour", "vaticle_typedb_protocol")
vaticle_typeql()
vaticle_typedb_behaviour()
vaticle_typedb_protocol()

# Load artifacts
load("//dependencies/vaticle:artifacts.bzl", "vaticle_typedb_artifact", "vaticle_typedb_cloud_artifact")
vaticle_typedb_artifact()
vaticle_typedb_cloud_artifact()

####################
# Load npm modules #
####################

# Load //builder/nodejs
load("@vaticle_dependencies//builder/nodejs:deps.bzl", nodejs_deps = "deps")
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
    name = "vaticle_typedb_protocol_npm",
    bins = {
        "protoc-gen-ts": {
            "protoc-gen-ts-js": "./bin/protoc-gen-ts.js",
        },
    },
    pnpm_lock = "@vaticle_typedb_protocol//grpc/nodejs:pnpm-lock.yaml",
)

npm_translate_lock(
    name = "npm",
    bins = {
        "@cucumber/cucumber": {
            "cucumber-js": "./bin/cucumber-js",
        },
    },
    pnpm_lock = "//nodejs:pnpm-lock.yaml",
)

load("@npm//:repositories.bzl", "npm_repositories")
npm_repositories()

load("@vaticle_typedb_protocol_npm//:repositories.bzl", vaticle_typedb_protocol_npm_repositories = "npm_repositories")
vaticle_typedb_protocol_npm_repositories()

# Setup rules_ts
load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")

rules_ts_dependencies(
    ts_version_from = "//nodejs:package.json",
)

load("@aspect_bazel_lib//lib:repositories.bzl", "register_jq_toolchains")
register_jq_toolchains()

# Load @vaticle_bazel_distribution_uploader
load("@vaticle_bazel_distribution//common/uploader:deps.bzl", uploader_deps = "deps")
uploader_deps()
load("@vaticle_bazel_distribution_uploader//:requirements.bzl", install_uploader_deps = "install_deps")
install_uploader_deps()

###############
# Load @maven #
###############

# Load maven artifacts
load("@vaticle_dependencies//tool/common:deps.bzl", vaticle_dependencies_tool_maven_artifacts = "maven_artifacts")
load("@vaticle_typeql//dependencies/maven:artifacts.bzl", vaticle_typeql_maven_artifacts = "artifacts")
load(
    "//dependencies/maven:artifacts.bzl",
    vaticle_typedb_driver_maven_artifacts = "artifacts",
    vaticle_typedb_driver_maven_overrides = "overrides",
)
load("@vaticle_bazel_distribution//maven:deps.bzl", vaticle_bazel_distribution_maven_artifacts = "maven_artifacts")

# Load Vaticle maven artifacts
load("//dependencies/vaticle:artifacts.bzl", vaticle_typedb_driver_vaticle_maven_artifacts = "maven_artifacts")

load("@vaticle_dependencies//library/maven:rules.bzl", "maven")
maven(
    vaticle_typeql_maven_artifacts +
    vaticle_dependencies_tool_maven_artifacts +
    vaticle_typedb_driver_maven_artifacts +
    vaticle_bazel_distribution_maven_artifacts,
    internal_artifacts = vaticle_typedb_driver_vaticle_maven_artifacts,
    override_targets = vaticle_typedb_driver_maven_overrides,
)

################################################
# Create @vaticle_typedb_driver_workspace_refs #
################################################
load("@vaticle_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "vaticle_typedb_driver_workspace_refs"
)
