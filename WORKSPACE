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

workspace(name = "vaticle_typedb_driver")

################################
# Load @vaticle_dependencies #
################################

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
python_register_toolchains(name = "python39", python_version = "3.9")

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

# Load //builder/grpc
load("@vaticle_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load //builder/rust
load("@vaticle_dependencies//builder/rust:deps.bzl", rust_deps = "deps")
rust_deps()

load("@rules_rust//rust:repositories.bzl", "rules_rust_dependencies", "rust_register_toolchains")
rules_rust_dependencies()
rust_register_toolchains(
    edition = "2021",
    include_rustc_srcs = True,
    extra_target_triples = [
        "aarch64-apple-darwin",
        "x86_64-apple-darwin",
        "x86_64-pc-windows-msvc",
        "x86_64-unknown-linux-gnu",
    ]
)

load("@vaticle_dependencies//library/crates:crates.bzl", "fetch_crates")
fetch_crates()
load("@crates//:defs.bzl", "crate_repositories")
crate_repositories()

load("@vaticle_dependencies//tool/swig:deps.bzl", swig_deps = "deps")
swig_deps()

# Load //tool/common
load("@vaticle_dependencies//tool/common:deps.bzl", "vaticle_dependencies_ci_pip")
vaticle_dependencies_ci_pip()
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
# Load @com_google_protobuf #
####################################
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "com_google_protobuf",
    remote = "https://github.com/protocolbuffers/protobuf",
    commit = "ab840345966d0fa8e7100d771c92a73bfbadd25c",
)

# Load protoc binary dependencies
load("@com_google_protobuf//:protobuf_deps.bzl", "PROTOBUF_MAVEN_ARTIFACTS", "protobuf_deps")
protobuf_deps()

######################################
# Load @vaticle_bazel_distribution #
######################################

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
load("@vaticle_bazel_distribution_pip//:requirements.bzl", "install_deps")
install_deps()

######################################
# Load @vaticle_typedb_client_python #
######################################

load("@rules_python//python:pip.bzl", "pip_parse")
pip_parse(
    name = "vaticle_typedb_client_python_pip",
    requirements_lock = "//python:requirements_dev.txt",
)
load("@vaticle_typedb_client_python_pip//:requirements.bzl", "install_deps")
install_deps()

################################
# Load @vaticle dependencies #
################################

# Load repositories
load("//dependencies/vaticle:repositories.bzl", "vaticle_typedb_common", "vaticle_typeql", "vaticle_typedb_behaviour", "vaticle_factory_tracing", "vaticle_typedb_protocol")
vaticle_typedb_common()
vaticle_typeql()
vaticle_typedb_behaviour()
vaticle_factory_tracing()
vaticle_typedb_protocol()

# Load artifacts
load("//dependencies/vaticle:artifacts.bzl", "vaticle_typedb_artifact", "vaticle_typedb_cluster_artifact")
vaticle_typedb_artifact()
vaticle_typedb_cluster_artifact()

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

###############
# Load @maven #
###############

# Load maven artifacts
load("@vaticle_dependencies//tool/common:deps.bzl", vaticle_dependencies_tool_maven_artifacts = "maven_artifacts")
load("@vaticle_typedb_common//dependencies/maven:artifacts.bzl", vaticle_typedb_common_maven_artifacts = "artifacts")
load("@vaticle_typeql//dependencies/maven:artifacts.bzl", vaticle_typeql_maven_artifacts = "artifacts")
load("@vaticle_factory_tracing//dependencies/maven:artifacts.bzl", vaticle_factory_tracing_maven_artifacts = "artifacts")
load("//dependencies/maven:artifacts.bzl", vaticle_typedb_client_java_maven_artifacts = "artifacts", vaticle_typedb_client_java_maven_overrides = "overrides")
load("@vaticle_bazel_distribution//maven:deps.bzl", vaticle_bazel_distribution_maven_artifacts = "maven_artifacts")

load("@vaticle_dependencies//library/maven:rules.bzl", "maven")
maven(
    vaticle_factory_tracing_maven_artifacts +
    vaticle_typedb_common_maven_artifacts +
    vaticle_typeql_maven_artifacts +
    vaticle_dependencies_tool_maven_artifacts +
    vaticle_typedb_client_java_maven_artifacts +
    vaticle_bazel_distribution_maven_artifacts,
    vaticle_typedb_client_java_maven_overrides
)

############################################
# Create @vaticle_typedb_driver_workspace_refs #
############################################
load("@vaticle_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "vaticle_typedb_driver_workspace_refs"
)
