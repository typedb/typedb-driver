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

load("//dependencies/vaticle:repositories.bzl", "vaticle_dependencies")
vaticle_dependencies()

# Load //builder/java
load("@vaticle_dependencies//builder/java:deps.bzl", java_deps = "deps")
java_deps()

# Load //builder/kotlin
load("@vaticle_dependencies//builder/kotlin:deps.bzl", kotlin_deps = "deps")
kotlin_deps()
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

# TODO: enable this when we load @vaticle_typeql
## Load //builder/antlr
#load("@vaticle_dependencies//builder/antlr:deps.bzl", antlr_deps = "deps", "antlr_version")
#antlr_deps()
#
#load("@rules_antlr//antlr:lang.bzl", "JAVA")
#load("@rules_antlr//antlr:repositories.bzl", "rules_antlr_dependencies")
#rules_antlr_dependencies(antlr_version, JAVA)

# Load //builder/grpc (required by @vaticle_typedb_protocol)
load("@vaticle_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()
load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load //builder/rust
load("@vaticle_dependencies//builder/rust:deps.bzl", rust_deps = "deps")
rust_deps()

load("@rules_rust//rust:repositories.bzl", "rules_rust_dependencies", "rust_register_toolchains")
rules_rust_dependencies()
rust_register_toolchains(edition = "2021", include_rustc_srcs = True)

load("@vaticle_dependencies//library/crates:crates.bzl", "raze_fetch_remote_crates")
raze_fetch_remote_crates()

# Load //builder/python
load("@vaticle_dependencies//builder/python:deps.bzl", python_deps = "deps")
python_deps()

# Load //tool/checkstyle
load("@vaticle_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

# Load //tool/common
load("@vaticle_dependencies//tool/common:deps.bzl", "vaticle_dependencies_ci_pip")
vaticle_dependencies_ci_pip()

######################################
# Load @vaticle_bazel_distribution #
######################################

load("@vaticle_dependencies//distribution:deps.bzl", "vaticle_bazel_distribution")
vaticle_bazel_distribution()

# Load //github
load("@vaticle_bazel_distribution//github:deps.bzl", github_deps = "deps")
github_deps()

################################
# Load @vaticle dependencies #
################################

load("//dependencies/vaticle:repositories.bzl", "vaticle_typedb_protocol")
vaticle_typedb_protocol()

############################
# Load @maven dependencies #
############################

load("@vaticle_dependencies//tool/common:deps.bzl", vaticle_dependencies_tool_maven_artifacts = "maven_artifacts")
load("@vaticle_bazel_distribution//maven:deps.bzl", vaticle_bazel_distribution_maven_artifacts = "maven_artifacts")

load("@vaticle_dependencies//library/maven:rules.bzl", "maven")
maven(vaticle_dependencies_tool_maven_artifacts + vaticle_bazel_distribution_maven_artifacts)
